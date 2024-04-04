package edu.postleadsfinder.heavyverticesbypass;

import edu.postleadsfinder.*;
import edu.postleadsfinder.dijkstras.DijPayload;

public class HeavyBypassFinderFactory extends AbstractFinderFactory<DijPayload> {

    @Override
    public GraphBuilder<DijPayload> createGraphBuilder() {
        GraphBuilder<DijPayload> graphBuilder = new GraphBuilder<>();
        graphBuilder.withPayloadFactoryFunction(DijPayload::new);
        return graphBuilder;
    }

    @Override
    public IDominatorsFinder<DijPayload> createFinder(Graph<DijPayload> graph,
                                                         Vertex<DijPayload> startVertex, Vertex<DijPayload> exitVertex) {
        return new HeavyVerticesBypassDominatorsFinder(graph, startVertex, exitVertex);
    }
}
