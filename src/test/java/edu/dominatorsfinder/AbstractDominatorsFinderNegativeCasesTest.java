package edu.dominatorsfinder;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.*;

public abstract class AbstractDominatorsFinderNegativeCasesTest<Payload> {

    protected abstract AbstractFinderFactory<Payload> getFactory();

    @Test
    void small_with_all_type_of_edges_target_node_unreachable_not_simply_connected() {
        GraphBuilder<Payload> graphBuilder = getFactory().createGraphBuilder();
        graphBuilder.build("{" +
                "\"e2\": \"Y\"," +
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
                        
                        X -> Y
                        Y -> Z
                        Z -> X
                        
                        T -> U
                        U -> T
                        
                        Q -> Q
                        }"
                """
                + "}");
        final Graph<Payload> graph = graphBuilder.getGraph();

        final Vertex<Payload> startVertex = graphBuilder.startVertex();
        final Vertex<Payload> exitVertex = graphBuilder.exitVertex();

        thenExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                getFactory().createFinder(graph, startVertex, exitVertex).computeDominators()
        ).withMessageContaining("Exit vertex [Y] appears to be unreachable from the start node [A]");

        final Vertex<Payload> vertexQ = graph.vertex("Q");
        thenExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                getFactory().createFinder(graph, startVertex, vertexQ).computeDominators()
        ).withMessageContaining("Exit vertex [Q] appears to be unreachable from the start node [A]");

        final Vertex<Payload> vertexT = graph.vertex("T");
        thenExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                getFactory().createFinder(graph, startVertex, vertexT).computeDominators()
        ).withMessageContaining("Exit vertex [T] appears to be unreachable from the start node [A]");
    }

    @Test
    void small_with_all_type_of_edges_target_node_unreachable_simply_connected() {
        GraphBuilder<Payload> graphBuilder = getFactory().createGraphBuilder();
        graphBuilder.build("{" +
                "\"e2\": \"Y\"," +
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
                        
                        X -> Y
                        Y -> Z
                        Z -> X
                        
                        T -> U
                        U -> T
                        
                        Q -> Q
                        
                        X -> A
                        T -> A
                        Q -> A
                        }"
                """
                + "}");
        final Graph<Payload> graph = graphBuilder.getGraph();

        final Vertex<Payload> startVertex = graphBuilder.startVertex();
        final Vertex<Payload> exitVertex = graphBuilder.exitVertex();

        thenExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                getFactory().createFinder(graph, startVertex, exitVertex).computeDominators()
        ).withMessageContaining("Exit vertex [Y] appears to be unreachable from the start node [A]");

        final Vertex<Payload> vertexQ = graph.vertex("Q");
        thenExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                getFactory().createFinder(graph, startVertex, vertexQ).computeDominators()
        ).withMessageContaining("Exit vertex [Q] appears to be unreachable from the start node [A]");

        final Vertex<Payload> vertexT = graph.vertex("T");
        thenExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                getFactory().createFinder(graph, startVertex, vertexT).computeDominators()
        ).withMessageContaining("Exit vertex [T] appears to be unreachable from the start node [A]");
    }

    @Test
    void unreachable_finish() {
        GraphBuilder<Payload> graphBuilder = getFactory().createGraphBuilder();
        graphBuilder.build("{" +
                "\"h\": \"A\"," +
                "\"e2\": \"B\"," +
                """ 
                  "graph": "digraph graphname{ B -> A }"
                """
                + "}");
        final Graph<Payload> graph = graphBuilder.getGraph();

        final Vertex<Payload> startVertex = graphBuilder.startVertex();
        final Vertex<Payload> exitVertex = graphBuilder.exitVertex();

        thenExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                        getFactory().createFinder(graph, startVertex, exitVertex).computeDominators())
                .withMessageContaining("unreachable");
    }

    @Test
    void unreachable_finish_not_simply_connected() {
        GraphBuilder<Payload> graphBuilder = getFactory().createGraphBuilder();
        graphBuilder.build("{" +
                "\"h\": \"A\"," +
                "\"e2\": \"B\"," +
                """ 
                  "graph": "digraph graphname{ 
                    A 
                    B 
                  }"
                """
                + "}");
        final Graph<Payload> graph = graphBuilder.getGraph();

        final Vertex<Payload> startVertex = graphBuilder.startVertex();
        final Vertex<Payload> exitVertex = graphBuilder.exitVertex();

        thenExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                        getFactory().createFinder(graph, startVertex, exitVertex).computeDominators())
                .withMessageContaining("unreachable");
    }

    @Test
    void unreachable_finish_two_loops_not_simply_connected() {
        GraphBuilder<Payload> graphBuilder = getFactory().createGraphBuilder();
        graphBuilder.build("{" +
                "\"h\": \"A\"," +
                "\"e2\": \"C\"," +
                """ 
                  "graph": "digraph graphname{
                    A -> A
                    A -> B
                    B -> A
                    C -> D
                    D -> C
                    D -> D
                  }"
                """
                + "}");
        final Graph<Payload> graph = graphBuilder.getGraph();

        final Vertex<Payload> startVertex = graphBuilder.startVertex();
        final Vertex<Payload> exitVertex = graphBuilder.exitVertex();

        thenExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                        getFactory().createFinder(graph, startVertex, exitVertex).computeDominators())
                .withMessageContaining("unreachable");
    }

}