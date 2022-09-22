package edu.postleadsfinder;

import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;

@RequiredArgsConstructor
public class DepthFirstSearch {

    /**
     * Custom function to be supplied to DFS traverse algorithm.
     */
    @FunctionalInterface
    public interface EdgeFunction {
        /**
         * Generic visit function. Same interface is used for "forward" (down the stack) and "backward" (up the stack) functions.
         * For forward function it is absolutely critical to return correct result, as otherwise same vertices may be visited twice.
         * @param time The time when {@code toVertex} is visited.
         * @param fromVertex The vertex next up to the stack at the moment of {@code toVertex} visit.
         * @param toVertex The vertex we're visiting ().
         * @return for forward direction (down the stack): {@code true} if we should traverse further, of {@code false} otherwise.
         * for backward direction (up the stack) the return value is ignored.
         */
        boolean apply(int time, @Nullable Vertex fromVertex, Vertex toVertex);
    }

    private final Graph graph;
    private final EdgeFunction preProcessFunction;
    private final EdgeFunction postProcessFunction;

    /**
     * Does the DFS traverse from the specified vertex.
     * @param vertex The Vertex to start from.
     * @return The "time" counter value.
     */
    public int dfsFrom(final Vertex vertex) {
        return dfsRecursive(0, null, vertex);
    }

    private int dfsRecursive(int time, final @Nullable Vertex currentVertex, final Vertex discoveredVertex) {
        time++;
        if (preProcessFunction.apply(time, currentVertex, discoveredVertex)) {

            for (Vertex outVertex: graph.outgoingVertices(discoveredVertex)) {
                time = dfsRecursive(time, discoveredVertex, outVertex);
            }

            time++;
            postProcessFunction.apply(time, currentVertex, discoveredVertex);
        }
        return time;
    }

}
