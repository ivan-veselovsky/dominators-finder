package edu.postleadsfinder.heavyverticesbypass;

import edu.postleadsfinder.Graph;
import edu.postleadsfinder.IDominatorsFinder;
import edu.postleadsfinder.Vertex;
import edu.postleadsfinder.dijkstras.DijPayload;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class HeavyVerticesBypassDominatorsFinder implements IDominatorsFinder<DijPayload> {

    private final Graph<DijPayload> graph;
    private final Vertex<DijPayload> startVertex;
    private final Vertex<DijPayload> finishVertex;

    @Override
    public List<Vertex<DijPayload>> computePostLeads() {
        return null; // TODO;
    }
}
