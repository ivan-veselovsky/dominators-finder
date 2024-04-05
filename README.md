### Task Description

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
The server outputs all the nodes that dominate ℎ in the
graph.

    $ curl -X POST http://localhost:10000/server -H "Content-Type: application/json" -d '{"e1": "1","e2": "7","h": "2","graph": " digraph graphname{\n1->2\n2->3\n2->5\n5->2\n3->5\n5->7}"}'

The server should return `{5,7}`


### Solution Description

#### I. Solution for Arbitrary Graphs 

This section describes the implemented universal solution that applicable to both cyclic and acyclic graphs. 
Solution, let's call it **Heavy Vertices Bypass**, is based on the following idea.

1) Let's find the shortest (in terms of number of edges) path from `h` to `e2`.  
2) Let's assign a weight to each vertex of `G`. (Normally weights are assigned to graph edges, but here it's
   more convenient to weight vertices). We will maintain set `H` -- set of "heavy" vertices: vertices in 
   this set have weight of `|V|` (here `|V|` is the number of vertices in the graph). Other vertices will have 
   default weight of `1`. Initially we add into `H` all vertices found in step 1.     
3) Now let's find the shortest path (the path of the smallest summary weight) from `h` to `e2` considering the assigned weights, 
   and remove from `H` all "heavy" vertices that were "bypassed", that is, that do not appear on the newly found path.
4) Will repeat step 3. until one of the following happens: 
    4a. Set `H` becomes empty, => algorithm exits with answer "there are no dominators";
    4b. Set `H` did not change during an iteration of step 3. => algorithm exits with answer "all the dominators are 
      contained in `H`".

Let's prove that this algorithm works correctly.
Initially all dominators are in `H` (because any path from `h` to `e2` contains all dominators, if there is any). 
Any non-dominator vertex will be sooner or later removed from `H`, because if it is non-dominant, so it is possible 
to bypass it, so this "bypass" will have smaller weight than a path containing all nodes from `H`. Thus, 
at least one non-dominator vertex (if any) will disappear from `H` on each iteration. Thus, all non-dominators 
will be removed from `H`, and in the step 4. `H` contains dominator vertices only. 

Why this algorithm is fast? Because in between of any 2 dominators **two non-intersecting** paths exists (this 
is a particular case of Menger's theorem). Thus, it can be proven that not more than 4 iteration of step 3. will be needed
to finish the process (see assertion in line 68 of class `edu.dominatorsfinder.heavyverticesbypass.HeavyVerticesBypassDominatorsFinder`).

##### Complexity

The search on weighted graph is done constant number of times (not more than 4), so the complexity boils down to 
the complexity of finding the shortest path on weighted graph (or complexity of computing the minimal spanning tree, 
what is the same). We use Dijkstra's algorithm with a priority queue 
implemented with an auto-balanced binary search tree, what gives us `O((V + E)log(V))`, and `O(V + E)` memory, 
as we store payload data on graph vertices.  

The described algorithm is slower than [the one](https://www.cs.princeton.edu/courses/archive/spr03/cs423/download/dominators.pdf)
suggested by Tomas Lengauer and Robert Tarjan, but it is extremely simple.

##### Ideas for Further Improvements 

1) The priority queue can be implemented more efficiently using Fibonacci Heap,
what would give us efficiency of `O((Vlog(V) + E)`. 
2) As weights of the vertices (or edges, what is the same) have only 2 possible weights (`1` and `|V|`),
we can also use Kruskal's algorithm to find the shortest path (Minimal Spanning Tree) in near-linear time `O(E * alpha(E, V)))` using 
counting sort to order the edges by weight, what would give us same runtime as "sophisticated" version of Lengauer and Tarjan's algorithm.  


#### II. "Linear" Solution for Acyclic Graphs.  

Note: 
this section describes solution that can be applied to acyclic graphs, or to cyclic graphs *between* Strongly 
Connected Components (SCC). In the latter case within each SCC we can apply "heavy vertices bypass" algorithm from section I.   
Graph partitioning into SCC-s is done using DFS search, this is currently not implemented in this solution.  

Solution is based on Depth First Search (DFS) algorithm, as per book
"Introduction to Algorithms" by T.H. Cormen, Ch.E. Leiserson, R.L. Rivest:
1) Build the DFS tree starting from the given Start vertex (_h_). (Exit vertex (_e2_) must
   be reachable from the Start vertex (_h_), so it appears on the tree. Otherwise, we throw an error.)

2) Classify all the DFS edges as being of _TREE_, _FORWARD_, _BACKWARD_, or _CROSS_ kind.

3) _BACKWARD_ edges will not be present, as the graph is acyclic. 

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

#### Build and Test

Solution was tested on `Ubuntu 20.04` with `Java 18`, `Apache Maven 3.8.6`.   

    mvn clean install
    java -jar ./target/dominators-finder-1.0-SNAPSHOT-exec.jar
    curl http://localhost:10000/

#### Notes

- Start vertex is always a _dominator_ of itself, but the task description suggests that it shall not be
 present in the returned result, so we explicitly skip the Start vertex in the output.
- Output is *not* a valid JSON, its just `{A, B, C}` string of type `text/plain`, as task description suggests.
- By default server starts on non-standard port `10000`, as task description suggests.
- When testing with `curl` please make sure to use `-H "Content-Type: application/json"` parameter, otherwise JSON gets encoded,
 and the service fails to parse it.

