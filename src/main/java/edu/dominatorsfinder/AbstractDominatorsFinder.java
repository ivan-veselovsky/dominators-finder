package edu.dominatorsfinder;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public abstract class AbstractDominatorsFinder<P> implements IDominatorsFinder<P> {
    protected final Graph<P> graph;
    protected final Vertex<P> startVertex;
    protected final Vertex<P> exitVertex;

    protected final boolean isExitVertex(Vertex<P> vertex) {
        return vertex == exitVertex;
    }

    protected final boolean isStartVertex(Vertex<P> vertex) {
        return vertex == startVertex;
    }

    /** NB: according to task description the start vertex should *not* be present in the result,
     so we explicitly skip it. */
    protected final void filterOutStartVertex(List<Vertex<P>> allDominators) {
        assert allDominators.size() > 0;
        Vertex<P> first = allDominators.remove(0);
        assert isStartVertex(first);
    }
}
