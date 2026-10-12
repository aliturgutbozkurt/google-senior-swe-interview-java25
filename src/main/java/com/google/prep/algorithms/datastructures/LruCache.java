package com.google.prep.algorithms.datastructures;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Production-grade Least Recently Used (LRU) Cache supporting O(1) operations.
 *
 * <p>Implemented using an explicit doubly-linked list and HashMap to demonstrate
 * low-level memory layout and pointer manipulation expected in Google L5/L6 rounds.
 */
public final class LruCache<K, V> {

    private static final class Node<K, V> {
        final K key;
        V value;
        Node<K, V> prev;
        Node<K, V> next;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private final int capacity;
    private final Map<K, Node<K, V>> map;
    private final Node<K, V> head;
    private final Node<K, V> tail;

    public LruCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive: " + capacity);
        }
        this.capacity = capacity;
        this.map = new HashMap<>();
        this.head = new Node<>(null, null);
        this.tail = new Node<>(null, null);
        head.next = tail;
        tail.prev = head;
    }

    public int size() {
        return map.size();
    }

    public Optional<V> get(K key) {
        Node<K, V> node = map.get(key);
        if (node == null) {
            return Optional.empty();
        }
        moveToHead(node);
        return Optional.of(node.value);
    }

    public void put(K key, V value) {
        Node<K, V> existing = map.get(key);
        if (existing != null) {
            existing.value = value;
            moveToHead(existing);
            return;
        }

        if (map.size() >= capacity) {
            Node<K, V> lru = removeTail();
            if (lru != null) {
                map.remove(lru.key);
            }
        }

        Node<K, V> newNode = new Node<>(key, value);
        map.put(key, newNode);
        addFirst(newNode);
    }

    private void addFirst(Node<K, V> node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }

    private void removeNode(Node<K, V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private void moveToHead(Node<K, V> node) {
        removeNode(node);
        addFirst(node);
    }

    private Node<K, V> removeTail() {
        if (tail.prev == head) return null;
        Node<K, V> node = tail.prev;
        removeNode(node);
        return node;
    }
}
