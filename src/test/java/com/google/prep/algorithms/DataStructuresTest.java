package com.google.prep.algorithms;

import com.google.prep.algorithms.datastructures.LfuCache;
import com.google.prep.algorithms.datastructures.LruCache;
import com.google.prep.algorithms.datastructures.MedianFinder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DataStructuresTest {

    @Test
    @DisplayName("LfuCache: O(1) operations with frequency eviction and LRU tie-breaking")
    void testLfuCache() {
        LfuCache<Integer, String> cache = new LfuCache<>(2);
        cache.put(1, "one");
        cache.put(2, "two");

        assertThat(cache.get(1)).contains("one"); // 1 freq = 2
        cache.put(3, "three"); // 2 is evicted (freq 1, compared to 1 which has freq 2)

        assertThat(cache.get(2)).isEmpty();
        assertThat(cache.get(3)).contains("three");
        assertThat(cache.get(1)).contains("one");
    }

    @Test
    @DisplayName("LruCache: O(1) operations with least recently used eviction")
    void testLruCache() {
        LruCache<Integer, String> cache = new LruCache<>(2);
        cache.put(1, "A");
        cache.put(2, "B");
        assertThat(cache.get(1)).contains("A"); // 1 becomes recently used, 2 becomes LRU

        cache.put(3, "C"); // 2 evicted
        assertThat(cache.get(2)).isEmpty();
        assertThat(cache.get(1)).contains("A");
        assertThat(cache.get(3)).contains("C");
    }

    @Test
    @DisplayName("MedianFinder: Dual heap maintains exact streaming median")
    void testMedianFinder() {
        MedianFinder mf = new MedianFinder();
        mf.addNum(1);
        mf.addNum(2);
        assertThat(mf.findMedian()).isEqualTo(1.5);

        mf.addNum(3);
        assertThat(mf.findMedian()).isEqualTo(2.0);

        mf.addNum(10);
        mf.addNum(5);
        // Sorted: 1, 2, 3, 5, 10 -> Median = 3.0
        assertThat(mf.findMedian()).isEqualTo(3.0);
    }
}
