package edu.postleadsfinder.dijkstras;

import edu.postleadsfinder.Vertex;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

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

    void clear() {
        distanceFromStart = null; // null == infinity
        relaxCount = 0;
        parentVertexId = null;
    }
}
