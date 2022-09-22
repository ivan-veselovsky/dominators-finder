package edu.postleadsfinder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import javax.annotation.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static com.google.common.base.Verify.verify;

@RequiredArgsConstructor
@Log4j2
public class PostLeadsFinder {
    /** When true, implementation makes mode diagnostic checks. */
    private static final boolean DEBUG_MODE = false;

    @Getter
    private final LinkedList<Vertex> topologicalSortList = new LinkedList<>();
    private final Graph graph;
    private final Vertex startVertex;
    private final Vertex exitVertex;

    public List<Vertex> computePostLeads() {
        topologicalSortList.clear();

        int time = new DepthFirstSearch(graph, this::preProcessVertex, this::postProcessVertex).dfsFrom(startVertex);

        log.debug("Total DFS traverse time: {}", () -> time);

        // check exit vertex was reached:
        if (exitVertex.getVertexPayload().getStartTime() < 0) {
            throw new IllegalArgumentException("Exit vertex [" + exitVertex.getKey() + "] appears to be unreachable " +
                    "from the start node [" + startVertex.getKey() + "]");
        }

        if (DEBUG_MODE) {
            graph.clearTime();
            ensureCorrectState();
        }

        return findPostLeads();
    }

    // Makes sure there are no dead end vertices except exit vertex.
    private void ensureCorrectState() {
        int totalTime = new DepthFirstSearch(graph, (time, u, v) -> {
            verify(u == null || isExitVertex(u) || !u.getVertexPayload().isDead(), "Expected to be live: %s", u);
            if (u != null && !u.getVertexPayload().isLiveEdge(v.getId())) {
                // do not visit edges that are detected to be dead:
                return false;
            }
            verify(isExitVertex(v) || !v.getVertexPayload().isDead());

            final VertexPayload.VertexColor discoveredVertexColor = v.getVertexPayload().getColor();
            if (discoveredVertexColor == VertexPayload.VertexColor.WHITE) {
                v.getVertexPayload().updateTimeOnPush(time);

                return true;
            }
            return false;
        }, (time, u, v) -> {
            v.getVertexPayload().updateTimeOnPop(time);

            verify(isStartVertex(v) || v.getVertexPayload().getInDegreeWithoutDeadEdges() > 0);
            verify(isExitVertex(v) || v.getVertexPayload().getOutDegreeWithoutDeadEdges() > 0);
            verify(u == null || u.getVertexPayload().getOutDegreeWithoutDeadEdges() > 0);

            log.debug(() -> "degrees of vertex [" + v +"]: " + v.getVertexPayload().getInDegreeWithoutDeadEdges() + ":"
                    + v.getVertexPayload().getOutDegreeWithoutDeadEdges());
            return true;
        }).dfsFrom(startVertex);

        log.debug("total time: {}", () -> totalTime);
    }

    private boolean preProcessVertex(int time, @Nullable Vertex currentVertex /* null for start vertex */,
                                     Vertex discoveredVertex) {
        final VertexPayload.VertexColor discoveredVertexColor = discoveredVertex.getVertexPayload().getColor();

        if (currentVertex != null) {
            EdgeKind edgeKind = colorEdge(currentVertex, discoveredVertex, discoveredVertexColor);

            discoveredVertex.getVertexPayload().incrementInDegree();

            if (edgeKind == EdgeKind.BACKWARD
                    || (discoveredVertex.getVertexPayload().isDead() && !isExitVertex(discoveredVertex))) {
                log.debug(() -> "Discovered dead edge: " + edgeKind
                        + " from " + currentVertex + " --> " + discoveredVertex);
                currentVertex.getVertexPayload().markEdgeDead(discoveredVertex);
            }
        }

        if (discoveredVertexColor == VertexPayload.VertexColor.WHITE) {
            discoveredVertex.getVertexPayload().updateTimeOnPush(time);

            return true; // visit it!
        }

        return false; // GREY or BLACK: already visited or being processed, do not visit again.
    }

    private EdgeKind colorEdge(Vertex fromVertex, Vertex toVertex, VertexPayload.VertexColor toVertexColor) {
        EdgeKind edgeKind = switch (toVertexColor) {
            case WHITE -> EdgeKind.TREE;
            case GREY -> EdgeKind.BACKWARD;
            case BLACK -> (fromVertex.getVertexPayload().getStartTime() < toVertex.getVertexPayload().getStartTime())
                    ? EdgeKind.FORWARD : EdgeKind.CROSS;
        };
        fromVertex.getVertexPayload().setEdgeKind(toVertex.getId(), edgeKind);
        return edgeKind;
    }

    private boolean postProcessVertex(int time, @Nullable Vertex currentVertex /* null for start vertex */, Vertex discoveredVertex) {
        discoveredVertex.getVertexPayload().updateTimeOnPop(time);

        // post-processing is done only for TREE-kind edges
        assert currentVertex == null || currentVertex.getVertexPayload().edgeKind(discoveredVertex.getId()) == EdgeKind.TREE;

        if (discoveredVertex.getVertexPayload().isDead()
                && !isExitVertex(discoveredVertex)) {
            if (currentVertex != null) {
                currentVertex.getVertexPayload().markEdgeDead(discoveredVertex);
            }
            return false;
        } else {
            topologicalSortList.addFirst(discoveredVertex);

            return true;
        }
    }

    private boolean isExitVertex(Vertex vertex) {
        return vertex == exitVertex;
    }

    private boolean isStartVertex(Vertex vertex) {
        return vertex == startVertex;
    }

    private List<Vertex> findPostLeads() {
        final List<Vertex> postLeadVertices = new LinkedList<>();
        int parallelEdgeCount = 0;
        for (Vertex vertex: topologicalSortList) {
            int inDegree = vertex.getVertexPayload().getInDegreeWithoutDeadEdges();
            int outDegree = vertex.getVertexPayload().getOutDegreeWithoutDeadEdges();

            assert isStartVertex(vertex) || inDegree > 0;
            assert isExitVertex(vertex) || outDegree > 0;

            parallelEdgeCount -= inDegree;
            boolean isPostLeadVertex = (parallelEdgeCount == 0);
            parallelEdgeCount += outDegree;

            assert (isExitVertex(vertex) && parallelEdgeCount == 0)
                    || (!isExitVertex(vertex) && parallelEdgeCount > 0);
            // NB: according to task description the start vertex should *not* be present in the result,
            // so we explicitly skip it:
            if (isPostLeadVertex && !isStartVertex(vertex)) {
                postLeadVertices.add(vertex);
            }
        }
        return postLeadVertices;
    }

    public static List<String> asKeys(Collection<Vertex> vertices) {
        return vertices.stream().map(Vertex::getKey).toList();
    }
}
