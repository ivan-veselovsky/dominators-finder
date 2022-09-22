#### Task Description

Definition of ‘post-lead’: In a single-entry, single-exit
control flow graph (CFG), a node _u_ post-leads _v_ if every path
from _v_ to the exit includes _u_.

Implement a Java http server with a REST API that gets as an
input a quadrupel (𝐺, 𝑒1, 𝑒2, ℎ) where:
𝐺: is a string in dot format that represent a control flow
graph.
𝑒1: the entry node of the graph
𝑒2: the exit node of the graph
ℎ: a node in the graph
The server outputs all the nodes that post-lead ℎ in the
graph.

#### For example:

    $ curl -X POST http://localhost:10000/server -H "Content-Type: application/json" -d '{"e1": "1","e2": "7","h": "2","graph": " digraph graphname{\n1->2\n2->3\n2->5\n5->2\n3->5\n5->7}"}'

The server should return `{5,7}`

#### Guidelines
- The code should be readable, maintainable, and clear.
- The code should be valid and tested.
- The project should be packaged for shipment to be used by
clients.
- The server should log the HTTP queries and errors.
- Do not use opensource for the main algorithm; however use
open-source in all the other places that you can instead
of implementing your own code.
- Consider time and space complexity – your solution should
be as efficient as possible. In an accompanying document,
describe the main ideas and the time and space complexity
of your solution.

#### Solution Description

Based on DFS (Depth First Search) algorithm, as per book 
"Introduction to Algorithms" by T.H. Cormen, Ch.E. Leiserson, R.L. Rivest.    
1) Build the DFS tree starting from the given Start vertex (_h_). (Finish vertex (_e2_) must 
 be reachable from the Start vertex (_h_), so it appears on the tree. Otherwise we throw an error.)
2) Classify all the DFS edges as being of _TREE_, _FORWARD_, _BACKWARD_, or _CROSS_ kind.
3) Remove loops by removing all _BACKWARD_ edges.
4) Remove all "dead ends" (vertices with zero out-degree), taking into account that removal 
 of some vertices may cause other "dead ends" to appear. The only "dead end" vertex
 is the Finish vertex. 
5) Take the remaining graph vertices in __topological sort order__. 
 (This is the order opposite to "end processing time" in DFS algorithm.)
6) Calculate in-degree and out-degree for each vertex.  
7) Traverse the vertices in "topological sorting order" maintaining "parallel edge count" integer metric: 
 for each node reduce the metric by the vertex in-degree, then increase it by the vertex out-degree. 
 Basing on this metric value detect all the post-lead vertices.   

##### Complexity

The complexity of this solution is the time of DFS traversal, which is `O(V + E)`,  
and `O(V + E)` memory, as we store some temporary data for each vertex and edge.      

##### Build and test:
(Solution was tested on `Ubuntu 20.04` with `Java 18`, `Apache Maven 3.8.6`)  

    mvn clean install
    java -jar ./target/post-leads-finder-1.0-SNAPSHOT-exec.jar
    curl http://localhost:10000/

##### Notes

- Known notion of graph _articulation points_ unfortunately cannot be directly applied here, 
 because the _post-leads_ become articulation points only after loops removal.
- The algorithm described above can naturally be implemented in several graph traversals, but
 in fact it can be implemented in just one DFS traversal, as implemented here.
- Start vertex is always a _post-lead_ of itself, but the above example shows that it shall not be
 present in the return value, so we explicitly exclude the Start vertex.
- Output is *not* a JSON, its just `{A, B, C}` string of type `text/plain`, as task description suggests.
- By default server starts on non-standard port `10000`, as task description suggests.
- When testing with `curl` please use `-H "Content-Type: application/json"` parameter, otherwise JSON gets encoded,
 and the service fails to parse it.
