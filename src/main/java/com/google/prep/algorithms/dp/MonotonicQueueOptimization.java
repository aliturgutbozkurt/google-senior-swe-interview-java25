package com.google.prep.algorithms.dp;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Monotonic Queue & Deque Optimizations:
 * 1. Sliding Window Maximum in O(N) amortized time.
 * 2. Constrained Subsequence Sum DP in O(N) amortized time.
 * 3. Largest Rectangle in Histogram via Monotonic Stack in O(N).
 */
public final class MonotonicQueueOptimization {

    /**
     * Sliding Window Maximum: Computes maximum of each sliding window of size k.
     * Amortized O(N) time and O(k) auxiliary space.
     */
    public static int[] maxSlidingWindow(int[] nums, int k) {
        if (nums == null || nums.length == 0 || k <= 0) return new int[0];
        int n = nums.length;
        if (k > n) k = n;

        int[] result = new int[n - k + 1];
        int resIdx = 0;
        // Store indices whose values are strictly decreasing
        Deque<Integer> deque = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            // 1. Evict elements outside of window [i - k + 1, i]
            while (!deque.isEmpty() && deque.peekFirst() < i - k + 1) {
                deque.pollFirst();
            }

            // 2. Maintain monotonic decreasing property
            while (!deque.isEmpty() && nums[deque.peekLast()] <= nums[i]) {
                deque.pollLast();
            }

            deque.offerLast(i);

            // 3. Record window maximum once initial window is satisfied
            if (i >= k - 1) {
                result[resIdx++] = nums[deque.peekFirst()];
            }
        }
        return result;
    }

    /**
     * Constrained Subsequence Sum:
     * Find max sum of non-empty subsequence such that for any two consecutive elements
     * nums[i] and nums[j] (i < j), j - i <= k.
     *
     * <p>Recurrence: dp[i] = nums[i] + max(0, max_{j in [i-k, i-1]} dp[j])
     * Optimized from O(N * k) to O(N) using a monotonic deque.
     */
    public static int constrainedSubsetSum(int[] nums, int k) {
        if (nums == null || nums.length == 0 || k <= 0) return 0;
        int n = nums.length;

        int[] dp = new int[n];
        Deque<Integer> deque = new ArrayDeque<>();
        int maxSum = Integer.MIN_VALUE;

        for (int i = 0; i < n; i++) {
            // Evict expired indices outside of [i - k, i - 1]
            while (!deque.isEmpty() && deque.peekFirst() < i - k) {
                deque.pollFirst();
            }

            int bestPrev = deque.isEmpty() ? 0 : Math.max(0, dp[deque.peekFirst()]);
            dp[i] = nums[i] + bestPrev;
            maxSum = Math.max(maxSum, dp[i]);

            // Maintain decreasing monotonic deque of dp values
            while (!deque.isEmpty() && dp[deque.peekLast()] <= dp[i]) {
                deque.pollLast();
            }
            deque.offerLast(i);
        }
        return maxSum;
    }

    /**
     * Largest Rectangle in Histogram: Monotonic increasing stack in O(N) time and space.
     */
    public static int largestRectangleArea(int[] heights) {
        if (heights == null || heights.length == 0) return 0;
        int n = heights.length;
        Deque<Integer> stack = new ArrayDeque<>();
        int maxArea = 0;

        for (int i = 0; i <= n; i++) {
            int h = (i == n) ? 0 : heights[i];
            while (!stack.isEmpty() && h < heights[stack.peek()]) {
                int height = heights[stack.pop()];
                int width = stack.isEmpty() ? i : (i - stack.peek() - 1);
                maxArea = Math.max(maxArea, height * width);
            }
            stack.push(i);
        }
        return maxArea;
    }
}
