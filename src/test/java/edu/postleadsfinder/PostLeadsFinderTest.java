package edu.postleadsfinder;

import org.junit.jupiter.api.Test;

import java.util.List;

import static edu.postleadsfinder.PostLeadsFinder.asKeys;
import static org.assertj.core.api.BDDAssertions.then;

class PostLeadsFinderTest {

    @Test
    void example_from_task_description() {
        GraphBuilder graphBuilder = new GraphBuilder();
        graphBuilder.build("{" +
                "\"e2\": \"7\"," +
                "\"h\": \"2\"," +
                "\"graph\": \" digraph graphname{\n1->2\n2->3\n2->5\n5->2\n3->5\n5->7}\"" +
                "}");
        final Graph graph = graphBuilder.getGraph();

        final Vertex startVertex = graphBuilder.startVertex();
        final Vertex exitVertex = graphBuilder.exitVertex();

        graph.vertexStream().forEachOrdered(System.out::println);

        // Edges:
        then(graph.vertex(0).getOutgoingEdges()).containsExactly(1);
        then(graph.vertex(1).getOutgoingEdges()).containsExactly(2, 3);
        then(graph.vertex(2).getOutgoingEdges()).containsExactly(3);
        then(graph.vertex(3).getOutgoingEdges()).containsExactly(1, 4);
        then(graph.vertex(4).getOutgoingEdges()).containsExactly();

        PostLeadsFinder finder = new PostLeadsFinder(graph, startVertex, exitVertex);
        List<String> postLeadKeys = asKeys(finder.doJob());
        then(postLeadKeys).containsExactly("5", "7");

        // Start/End times:
        then(graph.vertex(0).getNodePayload().getStartTime()).isEqualTo(-1);
        then(graph.vertex(0).getNodePayload().getFinishTime()).isEqualTo(-1);
        then(graph.vertex(1).getNodePayload().getStartTime()).isEqualTo(1);
        then(graph.vertex(1).getNodePayload().getFinishTime()).isEqualTo(10);
        then(graph.vertex(2).getNodePayload().getStartTime()).isEqualTo(2);
        then(graph.vertex(2).getNodePayload().getFinishTime()).isEqualTo(8);
        then(graph.vertex(3).getNodePayload().getStartTime()).isEqualTo(3);
        then(graph.vertex(3).getNodePayload().getFinishTime()).isEqualTo(7);
        then(graph.vertex(4).getNodePayload().getStartTime()).isEqualTo(5);
        then(graph.vertex(4).getNodePayload().getFinishTime()).isEqualTo(6);

        // Edges color:
        then(graph.vertex(1).getNodePayload().edgeKind(2)).isEqualTo(EdgeKind.TREE);
        then(graph.vertex(1).getNodePayload().edgeKind(3)).isEqualTo(EdgeKind.FORWARD);
        then(graph.vertex(2).getNodePayload().edgeKind(3)).isEqualTo(EdgeKind.TREE);
        then(graph.vertex(3).getNodePayload().edgeKind(4)).isEqualTo(EdgeKind.TREE);
        then(graph.vertex(3).getNodePayload().edgeKind(1)).isEqualTo(EdgeKind.BACKWARD);
    }

    @Test
    void large_graph() {
        GraphBuilder graphBuilder = new GraphBuilder();
        graphBuilder.build("{" +
                "\"e2\": \"o\"," +
                "\"h\": \"a\"," +
""" 
"graph": "digraph graphname{
a -> b
b -> c
b -> d
c -> e
d -> e
e -> f
f -> g
e -> h
g -> h
h -> d
f -> i
i -> j
i -> k
f -> l
a -> i
i -> l
j -> l
j -> m
c -> j
k -> m
l -> n
m -> n
n -> o
n -> i
n -> a
}"
"""
        + "}");
        final Graph graph = graphBuilder.getGraph();

        final Vertex startVertex = graphBuilder.startVertex();
        final Vertex exitVertex = graphBuilder.exitVertex();

        PostLeadsFinder finder = new PostLeadsFinder(graph, startVertex, exitVertex);
        List<String> keyList = asKeys(finder.doJob());

        then(keyList).containsExactly("n", "o");
    }

