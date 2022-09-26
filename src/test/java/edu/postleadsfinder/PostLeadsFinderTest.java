package edu.postleadsfinder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static edu.postleadsfinder.PostLeadsFinder.asKeys;
import static org.assertj.core.api.BDDAssertions.then;

class PostLeadsFinderTest {
    @Test
    void single_vertex_graph() {
        GraphBuilder graphBuilder = new GraphBuilder();
        graphBuilder.build("{" +
                "\"e2\": \"A\"," +
                "\"h\": \"A\"," +
                """ 
                  "graph": "digraph graphname{ A }"
                """
                + "}");
        final Graph graph = graphBuilder.getGraph();

        final Vertex startVertex = graphBuilder.startVertex();
        final Vertex exitVertex = graphBuilder.exitVertex();

        PostLeadsFinder finder = new PostLeadsFinder(graph, startVertex, exitVertex);
        List<String> keyList = asKeys(finder.computePostLeads());

        then(keyList).isEmpty(); // By convention start node is skipped.
    }

    @Test
    void simplest_graph_two_vertices() {
        GraphBuilder graphBuilder = new GraphBuilder();
        graphBuilder.build("{" +
                "\"h\": \"A\"," +
                "\"e2\": \"B\"," +
                """ 
                  "graph": "digraph graphname{ A -> B }"
                """
                + "}");
        final Graph graph = graphBuilder.getGraph();

        final Vertex startVertex = graphBuilder.startVertex();
        final Vertex exitVertex = graphBuilder.exitVertex();

        PostLeadsFinder finder = new PostLeadsFinder(graph, startVertex, exitVertex);
        List<String> keyList = asKeys(finder.computePostLeads());
        then(keyList).containsExactly("B");

        Vertex vertexA = graph.vertex("A");
        keyList = asKeys(new PostLeadsFinder(graph, vertexA, vertexA).computePostLeads());
        then(keyList).isEmpty();
    }

    @Test
    void two_vertices_graph_fully_connected() {
        GraphBuilder graphBuilder = new GraphBuilder();
        graphBuilder.build("{" +
                "\"h\": \"A\"," +
                "\"e2\": \"B\"," +
                """ 
                  "graph": "digraph graphname{ A -> B; B -> A; A -> A; B -> B }"
                """
                + "}");
        final Graph graph = graphBuilder.getGraph();

        final Vertex startVertex = graphBuilder.startVertex();
        final Vertex exitVertex = graphBuilder.exitVertex();

        PostLeadsFinder finder = new PostLeadsFinder(graph, startVertex, exitVertex);
        List<String> keyList = asKeys(finder.computePostLeads());
        then(keyList).containsExactly("B");

        Vertex vertexA = graph.vertex("A");
        keyList = asKeys(new PostLeadsFinder(graph, vertexA, vertexA).computePostLeads());
        then(keyList).isEmpty();
    }

    @Test
    void several_post_leads() {
        GraphBuilder graphBuilder = new GraphBuilder();
        graphBuilder.build("{" +
                "\"h\": \"A\"," +
                "\"e2\": \"K\"," +
                """ 
                  "graph": "digraph graphname{ 
                  A -> B
                   
                  B -> C
                  B -> D
                   
                  C -> D
                  
                  D -> E 
                  D -> F
                  D -> G
                  
                  E -> G 
                  F -> G
                  
                  G -> H
                  G -> I 
                  G -> J
                  G -> K
                  
                  H -> K
                  I -> K
                  J -> K 
                  }"
                """
                + "}");
        final Graph graph = graphBuilder.getGraph();

        final Vertex startVertex = graphBuilder.startVertex();
        final Vertex exitVertex = graphBuilder.exitVertex();

        PostLeadsFinder finder = new PostLeadsFinder(graph, startVertex, exitVertex);
        List<String> keyList = asKeys(finder.computePostLeads());

        then(keyList).containsExactly("B", "D", "G", "K");
    }

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
        List<String> postLeadKeys = asKeys(finder.computePostLeads());
        then(postLeadKeys).containsExactly("5", "7");

        // Start/End times:
        then(graph.vertex(0).getVertexPayload().getStartTime()).isEqualTo(-1);
        then(graph.vertex(0).getVertexPayload().getFinishTime()).isEqualTo(-1);
        then(graph.vertex(1).getVertexPayload().getStartTime()).isEqualTo(1);
        then(graph.vertex(1).getVertexPayload().getFinishTime()).isEqualTo(10);
        then(graph.vertex(2).getVertexPayload().getStartTime()).isEqualTo(2);
        then(graph.vertex(2).getVertexPayload().getFinishTime()).isEqualTo(8);
        then(graph.vertex(3).getVertexPayload().getStartTime()).isEqualTo(3);
        then(graph.vertex(3).getVertexPayload().getFinishTime()).isEqualTo(7);
        then(graph.vertex(4).getVertexPayload().getStartTime()).isEqualTo(5);
        then(graph.vertex(4).getVertexPayload().getFinishTime()).isEqualTo(6);

