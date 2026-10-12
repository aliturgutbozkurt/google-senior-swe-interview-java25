# Chapter 2: Advanced Data Structures & Algorithms Deep Dive (Google L5/L6)

## 1. Graph Suite

### 1.1 Tarjan's Algorithm: Strongly Connected Components (SCC) & Bridges
In Google infrastructure (e.g. Google Borg job dependency resolution, Spanner transaction dependency graphs, Google Maps road topology), graph cycle and connectivity analysis is standard.

```
       0 ----> 1
       ^     / |
       |    /  |
       |   v   v
       2 <     3 <---> 4
   [SCC 1: {0,1,2}]   [SCC 2: {3,4}]
```

#### Core Invariants
1. **DFS Tree Ordering**: Each vertex receives an incrementing discovery ID `ids[u]`.
2. **Low-Link Value**: `low[u]` represents the lowest discovery ID reachable from $u$ through a sequence of tree edges followed by at most one back-edge to a node currently on the DFS recursion stack.
   $$low[u] = \min(ids[u], \min_{(u, v) \in E} low[v] \text{ [if unvisited]}, \min_{(u, v) \in E} ids[v] \text{ [if on stack]})$$
3. **SCC Root Condition**: When unwinding DFS, if `ids[u] == low[u]`, $u$ is the root of an SCC. Popping elements from the stack until $u$ yields the exact SCC.
4. **Bridge Condition (Undirected)**: An edge $(u, v)$ is a critical bridge if and only if $low[v] > ids[u]$.

---

### 1.2 Dinic's Algorithm: Maximum Flow & Bipartite Matching
Google uses max flow formulations for cluster capacity allocation, machine placement across racks, and bipartite task scheduling.

#### Why Dinic beats Ford-Fulkerson and Edmonds-Karp
* **Edmonds-Karp**: $O(V \cdot E^2)$ via single shortest augmenting paths in BFS.
* **Dinic's Algorithm**: $O(V^2 \cdot E)$ on general networks and $O(E \sqrt{V})$ on unit networks (such as bipartite matching).

#### The Two-Phase Loop
1. **Phase 1: BFS Level Graph**: Construct level numbers $level[u]$ from source $s$ to sink $t$. If $level[t] == -1$, no augmenting path exists; terminate.
2. **Phase 2: DFS Blocking Flow with Work Pointers**: Push flows along paths where $level[v] == level[u] + 1$. An edge pointer array `work[u]` prevents re-examining saturated edges during DFS, achieving $O(V \cdot E)$ per phase.

---

### 1.3 Dijkstra with Indexed Priority Queue (IPQ)
Standard `java.util.PriorityQueue` does not support $O(\log V)$ decrease-key. In a dense graph with $E \approx V^2$, adding duplicate pairs $(dist, vertex)$ causes the heap to blow up to $O(E \log E)$ time and $O(E)$ memory.

#### Indexed Priority Queue Architecture
An IPQ maintains three coordinated arrays of size $V$:
* `heap[p]`: The vertex index stored at binary heap position $p$.
* `pos[v]`: The inverse lookup mapping giving the position in `heap` where vertex $v$ resides.
* `keys[v]`: The actual distance associated with vertex $v$.

When relaxing an edge $(u, v)$, if `newDist < dist[v]`, we execute `ipq.decreaseKey(v, newDist)`, which updates `keys[v]` and calls `swim(pos[v])` in $O(\log V)$ time with zero object allocations!

---

### 1.4 Topological Sort & Cycle Path Reconstruction
Given a directed dependency graph (e.g. Build rules in Bazel):
* **Kahn's Algorithm**: Maintain in-degree array. Push zero-in-degree nodes to queue. If processed count $< V$, graph has a cycle.
* **DFS 3-Coloring with Cycle Tracing**:
  * White (0): Unvisited
  * Gray (1): In current recursion call stack
  * Black (2): Fully explored
  * If an edge leads to a **Gray** node, a back-edge is found! Traverse `parent[curr]` backwards to reconstruct the exact cycle chain (e.g. `[A -> B -> C -> A]`).

---

## 2. Advanced Trees & Range Queries

### 2.1 Segment Tree with Lazy Propagation
A Segment Tree over an array of size $N$ represents intervals hierarchically.

```
                    [0, 3] (Sum: 16)
                   /      \
             [0, 1]        [2, 3]
            /     \        /     \
          [0]     [1]    [2]     [3]
```

#### Lazy Tag Invariants
When an update applies to an entire sub-interval $[ql, qr]$ covered by a node:
1. Apply the update directly to the node aggregate:
   $$\text{treeSum}[node] \mathrel{+}= \Delta \times (R - L + 1)$$
