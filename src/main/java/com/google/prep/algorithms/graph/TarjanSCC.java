package com.google.prep.algorithms.graph;

import java.util.*;

/**
 * Production-grade implementation of Tarjan's Algorithm for:
 * 1. Strongly Connected Components (SCC) in directed graphs.
 * 2. Critical Bridges and Articulation Points in undirected graphs.
 *
 * <p>Mathematical Invariants:
 * <ul>
 *   <li>Discovery order {@code ids[u]} is strictly increasing.</li>
 *   <li>{@code low[u] = min(ids[u], min_{(u,v) in E} low[v])} for tree edges and back edges to vertices on stack.</li>
 *   <li>A vertex {@code u} is the root of an SCC if and only if {@code ids[u] == low[u]}.</li>
 *   <li>An edge {@code (u, v)} is a critical bridge in an undirected graph if and only if {@code low[v] > ids[u]}.</li>
 * </ul>
 *
 * <p>Time Complexity: O(V + E) single pass.
 * Space Complexity: O(V) auxiliary memory for stacks and tracking arrays.
 */
public final class TarjanSCC {

    private final int n;
    private final List<List<Integer>> directedAdj;
    private final List<List<Edge>> undirectedAdj;

    public record Edge(int to, int edgeId) {}
    public record Bridge(int u, int v) {}

    public TarjanSCC(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Number of vertices must be positive: " + n);
        }
        this.n = n;
        this.directedAdj = new ArrayList<>(n);
        this.undirectedAdj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            directedAdj.add(new ArrayList<>());
            undirectedAdj.add(new ArrayList<>());
        }
    }

    public void addDirectedEdge(int from, int to) {
        validateVertex(from);
        validateVertex(to);
        directedAdj.get(from).add(to);
    }

    public void addUndirectedEdge(int u, int v, int edgeId) {
        validateVertex(u);
        validateVertex(v);
        undirectedAdj.get(u).add(new Edge(v, edgeId));
        undirectedAdj.get(v).add(new Edge(u, edgeId));
    }

    /**
     * Computes all Strongly Connected Components in a directed graph.
     *
     * @return List of SCCs, where each SCC is represented by a list of vertex IDs.
     */
    public List<List<Integer>> findStronglyConnectedComponents() {
        int[] ids = new int[n];
        int[] low = new int[n];
        boolean[] onStack = new boolean[n];
        Deque<Integer> stack = new ArrayDeque<>();
        Arrays.fill(ids, -1);

        List<List<Integer>> sccs = new ArrayList<>();
        int[] timer = new int[]{0};

        for (int i = 0; i < n; i++) {
            if (ids[i] == -1) {
                dfsScc(i, ids, low, onStack, stack, timer, sccs);
            }
        }
        return sccs;
    }

    private void dfsScc(int u, int[] ids, int[] low, boolean[] onStack,
                        Deque<Integer> stack, int[] timer, List<List<Integer>> sccs) {
        ids[u] = low[u] = timer[0]++;
        stack.push(u);
        onStack[u] = true;

        for (int v : directedAdj.get(u)) {
            if (ids[v] == -1) {
                dfsScc(v, ids, low, onStack, stack, timer, sccs);
                low[u] = Math.min(low[u], low[v]);
            } else if (onStack[v]) {
                low[u] = Math.min(low[u], ids[v]);
            }
        }

        // If u is a root node, pop the stack and generate an SCC
        if (ids[u] == low[u]) {
            List<Integer> scc = new ArrayList<>();
            while (true) {
                int node = stack.pop();
                onStack[node] = false;
                scc.add(node);
                if (node == u) break;
            }
            sccs.add(scc);
        }
    }

    /**
     * Identifies all critical bridges in an undirected graph.
     */
    public List<Bridge> findBridges() {
        int[] ids = new int[n];
        int[] low = new int[n];
        Arrays.fill(ids, -1);

        List<Bridge> bridges = new ArrayList<>();
        int[] timer = new int[]{0};

        for (int i = 0; i < n; i++) {
            if (ids[i] == -1) {
                dfsBridges(i, -1, ids, low, timer, bridges);
            }
        }
        return bridges;
    }

    private void dfsBridges(int u, int parentEdgeId, int[] ids, int[] low,
                            int[] timer, List<Bridge> bridges) {
        ids[u] = low[u] = timer[0]++;

        for (Edge edge : undirectedAdj.get(u)) {
            if (edge.edgeId() == parentEdgeId) continue; // Skip edge coming from parent

            int v = edge.to();
            if (ids[v] != -1) {
                // Back edge
                low[u] = Math.min(low[u], ids[v]);
            } else {
                // Forward tree edge
                dfsBridges(v, edge.edgeId(), ids, low, timer, bridges);
                low[u] = Math.min(low[u], low[v]);
                if (low[v] > ids[u]) {
                    bridges.add(new Bridge(u, v));
                }
            }
        }
    }

    private void validateVertex(int v) {
        if (v < 0 || v >= n) {
            throw new IndexOutOfBoundsException("Vertex index out of range [0, " + (n - 1) + "]: " + v);
        }
    }
}
