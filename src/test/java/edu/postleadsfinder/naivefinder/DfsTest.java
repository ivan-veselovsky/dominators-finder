package edu.postleadsfinder.naivefinder;

import edu.postleadsfinder.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static edu.postleadsfinder.Util.asKeys;
import static org.assertj.core.api.BDDAssertions.then;

public class DfsTest {

    @Test
    void example_from_task_description() {
        GraphBuilder<DfsPayload> graphBuilder = new GraphBuilder<>();
        graphBuilder.withPayloadFactoryFunction(DfsPayload::new);
        graphBuilder.build("""
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
                """);
        final Graph<DfsPayload> graph = graphBuilder.getGraph();

        final Vertex<DfsPayload> startVertex = graphBuilder.startVertex();
        final Vertex<DfsPayload> exitVertex = graphBuilder.exitVertex();

        graph.vertexStream().forEachOrdered(System.out::println);

        // Edges:
        then(graph.vertex(0).getOutgoingEdges()).containsExactly(1);
        then(graph.vertex(1).getOutgoingEdges()).containsExactly(2, 3);
        then(graph.vertex(2).getOutgoingEdges()).containsExactly(3);
        then(graph.vertex(3).getOutgoingEdges()).containsExactly(1, 4);
        then(graph.vertex(4).getOutgoingEdges()).containsExactly();

        NaiveDfsDominatorsFinder finder = new NaiveDfsDominatorsFinder(graph, startVertex, exitVertex);
        List<String> postLeadKeys = asKeys(finder.computeDominators());
        then(postLeadKeys).containsExactly("5", "7");

        // Start/End times:
        then(graph.vertex(0).getPayload().getDfsStartTime()).isEqualTo(-1);
        then(graph.vertex(0).getPayload().getDfsFinishTime()).isEqualTo(-1);
        then(graph.vertex(1).getPayload().getDfsStartTime()).isEqualTo(1);
        then(graph.vertex(1).getPayload().getDfsFinishTime()).isEqualTo(10);
        then(graph.vertex(2).getPayload().getDfsStartTime()).isEqualTo(2);
        then(graph.vertex(2).getPayload().getDfsFinishTime()).isEqualTo(8);
        then(graph.vertex(3).getPayload().getDfsStartTime()).isEqualTo(3);
        then(graph.vertex(3).getPayload().getDfsFinishTime()).isEqualTo(7);
        then(graph.vertex(4).getPayload().getDfsStartTime()).isEqualTo(5);
        then(graph.vertex(4).getPayload().getDfsFinishTime()).isEqualTo(6);

        // Edges color:
        then(graph.vertex(1).getPayload().edgeKind(2)).isEqualTo(EdgeKind.TREE);
        then(graph.vertex(1).getPayload().edgeKind(3)).isEqualTo(EdgeKind.FORWARD);
        then(graph.vertex(2).getPayload().edgeKind(3)).isEqualTo(EdgeKind.TREE);
        then(graph.vertex(3).getPayload().edgeKind(4)).isEqualTo(EdgeKind.TREE);
        then(graph.vertex(3).getPayload().edgeKind(1)).isEqualTo(EdgeKind.BACKWARD);
    }

}
