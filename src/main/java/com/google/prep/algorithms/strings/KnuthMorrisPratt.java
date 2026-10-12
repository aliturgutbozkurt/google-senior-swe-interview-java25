package com.google.prep.algorithms.strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Knuth-Morris-Pratt (KMP) string matching algorithm.
 *
 * <p>Guarantees:
 * <ul>
 *   <li>Precomputation: O(M) time and space for prefix function pi.</li>
 *   <li>Search: O(N) time with no backtracking on text pointer.</li>
 * </ul>
 */
public final class KnuthMorrisPratt {

    /**
     * Computes prefix function pi for pattern P.
     * pi[i] is the length of the longest proper prefix of P[0..i] that is also a suffix of P[0..i].
     */
    public static int[] computePrefixFunction(String pattern) {
        if (pattern == null || pattern.isEmpty()) return new int[0];
        int m = pattern.length();
        int[] pi = new int[m];

        for (int i = 1; i < m; i++) {
            int j = pi[i - 1];
            while (j > 0 && pattern.charAt(i) != pattern.charAt(j)) {
                j = pi[j - 1];
            }
            if (pattern.charAt(i) == pattern.charAt(j)) {
                j++;
            }
            pi[i] = j;
        }
        return pi;
    }

    /**
     * Finds all start indices where {@code pattern} occurs in {@code text}.
     */
    public static List<Integer> search(String text, String pattern) {
        if (text == null || pattern == null || pattern.isEmpty() || text.length() < pattern.length()) {
            return Collections.emptyList();
        }

        int[] pi = computePrefixFunction(pattern);
        List<Integer> occurrences = new ArrayList<>();

        int n = text.length();
        int m = pattern.length();
        int j = 0; // Number of characters matched in pattern

        for (int i = 0; i < n; i++) {
            while (j > 0 && text.charAt(i) != pattern.charAt(j)) {
                j = pi[j - 1];
            }
            if (text.charAt(i) == pattern.charAt(j)) {
                j++;
            }
            if (j == m) {
                occurrences.add(i - m + 1);
                j = pi[j - 1];
            }
        }
        return occurrences;
    }

    /**
     * Finds the shortest repeating period length of a string.
     * (e.g., "abcabcabc" -> 3)
     */
    public static int findShortestPeriod(String s) {
        if (s == null || s.isEmpty()) return 0;
        int n = s.length();
        int[] pi = computePrefixFunction(s);
        int longestBorder = pi[n - 1];
        int period = n - longestBorder;
        return (n % period == 0) ? period : n;
    }
}
