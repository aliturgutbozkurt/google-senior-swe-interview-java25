package com.google.prep.algorithms.graph;

import java.util.*;

/**
 * Production-grade Dinic's Algorithm for Maximum Network Flow and Maximum Bipartite Matching.
 *
 * <p>Key Properties:
 * <ul>
 *   <li>Constructs a level graph using BFS to guarantee augmenting paths are shortest paths.</li>
 *   <li>Pushes blocking flows using DFS with edge iteration pointers (work pointers).</li>
 *   <li>Time Complexity: O(V^2 * E) on general networks; O(E * sqrt(V)) on unit networks / bipartite matching.</li>
 *   <li>Space Complexity: O(V + E) for residual edge representation.</li>
 * </ul>
 */
public final class DinicMaxFlow {

    public static final class Edge {
        public final int from;
        public final int to;
        public final int residualIndex; // Index of the residual back-edge in adjacency list of 'to'
        public long capacity;
        public long flow;

        public Edge(int from, int to, int residualIndex, long capacity) {
            this.from = from;
            this.to = to;
            this.residualIndex = residualIndex;
            this.capacity = capacity;
            this.flow = 0;
        }

        public long remainingCapacity() {
            return capacity - flow;
        }
    }

    private final int n;
    private final List<List<Edge>> graph;
    private final int[] level;
    private final int[] work;

    public DinicMaxFlow(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Number of vertices must be positive: " + n);
        }
        this.n = n;
        this.graph = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            graph.add(new ArrayList<>());
        }
        this.level = new int[n];
        this.work = new int[n];
    }

    /**
     * Adds a directed edge with given capacity to the flow network.
     */
    public void addEdge(int from, int to, long capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Edge capacity must be non-negative: " + capacity);
        }
        validateVertex(from);
        validateVertex(to);

        Edge forward = new Edge(from, to, graph.get(to).size(), capacity);
        Edge residual = new Edge(to, from, graph.get(from).size(), 0);

        graph.get(from).add(forward);
        graph.get(to).add(residual);
    }

    /**
     * Computes the maximum flow from source {@code s} to sink {@code t}.
     *
     * @param s Source node index
     * @param t Sink node index
     * @return Maximum flow value
     */
    public long maxFlow(int s, int t) {
        validateVertex(s);
        validateVertex(t);
        if (s == t) {
            throw new IllegalArgumentException("Source and sink cannot be identical: " + s);
        }

        long totalFlow = 0;

        // While an augmenting path exists in the level graph
        while (bfsLevelGraph(s, t)) {
            Arrays.fill(work, 0);
            while (true) {
                long pushed = dfsBlockingFlow(s, t, Long.MAX_VALUE);
                if (pushed <= 0) break;
                totalFlow += pushed;
            }
        }
        return totalFlow;
    }

    /**
     * Computes the Minimum Cut partition (S, T) where S contains all nodes reachable from s in residual graph.
     */
    public boolean[] minCut(int s) {
        validateVertex(s);
        boolean[] visited = new boolean[n];
        Deque<Integer> queue = new ArrayDeque<>();
        visited[s] = true;
        queue.offer(s);

        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (Edge edge : graph.get(u)) {
                if (!visited[edge.to] && edge.remainingCapacity() > 0) {
                    visited[edge.to] = true;
                    queue.offer(edge.to);
                }
            }
        }
        return visited;
    }

    private boolean bfsLevelGraph(int s, int t) {
        Arrays.fill(level, -1);
        level[s] = 0;
        Deque<Integer> queue = new ArrayDeque<>();
        queue.offer(s);

        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (Edge edge : graph.get(u)) {
                if (edge.remainingCapacity() > 0 && level[edge.to] == -1) {
                    level[edge.to] = level[u] + 1;
                    queue.offer(edge.to);
                }
            }
        }
        return level[t] != -1;
    }

    private long dfsBlockingFlow(int u, int t, long flow) {
        if (u == t) return flow;

        List<Edge> edges = graph.get(u);
        for (int i = work[u]; i < edges.size(); i = ++work[u]) {
            Edge edge = edges.get(i);
            if (edge.remainingCapacity() > 0 && level[edge.to] == level[u] + 1) {
                long bottleNeck = dfsBlockingFlow(edge.to, t, Math.min(flow, edge.remainingCapacity()));
                if (bottleNeck > 0) {
                    edge.flow += bottleNeck;
                    graph.get(edge.to).get(edge.residualIndex).flow -= bottleNeck;
                    return bottleNeck;
                }
            }
        }
        return 0;
    }

    private void validateVertex(int v) {
        if (v < 0 || v >= n) {
            throw new IndexOutOfBoundsException("Vertex " + v + " out of bounds [0, " + (n - 1) + "]");
        }
    }
}
