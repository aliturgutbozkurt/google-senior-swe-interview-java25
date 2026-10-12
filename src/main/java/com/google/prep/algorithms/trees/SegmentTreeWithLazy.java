package com.google.prep.algorithms.trees;

/**
 * Production-grade Segment Tree with Lazy Propagation supporting:
 * 1. O(log N) Range Additions: {@code updateRange(left, right, delta)}
 * 2. O(log N) Range Sum Queries: {@code querySum(left, right)}
 * 3. O(log N) Range Min Queries: {@code queryMin(left, right)}
 *
 * <p>Mathematical Invariants:
 * <ul>
 *   <li>Node covering interval [L, R] has sum {@code treeSum[i]} and min {@code treeMin[i]}.</li>
 *   <li>Lazy tags {@code lazy[i]} store deferred values pending push-down to children.</li>
 *   <li>Before querying or updating children, {@code pushDown(i, L, R)} propagates lazy delta.</li>
 *   <li>After updating children, parent aggregates are updated via {@code pullUp(i)}.</li>
 * </ul>
 */
public final class SegmentTreeWithLazy {

    private final int n;
    private final long[] treeSum;
    private final long[] treeMin;
    private final long[] lazy;

    public SegmentTreeWithLazy(long[] initial) {
        if (initial == null || initial.length == 0) {
            throw new IllegalArgumentException("Initial array cannot be null or empty");
        }
        this.n = initial.length;
        this.treeSum = new long[4 * n];
        this.treeMin = new long[4 * n];
        this.lazy = new long[4 * n];
        build(1, 0, n - 1, initial);
    }

    public SegmentTreeWithLazy(int n) {
        this(new long[n]);
    }

    public int size() {
        return n;
    }

    public void updateRange(int ql, int qr, long delta) {
        validateRange(ql, qr);
        update(1, 0, n - 1, ql, qr, delta);
    }

    public long querySum(int ql, int qr) {
        validateRange(ql, qr);
        return querySum(1, 0, n - 1, ql, qr);
    }

    public long queryMin(int ql, int qr) {
        validateRange(ql, qr);
        return queryMin(1, 0, n - 1, ql, qr);
    }

    private void build(int node, int l, int r, long[] initial) {
        if (l == r) {
            treeSum[node] = initial[l];
            treeMin[node] = initial[l];
            return;
        }
        int mid = (l + r) >>> 1;
        int left = node << 1;
        int right = left | 1;

        build(left, l, mid, initial);
        build(right, mid + 1, r, initial);
        pullUp(node);
    }

    private void pullUp(int node) {
        int left = node << 1;
        int right = left | 1;
        treeSum[node] = treeSum[left] + treeSum[right];
        treeMin[node] = Math.min(treeMin[left], treeMin[right]);
    }

    private void applyLazy(int node, int l, int r, long delta) {
        treeSum[node] += delta * (r - l + 1);
        treeMin[node] += delta;
        lazy[node] += delta;
    }

    private void pushDown(int node, int l, int r) {
        if (lazy[node] != 0) {
            int mid = (l + r) >>> 1;
            int left = node << 1;
            int right = left | 1;
            applyLazy(left, l, mid, lazy[node]);
            applyLazy(right, mid + 1, r, lazy[node]);
            lazy[node] = 0;
        }
    }

    private void update(int node, int l, int r, int ql, int qr, long delta) {
        if (ql <= l && r <= qr) {
            applyLazy(node, l, r, delta);
            return;
        }
        pushDown(node, l, r);
        int mid = (l + r) >>> 1;
        int left = node << 1;
        int right = left | 1;

        if (ql <= mid) {
            update(left, l, mid, ql, qr, delta);
        }
        if (qr > mid) {
            update(right, mid + 1, r, ql, qr, delta);
        }
        pullUp(node);
    }

    private long querySum(int node, int l, int r, int ql, int qr) {
        if (ql <= l && r <= qr) {
            return treeSum[node];
        }
        pushDown(node, l, r);
        int mid = (l + r) >>> 1;
        int left = node << 1;
        int right = left | 1;
        long sum = 0;

        if (ql <= mid) {
            sum += querySum(left, l, mid, ql, qr);
        }
        if (qr > mid) {
            sum += querySum(right, mid + 1, r, ql, qr);
        }
        return sum;
    }

    private long queryMin(int node, int l, int r, int ql, int qr) {
        if (ql <= l && r <= qr) {
            return treeMin[node];
        }
        pushDown(node, l, r);
        int mid = (l + r) >>> 1;
        int left = node << 1;
        int right = left | 1;
        long minVal = Long.MAX_VALUE;

        if (ql <= mid) {
            minVal = Math.min(minVal, queryMin(left, l, mid, ql, qr));
        }
        if (qr > mid) {
            minVal = Math.min(minVal, queryMin(right, mid + 1, r, ql, qr));
        }
        return minVal;
    }

    private void validateRange(int ql, int qr) {
        if (ql < 0 || qr >= n || ql > qr) {
            throw new IndexOutOfBoundsException("Invalid range [" + ql + ", " + qr + "] for size " + n);
        }
    }
}
