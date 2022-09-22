package edu.postleadsfinder;

import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;

@RequiredArgsConstructor
public class DepthFirstSearch {

    @FunctionalInterface
    public interface EdgeFunction {
        boolean apply(int time, Vertex fromVertex, Vertex toVertex);
    }

    private final Graph graph;
    private final EdgeFunction preProcessFunction;
    private final EdgeFunction postProcessFunction;

    public int dfs(final Vertex vertex) {
        return dfsRecursive(0, null, vertex);
    }

    private int dfsRecursive(int time, final @Nullable Vertex currentVertex, final Vertex discoveredVertex) {
        time++;
        if (preProcessFunction.apply(time, currentVertex, discoveredVertex)) {

            for (Vertex outVertex: graph.outgoingNodes(discoveredVertex)) {
                time = dfsRecursive(time, discoveredVertex, outVertex);
            }

            time++;
            postProcessFunction.apply(time, currentVertex, discoveredVertex);
        }
        return time;
    }

}