    @Test
    void small_with_all_type_of_edges() {
        GraphBuilder graphBuilder = new GraphBuilder();
        graphBuilder.build("{" +
                "\"e2\": \"G\"," +
                "\"h\": \"A\"," +
                """ 
                        "graph": "digraph graphname{
                        A -> B
                        B -> C
                        B -> D
                        D -> E
                        C -> F
                        C -> E
                        F -> G
                        E -> B
                        B -> F
                        G -> B
                        }"
                """
                + "}");
        final Graph graph = graphBuilder.getGraph();

        final Vertex startVertex = graphBuilder.startVertex();
        final Vertex exitVertex = graphBuilder.exitVertex();

        PostLeadsFinder finder = new PostLeadsFinder(graph, startVertex, exitVertex);
        List<String> keyList = asKeys(finder.doJob());

        then(keyList).containsExactly("B", "F", "G");
    }

    @Test
    void small_graph_many_back_edges() {
        GraphBuilder graphBuilder = new GraphBuilder();
        graphBuilder.build("{" +
                "\"e2\": \"G\"," +
                "\"h\": \"A\"," +
                """ 
                        "graph": "digraph graphname{
                        A -> B
                        B -> C
                        B -> D
                        D -> E
                        C -> F
                        C -> E
                        F -> G
                        E -> B
                        B -> F
                        G -> X
                        X -> B
                        G -> A
                        G -> B
                        G -> C
                        G -> D
                        G -> E
                        G -> F
                        }"
                """
                + "}");
        final Graph graph = graphBuilder.getGraph();

        final Vertex startVertex = graphBuilder.startVertex();
        final Vertex exitVertex = graphBuilder.exitVertex();

        PostLeadsFinder finder = new PostLeadsFinder(graph, startVertex, exitVertex);
        List<String> keyList = asKeys(finder.doJob());

        then(keyList).containsExactly("B", "F", "G");
    }

    @Test
    void small_graph_many_back_edges_fully_reflective() {
        GraphBuilder graphBuilder = new GraphBuilder();
        graphBuilder.build("{" +
                "\"e2\": \"G\"," +
                "\"h\": \"A\"," +
                """ 
                        "graph": "digraph graphname{
                        A -> B
                        B -> C
                        B -> D
                        D -> E
                        C -> F
                        C -> E
                        F -> G
                        E -> B
                        B -> F
                        G -> X
                        X -> B
                        G -> A
                        G -> B
                        G -> C
                        G -> D
                        G -> E
                        G -> F
                        
                        A -> A
                        B -> B
                        C -> C
                        D -> D
                        E -> E
                        F -> F
                        G -> G
                        }"
                """
                + "}");
        final Graph graph = graphBuilder.getGraph();

        final Vertex startVertex = graphBuilder.startVertex();
        final Vertex exitVertex = graphBuilder.exitVertex();

        PostLeadsFinder finder = new PostLeadsFinder(graph, startVertex, exitVertex);
        List<String> keyList = asKeys(finder.doJob());

        then(keyList).containsExactly("B", "F", "G");
    }

    @Test
    void sand_glass_shape() {
        GraphBuilder graphBuilder = new GraphBuilder();
        graphBuilder.build("{" +
                "\"e2\": \"H\"," +
                "\"h\": \"A\"," +
                """ 
                        "graph": "digraph graphname{
                        A -> B
                        A -> C
                        B -> D
                        C -> D
                        D -> F
                        D -> G
                        F -> H
                        G -> H
                        }"
                """
                + "}");
        final Graph graph = graphBuilder.getGraph();

        final Vertex startVertex = graphBuilder.startVertex();
        final Vertex exitVertex = graphBuilder.exitVertex();

        PostLeadsFinder finder = new PostLeadsFinder(graph, startVertex, exitVertex);
        List<String> keyList = asKeys(finder.doJob());

        then(keyList).containsExactly("D", "H");
    }


