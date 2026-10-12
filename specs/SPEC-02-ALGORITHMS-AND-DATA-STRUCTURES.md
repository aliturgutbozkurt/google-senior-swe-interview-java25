# SPEC-02: Advanced Algorithms & Complex Data Structures (Google L5/L6 Focus)

## 1. Domain & Scope
This specification defines the functional contracts, mathematical invariants, asymptotic guarantees, and test suites for Google's highest-frequency advanced algorithmic challenges. Every algorithm must be implemented in idiomatic Java 25 with 100% test coverage.

---

## 2. Component Specifications

### 2.1 Graph Suite

#### 2.1.1 Tarjan's Strongly Connected Components (SCC) & Bridges
- **Problem Statement**: Given a directed graph $G = (V, E)$, partition $V$ into maximal strongly connected subgraphs where every vertex is reachable from every other vertex. For undirected graphs, identify critical bridges whose removal increases disconnected components.
- **Mathematical Invariants**:
  1. Discovery time `ids[u]` assigns a strictly increasing order during DFS traversal.
  2. `low[u] = \min(ids[u], \min_{(u, v) \in E, v \text{ on stack}} low[v])`.
  3. A vertex $u$ is the root of an SCC if and only if `ids[u] == low[u]`.
  4. An edge $(u, v)$ is a critical bridge in an undirected graph if and only if `low[v] > ids[u]`.
- **Complexity Budget**:
  - Time: $O(V + E)$ single-pass DFS.
  - Space: $O(V)$ auxiliary recursion stack and visited bitsets.

#### 2.1.2 Dinic's Algorithm (Maximum Network Flow & Bipartite Matching)
- **Problem Statement**: Given a directed flow network $G = (V, E)$ with source $s$, sink $t$, and edge capacities $c(u, v)$, compute maximum permissible flow such that capacity constraints and flow conservation are strictly preserved.
- **Mathematical Invariants**:
  1. Capacity constraint: $0 \le f(u, v) \le c(u, v)$.
  2. Skew symmetry: $f(u, v) = -f(v, u)$.
  3. Flow conservation: $\sum_{v} f(u, v) = 0$ for all $u \neq s, t$.
  4. Level graph construction via BFS ensures augmenting paths in blocking flow are shortest paths in terms of edge count.
- **Complexity Budget**:
  - Time: $O(V^2 E)$ general networks; $O(E \sqrt{V})$ for unit networks (Hopcroft-Karp equivalent for bipartite matching).
  - Space: $O(V + E)$ residual graph representation.

#### 2.1.3 Dijkstra with Indexed Priority Queue
- **Problem Statement**: Find the single-source shortest path in a weighted directed graph with non-negative edge weights $w(u, v) \ge 0$.
- **Mathematical Invariants**:
  1. For visited vertices, `dist[u]` represents the exact minimal geodesic distance.
  2. Decreasing key in an indexed min-heap takes $O(\log V)$ via direct index-to-position mapping array, eliminating redundant nodes in standard `java.util.PriorityQueue`.
- **Complexity Budget**:
  - Time: $O((V + E) \log V)$ strictly bounded.
  - Space: $O(V)$ heap and index lookup arrays.

#### 2.1.4 Robust Topological Sorter with Cycle Path Reconstruction
- **Problem Statement**: Given a directed graph with $V$ vertices and $E$ dependencies, produce a valid linear ordering or reconstruct the exact cycle path if cyclic.
- **Implementation**: Kahn's in-degree queue + DFS coloring with parent tracing for cycle extraction.
- **Complexity Budget**:
  - Time: $O(V + E)$.
  - Space: $O(V)$.

---

### 2.2 Advanced Trees & Range Queries

#### 2.2.1 Segment Tree with Lazy Propagation
- **Problem Statement**: Maintain an array of $N$ integers supporting two operations:
  1. `updateRange(left, right, delta)`: Add delta to all elements in $[left, right]$.
  2. `queryRange(left, right)`: Compute associative aggregate (Sum, Min, or Max) in $[left, right]$.
- **Mathematical Invariants**:
  1. Node $i$ covers interval $[L, R]$. If $L < R$, left child is $2i$, right child is $2i+1$.
  2. Lazy tag `lazy[i]` represents deferred delta updates not yet pushed to children.
  3. Invariant: Query on any node requires `pushDown(i)` if `lazy[i] != 0`.
