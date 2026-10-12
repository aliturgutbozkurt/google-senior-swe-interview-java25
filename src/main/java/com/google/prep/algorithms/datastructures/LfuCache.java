package com.google.prep.algorithms.datastructures;

import java.util.*;

/**
 * Production-grade Least Frequently Used (LFU) Cache supporting:
 * <ul>
 *   <li>O(1) get(K key)</li>
 *   <li>O(1) put(K key, V value)</li>
 *   <li>Tie-breaking via Least Recently Used (LRU) within the lowest frequency group</li>
 * </ul>
 */
public final class LfuCache<K, V> {

    private static final class Node<K, V> {
        final K key;
        V value;
        int freq;
        Node<K, V> prev;
        Node<K, V> next;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
            this.freq = 1;
        }
    }

    private static final class DoublyLinkedList<K, V> {
        final Node<K, V> head = new Node<>(null, null);
        final Node<K, V> tail = new Node<>(null, null);
        int size = 0;

        DoublyLinkedList() {
            head.next = tail;
            tail.prev = head;
        }

        void addFirst(Node<K, V> node) {
            node.next = head.next;
            node.prev = head;
            head.next.prev = node;
            head.next = node;
            size++;
        }

        void remove(Node<K, V> node) {
            node.prev.next = node.next;
            node.next.prev = node.prev;
            size--;
        }

        Node<K, V> removeLast() {
            if (size == 0) return null;
            Node<K, V> last = tail.prev;
            remove(last);
            return last;
        }

        boolean isEmpty() {
            return size == 0;
        }
    }

    private final int capacity;
    private int minFreq;
    private final Map<K, Node<K, V>> keyToNode;
    private final Map<Integer, DoublyLinkedList<K, V>> freqToList;

    public LfuCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("LFU Cache capacity must be positive: " + capacity);
        }
        this.capacity = capacity;
        this.minFreq = 0;
        this.keyToNode = new HashMap<>();
        this.freqToList = new HashMap<>();
    }

    public int size() {
        return keyToNode.size();
    }

    public Optional<V> get(K key) {
        Node<K, V> node = keyToNode.get(key);
        if (node == null) {
            return Optional.empty();
        }
        promoteFrequency(node);
        return Optional.of(node.value);
    }

    public void put(K key, V value) {
        if (keyToNode.containsKey(key)) {
            Node<K, V> node = keyToNode.get(key);
            node.value = value;
            promoteFrequency(node);
            return;
        }

        // Evict LFU element if at full capacity
        if (keyToNode.size() >= capacity) {
            DoublyLinkedList<K, V> minList = freqToList.get(minFreq);
            Node<K, V> evicted = minList.removeLast();
            if (evicted != null) {
                keyToNode.remove(evicted.key);
            }
        }

        // Insert new entry with frequency 1
        Node<K, V> newNode = new Node<>(key, value);
        keyToNode.put(key, newNode);
        minFreq = 1;
        freqToList.computeIfAbsent(1, k -> new DoublyLinkedList<>()).addFirst(newNode);
    }

    private void promoteFrequency(Node<K, V> node) {
        int oldFreq = node.freq;
        DoublyLinkedList<K, V> oldList = freqToList.get(oldFreq);
        oldList.remove(node);

        if (oldFreq == minFreq && oldList.isEmpty()) {
            minFreq++;
        }

        node.freq++;
        freqToList.computeIfAbsent(node.freq, k -> new DoublyLinkedList<>()).addFirst(node);
    }
}
