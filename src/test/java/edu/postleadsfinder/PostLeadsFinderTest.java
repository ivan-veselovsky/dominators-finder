package edu.postleadsfinder;

import edu.postleadsfinder.naivefinder.PostLeadsFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static edu.postleadsfinder.naivefinder.PostLeadsFinder.asKeys;
import static org.assertj.core.api.BDDAssertions.then;

// TODO: make these tests parametrized
public class PostLeadsFinderTest {

    protected <P> GraphBuilder<P> createGraphBuilder() {
        GraphBuilder<DfsPayload> graphBuilder = new GraphBuilder<>();
        graphBuilder.withPayloadFactoryFunction(DfsPayload::new);
        return (GraphBuilder)graphBuilder;
    }

    protected <P> IDominatorsFinder<P> createFinder(Graph graph, Vertex startVertex, Vertex exitVertex) {
        return (IDominatorsFinder)new PostLeadsFinder(graph, startVertex, exitVertex);
    }

    @ParameterizedTest(name = "{0}, {2} -> {3}")
    @MethodSource("testCases")
    protected final <P> void doTest(TestGraph testGraph,
                                    String startVertexKey, String exitVertexKey, List<String> expectedDominators) {
        GraphBuilder<P> graphBuilder = createGraphBuilder();
        graphBuilder.build(testGraph.graphTopologyJson());
        final Graph<P> graph = graphBuilder.getGraph();

        Vertex<P> startVertex = graph.vertex(startVertexKey);
        then(startVertex).isNotNull();
        Vertex<P> exitVertex = graph.vertex(exitVertexKey);
        then(exitVertex).isNotNull();

        IDominatorsFinder<P> finder = createFinder(graph, startVertex, exitVertex);
        List<String> keyList = asKeys(finder.computePostLeads());
        then(keyList).containsExactlyElementsOf(expectedDominators);
    }

    private static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of(two_vertices_fully_connected(), "A", "B", List.of("B")),
                Arguments.of(two_vertices_fully_connected(), "A", "A", List.of()),
                Arguments.of(single_vertex(), "A", "A", List.of()),
                Arguments.of(simplest_two_vertices(), "A", "B", List.of("B")),
                Arguments.of(simplest_two_vertices(), "A", "A", List.of())

//                Arguments.of("", """
//                    """, "", "", List.of()),
//                Arguments.of("", """
//                    """, "", "", List.of()),
//                Arguments.of("", """
//                    """, "", "", List.of()),
//                Arguments.of("", """
//                    """, "", "", List.of()),
//                Arguments.of("", """
//                    """, "", "", List.of()),
//                Arguments.of("", """
//                    """, "", "", List.of()),
//                Arguments.of("", """
//                    """, "", "", List.of()),
//                Arguments.of("", """
//                    """, "", "", List.of()),
//                Arguments.of("", """
//                    """, "", "", List.of()),
//                Arguments.of("", """
//                    """, "", "", List.of()),
//                Arguments.of("", """
//                    """, "", "", List.of()),
//                Arguments.of("", """
//                    """, "", "", List.of()),
                );
    }

//    @Test
//    void single_vertex_graph() {
//        GraphBuilder<DfsPayload> graphBuilder = createGraphBuilder();
//        graphBuilder.build("{" +
//                "\"e2\": \"A\"," +
//                "\"h\": \"A\"," +
//                + "}");
//        final Graph graph = graphBuilder.getGraph();
//
//        final Vertex startVertex = graphBuilder.startVertex();
//        final Vertex exitVertex = graphBuilder.exitVertex();
//
//        PostLeadsFinder finder = new PostLeadsFinder(graph, startVertex, exitVertex);
//        List<String> keyList = asKeys(finder.computePostLeads());
//
//        then(keyList).isEmpty(); // By convention start node is skipped.
//    }

    record TestGraph(String name, String graphTopologyJson) {
        @Override
        public String toString() {
            return name;
        }
    }

    static TestGraph single_vertex() {
        return new TestGraph(getCallerMethodName(), """
                    {
                        "graph": "digraph graphname{ A }"
                    }   
             """);
    }

    static TestGraph simplest_two_vertices() {
        return new TestGraph(getCallerMethodName(), """
                { 
                   "graph": "digraph graphname{ A -> B }"
                }
                """);
    }

    static TestGraph two_vertices_fully_connected() {
        return new TestGraph(getCallerMethodName(), """
                    {
                      "graph": "digraph graphname{ A -> B; B -> A; A -> A; B -> B }"
                    }
                    """);
    }

    private static String getCallerMethodName() {
        StackTraceElement e = new Throwable().getStackTrace()[1];
        return e.toString();
    }

