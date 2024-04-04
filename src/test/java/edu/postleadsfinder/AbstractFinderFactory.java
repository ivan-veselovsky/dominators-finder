package edu.postleadsfinder;

public abstract class AbstractFinderFactory<Payload> {

    protected abstract GraphBuilder<Payload> createGraphBuilder();

    protected abstract IDominatorsFinder<Payload> createFinder(Graph<Payload> graph, Vertex<Payload> startVertex, Vertex<Payload> exitVertex);

}
