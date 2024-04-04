package edu.dominatorsfinder;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

import static edu.dominatorsfinder.DfsPayload.VertexColor.*;

/**
 * Mutable auxiliary data class attached to a {@link Vertex}.
 * It holds a reference to its Vertex and some auxiliary mutable data for Graph processing.
 */
@Log4j2
public class DfsPayload {
    private final Vertex<DfsPayload> vertex;

    @Getter private int dfsStartTime;
    @Getter private int dfsFinishTime;

    private Map<Integer, Pair<Vertex<DfsPayload>, EdgeKind>> edgeKinds;
    /** Edges that go to "dead end" branches. */
    private Set<Integer> deadEdges;

    @Getter private int inDegreeWithoutDeadEdges;
    @Getter private int outDegreeWithoutDeadEdges;

    public DfsPayload(Vertex<?> vertex) {
        this.vertex = (Vertex)vertex;
        clear();
    }

    public void incrementInDegree() {
        inDegreeWithoutDeadEdges++;
    }
    void decrementInDegree() {
        inDegreeWithoutDeadEdges--;
        assert inDegreeWithoutDeadEdges >= 0;
    }

    public VertexColor getColor() {
        if (dfsStartTime < 0) {
            return WHITE;
        }
        if (dfsFinishTime < 0) {
            return GREY;
        }
        return BLACK;
    }

    public void setDfsStartTime(int time) {
        Preconditions.checkArgument(time > 0); // 1-based
        Preconditions.checkState(dfsStartTime < 0, "Start time already present: " + dfsStartTime);
        Preconditions.checkState(dfsFinishTime < 0);
        dfsStartTime = time;
    }

    public void setDfsFinishTime(int time) {
        Preconditions.checkArgument(time > 0); // 1-based
        Preconditions.checkState(dfsStartTime > 0);
        Preconditions.checkState(dfsFinishTime < 0);
        Preconditions.checkArgument(time > dfsStartTime);
        dfsFinishTime = time;
    }

    public void markEdgeDead(Vertex<DfsPayload> targetVertex) {
        assert edgeKinds.containsKey(targetVertex.getId());

        if (deadEdges == null) {
            deadEdges = new HashSet<>(2);
        }

        boolean added = deadEdges.add(targetVertex.getId());
        assert added;

        outDegreeWithoutDeadEdges--;
        assert outDegreeWithoutDeadEdges >= 0;

        targetVertex.getPayload().decrementInDegree();

        if (isDead()) {
            log.debug(() -> "Vertex " + this + " found to be DEAD.");
        }
    }

    public boolean isDead() {
        assert outDegreeWithoutDeadEdges >= 0;
        return outDegreeWithoutDeadEdges == 0;
    }

    public void setEdgeKind(Vertex targetVertex, EdgeKind edgeKind) {
        Preconditions.checkArgument(edgeKind != null);
        if (edgeKinds == null) {
            // NB: keep ordering for deterministic diagnostics.
            // Since edges are always traversed in deterministic order, the same order will be kept for edge kinds.
            edgeKinds = new LinkedHashMap<>();
        }
        Pair<Vertex, EdgeKind> previous = edgeKinds.putIfAbsent(targetVertex.getId(), Pair.of(targetVertex, edgeKind));
        Preconditions.checkState(previous == null, "Edge kind can be visited only once.");
    }

    public EdgeKind edgeKind(int vertexId) {
        Pair<Vertex<DfsPayload>, EdgeKind> edgeKindPair = edgeKinds.get(vertexId);
        return edgeKindPair == null ? null : edgeKindPair.getValue();
    }

    @Override
    public String toString() {
        return "(" + vertex + "[" + dfsStartTime + "/" + dfsFinishTime + "] -> {" + edgesToString() + "})";
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

    public boolean isLiveEdge(int toVertexId) {
        return deadEdges == null || !deadEdges.contains(toVertexId);
    }

    public void clearDfsTime() {
        dfsStartTime = -1;
        dfsFinishTime = -1;
    }

    /** Brings all the mutable payload data to initial state. */
    public void clear() {
        clearDfsTime();

        inDegreeWithoutDeadEdges = 0;
        outDegreeWithoutDeadEdges = vertex.getOutgoingEdges().size();

        edgeKinds = null;
        deadEdges = null;
    }

    public enum VertexColor {
        /** Not yet discovered. */
        WHITE,
        /** Processing started but not finished. */
        GREY,
        /** Processing finished. */
        BLACK
    }
}
