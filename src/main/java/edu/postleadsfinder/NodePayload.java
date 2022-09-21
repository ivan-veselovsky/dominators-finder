package edu.postleadsfinder;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static edu.postleadsfinder.NodePayload.VertexColor.*;

/**
 * Mutable auxiliary data class attached to a {@link Vertex}.
 */
@Log4j2
public class NodePayload {
    private final Vertex vertex;

    NodePayload(Vertex vertex) {
        this.vertex = vertex;
        this.outDegreeWithoutDeadEdges = vertex.getOutgoingEdges().length;
    }

    @Getter
    private int startTime = -1;
    @Getter
    private int finishTime = -1;

    private Map<Integer, EdgeKind> edgeKinds;

    private Set<Integer> deadEdges;

    @Getter
    private int inDegree = 0;
    @Getter
    private int outDegreeWithoutDeadEdges;

    void incrementInDegree() {
        inDegree++;
    }
    void decrementInDegree() {
        inDegree--;
        assert inDegree >= 0;
    }

    VertexColor getColor() {
        if (startTime < 0) {
            return WHITE;
        }
        if (finishTime < 0) {
            return GREY;
        }
        return BLACK;
    }

    VertexColor updateTimeOnPush(int time) {
        Preconditions.checkArgument(time > 0); // 1-based
        Preconditions.checkState(startTime < 0, "Start time already present: " + startTime);
        Preconditions.checkState(finishTime < 0);
        startTime = time;
        return getColor();
    }

    VertexColor updateTimeOnPop(int time) {
        Preconditions.checkArgument(time > 0); // 1-based
        Preconditions.checkState(startTime > 0);
        Preconditions.checkState(finishTime < 0);
        Preconditions.checkArgument(time > startTime);
        finishTime = time;
        return getColor();
    }

    void markEdgeDead(Vertex targetVertex) {
        if (deadEdges == null) {
            deadEdges = new HashSet<>(2);
        }
        int targetVertexId = targetVertex.getId();
        assert edgeKinds.containsKey(targetVertexId);
        boolean added = deadEdges.add(targetVertexId);
        assert added;

        outDegreeWithoutDeadEdges--;
        assert outDegreeWithoutDeadEdges >= 0;

        targetVertex.getNodePayload().decrementInDegree();

        log.info(() -> { if (isDead()) {
            return "Node " + vertex.getKey() + " became DEAD.";
        } else {
            return null;
        }});
    }

    boolean isDead() {
        return outDegreeWithoutDeadEdges <= 0;
    }

    void setEdgeKind(int vertexId, EdgeKind edgeKind) {
        Preconditions.checkArgument(edgeKind != null);
        if (edgeKinds == null) {
            edgeKinds = new TreeMap<>();
        }
        EdgeKind previous = edgeKinds.putIfAbsent(vertexId, edgeKind);
        Preconditions.checkState(previous == null, "Edge kind can be assigned only once.");
    }

    EdgeKind edgeKind(int vertexId) {
        return edgeKinds.get(vertexId);
    }

    @Override
    public String toString() {
        return startTime + "/" + finishTime + " " + edges();
    }

    private String edges() {
        if (edgeKinds != null) {
            StringBuilder sb = new StringBuilder();
            edgeKinds.forEach((k, v) -> sb.append(k).append(": ").append(v).append(", "));
            return sb.toString();
        }
        return "";
    }

    boolean isLiveEdge(int toVertexId) {
        return deadEdges == null || !deadEdges.contains(toVertexId);
    }

    void clearTime() {
        startTime = -1;
        finishTime = -1;
    }

    enum VertexColor {
        /** Not yet discovered. */
        WHITE,
        /** Processing started but not finished. */
        GREY,
        /** Processing finished. */
        BLACK
    }
}
