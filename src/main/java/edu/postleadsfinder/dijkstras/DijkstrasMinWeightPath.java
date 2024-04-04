package edu.postleadsfinder.dijkstras;

import edu.postleadsfinder.Graph;
import edu.postleadsfinder.Vertex;
import lombok.NonNull;

import java.util.*;
import java.util.function.ToIntBiFunction;

public class DijkstrasMinWeightPath {
    static List<Vertex<DijPayload>> dijkstrasAlgorithm(final Graph<DijPayload> graph, @NonNull Vertex<DijPayload> startVertex, @NonNull Vertex<DijPayload> targetVertex,
                                           @NonNull ToIntBiFunction<Vertex<DijPayload>, Vertex<DijPayload>> weightFunction, final int maxEdgeWeight) {
        graph.forAllPayloads(DijPayload::clear);

        final Comparator<Vertex<DijPayload>> comparator = (@NonNull Vertex<DijPayload> v1, @NonNull Vertex<DijPayload> v2) -> {
            if (v1 == v2) {
                return 0;
            }
            int diff = compareTwoNullables(
                    v1.getPayload().getDistanceFromStart(),
                    v2.getPayload().getDistanceFromStart());
            if (diff != 0) {
                return diff;
            }
            diff = Integer.compare(v1.getId(), v2.getId());
            if (diff != 0) {
                return diff;
            }
            throw new IllegalArgumentException("Different vertices with equal ids.");
        };

        // TODO: replace with vEB tree:
        final NavigableSet<Vertex<DijPayload>> verticesByDistance = new TreeSet<>(comparator);

        startVertex.getPayload().setDistanceFromStart(0);
        verticesByDistance.add(startVertex);

        while (!verticesByDistance.isEmpty()) {
            System.out.println("Queue: " + verticesByDistance);
            final Vertex<DijPayload> vertex = verticesByDistance.pollFirst(); // start Vertex appears first

            List<Integer> outgoingEdges = vertex.getOutgoingEdges();
            for (Integer adjacentVertexIndex: outgoingEdges) {
                Vertex<DijPayload> adjacentVertex = graph.vertex(adjacentVertexIndex);
                int weight = weightFunction.applyAsInt(vertex, adjacentVertex);
                int newDistance = vertex.getPayload().getDistanceFromStart() + weight;
                if (adjacentVertex.getPayload().canRelaxTo(newDistance)) {
                    boolean removed = verticesByDistance.remove(adjacentVertex);
                    checkQueue(maxEdgeWeight, verticesByDistance);

                    adjacentVertex.getPayload().relax(vertex, newDistance);

                    boolean added = verticesByDistance.add(adjacentVertex); // *** forces the resorting after distance update
                    assert added : "Was not added: " + adjacentVertex + ", queue: " + verticesByDistance;
                    checkQueue(maxEdgeWeight, verticesByDistance);
                }
            }
        }

        return extractParentPath(graph, targetVertex);
    }

    private static List<Vertex<DijPayload>> extractParentPath(final Graph<DijPayload> graph, Vertex<DijPayload> target) {
        Vertex<DijPayload> vertex = target;
        List<Vertex<DijPayload>> list = new LinkedList<>();
        list.add(target);
        while (true) {
            Integer parentId = vertex.getPayload().getParentVertexId();
            if (parentId == null) {
                break;
            }
            vertex = graph.vertex(parentId);
            list.add(vertex);
        }

        Collections.reverse(list);
        return list;
    }

    private static int compareTwoNullables(Integer x, Integer y) {
        if (x == null || y == null) {
            if (x == null && y == null) {
                return 0;
            }
            return (x == null) ? 1 : -1; // null is treated as infinity.
        } else {
            return x.compareTo(y);
        }
    }

    private static void checkQueue(int maxEdgeWeight, SortedSet<Vertex<DijPayload>> queue) {
        if (queue.size() > 1) {
            Vertex<DijPayload> first = queue.first();
            Vertex<DijPayload> last = queue.last();
            int diff = last.getPayload().getDistanceFromStart() - first.getPayload().getDistanceFromStart();
            assert diff <= maxEdgeWeight;
        }
    }

}
