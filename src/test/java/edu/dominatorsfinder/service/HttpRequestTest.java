package edu.dominatorsfinder.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.BDDAssertions.then;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HttpRequestTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void should_return_welcome_message_when_doing_get_on_root_url() {
        ResponseEntity<String> entity = restTemplate.getForEntity("http://localhost:" + port + "/", String.class);
        then(entity.getStatusCode()).isSameAs(HttpStatus.OK);
        then(entity.getBody()).startsWith("Welcome to ");
    }

    @Test
    public void should_correctly_find_post_lead_on_task_description_example() {
        ResponseEntity<String> entity = restTemplate.postForEntity("http://localhost:" + port + "/server",
                """
                {"e2": "7",
                 "h": "2",
                 "graph": " digraph graphname{
                    1->2
                    2->3
                    2->5
                    5->2
                    3->5
                    5->7
                   }"
                }
                """ , String.class
                );
        then(entity.getStatusCode()).isSameAs(HttpStatus.OK);
        then(entity.getBody()).isEqualTo("{5, 7}\n");
    }

    @Test
    public void should_return_error_on_incorrect_input() {
        ResponseEntity<String> entity = restTemplate.postForEntity("http://localhost:" + port + "/server",
                """
                {"e2": "7",
                 "h": "2",
                 "graph": " digraph graphname{
                    1->2
                    2->2
                    2->1
                    5->2
                    3->5
                    5->7
                   }"
                }
                """ , String.class
        );
        then(entity.getStatusCode()).isSameAs(HttpStatus.BAD_REQUEST);
        then(entity.getBody()).contains("java.lang.IllegalArgumentException: Exit vertex [7] appears to be unreachable from the start node [2]");
    }
}