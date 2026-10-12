package com.google.prep.algorithms.trees;

/**
 * Production-grade Fenwick Tree (Binary Indexed Tree) providing:
 * 1. 1D Point Update and Range Sum Queries in O(log N).
 * 2. 2D Point Update and Range Sum Queries in O(log N * log M).
 *
 * <p>Memory Footprint: Minimal flat primitive array with zero pointer indirection.
 */
public final class FenwickTree {

    private final int n;
    private final long[] tree;

    public FenwickTree(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Size must be positive: " + n);
        }
        this.n = n;
        this.tree = new long[n + 1]; // 1-indexed internally
    }

    public FenwickTree(long[] initial) {
        this(initial.length);
        for (int i = 0; i < initial.length; i++) {
            add(i, initial[i]);
        }
    }

    /**
     * Adds {@code delta} to 0-indexed position {@code index}.
     */
    public void add(int index, long delta) {
        validateIndex(index);
        for (int i = index + 1; i <= n; i += (i & -i)) {
            tree[i] += delta;
        }
    }

    /**
     * Computes prefix sum in range [0, index].
     */
    public long prefixSum(int index) {
        if (index < 0) return 0;
        if (index >= n) index = n - 1;
        long sum = 0;
        for (int i = index + 1; i > 0; i -= (i & -i)) {
            sum += tree[i];
        }
        return sum;
    }

    /**
     * Computes range sum in [left, right] inclusive.
     */
    public long rangeSum(int left, int right) {
        if (left < 0 || right >= n || left > right) {
            throw new IndexOutOfBoundsException("Invalid range [" + left + ", " + right + "] for size " + n);
        }
        return prefixSum(right) - prefixSum(left - 1);
    }

    private void validateIndex(int index) {
        if (index < 0 || index >= n) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds [0, " + (n - 1) + "]");
        }
    }

    /**
     * 2D Fenwick Tree for matrix prefix and 2D bounding box queries.
     */
    public static final class FenwickTree2D {
        private final int rows;
        private final int cols;
        private final long[][] tree;

        public FenwickTree2D(int rows, int cols) {
            if (rows <= 0 || cols <= 0) {
                throw new IllegalArgumentException("Matrix dimensions must be positive");
            }
            this.rows = rows;
            this.cols = cols;
            this.tree = new long[rows + 1][cols + 1];
        }

        public void add(int r, int c, long delta) {
            for (int i = r + 1; i <= rows; i += (i & -i)) {
                for (int j = c + 1; j <= cols; j += (j & -j)) {
                    tree[i][j] += delta;
                }
            }
        }

        public long prefixSum(int r, int c) {
            if (r < 0 || c < 0) return 0;
            if (r >= rows) r = rows - 1;
            if (c >= cols) c = cols - 1;
            long sum = 0;
            for (int i = r + 1; i > 0; i -= (i & -i)) {
                for (int j = c + 1; j > 0; j -= (j & -j)) {
                    sum += tree[i][j];
                }
            }
            return sum;
        }

        public long rangeSum(int r1, int c1, int r2, int c2) {
            return prefixSum(r2, c2)
                 - prefixSum(r1 - 1, c2)
                 - prefixSum(r2, c1 - 1)
                 + prefixSum(r1 - 1, c1 - 1);
        }
    }
}
