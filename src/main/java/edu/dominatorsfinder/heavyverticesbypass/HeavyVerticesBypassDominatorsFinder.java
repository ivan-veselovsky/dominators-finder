package edu.dominatorsfinder.heavyverticesbypass;

import edu.dominatorsfinder.AbstractDominatorsFinder;
import edu.dominatorsfinder.Graph;
import edu.dominatorsfinder.IDominatorsFinder;
import edu.dominatorsfinder.Vertex;
import edu.dominatorsfinder.dijkstras.DijPayload;
import edu.dominatorsfinder.dijkstras.DijkstrasMinWeightPath;
import lombok.val;

import java.util.*;
import java.util.function.ToIntBiFunction;

import static com.google.common.collect.Iterables.getLast;

public class HeavyVerticesBypassDominatorsFinder extends AbstractDominatorsFinder<DijPayload> implements IDominatorsFinder<DijPayload> {

    HeavyVerticesBypassDominatorsFinder(Graph<DijPayload> graph, Vertex<DijPayload> startVertex, Vertex<DijPayload> exitVertex) {
        super(graph, startVertex, exitVertex);
    }

    private int heavyWeight() {
        return graph.numberOfVertices();
    }

    @Override
    public List<Vertex<DijPayload>> computeDominators() {
        if (startVertex == exitVertex) {
            return List.of(); // shortcut for trivial case
        }
        final ToIntBiFunction<Vertex<DijPayload>, Vertex<DijPayload>> weightFunction = (v1, v2) -> v2.getPayload().getWeight();

        List<Vertex<DijPayload>> minWeightPath = DijkstrasMinWeightPath.computeMinWeightPath(graph, startVertex, exitVertex, weightFunction, heavyWeight());
        assert isExitVertex(getLast(minWeightPath));
        if (!isStartVertex(minWeightPath.get(0))) {
            throw new IllegalArgumentException("Exit vertex [" + exitVertex + "] appears to be unreachable from the start node [" + startVertex + "]");
        }
        assert minWeightPath.size() >= 2;

        LinkedList<Vertex<DijPayload>> heavyVertices = new LinkedList<>(minWeightPath);
        initialMarkHeavy(heavyVertices);
        int heavyCount = heavyVertices.size();
        int findingMinimalPathIterationCount = 1;

        while (true) {
            minWeightPath = DijkstrasMinWeightPath.computeMinWeightPath(graph, startVertex, exitVertex, weightFunction, heavyWeight());
            assert isStartVertex(minWeightPath.get(0));
            assert isExitVertex(getLast(minWeightPath));
            assert minWeightPath.size() >= 2;
            findingMinimalPathIterationCount++;

            minWeightPath.forEach(v -> v.getPayload().markIfHeavy());
            int droppedCount = dropUnmarkedHeavy(heavyVertices);

            System.out.println(findingMinimalPathIterationCount + ": Drop heavy: " + heavyCount + " - " + droppedCount + " = " + heavyVertices.size());

            if (heavyVertices.size() == 2) {
                assert isStartVertex(heavyVertices.get(0));
                assert isExitVertex(heavyVertices.get(1));
                break; // There are no Dominators (list contains only first and last node)
            } else if (heavyVertices.size() == heavyCount) {
                // heavy list did not change in last iteration, so it contains only Dominators:
                break;
            } else {
                heavyCount = heavyVertices.size();
            }
        }

        assert findingMinimalPathIterationCount <= 4 : findingMinimalPathIterationCount;

        filterOutStartVertex(heavyVertices);
        return List.copyOf(heavyVertices);
    }

    void initialMarkHeavy(Collection<Vertex<DijPayload>> vertices) {
        int heavyWeight = heavyWeight();
        vertices.forEach(v -> v.getPayload().setWeight(heavyWeight));
    }

    int dropUnmarkedHeavy(LinkedList<Vertex<DijPayload>> heavyVertices) {
        Iterator<Vertex<DijPayload>> it = heavyVertices.iterator();
        val initialSize = heavyVertices.size();
        int droppedCount = 0;
        while (it.hasNext()) {
            Vertex<DijPayload> v = it.next();
            if (v.getPayload().dropWeightExcludingMarked()) {
                it.remove();
                droppedCount++;
            }
        }
        assert initialSize - droppedCount == heavyVertices.size();
        return droppedCount;
    }
}
