package edu.postleadsfinder;

import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Represents a vertex of Graph.
 */
@ToString
public class Vertex {
    /**
     * Integer id of the vertex.
     * The id is unique withing the Graph.
     * The ids are assigned sequentially, and are zero-based.
     * So, the smallest id is 0, and largest id is N-1, where N is the number of nodes in the Graph.
     */
    @Getter
    private final int id;
    @Getter
    private final String key;
    @Getter
    private final List<Integer> outgoingEdges;
    @Getter
    private final NodePayload nodePayload;

    Vertex(int id, String key, int[] outgoingEdges, Function<Vertex, NodePayload> payloadFunction) {
        this.id = id;
        this.key = key;
        this.outgoingEdges = Arrays.stream(outgoingEdges).boxed().toList();
        this.nodePayload = payloadFunction.apply(this);
    }

}