//    @Test
//    void simplest_graph_two_vertices() {
//        GraphBuilder<DfsPayload> graphBuilder = createGraphBuilder();
//        graphBuilder.build("{" +
//                "\"h\": \"A\"," +
//                "\"e2\": \"B\"," +
//                """
//                  "graph": "digraph graphname{ A -> B }"
//                """
//                + "}");
//        final Graph graph = graphBuilder.getGraph();
//
//        final Vertex startVertex = graphBuilder.startVertex();
//        final Vertex exitVertex = graphBuilder.exitVertex();
//
//        PostLeadsFinder finder = new PostLeadsFinder(graph, startVertex, exitVertex);
//        List<String> keyList = asKeys(finder.computePostLeads());
//        then(keyList).containsExactly("B");
//
//        Vertex vertexA = graph.vertex("A");
//        keyList = asKeys(new PostLeadsFinder(graph, vertexA, vertexA).computePostLeads());
//        then(keyList).isEmpty();
//    }

//    @Test
//    void two_vertices_graph_fully_connected() {
//        GraphBuilder<DfsPayload> graphBuilder = createGraphBuilder();
//        graphBuilder.build("{" +
//                "\"h\": \"A\"," +
//                "\"e2\": \"B\"," +
//                """
//                  "graph": "digraph graphname{ A -> B; B -> A; A -> A; B -> B }"
//                """
//                + "}");
//        final Graph graph = graphBuilder.getGraph();
//
//        final Vertex startVertex = graphBuilder.startVertex();
//        final Vertex exitVertex = graphBuilder.exitVertex();
//
//        PostLeadsFinder finder = new PostLeadsFinder(graph, startVertex, exitVertex);
//        List<String> keyList = asKeys(finder.computePostLeads());
//        then(keyList).containsExactly("B");
//
//        Vertex vertexA = graph.vertex("A");
//        keyList = asKeys(new PostLeadsFinder(graph, vertexA, vertexA).computePostLeads());
//        then(keyList).isEmpty();
//    }

    @Test
    void several_post_leads() {
        GraphBuilder<DfsPayload> graphBuilder = createGraphBuilder();
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
    void large_graph() {
        GraphBuilder<DfsPayload> graphBuilder = createGraphBuilder();
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
        GraphBuilder<DfsPayload> graphBuilder = createGraphBuilder();
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
        GraphBuilder<DfsPayload> graphBuilder = createGraphBuilder();
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
        GraphBuilder<DfsPayload> graphBuilder = createGraphBuilder();
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
        GraphBuilder<DfsPayload> graphBuilder = createGraphBuilder();
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
        GraphBuilder<DfsPayload> graphBuilder = createGraphBuilder();
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
        GraphBuilder<DfsPayload> graphBuilder = createGraphBuilder();
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
        GraphBuilder<DfsPayload> graphBuilder = createGraphBuilder();
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
        GraphBuilder<DfsPayload> graphBuilder = createGraphBuilder();
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
        GraphBuilder<DfsPayload> graphBuilder = createGraphBuilder();
        String[] expectedKeys = expectedPostLeads.split("_");
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
        GraphBuilder<DfsPayload> graphBuilder = createGraphBuilder();
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
        GraphBuilder<DfsPayload> graphBuilder = createGraphBuilder();
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
        GraphBuilder<DfsPayload> graphBuilder = createGraphBuilder();
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
        GraphBuilder<DfsPayload> graphBuilder = createGraphBuilder();
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

//    @Test
//    void single_vertex_t() {
//        GraphBuilder<DfsPayload> graphBuilder = createGraphBuilder();
//        graphBuilder.build("{" +
//                "\"h\": \"A\"," +
//                "\"e2\": \"A\"," +
//                """
//                  "graph": "digraph graphname{ A }"
//                """
//                + "}");
//        final Graph graph = graphBuilder.getGraph();
//
//        final Vertex startVertex = graphBuilder.startVertex();
//        final Vertex exitVertex = graphBuilder.exitVertex();
//
//        PostLeadsFinder finder = new PostLeadsFinder(graph, startVertex, exitVertex);
//        List<String> keyList = asKeys(finder.computePostLeads());
//
//        then(keyList).isEmpty();
//    }

    @Test
    void many_dead_ends() {
        GraphBuilder<DfsPayload> graphBuilder = createGraphBuilder();
        graphBuilder.withPayloadFactoryFunction(DfsPayload::new);
        graphBuilder.build("{" +
                "\"h\": \"A\"," +
                "\"e2\": \"K\"," +
                """ 
                        "graph": "digraph graphname{
                        A -> B
                        A -> C
                        B -> M
                        B -> D
                        C -> D
                        D -> N
                        
                        D -> E
                        D -> F
                        E -> P
                        F -> Q -> R
                        E -> G
                        F -> G
                        
                        E -> H
                        H -> I
                        H -> J

                        G -> K -> U -> V
                        V -> K
                        X -> K
                        U -> W
                        K -> X
                        G -> L -> T
                        }"
                """
                + "}");
        final Graph graph = graphBuilder.getGraph();

        final Vertex startVertex = graphBuilder.startVertex();
        final Vertex exitVertex = graphBuilder.exitVertex();

        List<String> postLeads = asKeys(new PostLeadsFinder(graph, startVertex, exitVertex).computePostLeads());
        then(postLeads).containsExactly("D", "G", "K");
    }

    /**
     * Example when the algorithm does not work, pointed by the task authors:
     */
    @Test
    void bug_in_algoritrhm_0() {
        GraphBuilder<DfsPayload> graphBuilder = createGraphBuilder();
        graphBuilder.build("{" +
                "\"h\": \"A\"," +
                "\"e2\": \"G\"," +
                """ 
                  "graph": "digraph graphname{ 
                    A -> D
                    D -> F
                    F -> E
                    E -> F
                    A -> E
                    F -> G 
                  }"
                """
                + "}");
        final Graph graph = graphBuilder.getGraph();

        final Vertex startVertex = graphBuilder.startVertex();
        final Vertex exitVertex = graphBuilder.exitVertex();

        PostLeadsFinder finder = new PostLeadsFinder(graph, startVertex, exitVertex);
        List<String> keyList = asKeys(finder.computePostLeads());

        then(keyList).containsExactly("F", "G");
    }

    /**
     * Same example as previous one, but this time
     * result (incorrect) does not depend on traversal order,
     * as the graph is symmetric.
     */
    @Test
    void bug_in_algorithm_0_false_result_under_any_traversal_order() {
        GraphBuilder<DfsPayload> graphBuilder = createGraphBuilder();
        graphBuilder.build("{" +
                "\"h\": \"A\"," +
                "\"e2\": \"G\"," +
                """ 
                  "graph": "digraph graphname{ 
                    A -> D
                    D -> F
                    F -> D
                    
                    A -> E
                    E -> F
                    F -> E
                    
                    F -> G 
                  }"
                """
                + "}");
        final Graph graph = graphBuilder.getGraph();

        final Vertex startVertex = graphBuilder.startVertex();
        final Vertex exitVertex = graphBuilder.exitVertex();

        PostLeadsFinder finder = new PostLeadsFinder(graph, startVertex, exitVertex);
        List<String> keyList = asKeys(finder.computePostLeads());

        then(keyList).containsExactly("F", "G");
    }

    /** Example invented by me -- related to incorrect dropping of "back" edge "FD": */
    @Test
    void bug_in_algoritrhm_1() {
        GraphBuilder<DfsPayload> graphBuilder = createGraphBuilder();
        graphBuilder.build("{" +
                "\"h\": \"A\"," +
                "\"e2\": \"D\"," +
                """ 
                  "graph": "digraph graphname{ 
                    A -> B
                    A -> C
                    B -> D
                    C -> E
                    E -> B
                    D -> E
                    E -> F
                    F -> D 
                  }"
                """
                + "}");
        final Graph graph = graphBuilder.getGraph();

        final Vertex startVertex = graphBuilder.startVertex();
        final Vertex exitVertex = graphBuilder.exitVertex();

        PostLeadsFinder finder = new PostLeadsFinder(graph, startVertex, exitVertex);
        List<String> keyList = asKeys(finder.computePostLeads());

        then(keyList).containsExactly("D");
    }

}