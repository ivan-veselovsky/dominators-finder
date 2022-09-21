package edu.postleadsfinder;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;

class GraphBuilderTest {

    @Test
    void test_build_example_graph() {
        GraphBuilder graphBuilder = new GraphBuilder();
        graphBuilder.build("{\"e1\": \"1\"," +
                "\"e2\": \"7\"," +
                "\"h\":\"2\"," +
                "\"graph\": \" digraph graphname{\n1->2\n2->3\n2->5\n5->2\n3->5\n5->7}\"" +
                "}");
        Graph graph = graphBuilder.getGraph();

        then(graphBuilder.startVertex().getId()).isEqualTo(1);
        then(graphBuilder.exitVertex().getId()).isEqualTo(4);

        graph.vertexStream().forEachOrdered(System.out::println);

        then(graph.size()).isEqualTo(5);
        then(graph.numberOfEdges()).isEqualTo(6);

        then(graph.vertex(0).getOutgoingEdges()).containsExactly(1);
        then(graph.vertex(1).getOutgoingEdges()).containsExactly(2, 3);
        then(graph.vertex(2).getOutgoingEdges()).containsExactly(3);
        then(graph.vertex(3).getOutgoingEdges()).containsExactly(1, 4);
        then(graph.vertex(4).getOutgoingEdges()).containsExactly();
    }
}