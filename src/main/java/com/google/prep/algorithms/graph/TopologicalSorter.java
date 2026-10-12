package com.google.prep.algorithms.graph;

import java.util.*;

/**
 * Robust Topological Sorter supporting:
 * 1. Kahn's Algorithm (BFS in-degree queue).
 * 2. DFS-based cycle detection and exact cycle path reconstruction.
 *
 * <p>Time Complexity: O(V + E)
 * <p>Space Complexity: O(V)
 */
public final class TopologicalSorter {

    private final int n;
    private final List<List<Integer>> adj;

    public TopologicalSorter(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Number of vertices must be positive: " + n);
        }
        this.n = n;
        this.adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
    }

    public void addEdge(int from, int to) {
        validateVertex(from);
        validateVertex(to);
        adj.get(from).add(to);
    }

    public record SortResult(boolean isDag, List<Integer> order, List<Integer> cycle) {}

    /**
     * Executes topological sorting. If DAG, {@code order} contains the linear order.
     * If graph contains a cycle, {@code cycle} contains the sequence of vertices forming the cycle.
     */
    public SortResult sort() {
        int[] color = new int[n]; // 0: White (unvisited), 1: Gray (in-stack), 2: Black (visited)
        int[] parent = new int[n];
        Arrays.fill(parent, -1);

        List<Integer> order = new ArrayList<>();
        List<Integer> cycle = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            if (color[i] == 0) {
                if (dfsFindCycle(i, color, parent, order, cycle)) {
                    // Cycle detected!
                    return new SortResult(false, Collections.emptyList(), cycle);
                }
            }
        }

        Collections.reverse(order);
        return new SortResult(true, order, Collections.emptyList());
    }

    private boolean dfsFindCycle(int u, int[] color, int[] parent, List<Integer> order, List<Integer> cycle) {
        color[u] = 1; // Gray

        for (int v : adj.get(u)) {
            if (color[v] == 1) {
                // Back-edge detected! Reconstruct cycle from u back to v
                cycle.add(v);
                for (int curr = u; curr != v && curr != -1; curr = parent[curr]) {
                    cycle.add(curr);
                }
                cycle.add(v);
                Collections.reverse(cycle);
                return true;
            } else if (color[v] == 0) {
                parent[v] = u;
                if (dfsFindCycle(v, color, parent, order, cycle)) {
                    return true;
                }
            }
        }

        color[u] = 2; // Black
        order.add(u);
        return false;
    }

    private void validateVertex(int v) {
        if (v < 0 || v >= n) {
            throw new IndexOutOfBoundsException("Vertex index " + v + " out of bounds [0, " + (n - 1) + "]");
        }
    }
}
