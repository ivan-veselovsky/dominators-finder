package edu.dominatorsfinder;

import com.google.common.base.Verify;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

/**
 * Represents a generic Directed Graph.
 * All the Graph structure is immutable except the {@link DfsPayload}-s attached to each vertex.
 */
public class Graph <P> {

    private final List<Vertex<P>> vertices;
    private final Map<String, Vertex<P>> verticesByKeyMap;

    Graph(Vertex<P>[] vertices) {
        checkInvariants(vertices);
        this.vertices = List.of(vertices);
        this.verticesByKeyMap = Arrays.stream(vertices).collect(toMap(Vertex::getKey, Function.identity()));
    }

    private void checkInvariants(Vertex<P>[] vertices) {
        for (int i = 0; i< vertices.length; i++) {
            Verify.verify(vertices[i] != null);
            Verify.verify(vertices[i].getId() == i);
        }
    }

    public int numberOfVertices() {
        return vertices.size();
    }

    public int numberOfEdges() {
        return vertices.stream().mapToInt(v -> v.getOutgoingEdges().size()).sum();
    }

    public List<Vertex<P>> outgoingVertices(Vertex<P> vertex) {
        return vertex.getOutgoingEdges().stream().map(vertices::get).toList();
    }

    /** Vertices appear in the Stream sorted by String key and id. */
    public Stream<Vertex<P>> vertexStream() {
        return vertices.stream();
    }

    public Vertex<P> vertex(int index) {
        return vertices.get(index);
    }

    /** Vertex lookup by key. */
    public Vertex<P> vertex(String key) {
        return verticesByKeyMap.get(key);
    }

    /** Brings all the mutable data to blank state. */
    public void forAllPayloads(Consumer<P> consumer) {
        vertexStream().forEach(v -> consumer.accept(v.getPayload()));
    }


}
