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
    @Getter
    private final LinkedList<Vertex> topologicalSortList = new LinkedList<>();
    private final Graph graph;
    private final Vertex startVertex;
    private final Vertex exitVertex;

    public List<Vertex> doJob() {
        topologicalSortList.clear();

        int time = new DepthFirstSearch(graph, this::preProcessVertex, this::postProcessVertex).dfs(startVertex);

        log.debug("Time: {}", () -> time);

        // check exit vertex was reached:
        if (exitVertex.getNodePayload().getStartTime() < 0) {
            throw new IllegalArgumentException("Exit vertex [" + exitVertex.getKey() + "] appears to be unreachable " +
                    "from the start node [" + startVertex.getKey() + "]");
        }

        graph.clearTime();
        ensureCorrectGraph();

        return findPostLeads();
    }

    // Makes sure there are no dead end vertices except exit vertex.
    private void ensureCorrectGraph() {
        int totalTime = new DepthFirstSearch(graph, (time, u, v) -> {
            verify(u == null || isExitVertex(u) || !u.getNodePayload().isDead(), "Expected to be live: " + u);
            if (u != null && !u.getNodePayload().isLiveEdge(v.getId())) {
                // do not visit edges that are detected to be dead:
                //System.out.println("edge " + u + " -> " + v + " is dead, so not traversed.");
                return false;
            }
            verify(isExitVertex(v) || !v.getNodePayload().isDead());

            final NodePayload.VertexColor discoveredVertexColor = v.getNodePayload().getColor();
            if (discoveredVertexColor == NodePayload.VertexColor.WHITE) {
                v.getNodePayload().updateTimeOnPush(time);

                return true;
            }
            return false;
        }, (time, u, v) -> {
            v.getNodePayload().updateTimeOnPop(time);

            verify(isStartVertex(v) || v.getNodePayload().getInDegree() > 0);
            verify(isExitVertex(v) || v.getNodePayload().getOutDegreeWithoutDeadEdges() > 0);
            verify(u == null || u.getNodePayload().getOutDegreeWithoutDeadEdges() > 0);

            System.out.println("degrees [" + v +"]: " + v.getNodePayload().getInDegree() + ":" +v.getNodePayload().getOutDegreeWithoutDeadEdges());
            return true;
        }).dfs(startVertex);

        log.debug("total time: {}", () -> totalTime);
    }

    private boolean preProcessVertex(int time, @Nullable Vertex currentVertex /* null for start vertex */,
                                     Vertex discoveredVertex) {
        final NodePayload.VertexColor discoveredVertexColor = discoveredVertex.getNodePayload().getColor();

        if (currentVertex != null) {
            EdgeKind edgeKind = colorEdge(currentVertex, discoveredVertex, discoveredVertexColor);

            discoveredVertex.getNodePayload().incrementInDegree();

            if (edgeKind == EdgeKind.BACKWARD
                    || (discoveredVertex.getNodePayload().isDead() && !isExitVertex(discoveredVertex))) {
                System.out.println("Discovered dead edge: " + edgeKind
                        + " from " + currentVertex + " --> " + discoveredVertex);
                currentVertex.getNodePayload().markEdgeDead(discoveredVertex);
            }
        }

        if (discoveredVertexColor == NodePayload.VertexColor.WHITE) {
            discoveredVertex.getNodePayload().updateTimeOnPush(time);

            return true; // visit it!
        }

        return false; // GREY or BLACK: already visited or being processed, do not visit again.
    }

    private EdgeKind colorEdge(Vertex fromVertex, Vertex toVertex, NodePayload.VertexColor toVertexColor) {
        EdgeKind edgeKind = switch (toVertexColor) {
            case WHITE -> EdgeKind.TREE;
            case GREY -> EdgeKind.BACKWARD;
            case BLACK -> (fromVertex.getNodePayload().getStartTime() < toVertex.getNodePayload().getStartTime())
                    ? EdgeKind.FORWARD : EdgeKind.CROSS;
        };
        fromVertex.getNodePayload().setEdgeKind(toVertex.getId(), edgeKind);
        return edgeKind;
    }

    private boolean postProcessVertex(int time, @Nullable Vertex currentVertex /* null for start vertex */, Vertex discoveredVertex) {
        discoveredVertex.getNodePayload().updateTimeOnPop(time);

        // post-processing is done only for TREE-kind edges
        assert currentVertex == null || currentVertex.getNodePayload().edgeKind(discoveredVertex.getId()) == EdgeKind.TREE;

        if (discoveredVertex.getNodePayload().isDead()
                && !isExitVertex(discoveredVertex)) {
            if (currentVertex != null) {
                currentVertex.getNodePayload().markEdgeDead(discoveredVertex);
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
        final List<Vertex> result = new LinkedList<>();
        int parallelEdgeCount = 0;
        for (Vertex vertex: topologicalSortList) {
            int inDegree = vertex.getNodePayload().getInDegree();
            int outDegree = vertex.getNodePayload().getOutDegreeWithoutDeadEdges();

            assert isStartVertex(vertex) || inDegree > 0;
            assert isExitVertex(vertex) || outDegree > 0;

            parallelEdgeCount -= inDegree;
            boolean isPostLeadVertex = (parallelEdgeCount == 0);

            parallelEdgeCount += outDegree;
            isPostLeadVertex |= (parallelEdgeCount == 1);

            assert (isExitVertex(vertex) && parallelEdgeCount == 0)
                    || (!isExitVertex(vertex) && parallelEdgeCount > 0);
            // NB: according to task description the start vertex should *not* be present in the answer,
            // so we explicitly omit it:
            if (isPostLeadVertex && !isStartVertex(vertex)) {
                result.add(vertex);
            }
        }
        return result;
    }

    static List<String> asKeys(Collection<Vertex> vertices) {
        return vertices.stream().map(Vertex::getKey).toList();
    }
}
