package com.google.prep.systemdesign.bloomfilter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;

/**
 * Scalable Bloom Filter with Optimal Hash Sizing.
 *
 * <p>System Design Context (Google Web Crawler / Bigtable / RocksDB):
 * <ul>
 *   <li>Avoids disk/network lookups for non-existent keys in 99%+ of queries.</li>
 *   <li>Optimal bit array size: m = -(n * ln(p)) / (ln(2)^2)</li>
 *   <li>Optimal hash count: k = (m / n) * ln(2)</li>
 *   <li>Uses Kirsch-Mitzenmacher optimization: generate k hashes from two independent hash values:
 *       g_i(x) = h1(x) + i * h2(x) mod m</li>
 * </ul>
 */
public final class ScalableBloomFilter {

    private final int bitSetSize;
    private final int numHashFunctions;
    private final BitSet bits;
    private int elementCount;

    /**
     * Initializes Bloom Filter with target capacity and acceptable false-positive probability.
     *
     * @param expectedInsertions Maximum expected elements (n)
     * @param falsePositiveRate Acceptable error probability (e.g., 0.01 for 1%)
     */
    public ScalableBloomFilter(int expectedInsertions, double falsePositiveRate) {
        if (expectedInsertions <= 0 || falsePositiveRate <= 0.0 || falsePositiveRate >= 1.0) {
            throw new IllegalArgumentException("Invalid Bloom Filter parameters");
        }

        // m = -(n * ln(p)) / (ln(2)^2)
        this.bitSetSize = (int) Math.ceil(-expectedInsertions * Math.log(falsePositiveRate) / (Math.log(2) * Math.log(2)));
        // k = (m / n) * ln(2)
        this.numHashFunctions = Math.max(1, (int) Math.round((double) bitSetSize / expectedInsertions * Math.log(2)));

        this.bits = new BitSet(bitSetSize);
        this.elementCount = 0;
    }

    public synchronized void put(String item) {
        if (item == null) return;
        long[] hashes = computeTwoHashes(item);
        long h1 = hashes[0];
        long h2 = hashes[1];

        for (int i = 0; i < numHashFunctions; i++) {
            int combinedHash = (int) Math.abs((h1 + i * h2) % bitSetSize);
            bits.set(combinedHash);
        }
        elementCount++;
    }

    public synchronized boolean mightContain(String item) {
        if (item == null) return false;
        long[] hashes = computeTwoHashes(item);
        long h1 = hashes[0];
        long h2 = hashes[1];

        for (int i = 0; i < numHashFunctions; i++) {
            int combinedHash = (int) Math.abs((h1 + i * h2) % bitSetSize);
            if (!bits.get(combinedHash)) {
                return false; // Definitely does not exist
            }
        }
        return true; // Might exist (subject to false positive rate)
    }

    public int getBitSetSize() {
        return bitSetSize;
    }

    public int getNumHashFunctions() {
        return numHashFunctions;
    }

    public int getElementCount() {
        return elementCount;
    }

    private long[] computeTwoHashes(String item) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(item.getBytes(StandardCharsets.UTF_8));

            long h1 = 0;
            for (int i = 0; i < 8; i++) {
                h1 = (h1 << 8) | (digest[i] & 0xFF);
            }
            long h2 = 0;
            for (int i = 8; i < 16; i++) {
                h2 = (h2 << 8) | (digest[i] & 0xFF);
            }
            return new long[]{h1, h2};
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
