package edu.postleadsfinder.naivefinder;

import edu.postleadsfinder.*;
import lombok.extern.log4j.Log4j2;

import javax.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;

import static com.google.common.base.Verify.verify;

@Log4j2
public class NaiveDfsDominatorsFinder extends AbstractDominatorsFinder<DfsPayload> implements IDominatorsFinder<DfsPayload> {
    /** When true, implementation makes mode diagnostic checks. */
    private static final boolean DEBUG_MODE = Util.areAssertionsEnabled();

    public NaiveDfsDominatorsFinder(Graph<DfsPayload> graph, Vertex<DfsPayload> startVertex, Vertex<DfsPayload> exitVertex) {
        super(graph, startVertex, exitVertex);
    }

    private final LinkedList<Vertex<DfsPayload>> topologicalSortList = new LinkedList<>();

    public List<Vertex<DfsPayload>> computeDominators() {
        graph.forAllPayloads(DfsPayload::clear);
        topologicalSortList.clear();

        int time = new DepthFirstSearch(graph, this::preProcessVertex, this::postProcessVertex).dfsFrom(startVertex);

        log.debug("Total DFS traverse time: {}", () -> time);

        // check exit vertex was reached:
        if (exitVertex.getPayload().getDfsStartTime() < 0) {
            throw new IllegalArgumentException("Exit vertex [" + exitVertex.getKey() + "] appears to be unreachable " +
                    "from the start node [" + startVertex.getKey() + "]");
        }

        if (DEBUG_MODE) {
            graph.forAllPayloads(DfsPayload::clearDfsTime);
            ensureCorrectState();
        }

        List<Vertex<DfsPayload>> postLeads = findDominators();
        filterOutStartVertex(postLeads);
        return postLeads;
    }

    // Makes sure there are no dead end vertices except exit vertex.
    private void ensureCorrectState() {
        int totalTime = new DepthFirstSearch(graph, (time, u, v) -> {
            verify(u == null || isExitVertex(u) || !u.getPayload().isDead(), "Expected to be live: %s", u);
            if (u != null && !u.getPayload().isLiveEdge(v.getId())) {
                // do not visit edges that are detected to be dead:
                return false;
            }
            verify(isExitVertex(v) || !v.getPayload().isDead(), "Vertex %s expected to be alive.", v);
            return true;
        }, (time, u, v) -> {
            verify(isStartVertex(v) || v.getPayload().getInDegreeWithoutDeadEdges() > 0);
            verify(isExitVertex(v) || v.getPayload().getOutDegreeWithoutDeadEdges() > 0);
            verify(u == null || u.getPayload().getOutDegreeWithoutDeadEdges() > 0);

            log.debug(() -> "In/Out degrees of vertex [" + v +"]: " + v.getPayload().getInDegreeWithoutDeadEdges() + ":"
                    + v.getPayload().getOutDegreeWithoutDeadEdges());
            return true;
        }).dfsFrom(startVertex);

        log.debug("total time: {}", () -> totalTime);
    }

    private boolean preProcessVertex(int time, @Nullable Vertex<DfsPayload> currentVertex /* null for start vertex */,
                                     Vertex<DfsPayload> discoveredVertex) {
        if (currentVertex != null) {
            EdgeKind edgeKind = colorEdge(currentVertex, discoveredVertex);

            discoveredVertex.getPayload().incrementInDegree();

            boolean deadEdge = false;
            if (edgeKind == EdgeKind.BACKWARD) {
                log.debug(() -> "in: BACKWARD edge marked dead: " + currentVertex + " -> " + discoveredVertex);
                deadEdge = true;
            } else if (discoveredVertex.getPayload().isDead() && !isExitVertex(discoveredVertex)) {
                log.debug(() -> "in: edge to DEAD vertex marked dead: " + currentVertex.getPayload().edgeKind(discoveredVertex.getId())
                        + " " + currentVertex + " -> " + discoveredVertex);
                deadEdge = true;
            }
            if (deadEdge) {
                currentVertex.getPayload().markEdgeDead(discoveredVertex);
            }
        }

        return true;
    }

    private EdgeKind colorEdge(Vertex<DfsPayload> fromVertex, Vertex<DfsPayload> toVertex) {
        EdgeKind edgeKind = switch (toVertex.getPayload().getColor()) {
            case WHITE -> EdgeKind.TREE;
            case GREY -> EdgeKind.BACKWARD;
            case BLACK -> (fromVertex.getPayload().getDfsStartTime() < toVertex.getPayload().getDfsStartTime())
                    ? EdgeKind.FORWARD : EdgeKind.CROSS;
        };
        fromVertex.getPayload().setEdgeKind(toVertex, edgeKind);
        return edgeKind;
    }

    private boolean postProcessVertex(int time, @Nullable Vertex<DfsPayload> currentVertex /* null for start vertex */,
                                      Vertex<DfsPayload> discoveredVertex) {
        // post-processing is done only for TREE-kind edges
        assert currentVertex == null
                || currentVertex.getPayload().edgeKind(discoveredVertex.getId()) == EdgeKind.TREE;

        if (discoveredVertex.getPayload().isDead() && !isExitVertex(discoveredVertex)) {
            if (currentVertex != null
                    // NB: the edge may have already been marked dead in "IN" function.
                    // This happens when we traverse a dead-end tree branch that does not end with the finish vertex.
                    && currentVertex.getPayload().isLiveEdge(discoveredVertex.getId())) {
                log.debug("out: edge to DEAD vertex marked dead: " + currentVertex.getPayload().edgeKind(discoveredVertex.getId())
                        + " " + currentVertex + " -> " + discoveredVertex);
                currentVertex.getPayload().markEdgeDead(discoveredVertex);
            }
            return false;
        } else {
            topologicalSortList.addFirst(discoveredVertex);

            return true;
        }
    }

    private List<Vertex<DfsPayload>> findDominators() {
        final List<Vertex<DfsPayload>> postLeadVertices = new LinkedList<>();
        int parallelEdgeCount = 0;
        for (Vertex<DfsPayload> vertex: topologicalSortList) {
            int inDegree = vertex.getPayload().getInDegreeWithoutDeadEdges();
            int outDegree = vertex.getPayload().getOutDegreeWithoutDeadEdges();

            assert (isStartVertex(vertex) && inDegree == 0) || (!isStartVertex(vertex) && inDegree > 0);
            assert (isExitVertex(vertex) && outDegree == 0) || (!isExitVertex(vertex) && outDegree > 0);

            parallelEdgeCount -= inDegree;

            if (parallelEdgeCount == 0) {
                postLeadVertices.add(vertex);
            }

            parallelEdgeCount += outDegree;

            assert (isExitVertex(vertex) && parallelEdgeCount == 0)
                    || (!isExitVertex(vertex) && parallelEdgeCount > 0);
        }
        return postLeadVertices;
    }
}
