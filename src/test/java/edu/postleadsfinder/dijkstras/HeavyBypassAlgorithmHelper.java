package edu.postleadsfinder.dijkstras;

import edu.postleadsfinder.*;
import edu.postleadsfinder.heavyverticesbypass.HeavyVerticesBypassDominatorsFinder;

public class HeavyBypassAlgorithmHelper extends AlgorithmHelper<DijPayload> {

    @Override
    protected GraphBuilder<DijPayload> createGraphBuilder() {
        GraphBuilder<DijPayload> graphBuilder = new GraphBuilder<>();
        graphBuilder.withPayloadFactoryFunction(DijPayload::new);
        return graphBuilder;
    }

    @Override
    protected IDominatorsFinder<DijPayload> createFinder(Graph<DijPayload> graph,
                                                         Vertex<DijPayload> startVertex, Vertex<DijPayload> exitVertex) {
        return new HeavyVerticesBypassDominatorsFinder(graph, startVertex, exitVertex);
    }
}
