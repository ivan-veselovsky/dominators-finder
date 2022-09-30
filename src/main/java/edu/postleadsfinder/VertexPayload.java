package edu.postleadsfinder;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

import static edu.postleadsfinder.VertexPayload.VertexColor.*;

/**
 * Mutable auxiliary data class attached to a {@link Vertex}.
 * It holds a reference to its Vertex and some auxiliary mutable data for Graph processing.
 */
@Log4j2
public class VertexPayload {
    private final Vertex vertex;

    @Getter private int startTime;
    @Getter private int finishTime;

    private Map<Integer, Pair<Vertex, EdgeKind>> edgeKinds;
    /** Edges that go to "dead end" branches. */
    private Set<Integer> deadEdges;

    @Getter private int inDegreeWithoutDeadEdges;
    @Getter private int outDegreeWithoutDeadEdges;

    VertexPayload(Vertex vertex) {
        this.vertex = vertex;
        clear();
    }

    void incrementInDegree() {
        inDegreeWithoutDeadEdges++;
    }
    void decrementInDegree() {
        inDegreeWithoutDeadEdges--;
        assert inDegreeWithoutDeadEdges >= 0;
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

    void setStartTime(int time) {
        Preconditions.checkArgument(time > 0); // 1-based
        Preconditions.checkState(startTime < 0, "Start time already present: " + startTime);
        Preconditions.checkState(finishTime < 0);
        startTime = time;
    }

    void setFinishTime(int time) {
        Preconditions.checkArgument(time > 0); // 1-based
        Preconditions.checkState(startTime > 0);
        Preconditions.checkState(finishTime < 0);
        Preconditions.checkArgument(time > startTime);
        finishTime = time;
    }

    void markEdgeDead(Vertex targetVertex) {
        assert edgeKinds.containsKey(targetVertex.getId());

        if (deadEdges == null) {
            deadEdges = new HashSet<>(2);
        }

        boolean added = deadEdges.add(targetVertex.getId());
        assert added;

        outDegreeWithoutDeadEdges--;
        assert outDegreeWithoutDeadEdges >= 0;

        targetVertex.getVertexPayload().decrementInDegree();

        if (isDead()) {
            log.debug(() -> "Vertex " + this + " found to be DEAD.");
        }
    }

    boolean isDead() {
        assert outDegreeWithoutDeadEdges >= 0;
        return outDegreeWithoutDeadEdges == 0;
    }

    void setEdgeKind(Vertex targetVertex, EdgeKind edgeKind) {
        Preconditions.checkArgument(edgeKind != null);
        if (edgeKinds == null) {
            // NB: keep ordering for deterministic diagnostics.
            // Since edges are always traversed in deterministic order, the same order will be kept for edge kinds.
            edgeKinds = new LinkedHashMap<>();
        }
        Pair<Vertex, EdgeKind> previous = edgeKinds.putIfAbsent(targetVertex.getId(), Pair.of(targetVertex, edgeKind));
        Preconditions.checkState(previous == null, "Edge kind can be visited only once.");
    }

    EdgeKind edgeKind(int vertexId) {
        Pair<Vertex, EdgeKind> edgeKindPair = edgeKinds.get(vertexId);
        return edgeKindPair == null ? null : edgeKindPair.getValue();
    }

    @Override
    public String toString() {
        return "(" + vertex + "[" + startTime + "/" + finishTime + "] -> {" + edgesToString() + "})";
    }

    private String edgesToString() {
        if (edgeKinds != null) {
            StringBuilder sb = new StringBuilder();
            edgeKinds.forEach((id, kindPair) -> {
                Vertex targetVertex = kindPair.getKey();
                EdgeKind edgeKind = kindPair.getValue();
                boolean isAlive = isLiveEdge(id);
                sb.append(targetVertex)
                        .append("[")
                        .append(edgeKind)
                        .append(isAlive ? "" : "-DEAD")
                        .append("], ");
            });
            if (!sb.isEmpty()) {
                sb.delete(sb.length() - 2, sb.length());
            }
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

    /** Brings all the mutable payload data to initial state. */
    public void clear() {
        clearTime();

        inDegreeWithoutDeadEdges = 0;
        outDegreeWithoutDeadEdges = vertex.getOutgoingEdges().size();

        edgeKinds = null;
        deadEdges = null;
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
