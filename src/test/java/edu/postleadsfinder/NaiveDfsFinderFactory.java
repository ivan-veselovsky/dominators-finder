package edu.postleadsfinder;

import edu.postleadsfinder.naivefinder.NaiveDfsDominatorsFinder;

public class NaiveDfsFinderFactory extends AbstractFinderFactory<DfsPayload> {

    @Override
    protected GraphBuilder<DfsPayload> createGraphBuilder() {
        GraphBuilder<DfsPayload> graphBuilder = new GraphBuilder<>();
        graphBuilder.withPayloadFactoryFunction(DfsPayload::new);
        return graphBuilder;
    }

    @Override
    protected IDominatorsFinder<DfsPayload> createFinder(Graph<DfsPayload> graph, Vertex<DfsPayload> startVertex,
                                                         Vertex<DfsPayload> exitVertex) {
        return new NaiveDfsDominatorsFinder(graph, startVertex, exitVertex);
    }

}
