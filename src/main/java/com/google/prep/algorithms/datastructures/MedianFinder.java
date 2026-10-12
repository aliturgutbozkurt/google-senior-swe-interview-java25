package com.google.prep.algorithms.datastructures;

import java.util.Collections;
import java.util.PriorityQueue;

/**
 * Dual-Heap Streaming Median Finder.
 *
 * <p>Guarantees:
 * <ul>
 *   <li>O(log N) insertion</li>
 *   <li>O(1) median query</li>
 *   <li>Invariant: maxHeap.size() == minHeap.size() OR maxHeap.size() == minHeap.size() + 1</li>
 * </ul>
 */
public final class MedianFinder {

    // Stores smaller half of numbers (max-heap)
    private final PriorityQueue<Integer> maxHeap;

    // Stores larger half of numbers (min-heap)
    private final PriorityQueue<Integer> minHeap;

    public MedianFinder() {
        this.maxHeap = new PriorityQueue<>(Collections.reverseOrder());
        this.minHeap = new PriorityQueue<>();
    }

    public void addNum(int num) {
        if (maxHeap.isEmpty() || num <= maxHeap.peek()) {
            maxHeap.offer(num);
        } else {
            minHeap.offer(num);
        }

        // Rebalance heaps to preserve invariant: 0 <= maxHeap.size() - minHeap.size() <= 1
        if (maxHeap.size() > minHeap.size() + 1) {
            minHeap.offer(maxHeap.poll());
        } else if (minHeap.size() > maxHeap.size()) {
            maxHeap.offer(minHeap.poll());
        }
    }

    public double findMedian() {
        if (maxHeap.isEmpty() && minHeap.isEmpty()) {
            throw new IllegalStateException("Cannot compute median of empty stream");
        }
        if (maxHeap.size() > minHeap.size()) {
            return maxHeap.peek();
        } else {
            return (maxHeap.peek() + (double) minHeap.peek()) / 2.0;
        }
    }

    public int size() {
        return maxHeap.size() + minHeap.size();
    }
}