    @Test
    void bridge_shape() {
        GraphBuilder graphBuilder = new GraphBuilder();
        graphBuilder.build("{" +
                "\"e2\": \"H\"," +
                "\"h\": \"A\"," +
                """ 
                        "graph": "digraph graphname{
                        A -> B
                        A -> C
                        A -> D
                        B -> D
                        C -> D
                        D -> E
                        E -> F
                        E -> G
                        F -> H
                        G -> H
                        }"
                """
                + "}");
        final Graph graph = graphBuilder.getGraph();

        final Vertex startVertex = graphBuilder.startVertex();
        final Vertex exitVertex = graphBuilder.exitVertex();

        PostLeadsFinder finder = new PostLeadsFinder(graph, startVertex, exitVertex);
        List<String> keyList = asKeys(finder.doJob());

        then(keyList).containsExactly("D", "E", "H");
    }

    @Test
    void long_bridge_shape() {
        GraphBuilder graphBuilder = new GraphBuilder();
        graphBuilder.build("{" +
                "\"e2\": \"H\"," +
                "\"h\": \"A\"," +
                """ 
                        "graph": "digraph graphname{
                        A -> B
                        A -> C
                        A -> D
                        B -> D
                        C -> D
                        D -> X -> Y -> Z -> E
                        E -> F
                        E -> G
                        E -> H
                        F -> H
                        G -> H
                        }"
                """
                + "}");
        final Graph graph = graphBuilder.getGraph();

        final Vertex startVertex = graphBuilder.startVertex();
        final Vertex exitVertex = graphBuilder.exitVertex();

        PostLeadsFinder finder = new PostLeadsFinder(graph, startVertex, exitVertex);
        List<String> keyList = asKeys(finder.doJob());

        then(keyList).containsExactly("D", "X", "Y", "Z", "E", "H");
    }

    @Test
    void long_bridge_shape_with_back_edges() {
        GraphBuilder graphBuilder = new GraphBuilder();
        graphBuilder.build("{" +
                "\"e2\": \"H\"," +
                "\"h\": \"A\"," +
                """ 
                        "graph": "digraph graphname{
                        A -> B
                        A -> C
                        A -> D
                        B -> D
                        C -> D
                        D -> X -> Y -> Z -> E
                        X -> Z
                        E -> F
                        E -> G
                        E -> H
                        F -> H
                        F -> B
                        G -> H
                        G -> C
                        H -> A
                        H -> X
                        Y -> C
                        }"
                """
                + "}");
        final Graph graph = graphBuilder.getGraph();

        final Vertex startVertex = graphBuilder.startVertex();
        final Vertex exitVertex = graphBuilder.exitVertex();

        PostLeadsFinder finder = new PostLeadsFinder(graph, startVertex, exitVertex);
        List<String> keyList = asKeys(finder.doJob());

        then(keyList).containsExactly("D", "X", "Z", "E", "H");
    }

    @Test
    void long_bridge_shape_with_back_edges_fully_reflective() {
        GraphBuilder graphBuilder = new GraphBuilder();
        graphBuilder.build("{" +
                "\"e2\": \"H\"," +
                "\"h\": \"A\"," +
                """ 
                        "graph": "digraph graphname{
                        A -> B
                        A -> C
                        A -> D
                        B -> D
                        C -> D
                        D -> X -> Y -> Z -> E
                        X -> Z
                        E -> F
                        E -> G
                        E -> H
                        F -> H
                        F -> B
                        G -> H
                        G -> C
                        H -> A
                        H -> X
                        Y -> C
                        A -> A
                        B -> B
                        C -> C
                        D -> D
                        E -> E
                        F -> F
                        G -> G
                        }"
                """
                + "}");
        final Graph graph = graphBuilder.getGraph();

        final Vertex startVertex = graphBuilder.startVertex();
        final Vertex exitVertex = graphBuilder.exitVertex();

        PostLeadsFinder finder = new PostLeadsFinder(graph, startVertex, exitVertex);
        List<String> keyList = asKeys(finder.doJob());

        then(keyList).containsExactly("D", "X", "Z", "E", "H");
    }

}