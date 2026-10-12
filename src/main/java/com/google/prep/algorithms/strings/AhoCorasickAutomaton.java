package com.google.prep.algorithms.strings;

import java.util.*;

/**
 * Aho-Corasick Multi-Pattern String Matching Automaton.
 *
 * <p>Locates all occurrences of a dictionary of keywords in an input text simultaneously.
 *
 * <p>Complexity:
 * <ul>
 *   <li>Construction: O(Total characters in dictionary * Alphabet size)</li>
 *   <li>Search: O(|Text| + matches)</li>
 * </ul>
 */
public final class AhoCorasickAutomaton {

    public record Match(int endIndex, String pattern) {}

    private static final class Node {
        final Map<Character, Node> next = new HashMap<>();
        Node fail;
        final List<String> output = new ArrayList<>();
    }

    private final Node root;

    public AhoCorasickAutomaton(Collection<String> dictionary) {
        this.root = new Node();
        buildTrie(dictionary);
        buildFailureLinks();
    }

    private void buildTrie(Collection<String> dictionary) {
        for (String pattern : dictionary) {
            if (pattern == null || pattern.isEmpty()) continue;
            Node curr = root;
            for (char c : pattern.toCharArray()) {
                curr = curr.next.computeIfAbsent(c, k -> new Node());
            }
            curr.output.add(pattern);
        }
    }

    private void buildFailureLinks() {
        Deque<Node> queue = new ArrayDeque<>();

        // Level 1 children fail to root
        for (Node child : root.next.values()) {
            child.fail = root;
            queue.offer(child);
        }

        while (!queue.isEmpty()) {
            Node curr = queue.poll();

            for (Map.Entry<Character, Node> entry : curr.next.entrySet()) {
                char ch = entry.getKey();
                Node child = entry.getValue();

                // Find failure link for child
                Node f = curr.fail;
                while (f != null && !f.next.containsKey(ch)) {
                    f = f.fail;
                }
                child.fail = (f != null) ? f.next.get(ch) : root;

                // Inherit output matches from fail node
                if (child.fail != null) {
                    child.output.addAll(child.fail.output);
                }

                queue.offer(child);
            }
        }
    }

    /**
     * Scans {@code text} and returns all pattern matches with their ending index.
     */
    public List<Match> searchInText(String text) {
        if (text == null || text.isEmpty()) return Collections.emptyList();

        List<Match> matches = new ArrayList<>();
        Node curr = root;

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);

            // Follow failure links if character transition does not exist
            while (curr != root && !curr.next.containsKey(ch)) {
                curr = curr.fail;
            }

            curr = curr.next.getOrDefault(ch, root);

            // Report all matching patterns at current state
            for (String pattern : curr.output) {
                matches.add(new Match(i, pattern));
            }
        }
        return matches;
    }
}
