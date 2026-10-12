package com.google.prep.algorithms.graph;

import java.util.*;

/**
 * Production-grade Shortest Path using Dijkstra's Algorithm with an Indexed Priority Queue.
 *
 * <p>Guarantees:
 * <ul>
 *   <li>Time Complexity: O((V + E) log V) strict upper bound.</li>
 *   <li>Space Complexity: O(V) auxiliary memory — no stale key bloat.</li>
 *   <li>Supports full path reconstruction from source to destination.</li>
 * </ul>
 */
public final class DijkstraShortestPath {

    public record Edge(int to, long weight) {}

    private final int n;
    private final List<List<Edge>> adj;

    public DijkstraShortestPath(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Number of vertices must be positive: " + n);
        }
        this.n = n;
        this.adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
    }

    public void addEdge(int from, int to, long weight) {
        if (weight < 0) {
            throw new IllegalArgumentException("Dijkstra does not support negative weights: " + weight);
        }
        validateVertex(from);
        validateVertex(to);
        adj.get(from).add(new Edge(to, weight));
    }

    public record Result(long[] distances, int[] parents) {
        public boolean hasPathTo(int target) {
            return distances[target] != Long.MAX_VALUE;
        }

        public long distanceTo(int target) {
            return distances[target];
        }

        public List<Integer> pathTo(int target) {
            if (!hasPathTo(target)) return Collections.emptyList();
            List<Integer> path = new ArrayList<>();
            for (int at = target; at != -1; at = parents[at]) {
                path.add(at);
            }
            Collections.reverse(path);
            return path;
        }
    }

    /**
     * Executes Dijkstra from source vertex {@code source}.
     */
    public Result computeShortestPaths(int source) {
        validateVertex(source);

        long[] dist = new long[n];
        int[] parent = new int[n];
        Arrays.fill(dist, Long.MAX_VALUE);
        Arrays.fill(parent, -1);

        IndexedPriorityQueue<Long> ipq = new IndexedPriorityQueue<>(n);

        dist[source] = 0;
        ipq.insert(source, 0L);

        while (!ipq.isEmpty()) {
            int u = ipq.pollMinIndex();
            long d = dist[u];

            for (Edge edge : adj.get(u)) {
                int v = edge.to();
                long newDist = d + edge.weight();

                if (newDist < dist[v]) {
                    dist[v] = newDist;
                    parent[v] = u;

                    if (ipq.contains(v)) {
                        ipq.decreaseKey(v, newDist);
                    } else {
                        ipq.insert(v, newDist);
                    }
                }
            }
        }

        return new Result(dist, parent);
    }

    private void validateVertex(int v) {
        if (v < 0 || v >= n) {
            throw new IndexOutOfBoundsException("Vertex index " + v + " out of bounds [0, " + (n - 1) + "]");
        }
    }
}
