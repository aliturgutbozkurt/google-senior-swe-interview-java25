package com.google.prep.algorithms;

import com.google.prep.algorithms.trees.FenwickTree;
import com.google.prep.algorithms.trees.LowestCommonAncestor;
import com.google.prep.algorithms.trees.SegmentTreeWithLazy;
import com.google.prep.algorithms.trees.Trie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TreeAlgorithmsTest {

    @Test
    @DisplayName("SegmentTreeWithLazy: Supports range additions, range sums, and range minimums")
    void testSegmentTree() {
        long[] initial = {1, 3, 5, 7, 9, 11};
        SegmentTreeWithLazy segTree = new SegmentTreeWithLazy(initial);

        assertThat(segTree.querySum(1, 3)).isEqualTo(3 + 5 + 7); // 15
        assertThat(segTree.queryMin(1, 3)).isEqualTo(3);

        // Add 10 to range [1, 4]
        segTree.updateRange(1, 4, 10);
        // Elements now: [1, 13, 15, 17, 19, 11]
        assertThat(segTree.querySum(1, 3)).isEqualTo(13 + 15 + 17); // 45
        assertThat(segTree.queryMin(0, 5)).isEqualTo(1);
    }

    @Test
    @DisplayName("FenwickTree: 1D and 2D range sum operations")
    void testFenwickTree() {
        long[] data = {2, 4, 6, 8, 10};
        FenwickTree bit = new FenwickTree(data);

        assertThat(bit.rangeSum(1, 3)).isEqualTo(4 + 6 + 8); // 18
        bit.add(2, 5); // data[2] becomes 11
        assertThat(bit.rangeSum(1, 3)).isEqualTo(4 + 11 + 8); // 23

        // 2D test
        FenwickTree.FenwickTree2D bit2D = new FenwickTree.FenwickTree2D(3, 3);
        bit2D.add(0, 0, 1);
        bit2D.add(1, 1, 5);
        bit2D.add(2, 2, 9);
        assertThat(bit2D.rangeSum(0, 0, 1, 1)).isEqualTo(6);
        assertThat(bit2D.rangeSum(0, 0, 2, 2)).isEqualTo(15);
    }

    @Test
    @DisplayName("LowestCommonAncestor: Binary lifting finds common ancestor and tree distance")
    void testLca() {
        int n = 8;
        List<List<Integer>> adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());

        // Tree structure:
        //        0
        //      /   \
        //     1     2
        //    / \   / \
        //   3   4 5   6
        //  /
        // 7
        adj.get(0).addAll(List.of(1, 2));
        adj.get(1).addAll(List.of(0, 3, 4));
        adj.get(2).addAll(List.of(0, 5, 6));
        adj.get(3).addAll(List.of(1, 7));
        adj.get(4).add(1);
        adj.get(5).add(2);
        adj.get(6).add(2);
        adj.get(7).add(3);

        LowestCommonAncestor lca = new LowestCommonAncestor(n, adj, 0);

        assertThat(lca.queryLca(7, 4)).isEqualTo(1);
        assertThat(lca.queryLca(7, 5)).isEqualTo(0);
        assertThat(lca.distanceBetween(7, 4)).isEqualTo(3); // 7->3->1->4
    }

    @Test
    @DisplayName("Trie & BitwiseTrie: Prefix search, autocomplete, and Maximum XOR")
    void testTrie() {
        Trie trie = new Trie();
        trie.insert("google");
        trie.insert("golang");
        trie.insert("go");
        trie.insert("goroutine");

        assertThat(trie.search("golang")).isTrue();
        assertThat(trie.search("g")).isFalse();
        assertThat(trie.startsWith("goo")).isTrue();
        assertThat(trie.countPrefix("go")).isEqualTo(4);

        List<String> suggestions = trie.autocomplete("gol", 5);
        assertThat(suggestions).containsExactly("golang");

        // Bitwise Trie Maximum XOR
        Trie.BitwiseTrie bitwiseTrie = new Trie.BitwiseTrie();
        int[] nums = {3, 10, 5, 25, 2, 8};
        for (int x : nums) {
            bitwiseTrie.insert(x);
        }
        int maxXor = 0;
        for (int x : nums) {
            maxXor = Math.max(maxXor, bitwiseTrie.findMaximumXor(x));
        }
        assertThat(maxXor).isEqualTo(5 ^ 25); // 28
    }
}
