package edu.postleadsfinder;

public abstract class AbstractFinderFactory<Payload> {

    public abstract GraphBuilder<Payload> createGraphBuilder();

    public abstract IDominatorsFinder<Payload> createFinder(Graph<Payload> graph, Vertex<Payload> startVertex, Vertex<Payload> exitVertex);

}
