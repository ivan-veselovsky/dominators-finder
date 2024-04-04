package edu.postleadsfinder;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static edu.postleadsfinder.TestUtil.getCallerMethodName;
import static edu.postleadsfinder.Util.asKeys;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public abstract class AbstractDominatorsFinderTest<Payload> {

    protected abstract AbstractFinderFactory<Payload> getFactory();

    @ParameterizedTest(name = "{0}, {1} -> {2}")
    @MethodSource("testCases")
    protected void doTest(TestGraph testGraph,
                                    String startVertexKey, String exitVertexKey, List<String> expectedDominators) {
        GraphBuilder<Payload> graphBuilder = getFactory().createGraphBuilder();
        graphBuilder.build(testGraph.graphTopologyJson());
        final Graph<Payload> graph = graphBuilder.getGraph();

        Vertex<Payload> startVertex = graph.vertex(startVertexKey);
        then(startVertex).isNotNull();
        Vertex<Payload> exitVertex = graph.vertex(exitVertexKey);
        then(exitVertex).isNotNull();

        IDominatorsFinder<Payload> finder = getFactory().createFinder(graph, startVertex, exitVertex);
        List<String> keyList = asKeys(finder.computeDominators());
        then(keyList).containsExactlyElementsOf(expectedDominators);
    }

    private static Stream<Arguments> testCases() {
        return Stream.of(
                arguments(two_vertices_fully_connected(), "A", "B", List.of("B")),
                arguments(two_vertices_fully_connected(), "A", "A", List.of()),
                arguments(single_vertex(), "A", "A", List.of()),
                arguments(simplest_two_vertices(), "A", "B", List.of("B")),
                arguments(simplest_two_vertices(), "A", "A", List.of()),

                arguments(several_post_leads(), "A", "K", List.of("B", "D", "G", "K")),
                arguments(large_graph(), "a", "o", List.of("n", "o")),
                arguments(small_with_all_type_of_edges(), "A", "G", List.of("B", "F", "G")),
                arguments(small_graph_many_back_edges_fully_reflective(), "A", "G", List.of("B", "F", "G")),
                arguments(sand_glass_shape(), "A", "H", List.of("D", "H")),
                arguments(bridge_shape(), "A", "H", List.of("D", "E", "H")),
                arguments(long_bridge_shape(), "A", "H", List.of("D", "X", "Y", "Z", "E", "H")),
                arguments(long_bridge_shape_with_back_edges(), "A", "H", List.of("D", "X", "Z", "E", "H")),
                arguments(long_bridge_shape_with_back_edges_fully_reflective(), "A", "H", List.of("D", "X", "Z", "E", "H")),
                arguments(all2all_two_vertices(), "A", "B", List.of("B")),
                arguments(all2all_three_vertices(), "A", "B", List.of("B")),
                arguments(butterfly_shape(), "A", "E", List.of("C", "E")),
                arguments(butterfly_shape(), "A", "B", List.of("B")),
                arguments(butterfly_shape(), "A", "D", List.of("C", "D")),
                arguments(edge_made_dead_twice_simplest_example(), "A", "B", List.of("B")),
                arguments(many_dead_ends(), "A", "K", List.of("D", "G", "K")),

                /* Example when the algorithm does not work, pointed by the task authors: */
                arguments(bug_in_naive_algorithm_0(), "A", "G", List.of("F", "G")),
                /*
                 * Same example as previous one, but this time
                 * result (incorrect) does not depend on traversal order,
                 * as the graph is symmetric.
                 */
                arguments(bug_in_naive_algorithm_0_false_result_under_any_traversal_order(), "A", "G", List.of("F", "G")),
                /* Example of incorrect dropping of "back" edge "F -> D": */
                arguments(bug_in_naive_algorithm_incorrect_dropping_of_back_edge(), "A", "D", List.of("D"))
        );
    }

    protected record TestGraph(String name, String graphTopologyJson) {
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

    static TestGraph several_post_leads() {
        return new TestGraph(getCallerMethodName(), """
          {
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
          }
          """);
    }

    static TestGraph large_graph() {
        return new TestGraph(getCallerMethodName(), """
{
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
}                    """);
    }

    static TestGraph small_with_all_type_of_edges() {
        return new TestGraph(getCallerMethodName(), """
{
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
}""");
    }

    static TestGraph small_graph_many_back_edges_fully_reflective() {
        return new TestGraph(getCallerMethodName(), """
{
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
}""");
    }

    static TestGraph sand_glass_shape() {
        return new TestGraph(getCallerMethodName(), """
{
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
}""");
    }

    static TestGraph bridge_shape() {
        return new TestGraph(getCallerMethodName(), """
{
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
}""");
    }

    static TestGraph long_bridge_shape() {
        return new TestGraph(getCallerMethodName(), """
{
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
}""");
    }

    static TestGraph long_bridge_shape_with_back_edges() {
        return new TestGraph(getCallerMethodName(), """
{
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
}""");
    }

    static TestGraph long_bridge_shape_with_back_edges_fully_reflective() {
        return new TestGraph(getCallerMethodName(), """
{
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
}""");
    }

    static TestGraph all2all_two_vertices() {
        return new TestGraph(getCallerMethodName(), """
{
                   "graph": "digraph graphname{
                        A -> B
                        A -> A
                        B -> A
                        B -> B
                        }"
}""");
    }

    static TestGraph all2all_three_vertices() {
        return new TestGraph(getCallerMethodName(), """
{
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
}""");
    }

    static TestGraph butterfly_shape() {
        return new TestGraph(getCallerMethodName(), """
{
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
}""");
    }

    static TestGraph edge_made_dead_twice_simplest_example() {
        return new TestGraph(getCallerMethodName(), """
{
                "graph": "digraph graphname{
                    A -> B
                    A -> C
                  }"
}""");
    }

    static TestGraph many_dead_ends() {
        return new TestGraph(getCallerMethodName(), """
{
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
}""");
    }

    static TestGraph bug_in_naive_algorithm_0() {
        return new TestGraph(getCallerMethodName(), """
{
                  "graph": "digraph graphname{ 
                    A -> D
                    D -> F
                    F -> E
                    E -> F
                    A -> E
                    F -> G 
                  }"
}""");
    }

    static TestGraph bug_in_naive_algorithm_0_false_result_under_any_traversal_order() {
        return new TestGraph(getCallerMethodName(), """
{
                  "graph": "digraph graphname{ 
                    A -> D
                    D -> F
                    F -> D
                    
                    A -> E
                    E -> F
                    F -> E
                    
                    F -> G 
                  }"
}""");
    }

    static TestGraph bug_in_naive_algorithm_incorrect_dropping_of_back_edge() {
        return new TestGraph(getCallerMethodName(), """
{
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
}""");
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
        GraphBuilder<Payload> graphBuilder = getFactory().createGraphBuilder();
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
        final Graph<Payload> graph = graphBuilder.getGraph();

        final Vertex<Payload> startVertex = graphBuilder.startVertex();
        final Vertex<Payload> exitVertex = graphBuilder.exitVertex();

        IDominatorsFinder<Payload> finder = getFactory().createFinder(graph, startVertex, exitVertex);
        List<String> keyList = asKeys(finder.computeDominators());

        then(keyList).containsExactly(expectedKeys);
    }
}