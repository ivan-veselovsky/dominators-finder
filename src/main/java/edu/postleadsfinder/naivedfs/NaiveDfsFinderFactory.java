package edu.postleadsfinder.naivedfs;

import edu.postleadsfinder.*;

public class NaiveDfsFinderFactory extends AbstractFinderFactory<DfsPayload> {

    @Override
    public GraphBuilder<DfsPayload> createGraphBuilder() {
        GraphBuilder<DfsPayload> graphBuilder = new GraphBuilder<>();
        graphBuilder.withPayloadFactoryFunction(DfsPayload::new);
        return graphBuilder;
    }

    @Override
    public IDominatorsFinder<DfsPayload> createFinder(Graph<DfsPayload> graph, Vertex<DfsPayload> startVertex,
                                                         Vertex<DfsPayload> exitVertex) {
        return new NaiveDfsDominatorsFinder(graph, startVertex, exitVertex);
    }

}