2. Record deferred delta in $\text{lazy}[node] \mathrel{+}= \Delta$.
3. **Crucial Rule**: Before descending into children during query or partial update, call $\text{pushDown}(node)$ to transmit lazy delta to children and clear $\text{lazy}[node] = 0$.

---

### 2.2 Fenwick Tree (Binary Indexed Tree - 1D & 2D)
A Fenwick Tree computes prefix sums in $O(\log N)$ time with zero pointer indirection.
* **The Low-Bit Trick**: `i & (-i)` isolates the lowest power-of-two bit in $i$.
* **Prefix Sum**: Accumulate `tree[i]` and subtract `(i & -i)` until $i = 0$.
* **Point Add**: Add `delta` to `tree[i]` and add `(i & -i)` until $i > N$.
* **2D Bounding Box**: Range sum in $[r_1, c_1]$ to $[r_2, c_2]$ via inclusion-exclusion:
  $$\text{Sum} = P(r_2, c_2) - P(r_1-1, c_2) - P(r_2, c_1-1) + P(r_1-1, c_1-1)$$

---

### 2.3 Lowest Common Ancestor (LCA) via Binary Lifting
In hierarchical tree structures (directory trees, organizational hierarchies, DOM trees):
* Define `up[u][k]` as the $2^k$-th ancestor of node $u$.
* **DP Recurrence**:
  $$\text{up}[u][k] = \text{up}[\text{up}[u][k-1]][k-1]$$
* **Query in $O(\log N)$**:
  1. Equalize depths of $u$ and $v$ by lifting the deeper node using powers of 2.
  2. If $u == v$, return $u$.
  3. Lift both nodes simultaneously from $k = \log N - 1$ down to 0 whenever $\text{up}[u][k] \neq \text{up}[v][k]$.
  4. The LCA is $\text{up}[u][0]$.

---

## 3. Dynamic Programming & Optimizations

### 3.1 Bitmask DP
When $N \le 20$, subsets of vertices can be encoded into bits of a 32-bit integer:
* Traveling Salesperson: $dp(mask, u)$ is the minimal cost to visit remaining unvisited cities starting at city $u$.
* State transitions iterate over all unvisited $v$ where $((mask \gg v) \& 1) == 0$.
* Time: $O(2^N \cdot N^2)$, Space: $O(2^N \cdot N)$.

### 3.2 Monotonic Queue DP Optimization
When a DP recurrence has the sliding-window form:
$$dp[i] = \text{cost}[i] + \max_{j \in [i-k, i-1]} dp[j]$$
Naive evaluation takes $O(N \cdot k)$. Using a double-ended queue (`Deque`) that maintains indices in strictly decreasing order of $dp$ values:
1. Pop elements from the head whose indices are $< i - k$ (expired).
2. The head of the deque is the optimal $\max_{j} dp[j]$.
3. Before pushing $i$, pop elements from the tail whose $dp$ values are $\le dp[i]$ (dominated).
* **Result**: Each element is pushed and popped at most once, reducing total time to $O(N)$!

---

## 4. String Algorithms

### 4.1 Knuth-Morris-Pratt (KMP)
KMP avoids resetting the text pointer $i$ during pattern matching using the prefix function $\pi$:
* $\pi[i]$ is the length of the longest proper prefix of $P[0 \dots i]$ that is also a suffix of $P[0 \dots i]$.
* If a mismatch occurs at $P[j]$, instead of restarting at $P[0]$, we continue matching against $P[\pi[j-1]]$.

### 4.2 Aho-Corasick Automaton
Used in Google content filtering, regex search engines, and keyword discovery:
* Combines a **Trie** representing dictionary patterns with **failure transitions** (similar to KMP).
* BFS builds failure links: if a character transition does not exist, follow the failure link until a match or root is found.
* Output links allow emitting all matching dictionary keywords in $O(|Text| + \text{matches})$.

### 4.3 Rabin-Karp Double Rolling Hash
To find duplicate substrings of length $L$ in $O(N)$:
* Polynomial rolling hash: $H(s) = \sum_{i=0}^{L-1} s[i] \cdot B^{L-1-i} \pmod M$.
* Shifting the window by 1 character takes $O(1)$:
  $$H_{\text{new}} = ((H_{\text{old}} - s[i] \cdot B^{L-1}) \cdot B + s[i+L]) \pmod M$$
* **Double Hash Invariant**: By calculating two independent hashes with $M_1 = 10^9+7$ and $M_2 = 10^9+9$, collision probability drops below $10^{-18}$, rendering string checks redundant.
