package com.google.prep.systemdesign;

import com.google.prep.systemdesign.bloomfilter.ScalableBloomFilter;
import com.google.prep.systemdesign.consistenthash.ConsistentHashRing;
import com.google.prep.systemdesign.lsm.MiniLsmTree;
import com.google.prep.systemdesign.ratelimiter.DistributedRateLimiterSimulator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SystemDesignTest {

    @Test
    @DisplayName("ConsistentHashRing: Virtual nodes balance distribution and provide quorum replicas")
    void testConsistentHashRing() {
        List<String> nodes = List.of("node-1:8080", "node-2:8080", "node-3:8080");
        ConsistentHashRing<String> ring = new ConsistentHashRing<>(100, nodes);

        assertThat(ring.getPhysicalNodeCount()).isEqualTo(3);
        assertThat(ring.getRingSize()).isEqualTo(300);

        var targetNode = ring.getNode("user-session-12345");
        assertThat(targetNode).isPresent();
        assertThat(nodes).contains(targetNode.get());

        // Quorum replica lookup (3 replicas)
        List<String> replicas = ring.getNReplicas("user-session-12345", 3);
        assertThat(replicas).hasSize(3);
        assertThat(replicas).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("ScalableBloomFilter: Zero false negatives and bounded false positive probability")
    void testScalableBloomFilter() {
        ScalableBloomFilter filter = new ScalableBloomFilter(1000, 0.01);

        for (int i = 0; i < 500; i++) {
            filter.put("https://google.com/doc/" + i);
        }

        // Test: Zero False Negatives (Fundamental Bloom Filter Invariant)
        for (int i = 0; i < 500; i++) {
            assertThat(filter.mightContain("https://google.com/doc/" + i)).isTrue();
        }

        // Test non-existent keys
        int falsePositives = 0;
        for (int i = 10000; i < 11000; i++) {
            if (filter.mightContain("https://google.com/nonexistent/" + i)) {
                falsePositives++;
            }
        }
        // False positive rate should be <= 3% given 1% target on 1000 trials
        assertThat(falsePositives).isLessThan(30);
    }

    @Test
    @DisplayName("MiniLsmTree: MemTable flush, SSTable Bloom filter search, and compaction")
    void testMiniLsmTree() {
        MiniLsmTree lsm = new MiniLsmTree(3); // Threshold 3 triggers flush

        lsm.put("user:101", "Alice");
        lsm.put("user:102", "Bob");
        assertThat(lsm.getMemTableSize()).isEqualTo(2);
        assertThat(lsm.getSsTableCount()).isEqualTo(0);

        // 3rd write triggers MemTable flush to SSTable
        lsm.put("user:103", "Charlie");
        assertThat(lsm.getSsTableCount()).isEqualTo(1);
        assertThat(lsm.getMemTableSize()).isEqualTo(0);

        assertThat(lsm.get("user:101")).contains("Alice");
        assertThat(lsm.get("user:103")).contains("Charlie");

        // Tombstone deletion
        lsm.delete("user:101");
        assertThat(lsm.get("user:101")).isEmpty();

        // More writes to trigger second SSTable
        lsm.put("user:104", "Dave");
        lsm.put("user:105", "Eve");
        assertThat(lsm.getSsTableCount()).isEqualTo(2);

        // Major compaction consolidates all SSTables into 1 and purges tombstones
        lsm.compact();
        assertThat(lsm.getSsTableCount()).isEqualTo(1);
        assertThat(lsm.get("user:101")).isEmpty();
        assertThat(lsm.get("user:102")).contains("Bob");
    }

    @Test
    @DisplayName("DistributedRateLimiterSimulator: Edge proxy quota leasing and partition fail-open")
    void testDistributedRateLimiterSimulator() {
        DistributedRateLimiterSimulator simulator = new DistributedRateLimiterSimulator(100);

        var proxyUsEast = simulator.registerProxy("us-east", 10);
        var proxyEuWest = simulator.registerProxy("eu-west", 10);

        // Proxies handle requests successfully
        for (int i = 0; i < 5; i++) {
            assertThat(proxyUsEast.handleRequest()).isTrue();
            assertThat(proxyEuWest.handleRequest()).isTrue();
        }

        // Simulate WAN partition: proxy loses connectivity to central quota server
        proxyUsEast.setCentralReachable(false);
        // Proxy uses local fail-open degraded quota without dropping all traffic
        assertThat(proxyUsEast.handleRequest()).isTrue();
    }
}