- **Complexity Budget**:
  - Tree Construction: $O(N)$.
  - Range Update: $O(\log N)$.
  - Range Query: $O(\log N)$.
  - Space: $4N$ elements.

#### 2.2.2 Fenwick Tree (Binary Indexed Tree - 1D & 2D)
- **Problem Statement**: Efficient prefix sum and range queries using low-bit bitwise isolation `(x & -x)`.
- **Complexity Budget**:
  - Point Update: $O(\log N)$ (or $O(\log N \log M)$ in 2D).
  - Prefix Query: $O(\log N)$ (or $O(\log N \log M)$ in 2D).
  - Space: $O(N)$ (or $O(N \cdot M)$ in 2D) flat primitive array with zero pointer overhead.

#### 2.2.3 Lowest Common Ancestor (LCA) via Binary Lifting
- **Problem Statement**: In a tree of $N$ nodes, find the lowest common ancestor of any pair $(u, v)$ in $O(\log N)$ time after $O(N \log N)$ preprocessing.
- **Mathematical Invariants**:
  - `up[node][k]` stores the $2^k$-th ancestor of `node`.
  - Recurrence: `up[node][k] = up[up[node][k-1]][k-1]`.
- **Complexity Budget**:
  - Preprocessing: $O(N \log N)$ time, $O(N \log N)$ space.
  - Query: $O(\log N)$ time.

---

### 2.3 Dynamic Programming & Monotonic Optimizations

#### 2.3.1 Bitmask DP (Traveling Salesperson / Exact Cover)
- **State Representation**: $dp(mask, u)$ where $mask$ is a bitmask of visited vertices, $u$ is the current vertex.
- **Recurrence**: $dp(mask, u) = \min_{(u, v) \in E} (weight(u, v) + dp(mask \cup \{v\}, v))$.
- **Complexity Budget**:
  - Time: $O(2^N \cdot N^2)$.
  - Space: $O(2^N \cdot N)$.

#### 2.3.2 Monotonic Queue DP Optimization
- **Problem Statement**: Sliding window minimum/maximum and constrained subsequence sum $dp[i] = nums[i] + \max_{j \in [i-k, i-1]} dp[j]$.
- **Mathematical Invariants**:
  - Deque elements maintain strictly monotonic values; elements outside current sliding window $[i-k, i]$ are evicted from the head in $O(1)$ amortized time.
- **Complexity Budget**:
  - Time: $O(N)$ total amortized time across $N$ elements.
  - Space: $O(k)$ queue capacity.

---

### 2.4 String Algorithms

#### 2.4.1 Knuth-Morris-Pratt (KMP)
- **Problem Statement**: Find all occurrences of pattern $P$ of length $M$ in text $T$ of length $N$.
- **Mathematical Invariants**:
  - Failure function $\pi[i]$ is the length of the longest proper prefix of $P[0 \dots i]$ that is also a suffix of $P[0 \dots i]$.
  - Backtracking never rewinds text pointer $i$.
- **Complexity Budget**:
  - Preprocessing: $O(M)$ time and space.
  - Search: $O(N)$ time.

#### 2.4.2 Aho-Corasick Multi-Pattern Automaton
- **Problem Statement**: Search for occurrences of a dictionary of $K$ patterns simultaneously in a streaming text $T$.
- **Components**: Trie graph + BFS failure links + dictionary output suffix links.
- **Complexity Budget**:
  - Build Automaton: $O(\sum |P_i| \cdot \Sigma)$.
  - Text Search: $O(|T| + \text{matches})$.

---

### 2.5 In-Memory Production Data Structures

#### 2.5.1 O(1) LFU (Least Frequently Used) Cache
- **Operations**: `get(key)` in $O(1)$, `put(key, value)` in $O(1)$ with tie-breaking by LRU.
- **Invariants**:
  - Two-level index: `keyToNode` map + `frequencyToList` map.
  - `minFrequency` pointer updated in $O(1)$ on eviction or frequency promotion.

#### 2.5.2 Dual-Heap Streaming Median Finder
- **Operations**: `addNum(int num)` in $O(\log N)$, `findMedian()` in $O(1)$.
- **Invariants**:
  - Max-heap stores lower half; min-heap stores upper half.
  - Balance condition: $0 \le |\text{maxHeap}| - |\text{minHeap}| \le 1$.
  - Ordered condition: $\max(\text{maxHeap}) \le \min(\text{minHeap})$.
