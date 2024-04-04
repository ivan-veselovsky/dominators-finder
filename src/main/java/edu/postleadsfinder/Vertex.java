package edu.postleadsfinder;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Represents a vertex of Graph.
 */
@Getter
public class Vertex <P> {
    /**
     * Integer {@code id} of the vertex.
     * The {@code id} is unique withing the Graph.
     * The ids are assigned sequentially, and are zero-based.
     * So, the smallest id is 0, and largest id is N-1, where N is the number of nodes in the Graph.
     */
    private final int id;
    /**
     * String {@code key} of the Vertex as specified in the dot-format input.
     * The {@code key} is unique withing the Graph.
     */
    private final String key;
    /**
     * Outgoing edge list, with each edge being identified by its target vertex {@code id}.
     * The {@code id}s in the List are unique and sorted in ascending order.
     */
    private final List<Integer> outgoingEdges;
    private final P payload;

    Vertex(int id, String key, int[] outgoingEdges, Function<Vertex<P>, P> payloadFunction) {
        this.id = id;
        this.key = key;
        assert areSorted(outgoingEdges);
        this.outgoingEdges = Arrays.stream(outgoingEdges).boxed().toList();
        this.payload = payloadFunction.apply(this);
    }

    private boolean areSorted(int[] indexes) {
        for (int i = 1; i < indexes.length; i++) {
            if (indexes[i] <= indexes[i - 1]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return key;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vertex)) {
            return false;
        }
        return id == ((Vertex)obj).id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
