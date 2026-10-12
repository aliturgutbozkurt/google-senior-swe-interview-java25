package com.google.prep.algorithms.trees;

import java.util.*;

/**
 * Lowest Common Ancestor (LCA) in a tree using Binary Lifting.
 *
 * <p>Guarantees:
 * <ul>
 *   <li>Preprocessing Time: O(N log N)</li>
 *   <li>Query Time: O(log N) per LCA query</li>
 *   <li>Tree Distance Query: O(log N)</li>
 * </ul>
 */
public final class LowestCommonAncestor {

    private final int n;
    private final int logMax;
    private final int[][] up;
    private final int[] depth;

    public LowestCommonAncestor(int n, List<List<Integer>> adj, int root) {
        if (n <= 0) {
            throw new IllegalArgumentException("Tree size must be positive: " + n);
        }
        this.n = n;
        this.logMax = 32 - Integer.numberOfLeadingZeros(n);
        this.up = new int[n][logMax];
        this.depth = new int[n];

        dfsPreprocess(root, root, 0, adj);
    }

    private void dfsPreprocess(int u, int p, int d, List<List<Integer>> adj) {
        depth[u] = d;
        up[u][0] = p;

        for (int k = 1; k < logMax; k++) {
            up[u][k] = up[up[u][k - 1]][k - 1];
        }

        for (int v : adj.get(u)) {
            if (v != p) {
                dfsPreprocess(v, u, d + 1, adj);
            }
        }
    }

    /**
     * Finds the Lowest Common Ancestor of nodes u and v in O(log N).
     */
    public int queryLca(int u, int v) {
        validateNode(u);
        validateNode(v);

        // Bring both nodes to the same depth
        if (depth[u] < depth[v]) {
            int tmp = u; u = v; v = tmp;
        }

        int diff = depth[u] - depth[v];
        for (int k = 0; k < logMax; k++) {
            if (((diff >> k) & 1) == 1) {
                u = up[u][k];
            }
        }

        if (u == v) return u;

        // Lift both simultaneously until their parents match
        for (int k = logMax - 1; k >= 0; k--) {
            if (up[u][k] != up[v][k]) {
                u = up[u][k];
                v = up[v][k];
            }
        }

        return up[u][0];
    }

    /**
     * Computes the number of edges along the unique simple path between u and v.
     */
    public int distanceBetween(int u, int v) {
        int lca = queryLca(u, v);
        return depth[u] + depth[v] - 2 * depth[lca];
    }

    private void validateNode(int node) {
        if (node < 0 || node >= n) {
            throw new IndexOutOfBoundsException("Node " + node + " out of bounds [0, " + (n - 1) + "]");
        }
    }
}
