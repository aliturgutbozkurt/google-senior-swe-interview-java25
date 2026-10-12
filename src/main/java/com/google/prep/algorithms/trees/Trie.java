package com.google.prep.algorithms.trees;

import java.util.*;

/**
 * Production Trie implementations:
 * 1. Standard Prefix Trie supporting autocomplete and prefix matching.
 * 2. Bitwise 31-bit Trie supporting maximum XOR pair search in O(31) per query.
 */
public final class Trie {

    private static final class Node {
        final Map<Character, Node> children = new HashMap<>();
        boolean isEndOfWord = false;
        int prefixCount = 0;
    }

    private final Node root = new Node();

    public void insert(String word) {
        if (word == null) return;
        Node curr = root;
        curr.prefixCount++;
        for (char c : word.toCharArray()) {
            curr = curr.children.computeIfAbsent(c, k -> new Node());
            curr.prefixCount++;
        }
        curr.isEndOfWord = true;
    }

    public boolean search(String word) {
        if (word == null) return false;
        Node node = findNode(word);
        return node != null && node.isEndOfWord;
    }

    public boolean startsWith(String prefix) {
        if (prefix == null) return false;
        return findNode(prefix) != null;
    }

    public int countPrefix(String prefix) {
        if (prefix == null) return 0;
        Node node = findNode(prefix);
        return node == null ? 0 : node.prefixCount;
    }

    /**
     * Autocomplete: Returns all words currently stored starting with {@code prefix}, bounded by {@code limit}.
     */
    public List<String> autocomplete(String prefix, int limit) {
        if (prefix == null || limit <= 0) return Collections.emptyList();
        Node node = findNode(prefix);
        if (node == null) return Collections.emptyList();

        List<String> results = new ArrayList<>();
        StringBuilder sb = new StringBuilder(prefix);
        dfsCollect(node, sb, results, limit);
        return results;
    }

    private void dfsCollect(Node curr, StringBuilder sb, List<String> results, int limit) {
        if (results.size() >= limit) return;
        if (curr.isEndOfWord) {
            results.add(sb.toString());
        }
        for (Map.Entry<Character, Node> entry : curr.children.entrySet()) {
            sb.append(entry.getKey());
            dfsCollect(entry.getValue(), sb, results, limit);
            sb.deleteCharAt(sb.length() - 1);
            if (results.size() >= limit) break;
        }
    }

    private Node findNode(String str) {
        Node curr = root;
        for (char c : str.toCharArray()) {
            curr = curr.children.get(c);
            if (curr == null) return null;
        }
        return curr;
    }

    /**
     * Bitwise Trie for 31-bit integer maximum XOR queries in O(31) time.
     */
    public static final class BitwiseTrie {
        private static final class BitNode {
            BitNode left;  // 0 bit
            BitNode right; // 1 bit
        }

        private final BitNode root = new BitNode();

        public void insert(int num) {
            BitNode curr = root;
            for (int i = 30; i >= 0; i--) {
                int bit = (num >>> i) & 1;
                if (bit == 0) {
                    if (curr.left == null) curr.left = new BitNode();
                    curr = curr.left;
                } else {
                    if (curr.right == null) curr.right = new BitNode();
                    curr = curr.right;
                }
            }
        }

        /**
         * Finds the maximum XOR possible between {@code num} and any previously inserted integer.
         */
        public int findMaximumXor(int num) {
            BitNode curr = root;
            int maxXor = 0;
            for (int i = 30; i >= 0; i--) {
                int bit = (num >>> i) & 1;
                // Greedily choose opposite bit to maximize XOR
                if (bit == 0) {
                    if (curr.right != null) {
                        maxXor |= (1 << i);
                        curr = curr.right;
                    } else {
                        curr = curr.left;
                    }
                } else {
                    if (curr.left != null) {
                        maxXor |= (1 << i);
                        curr = curr.left;
                    } else {
                        curr = curr.right;
                    }
                }
            }
            return maxXor;
        }
    }
}
