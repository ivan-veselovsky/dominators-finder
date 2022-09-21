package edu.postleadsfinder;

import lombok.Getter;
import lombok.ToString;

import java.util.function.Function;

/**
 * Represents a node of Graph.
 */
@ToString
public class Vertex {
    /**
     * Integer id of the node.
     * The id is unique withing the Graph.
     * The ids are assigned sequentially, and are zero-based.
     * So, the smallest id is 0, and largest id is N-1, where N is the number of nodes in the Graph.
     */
    @Getter
    private final int id;
    @Getter
    private final String key;
    @Getter
    private final int[] outgoingEdges; // todo: make it an immutable list
    @Getter
    private final NodePayload nodePayload;

    Vertex(int id, String key, int[] outgoingEdges, Function<Vertex, NodePayload> payloadFunction) {
        this.id = id;
        this.key = key;
        this.outgoingEdges = outgoingEdges;
        this.nodePayload = payloadFunction.apply(this);
    }

}
