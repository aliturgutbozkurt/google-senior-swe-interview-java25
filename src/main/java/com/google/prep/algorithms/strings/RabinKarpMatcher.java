package com.google.prep.algorithms.strings;

import java.util.*;

/**
 * Rabin-Karp String Matcher with Double Rolling Hash.
 *
 * <p>Uses two distinct prime moduli (10^9 + 7 and 10^9 + 9) with base 257 to ensure
 * collision resistance without requiring expensive secondary string comparisons.
 *
 * <p>Includes:
 * <ul>
 *   <li>O(N + M) substring search</li>
 *   <li>O(N log N) longest duplicate substring via rolling hash binary search</li>
 * </ul>
 */
public final class RabinKarpMatcher {

    private static final long MOD1 = 1_000_000_007L;
    private static final long MOD2 = 1_000_000_009L;
    private static final long BASE = 257L;

    public record HashPair(long h1, long h2) {}

    public static List<Integer> search(String text, String pattern) {
        if (text == null || pattern == null || pattern.isEmpty() || text.length() < pattern.length()) {
            return Collections.emptyList();
        }

        int n = text.length();
        int m = pattern.length();
        List<Integer> occurrences = new ArrayList<>();

        long targetH1 = 0, targetH2 = 0;
        long windowH1 = 0, windowH2 = 0;
        long power1 = 1, power2 = 1;

        // Compute BASE^(m-1) and initial window / target hashes
        for (int i = 0; i < m; i++) {
            targetH1 = (targetH1 * BASE + pattern.charAt(i)) % MOD1;
            targetH2 = (targetH2 * BASE + pattern.charAt(i)) % MOD2;

            windowH1 = (windowH1 * BASE + text.charAt(i)) % MOD1;
            windowH2 = (windowH2 * BASE + text.charAt(i)) % MOD2;

            if (i < m - 1) {
                power1 = (power1 * BASE) % MOD1;
                power2 = (power2 * BASE) % MOD2;
            }
        }

        for (int i = 0; i <= n - m; i++) {
            if (windowH1 == targetH1 && windowH2 == targetH2) {
                occurrences.add(i);
            }

            if (i < n - m) {
                // Roll hash: subtract outgoing char, shift, add incoming char
                long outChar = text.charAt(i);
                long inChar = text.charAt(i + m);

                windowH1 = ((windowH1 - outChar * power1 % MOD1 + MOD1) * BASE + inChar) % MOD1;
                windowH2 = ((windowH2 - outChar * power2 % MOD2 + MOD2) * BASE + inChar) % MOD2;
            }
        }

        return occurrences;
    }

    /**
     * Finds the longest duplicate substring in O(N log N) using binary search + double rolling hash.
     */
    public static String longestDuplicateSubstring(String s) {
        if (s == null || s.length() <= 1) return "";

        int n = s.length();
        int low = 1, high = n - 1;
        String best = "";

        while (low <= high) {
            int mid = (low + high) >>> 1;
            String dup = findDuplicateOfLength(s, mid);
            if (dup != null) {
                best = dup;
                low = mid + 1; // Try longer length
            } else {
                high = mid - 1;
            }
        }
        return best;
    }

    private static String findDuplicateOfLength(String s, int len) {
        long h1 = 0, h2 = 0;
        long p1 = 1, p2 = 1;

        for (int i = 0; i < len; i++) {
            h1 = (h1 * BASE + s.charAt(i)) % MOD1;
            h2 = (h2 * BASE + s.charAt(i)) % MOD2;
            if (i < len - 1) {
                p1 = (p1 * BASE) % MOD1;
                p2 = (p2 * BASE) % MOD2;
            }
        }

        Set<Long> seen = new HashSet<>();
        seen.add((h1 << 32) | h2);

        for (int i = 1; i <= s.length() - len; i++) {
            long outChar = s.charAt(i - 1);
            long inChar = s.charAt(i + len - 1);

            h1 = ((h1 - outChar * p1 % MOD1 + MOD1) * BASE + inChar) % MOD1;
            h2 = ((h2 - outChar * p2 % MOD2 + MOD2) * BASE + inChar) % MOD2;

            long combined = (h1 << 32) | h2;
            if (!seen.add(combined)) {
                return s.substring(i, i + len);
            }
        }
        return null;
    }
}
