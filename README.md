#### Task Description

Definition of ‘dominator’: In a single-entry, single-exit
control flow graph (CFG), a node _u_ dominators _v_ if every path
from _v_ to the exit includes _u_.

Implement a Java http server with a REST API that gets as an
input a quadrupel (𝐺, 𝑒1, 𝑒2, ℎ) where:
𝐺: is a string in dot format that represent a control flow
graph.
𝑒1: the entry node of the graph
𝑒2: the exit node of the graph
ℎ: a node in the graph
The server outputs all the nodes that dominator ℎ in the
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

Solution is based on Depth First Search (DFS) algorithm, as per book 
"Introduction to Algorithms" by T.H. Cormen, Ch.E. Leiserson, R.L. Rivest:    
1) Build the DFS tree starting from the given Start vertex (_h_). (Exit vertex (_e2_) must 
 be reachable from the Start vertex (_h_), so it appears on the tree. Otherwise, we throw an error.)

2) Classify all the DFS edges as being of _TREE_, _FORWARD_, _BACKWARD_, or _CROSS_ kind.

3) Remove loops by removing all _BACKWARD_ edges. (!!! this is the mistake. In some cases this deleted paths
that are critical to correct answer.) 

5) Remove all "dead ends" (vertices with zero out-degree), taking into account that removal 
 of some vertices may cause other "dead ends" to appear. When this step is finished, the only "dead end" vertex
 is the Exit vertex.

6) Take the remaining graph vertices in _topological sort order_. 
 (This is the descending order of "end processing time" in DFS algorithm).

7) Calculate in-degree and out-degree for each vertex.

8) Traverse the vertices in _topological sorting order_ maintaining _parallel edge count_ integer metric: 
 for each vertex reduce the metric by the vertex in-degree, then increase it by the vertex out-degree. 
 Basing on this metric detect all the dominator vertices: the dominator is a vertex where this metric drops to one.   

##### Complexity

The complexity of this solution is the time of DFS traversal, which is `O(V + E)`,  
and `O(V + E)` memory, as we store some temporary data for each vertex and edge.      

##### Build and Test

Solution was tested on `Ubuntu 20.04` with `Java 18`, `Apache Maven 3.8.6`.   

    mvn clean install
    java -jar ./target/dominators-finder-1.0-SNAPSHOT-exec.jar
    curl http://localhost:10000/

##### Notes

- Known notion of graph _articulation points_ cannot be directly applied here, 
 because the _dominators_ become articulation points only after loops removal.
- The algorithm described above can naturally be implemented in several graph traversals, but
 in fact it can be implemented in just one DFS traversal, as done here.
- Start vertex is always a _dominator_ of itself, but the task description suggests that it shall not be
 present in the returned result, so we explicitly skip the Start vertex in the output.
- Output is *not* a valid JSON, its just `{A, B, C}` string of type `text/plain`, as task description suggests.
- By default server starts on non-standard port `10000`, as task description suggests.
- When testing with `curl` please make sure to use `-H "Content-Type: application/json"` parameter, otherwise JSON gets encoded,
 and the service fails to parse it.


#### TODO

 - add ref to Tomas Lengauer and Robert Tarjan : https://www.cs.princeton.edu/courses/archive/spr03/cs423/download/dominators.pdf
 - replace Binary Search tree with vEB tree.
