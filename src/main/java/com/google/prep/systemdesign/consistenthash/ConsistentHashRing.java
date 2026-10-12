package com.google.prep.systemdesign.consistenthash;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Consistent Hash Ring with Virtual Nodes.
 *
 * <p>Key System Design Invariants (Google Spanner / Dynamo / Cassandra / Memcached):
 * <ul>
 *   <li>Uniform distribution: Each physical node is mapped to {@code virtualNodesCount} positions on a 64-bit hash ring.</li>
 *   <li>Smooth rebalancing: When a node is added or removed, only K/N keys are moved on average.</li>
 *   <li>Lookup time: O(log(N * V)) using TreeMap binary search ceiling entry.</li>
 * </ul>
 *
 * @param <T> Node identifier type (e.g. Server host/port)
 */
public final class ConsistentHashRing<T> {

    private final int virtualNodesCount;
    private final NavigableMap<Long, T> ring = new TreeMap<>();
    private final Set<T> physicalNodes = new HashSet<>();

    public ConsistentHashRing(int virtualNodesCount, Collection<T> initialNodes) {
        if (virtualNodesCount <= 0) {
            throw new IllegalArgumentException("Virtual nodes count must be positive: " + virtualNodesCount);
        }
        this.virtualNodesCount = virtualNodesCount;
        if (initialNodes != null) {
            for (T node : initialNodes) {
                addNode(node);
            }
        }
    }

    public synchronized void addNode(T node) {
        if (node == null || physicalNodes.contains(node)) return;
        physicalNodes.add(node);
        for (int i = 0; i < virtualNodesCount; i++) {
            long hash = hash(node.toString() + "#VN#" + i);
            ring.put(hash, node);
        }
    }

    public synchronized void removeNode(T node) {
        if (node == null || !physicalNodes.contains(node)) return;
        physicalNodes.remove(node);
        for (int i = 0; i < virtualNodesCount; i++) {
            long hash = hash(node.toString() + "#VN#" + i);
            ring.remove(hash);
        }
    }

    /**
     * Resolves the target server responsible for {@code key}.
     */
    public synchronized Optional<T> getNode(String key) {
        if (ring.isEmpty()) {
            return Optional.empty();
        }
        long hash = hash(key);
        Map.Entry<Long, T> entry = ring.ceilingEntry(hash);
        if (entry == null) {
            // Wrap around to start of circle
            entry = ring.firstEntry();
        }
        return Optional.of(entry.getValue());
    }

    /**
     * Returns N distinct physical replicas for a key (used in quorum reads/writes).
     */
    public synchronized List<T> getNReplicas(String key, int numReplicas) {
        if (ring.isEmpty() || numReplicas <= 0) return Collections.emptyList();
        int targetCount = Math.min(numReplicas, physicalNodes.size());
        List<T> replicas = new ArrayList<>(targetCount);
        Set<T> seen = new HashSet<>();

        long hash = hash(key);
        NavigableMap<Long, T> tail = ring.tailMap(hash, true);

        for (T node : tail.values()) {
            if (seen.add(node)) {
                replicas.add(node);
                if (replicas.size() == targetCount) return replicas;
            }
        }

        // Wrap around from beginning of ring
        for (T node : ring.values()) {
            if (seen.add(node)) {
                replicas.add(node);
                if (replicas.size() == targetCount) return replicas;
            }
        }
        return replicas;
    }

    public synchronized int getPhysicalNodeCount() {
        return physicalNodes.size();
    }

    public synchronized int getRingSize() {
        return ring.size();
    }

    private long hash(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(key.getBytes(StandardCharsets.UTF_8));
            // Read 8 bytes as a long
            long h = 0;
            for (int i = 0; i < 8; i++) {
                h = (h << 8) | (bytes[i] & 0xFF);
            }
            return h;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 digest not available", e);
        }
    }
}
