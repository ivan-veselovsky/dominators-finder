package edu.postleadsfinder;

import com.google.common.base.Verify;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Graph {

    private final Vertex[] vertices;

    Graph(Vertex[] vertices) {
        checkInvariants(vertices);
        this.vertices = vertices;
    }

    private void checkInvariants(Vertex[] vertices) {
        for (int i = 0; i< vertices.length; i++) {
            Verify.verify(vertices[i] != null);
            Verify.verify(vertices[i].getId() == i);
        }
    }

    int size() {
        return vertices.length;
    }

    int numberOfEdges() {
        return (int)Arrays.stream(vertices).map(Vertex::getOutgoingEdges).flatMapToInt(Arrays::stream).count();
    }

    List<Vertex> outgoingNodes(Vertex vertex) {
        int[] childrenIndices = vertex.getOutgoingEdges();
        return Arrays.stream(childrenIndices).mapToObj(i -> vertices[i]).toList();
    }

//    List<Vertex> outgoingNodesReverseOrder(Vertex vertex) {
//        int[] childrenIndices = vertex.getOutgoingEdges();
//        List<Vertex> list = new ArrayList<>(childrenIndices.length);
//        for (int i=childrenIndices.length - 1; i>=0 ; i--) {
//            list.add(vertices[i]);
//        }
//        return list;
//    }

    Stream<Vertex> vertexStream() {
        return Arrays.stream(vertices);
    }

    Vertex vertex(int index) {
        return vertices[index];
    }

    /**
     * Can be used before next traversal to clear the "color" of Vertices.
     */
    void clearTime() {
        vertexStream().forEach(v -> v.getNodePayload().clearTime());
    }

}
