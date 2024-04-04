package edu.postleadsfinder.naivefinder;

import edu.postleadsfinder.Graph;
import edu.postleadsfinder.Vertex;
import edu.postleadsfinder.DfsPayload;
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
         * Generic visit function. Same interface is used for pre-processing and post-processing functions.
         * Note that for pre-processing this method is invoked prio to the {@code toVertex} start time update (when
         * {@code toVertex} is still WHITE), while for post-processing it is invoked after the finish time
         * update (when {@code toVertex} is BLACK).
         * @param time The time when {@code toVertex} is visited.
         * @param fromVertex The vertex next up to the stack at the moment of {@code toVertex} visit.
         * @param toVertex The vertex we're visiting ().
         * @return for forward direction (down the stack): {@code true} if we should traverse further, of {@code false} otherwise.
         * for backward direction (up the stack) the return value is ignored.
         */
        boolean apply(int time, @Nullable Vertex<DfsPayload> fromVertex, Vertex<DfsPayload> toVertex);
    }

    private final Graph<DfsPayload> graph;
    private final EdgeFunction preProcessFunction;
    private final EdgeFunction postProcessFunction;

    /**
     * Does the DFS traverse from the specified vertex.
     * @param vertex The Vertex to start from.
     * @return The "time" counter value.
     */
    public int dfsFrom(final Vertex<DfsPayload> vertex) {
        return dfsRecursive(0, null, vertex);
    }

    private int dfsRecursive(int time, final @Nullable Vertex<DfsPayload> currentVertex, final Vertex<DfsPayload> discoveredVertex) {
        time++;
        // NB: notice "&&" below: time is not updated (vertex stays white) if preProcessFunction says we should not visit it:
        if (preProcessFunction.apply(time, currentVertex, discoveredVertex)
                && preUpdateTime(time, currentVertex, discoveredVertex)) {

            for (Vertex<DfsPayload> outVertex: graph.outgoingVertices(discoveredVertex)) {
                time = dfsRecursive(time, discoveredVertex, outVertex);
            }

            time++;
            postUpdateTime(time, currentVertex, discoveredVertex);
            postProcessFunction.apply(time, currentVertex, discoveredVertex);
        }
        return time;
    }

    public boolean preUpdateTime(int time, @Nullable Vertex<DfsPayload> currentVertex, Vertex<DfsPayload> discoveredVertex) {
        if (discoveredVertex.getPayload().getColor() == DfsPayload.VertexColor.WHITE) {
            discoveredVertex.getPayload().setDfsStartTime(time);

            return true; // visit it!
        }

        return false; // GREY or BLACK: already visited or being processed, do not visit again.
    }

    public void postUpdateTime(int time, @Nullable Vertex<DfsPayload> currentVertex, Vertex<DfsPayload> discoveredVertex) {
        discoveredVertex.getPayload().setDfsFinishTime(time);
    }

}
