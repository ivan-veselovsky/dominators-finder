package edu.postleadsfinder.dijkstras;

import edu.postleadsfinder.Graph;
import edu.postleadsfinder.GraphBuilder;
import edu.postleadsfinder.Vertex;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;

class DijkstrasMinWeightPathTest {

    @Test
    void test() {
        GraphBuilder<DijPayload> graphBuilder = new GraphBuilder<>();
        graphBuilder.withPayloadFactoryFunction(DijPayload::new);
        graphBuilder.build("""
                {"e2": "E",
                 "h": "C",
                 "graph": " digraph graphname{
                    A->B
                    B->C
                    B->D
                    D->B
                    C->D
                    D->E
                   }"
                }
                """);
        final Graph<DijPayload> graph = graphBuilder.getGraph();

        final Vertex<DijPayload> startVertex = graph.vertex("A");
        final Vertex<DijPayload> exitVertex = graph.vertex("E");

        List<Vertex<DijPayload>> path = DijkstrasMinWeightPath.computeMinWeightPath(graph, startVertex, exitVertex, (v1, v2) -> 1, 1);
        List<String> keys = path.stream().map(Vertex::getKey).toList();
        then(keys).containsExactly("A", "B", "D", "E");

        path = DijkstrasMinWeightPath.computeMinWeightPath(graph, startVertex, exitVertex, (v1, v2) -> {
            if (v1.getId() == 1 && v2.getId() == 3) {
                return 10; // heavy edge
            }
            return 1;
        }, 10);
        keys = path.stream().map(Vertex::getKey).toList();
        then(keys).containsExactly("A", "B", "C", "D", "E");
    }
}