        // Edges color:
        then(graph.vertex(1).getVertexPayload().edgeKind(2)).isEqualTo(EdgeKind.TREE);
        then(graph.vertex(1).getVertexPayload().edgeKind(3)).isEqualTo(EdgeKind.FORWARD);
        then(graph.vertex(2).getVertexPayload().edgeKind(3)).isEqualTo(EdgeKind.TREE);
        then(graph.vertex(3).getVertexPayload().edgeKind(4)).isEqualTo(EdgeKind.TREE);
        then(graph.vertex(3).getVertexPayload().edgeKind(1)).isEqualTo(EdgeKind.BACKWARD);
    }

    @Test
    void large_graph() {
        GraphBuilder graphBuilder = new GraphBuilder();
        graphBuilder.build("{" +
                "\"e2\": \"o\"," +
                "\"h\": \"a\"," +
""" 
"graph": "digraph graphname{
b -> c
b -> d
a -> b
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
        List<String> keyList = asKeys(finder.computePostLeads());

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
        List<String> keyList = asKeys(finder.computePostLeads());

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
        List<String> keyList = asKeys(finder.computePostLeads());

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
        List<String> keyList = asKeys(finder.computePostLeads());

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
        List<String> keyList = asKeys(finder.computePostLeads());

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
        List<String> keyList = asKeys(finder.computePostLeads());

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
        List<String> keyList = asKeys(finder.computePostLeads());

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
        List<String> keyList = asKeys(finder.computePostLeads());

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
        List<String> keyList = asKeys(finder.computePostLeads());

        then(keyList).containsExactly("D", "X", "Z", "E", "H");
    }

    @ParameterizedTest
    @CsvSource({"B, F_G",
            "C, F_G",
            "D, E_B_F_G",
            "E, B_F_G",
            "F, G",
            "G, _"
    })
    void all_possible_start_vertices(String start, String expectedPostLeads) {
        String[] expectedKeys = expectedPostLeads.split("_");
        GraphBuilder graphBuilder = new GraphBuilder();
        graphBuilder.build("{" +
                "\"h\": \"" + start + "\"," +
                "\"e2\": \"G\"," +
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
        List<String> keyList = asKeys(finder.computePostLeads());

        then(keyList).containsExactly(expectedKeys);
    }

    @Test
    void all2all_two_vertices() {
        GraphBuilder graphBuilder = new GraphBuilder();
        graphBuilder.build("{" +
                "\"h\": \"A\"," +
                "\"e2\": \"B\"," +
                """ 
                        "graph": "digraph graphname{
                        A -> B
                        A -> A
                        B -> A
                        B -> B
                        }"
                """
                + "}");
        final Graph graph = graphBuilder.getGraph();

        final Vertex startVertex = graphBuilder.startVertex();
        final Vertex exitVertex = graphBuilder.exitVertex();

        List<String> postLeads = asKeys(new PostLeadsFinder(graph, startVertex, exitVertex).computePostLeads());
        then(postLeads).containsExactly("B");
    }

    @Test
    void all2all_three_vertices() {
        GraphBuilder graphBuilder = new GraphBuilder();
        graphBuilder.build("{" +
                "\"h\": \"A\"," +
                "\"e2\": \"B\"," +
                """ 
                        "graph": "digraph graphname{
                        A -> A
                        A -> B
                        A -> C
                        
                        B -> A
                        B -> B
                        B -> C
                        
                        C -> A
                        C -> B
                        C -> C
                        }"
                """
                + "}");
        final Graph graph = graphBuilder.getGraph();

        final Vertex startVertex = graphBuilder.startVertex();
        final Vertex exitVertex = graphBuilder.exitVertex();

        List<String> postLeads = asKeys(new PostLeadsFinder(graph, startVertex, exitVertex).computePostLeads());
        then(postLeads).containsExactly("B");
    }

    @Test
    void butterfly_shape() {
        GraphBuilder graphBuilder = new GraphBuilder();
        graphBuilder.build("{" +
                "\"h\": \"A\"," +
                "\"e2\": \"E\"," +
                """ 
                        "graph": "digraph graphname{
                        A -> A
                        A -> B
                        A -> C
                        
                        B -> A
                        B -> B
                        B -> C
                        
                        C -> A
                        C -> B
                        C -> C
                        C -> D
                        C -> E
                        
                        D -> C
                        D -> E
                        D -> D
                        
                        E -> D
                        E -> C
                        E -> E
                        }"
                """
                + "}");
        final Graph graph = graphBuilder.getGraph();

        final Vertex startVertex = graphBuilder.startVertex();
        final Vertex exitVertex = graphBuilder.exitVertex();

        List<String> postLeads = asKeys(new PostLeadsFinder(graph, startVertex, exitVertex).computePostLeads());
        then(postLeads).containsExactly("C", "E");

        Vertex vertexB = graph.vertex("B");
        postLeads = asKeys(new PostLeadsFinder(graph, startVertex, vertexB).computePostLeads());
        then(postLeads).containsExactly("B");

        Vertex vertexD = graph.vertex("D");
        postLeads = asKeys(new PostLeadsFinder(graph, startVertex, vertexD).computePostLeads());
        then(postLeads).containsExactly("C", "D");
    }

    @Test
    void edge_made_dead_twice_simplest_example() {
        GraphBuilder graphBuilder = new GraphBuilder();
        graphBuilder.build("{" +
                "\"h\": \"A\"," +
                "\"e2\": \"B\"," +
                """ 
                  "graph": "digraph graphname{ 
                    A -> B
                    A -> C
                  }"
                """
                + "}");
        final Graph graph = graphBuilder.getGraph();

        final Vertex startVertex = graphBuilder.startVertex();
        final Vertex exitVertex = graphBuilder.exitVertex();

        PostLeadsFinder finder = new PostLeadsFinder(graph, startVertex, exitVertex);
        List<String> keyList = asKeys(finder.computePostLeads());

        then(keyList).containsExactly("B");
    }

}