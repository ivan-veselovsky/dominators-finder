package edu.postleadsfinder;

import com.google.common.base.Verify;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

/**
 * Represents a generic Directed Graph.
 * All the Graph structure is immutable except the {@link VertexPayload}-s attached to each vertex.
 */
public class Graph {

    private final Vertex[] vertices;
    private final Map<String, Vertex> verticesByKeyMap;

    Graph(Vertex[] vertices) {
        checkInvariants(vertices);
        this.vertices = vertices;
        this.verticesByKeyMap = Arrays.stream(vertices).collect(toMap(Vertex::getKey, Function.identity()));
    }

    private void checkInvariants(Vertex[] vertices) {
        for (int i = 0; i< vertices.length; i++) {
            Verify.verify(vertices[i] != null);
            Verify.verify(vertices[i].getId() == i);
        }
    }

    int numberOfVertices() {
        return vertices.length;
    }

    int numberOfEdges() {
        return Arrays.stream(vertices).mapToInt(v -> v.getOutgoingEdges().size()).sum();
    }

    List<Vertex> outgoingVertices(Vertex vertex) {
        return vertex.getOutgoingEdges().stream().map(i -> vertices[i]).toList();
    }

    /** Vertices appear in the Stream sorted by String key and id. */
    Stream<Vertex> vertexStream() {
        return Arrays.stream(vertices);
    }

    Vertex vertex(int index) {
        return vertices[index];
    }

    /** Vertex lookup by key. */
    Vertex vertex(String key) {
        return verticesByKeyMap.get(key);
    }

    /**
     * Utility method. Can be used before next traversal to clear the "color" of Vertices.
     */
    void clearTime() {
        vertexStream().forEach(v -> v.getVertexPayload().clearTime());
    }

}
