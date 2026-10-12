package com.google.prep.algorithms.graph;

import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * An Indexed Priority Queue (Indexed Binary Min-Heap) supporting O(log N) decrease-key,
 * delete, and update operations, and O(1) key lookups.
 *
 * <p>Essential for Google L5/L6 graph rounds to avoid O(E log E) stale-node allocations in Dijkstra.
 *
 * @param <T> Key type implementing {@link Comparable}
 */
public final class IndexedPriorityQueue<T extends Comparable<T>> {

    private final int capacity;
    private int size;

    // Binary heap containing vertex indices (0-indexed)
    private final int[] heap;

    // Inverse mapping: pos[i] = index of vertex i in the binary heap array
    // -1 if vertex i is not in the heap
    private final int[] pos;

    // Actual keys (e.g. shortest distances) associated with vertex i
    private final Object[] keys;

    public IndexedPriorityQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive: " + capacity);
        }
        this.capacity = capacity;
        this.size = 0;
        this.heap = new int[capacity];
        this.pos = new int[capacity];
        this.keys = new Object[capacity];
        Arrays.fill(pos, -1);
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean contains(int index) {
        validateIndex(index);
        return pos[index] != -1;
    }

    public void insert(int index, T key) {
        validateIndex(index);
        if (contains(index)) {
            throw new IllegalArgumentException("Index already exists in IPQ: " + index);
        }
        if (key == null) {
            throw new NullPointerException("Key cannot be null");
        }

        int p = size;
        heap[p] = index;
        pos[index] = p;
        keys[index] = key;
        swim(p);
        size++;
    }

    public int minIndex() {
        if (isEmpty()) throw new NoSuchElementException("Priority queue underflow");
        return heap[0];
    }

    @SuppressWarnings("unchecked")
    public T minKey() {
        if (isEmpty()) throw new NoSuchElementException("Priority queue underflow");
        return (T) keys[heap[0]];
    }

    public int pollMinIndex() {
        if (isEmpty()) throw new NoSuchElementException("Priority queue underflow");
        int minIdx = heap[0];
        swap(0, --size);
        sink(0);
        pos[minIdx] = -1;
        keys[minIdx] = null;
        return minIdx;
    }

    @SuppressWarnings("unchecked")
    public T keyOf(int index) {
        validateIndex(index);
        if (!contains(index)) throw new NoSuchElementException("Index " + index + " not in IPQ");
        return (T) keys[index];
    }

    public void decreaseKey(int index, T newKey) {
        validateIndex(index);
        if (!contains(index)) throw new NoSuchElementException("Index " + index + " not in IPQ");
        @SuppressWarnings("unchecked")
        T currentKey = (T) keys[index];
        if (newKey.compareTo(currentKey) >= 0) {
            throw new IllegalArgumentException("New key (" + newKey + ") is not strictly smaller than current key (" + currentKey + ")");
        }
        keys[index] = newKey;
        swim(pos[index]);
    }

    public void updateKey(int index, T newKey) {
        validateIndex(index);
        if (!contains(index)) throw new NoSuchElementException("Index " + index + " not in IPQ");
        keys[index] = newKey;
        swim(pos[index]);
        sink(pos[index]);
    }

    private void swim(int p) {
        while (p > 0) {
            int parent = (p - 1) >>> 1;
            if (compareKeys(heap[p], heap[parent]) < 0) {
                swap(p, parent);
                p = parent;
            } else {
                break;
            }
        }
    }

    private void sink(int p) {
        while ((p << 1) + 1 < size) {
            int left = (p << 1) + 1;
            int right = left + 1;
            int smallest = left;

            if (right < size && compareKeys(heap[right], heap[left]) < 0) {
                smallest = right;
            }
            if (compareKeys(heap[smallest], heap[p]) < 0) {
                swap(p, smallest);
                p = smallest;
            } else {
                break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private int compareKeys(int idx1, int idx2) {
        return ((T) keys[idx1]).compareTo((T) keys[idx2]);
    }

    private void swap(int p1, int p2) {
        int idx1 = heap[p1];
        int idx2 = heap[p2];

        heap[p1] = idx2;
        heap[p2] = idx1;

        pos[idx1] = p2;
        pos[idx2] = p1;
    }

    private void validateIndex(int index) {
        if (index < 0 || index >= capacity) {
            throw new IndexOutOfBoundsException("Index " + index + " out of capacity bounds [0, " + (capacity - 1) + "]");
        }
    }
}
