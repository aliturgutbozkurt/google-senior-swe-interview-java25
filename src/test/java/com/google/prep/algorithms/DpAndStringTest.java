package com.google.prep.algorithms;

import com.google.prep.algorithms.dp.BitmaskTsp;
import com.google.prep.algorithms.dp.MonotonicQueueOptimization;
import com.google.prep.algorithms.strings.AhoCorasickAutomaton;
import com.google.prep.algorithms.strings.KnuthMorrisPratt;
import com.google.prep.algorithms.strings.RabinKarpMatcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DpAndStringTest {

    @Test
    @DisplayName("BitmaskTsp: Solves optimal Hamiltonian cycle and tour path")
    void testBitmaskTsp() {
        double[][] dist = {
                {0, 10, 15, 20},
                {10, 0, 35, 25},
                {15, 35, 0, 30},
                {20, 25, 30, 0}
        };

        var result = BitmaskTsp.solveTsp(dist);
        // Optimal tour: 0 -> 1 -> 3 -> 2 -> 0 = 10 + 25 + 30 + 15 = 80
        assertThat(result.minCost()).isEqualTo(80.0);
        assertThat(result.tour()).hasSize(5);
        assertThat(result.tour().get(0)).isEqualTo(0);
        assertThat(result.tour().get(4)).isEqualTo(0);
    }

    @Test
    @DisplayName("MonotonicQueueOptimization: Sliding window max and constrained DP")
    void testMonotonicQueue() {
        int[] nums = {1, 3, -1, -3, 5, 3, 6, 7};
        int[] maxWindow = MonotonicQueueOptimization.maxSlidingWindow(nums, 3);
        assertThat(maxWindow).containsExactly(3, 3, 5, 5, 6, 7);

        int[] subsetNums = {10, 2, -10, 5, 20};
        int maxSubset = MonotonicQueueOptimization.constrainedSubsetSum(subsetNums, 2);
        // Subsequence: 10, 2, 5, 20 = 37
        assertThat(maxSubset).isEqualTo(37);

        int[] heights = {2, 1, 5, 6, 2, 3};
        int maxArea = MonotonicQueueOptimization.largestRectangleArea(heights);
        assertThat(maxArea).isEqualTo(10); // 5 and 6 form 2 * 5 = 10
    }

    @Test
    @DisplayName("KnuthMorrisPratt: Prefix function, pattern search, and period length")
    void testKmp() {
        String text = "ABABDABACDABABCABAB";
        String pattern = "ABABCABAB";

        List<Integer> matches = KnuthMorrisPratt.search(text, pattern);
        assertThat(matches).containsExactly(10);

        assertThat(KnuthMorrisPratt.findShortestPeriod("abcabcabc")).isEqualTo(3);
        assertThat(KnuthMorrisPratt.findShortestPeriod("aaaa")).isEqualTo(1);
    }

    @Test
    @DisplayName("AhoCorasickAutomaton: Multi-pattern simultaneous text search")
    void testAhoCorasick() {
        List<String> keywords = List.of("he", "she", "his", "hers");
        AhoCorasickAutomaton ac = new AhoCorasickAutomaton(keywords);

        var matches = ac.searchInText("ushers");
        // "ushers" contains "she" ending at 3, "he" ending at 3, "hers" ending at 5
        assertThat(matches).extracting(AhoCorasickAutomaton.Match::pattern)
                .contains("she", "he", "hers");
    }

    @Test
    @DisplayName("RabinKarpMatcher: Rolling hash search and longest duplicate substring")
    void testRabinKarp() {
        String text = "googolplexgooglegoogle";
        List<Integer> matches = RabinKarpMatcher.search(text, "google");
        assertThat(matches).containsExactly(10, 16);

        String dup = RabinKarpMatcher.longestDuplicateSubstring("banana");
        assertThat(dup).isEqualTo("ana");
    }
}
