package edu.postleadsfinder.service;

import edu.postleadsfinder.*;
import edu.postleadsfinder.dijkstras.DijPayload;
import edu.postleadsfinder.heavyverticesbypass.HeavyVerticesBypassDominatorsFinder;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.StringJoiner;

import static edu.postleadsfinder.Util.asKeys;

@RestController
@Log4j2
public class GraphPostLeadsFinderRestController {

	@GetMapping("/")
	public String index() {
		return "Welcome to graph-post-leads-finder! <br>" +
			"Please use <a href=\"./swagger-ui/index.html\">Swagger UI</a> for all service methods description.";
	}

	@RequestMapping(path = "/server", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<String> findDominators(@RequestBody String inputJson) {
		log.info(">>> Request: [{}]", inputJson);
		try {
			// NB: here we use "bypass heavy vertices" algo as it is correct and fast:
			GraphBuilder<DijPayload> graphBuilder = new GraphBuilder<>();
			graphBuilder.withPayloadFactoryFunction(DijPayload::new);
			graphBuilder.build(inputJson);

			final Graph<DijPayload> graph = graphBuilder.getGraph();
			final Vertex<DijPayload> startVertex = graphBuilder.startVertex();
			final Vertex<DijPayload> exitVertex = graphBuilder.exitVertex();

			IDominatorsFinder<DijPayload> finder = new HeavyVerticesBypassDominatorsFinder(graph, startVertex, exitVertex);
			List<String> postLeadKeys = asKeys(finder.computeDominators());
			String response = formatResponseText(postLeadKeys);

			log.info("<<< Response: [{}]", response);
			return toResponseEntity(response + "\n");
		} catch (IllegalArgumentException iae) {
			log.info("Illegal input: ", iae);
			return toResponseEntity(iae);
		} catch (RuntimeException re) {
			log.error("Unexpected error: ", re);
			return toResponseEntity(re);
		}
	}

	private String formatResponseText(List<String> postLeadKeys) {
		StringJoiner joiner = new StringJoiner(", ", "{", "}");
		postLeadKeys.forEach(joiner::add);
		return joiner.toString();
	}

	private ResponseEntity<String> toResponseEntity(String responseText) {
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("Content-Type", "text/plain");
		return new ResponseEntity<>(responseText, responseHeaders, HttpStatus.OK);
	}

	private ResponseEntity<String> toResponseEntity(IllegalArgumentException iae) {
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("Content-Type", "text/plain");
		return new ResponseEntity<>(getFullMessageWithStackTrace(iae), responseHeaders, HttpStatus.BAD_REQUEST);
	}

	private ResponseEntity<String> toResponseEntity(Exception e) {
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("Content-Type", "text/plain");
		return new ResponseEntity<>(getFullMessageWithStackTrace(e), responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private String getFullMessageWithStackTrace(Throwable t) {
		StringWriter stringWriter = new StringWriter();
		try (PrintWriter pw = new PrintWriter(stringWriter)) {
			t.printStackTrace(pw);
		}
		return stringWriter.toString();
	}

}
