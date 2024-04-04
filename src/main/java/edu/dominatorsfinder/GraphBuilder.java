package edu.dominatorsfinder;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import lombok.*;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.nio.dot.DOTImporter;

import java.io.Reader;
import java.io.StringReader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Verify.verify;
import static java.util.Comparator.comparing;


public class GraphBuilder <P> {

    private InputData inputData;
    private DefaultDirectedGraph<JGraphtVertex, JGraphtEdge> jgraphtGraph;

    private Graph<P> graph;

    private JGraphtVertex startVertex;
    private JGraphtVertex exitVertex;

    private Function<Vertex<P>, P> payloadFactoryFunction;

    @RequiredArgsConstructor
    @Getter
    @ToString(of = "key")
    static class JGraphtVertex {
        private final String key;
        @Setter
        int id;
    }

    /** NB: For record it would be {@code new Edge().equals(new Edge()) == true}, so it has to be a class. */
    static class JGraphtEdge {}

    public GraphBuilder<P> withPayloadFactoryFunction(Function<Vertex<P>, P> factoryFunction) {
        this.payloadFactoryFunction = factoryFunction;
        return this;
    }

    public void build(String inputJson) {
        buildInputData(inputJson);
        preCheckInputData();
        buildGraph(inputData.getDotFormatGraph());
        checkInvariants();
        buildGraph();
    }

    public Graph<P> getGraph() {
        return graph;
    }

    void preCheckInputData() {
        // NB: these restrictions are relaxed for the sake of tests:
        //Preconditions.checkArgument(!isNullOrEmpty(inputData.getStartNodeKey()), "Start vertex (\"h\") must be given.");
        //Preconditions.checkArgument(!isNullOrEmpty(inputData.getExitNodeKey()), "Exit vertex (\"e2\") must be given.");
        Preconditions.checkArgument(!isNullOrEmpty(inputData.getDotFormatGraph()), "Graph (\"graph\") must be given.");
    }

    void buildInputData(String inputJson) {
        Gson gson = new Gson();
        inputData = gson.fromJson(inputJson, InputData.class);
    }

    @SneakyThrows
    void buildGraph(String digraphDSLString) {
        jgraphtGraph = new DefaultDirectedGraph<>(null, JGraphtEdge::new, false);

        DOTImporter<JGraphtVertex, JGraphtEdge> importer = new DOTImporter<>();
        final Map<String, JGraphtVertex> vertices = new HashMap<>();
        importer.setVertexFactory(id -> vertices.computeIfAbsent(id, JGraphtVertex::new));

        try (Reader reader = new StringReader(digraphDSLString)) {
            importer.importGraph(jgraphtGraph, reader);
        }
    }

    void checkInvariants() {
        final Map<String, JGraphtVertex> vertexMap = jgraphtGraph.vertexSet().stream().collect(Collectors.toMap(JGraphtVertex::getKey, Function.identity()));

        final String entryNodeKey = inputData.getEntryNodeKey();
        if (!isNullOrEmpty(entryNodeKey)) {
            JGraphtVertex entryVertex =  vertexMap.get(entryNodeKey);
            Preconditions.checkArgument(entryVertex != null, "Entry vertex [%s] must be present in the Graph.", entryNodeKey);
            Preconditions.checkArgument(jgraphtGraph.outDegreeOf(entryVertex) > 0, "Entry vertex [%s] must have outgoing edges.", entryNodeKey);
        }

        final String startNodeKey = inputData.getStartNodeKey();
        startVertex =  vertexMap.get(startNodeKey);
        //Preconditions.checkArgument(startVertex != null, "Start vertex [%s] must be present in the Graph.", startNodeKey);

        final String exitNodeKey = inputData.getExitNodeKey();
        exitVertex =  vertexMap.get(exitNodeKey);
        //Preconditions.checkArgument(exitVertex != null, "Exit vertex [%s] must be present in the Graph.", exitNodeKey);
    }

    void buildGraph() {
        final Set<JGraphtVertex> vertexSet = new TreeSet<>(comparing(JGraphtVertex::getKey));
        Set<JGraphtVertex> set = jgraphtGraph.vertexSet();
        vertexSet.addAll(set);
        verify(set.size() == vertexSet.size());

        int vertexId = 0;
        for (JGraphtVertex vertex: vertexSet) {
            vertex.id = vertexId;
            vertexId++;
        }

        final Vertex<P>[] vertices = new Vertex[vertexSet.size()];

        for (JGraphtVertex vertex: vertexSet) {
            int[] outIndexes = jgraphtGraph.outgoingEdgesOf(vertex).stream()
                    .map(edge -> jgraphtGraph.getEdgeTarget(edge))
                    .mapToInt(JGraphtVertex::getId)
                    .toArray();
            assert areCorrectIndices(outIndexes, vertexSet.size());
            // NB: make the order of outgoing edges fully deterministic: sort them by id:
            Arrays.sort(outIndexes);
            vertices[vertex.getId()] = new Vertex<>(vertex.getId(), vertex.getKey(), outIndexes, payloadFactoryFunction);
        }

        graph = new Graph<>(vertices);
    }

    boolean areCorrectIndices(int[] indexes, int numNodes) {
        for (int index : indexes) {
            if (index < 0 || index >= numNodes) {
                return false;
            }
        }
        return true;
    }

    public Vertex<P> startVertex() {
        return graph.vertex(startVertex.getId());
    }
    public Vertex<P> exitVertex() {
        return graph.vertex(exitVertex.getId());
    }
}
