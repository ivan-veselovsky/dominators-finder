package edu.dominatorsfinder.dijkstras;

import edu.dominatorsfinder.Vertex;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/** Represents vertex Payload for Dijkstras and "Heavy Vertices Bypass" algorithm. */
@RequiredArgsConstructor
public class DijPayload {

    private final Vertex<DijPayload> vertex;
    @Getter
    @Setter
    private Integer distanceFromStart;
    @Getter
    private int relaxCount;
    @Getter
    private Integer parentVertexId;

    @Getter @Setter
    private int weight = 1; // NB: this field is not cleared in clear()!

    public boolean canRelaxTo(int newDistance) {
        return distanceFromStart == null || newDistance < distanceFromStart;
    }

    public void relax(Vertex<DijPayload> parent, int newDistance) {
        assert distanceFromStart == null || newDistance < distanceFromStart;
        System.out.println("    rlx " + vertex + " " + distanceFromStart + " -> " + newDistance);
        if (distanceFromStart != null) {
            relaxCount++;
        }
        distanceFromStart = newDistance;
        parentVertexId = parent.getId();
    }

    void clearAllExceptWeights() {
        distanceFromStart = null; // null == infinity
        relaxCount = 0;
        parentVertexId = null;
    }

    public boolean dropWeightExcludingMarked() {
        if (weight > 0) {
            assert weight > 1 : weight;
            weight = 1;
            return true; // weight dropped
        } else {
            weight = -weight;
            return false; // restored heavy weight
        }
    }

    public void markIfHeavy() {
        if (weight > 1) {
            weight = -weight;
        }
    }
}
