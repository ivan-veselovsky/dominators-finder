package edu.postleadsfinder;

import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;

@RequiredArgsConstructor
public class DepthFirstSearch {

    @FunctionalInterface
    public interface EdgeFunction {
        boolean apply(int time, Vertex fromVertex, Vertex toVertex);
    }

    private final Graph graph;
    private final EdgeFunction preProcessFunction;
    private final EdgeFunction postProcessFunction;
    
//    private final Stack<Node<NodePayload>> stack = new Stack<>();
//
//    void traverse(Graph<NodePayload> graph) {
//        for (int i=0; i<graph.size(); i++) {
//
//        }
//    }
    
//void dfs(int startIndex, Consumer<Node> onPush, Consumer<Node> onPop) {
//    Preconditions.checkArgument(startIndex >= 0 && startIndex < graph.size());
//
//    int time = 0;
//
//    final Stack<Node> stack = new Stack<>();
//    Node node = node(startIndex);
//    do {
//        time++;
//        onPush.accept(node);
//        stack.push(node);
//
//        for (int out: node.getOutgoingEdges()) {
//            Node outNode = node(out);
//            NodePayload payload = outNode.getPayloadData();
//            if (payload.startTime > 0) {
//
//            }
//            onPush.accept(outNode);
//            stack.push(outNode);
//        }
//
//        process(node);
//
//    } while (!stack.isEmpty());
//}

    public int dfs(final Vertex vertex) {
        return dfsRecursive(0, null, vertex);
    }

    private int dfsRecursive(int time, final @Nullable Vertex currentVertex, final Vertex discoveredVertex) {
        time++;
        if (preProcessFunction.apply(time, currentVertex, discoveredVertex)) {

            for (Vertex outVertex: graph.outgoingNodes(discoveredVertex)) {
                time = dfsRecursive(time, discoveredVertex, outVertex);
            }

            time++;
            postProcessFunction.apply(time, currentVertex, discoveredVertex);
        }
        return time;
    }

    // TODO: make clarity with this shit:
//    void dfsIterative2(final Node startNode) {
//        final Stack<Node> stack = new Stack<>();
//
//        Node node = startNode;
//        while (true) {
//            if (preProcessFunction.test(node)) { // true: assign start time; true/false: assign edge kind;
//                stack.push(node);
//
//                for (Node outNode: graph.outgoingNodes(node)) { // todo: reverse order
//                    if (preProcessFunction.test(outNode)) {
//                        stack.push(outNode);
//                        node = outNode;
//                    }
//                }
//            } else {
//                break;
//            }
//        }
//
//        while (true) {
//            node = stack.pop();
//
//            postProcessFunction.accept(node);
//        }
//    }
//
//
//    void dfsIterativeEnqueue(Node startNode) {
//        final Stack<Node> stack = new Stack<>();
//        stack.push(startNode);
//
//        //Node node;
//        while (true) {
//            Node node = stack.pop();
//
//            if (preProcessFunction.test(node)) { // true: assign start time; true/false: assign edge kind;
//                stack.push(node);
//
//                for (Node outNode : graph.outgoingNodes(node)) {
//                    stack.push(outNode);
//                }
//
//                node = stack.pop();
//                postProcessFunction.accept(node); // assign finish time;
//            }
//        }
//    }

}
