package edu.postleadsfinder;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import lombok.*;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.nio.dot.DOTImporter;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;


public class GraphBuilder {

    private InputData inputData;
    private DefaultDirectedGraph<JGraphtVertex, JGraphtEdge> jgraphtGraph;

    private Graph graph;

    private JGraphtVertex startVertex;
    private JGraphtVertex exitVertex;

    private Function<Vertex, NodePayload> payloadFactoryFunction = NodePayload::new;

    @RequiredArgsConstructor
    @Getter
    @ToString(of = "key")
    static class JGraphtVertex {
        private final String key;
        @Setter
        int id;
    }

    /** NB: For record it would be {@code new Edge().equals(new Edge()) == true}, so it has to be a class. */
    static class JGraphtEdge {}

    public void withPayloadFactoryFunction(Function<Vertex, NodePayload> factoryFunction) {
        this.payloadFactoryFunction = factoryFunction;
    }

    public void build(String inputJson) {
        buildInputData(inputJson);
        preCheckInputData();
        buildGraph(inputData.getDotFormatGraph());
        checkInvariants();
        buildGraph();
    }

    public Graph getGraph() {
        return graph;
    }

    void preCheckInputData() {
        Preconditions.checkArgument(!isNullOrEmpty(inputData.getStartNodeId()), "Start vertex (\"h\") must be given.");
        Preconditions.checkArgument(!isNullOrEmpty(inputData.getExitNodeId()), "Exit vertex (\"e2\") must be given.");
        Preconditions.checkArgument(!isNullOrEmpty(inputData.getDotFormatGraph()), "Graph (\"graph\") must be given.");
    }

    void buildInputData(String inputJson) {
        Gson gson = new Gson();
        inputData = gson.fromJson(inputJson, InputData.class);
    }

    @SneakyThrows
    void buildGraph(String digraphDSLString) {
        jgraphtGraph = new DefaultDirectedGraph<>(null, JGraphtEdge::new, false);

        DOTImporter<JGraphtVertex, JGraphtEdge> importer = new DOTImporter<>();
        final Map<String, JGraphtVertex> vertices = new HashMap<>();
        importer.setVertexFactory(id -> vertices.computeIfAbsent(id, JGraphtVertex::new));

        try (Reader reader = new StringReader(digraphDSLString)) {
            importer.importGraph(jgraphtGraph, reader);
        }
    }

    void checkInvariants() {
        final Map<String, JGraphtVertex> vertexMap = jgraphtGraph.vertexSet().stream().collect(Collectors.toMap(JGraphtVertex::getKey, Function.identity()));

        final String entryNodeId = inputData.getEntryNodeId();
        if (!isNullOrEmpty(entryNodeId)) {
            JGraphtVertex entryVertex =  vertexMap.get(entryNodeId);
            Preconditions.checkArgument(entryVertex != null, "Entry vertex [%s] must be present in the Graph.", entryNodeId);
            Preconditions.checkArgument(jgraphtGraph.outDegreeOf(entryVertex) > 0, "Entry vertex [%s] must have outgoing edges.", entryNodeId);
        }

        final String startNodeId = inputData.getStartNodeId();
        startVertex =  vertexMap.get(startNodeId);
        Preconditions.checkArgument(startVertex != null, "Start vertex [%s] must be present in the Graph.", startNodeId);
        Preconditions.checkArgument(jgraphtGraph.outDegreeOf(startVertex) > 0, "Start vertex [%s] must have outgoing edges.", startNodeId);

        final String exitNodeId = inputData.getExitNodeId();
        exitVertex =  vertexMap.get(exitNodeId);
        Preconditions.checkArgument(exitVertex != null, "Exit vertex [%s] must be present in the Graph.", exitNodeId);
        Preconditions.checkArgument(jgraphtGraph.inDegreeOf(exitVertex) > 0, "Exit vertex [%s] must have incoming edges.", exitNodeId);
    }

    void buildGraph() {
        int vertexId = 0;
        final Set<JGraphtVertex> vertexes = jgraphtGraph.vertexSet();
        for (JGraphtVertex vertex: vertexes) {
            vertex.id = vertexId;
            vertexId++;
        }

        final Vertex[] vertices = new Vertex[vertexes.size()];

        for (JGraphtVertex vertex: vertexes) {
            int[] outIndexes = jgraphtGraph.outgoingEdgesOf(vertex).stream()
                    .map(edge -> jgraphtGraph.getEdgeTarget(edge))
                    .mapToInt(JGraphtVertex::getId)
                    .toArray();
            vertices[vertex.getId()] = new Vertex(vertex.getId(), vertex.getKey(), outIndexes, payloadFactoryFunction);
        }

        graph = new Graph(vertices);
    }

    public Vertex startVertex() {
        return graph.vertex(startVertex.getId());
    }
    public Vertex exitVertex() {
        return graph.vertex(exitVertex.getId());
    }
}
