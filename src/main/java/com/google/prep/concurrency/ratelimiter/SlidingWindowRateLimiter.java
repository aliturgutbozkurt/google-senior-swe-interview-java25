package com.google.prep.concurrency.ratelimiter;

import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Sliding Window Counter Rate Limiter.
 *
 * <p>Divides a sliding window into {@code numBuckets} sub-windows to smooth boundary burst spikes
 * while requiring O(B) fixed memory instead of unbounded log size.
 */
public final class SlidingWindowRateLimiter {

    private final long windowMillis;
    private final int numBuckets;
    private final long bucketMillis;
    private final long maxRequests;

    private final AtomicLongArray bucketCounts;
    private final AtomicLongArray bucketTimestamps;
    private final ReentrantLock updateLock = new ReentrantLock();

    public SlidingWindowRateLimiter(long windowMillis, int numBuckets, long maxRequests) {
        if (windowMillis <= 0 || numBuckets <= 0 || maxRequests <= 0) {
            throw new IllegalArgumentException("Parameters must be positive");
        }
        this.windowMillis = windowMillis;
        this.numBuckets = numBuckets;
        this.bucketMillis = windowMillis / numBuckets;
        this.maxRequests = maxRequests;

        this.bucketCounts = new AtomicLongArray(numBuckets);
        this.bucketTimestamps = new AtomicLongArray(numBuckets);
    }

    public boolean tryAcquire() {
        long now = System.currentTimeMillis();
        long currentBucketIndex = (now / bucketMillis) % numBuckets;
        long currentBucketTime = now / bucketMillis;

        updateLock.lock();
        try {
            // Reset expired bucket if this bucket belongs to an earlier cycle
            long recordedTime = bucketTimestamps.get((int) currentBucketIndex);
            if (recordedTime != currentBucketTime) {
                bucketCounts.set((int) currentBucketIndex, 0);
                bucketTimestamps.set((int) currentBucketIndex, currentBucketTime);
            }

            // Calculate total requests in active window
            long total = 0;
            for (int i = 0; i < numBuckets; i++) {
                if (currentBucketTime - bucketTimestamps.get(i) < numBuckets) {
                    total += bucketCounts.get(i);
                }
            }

            if (total < maxRequests) {
                bucketCounts.incrementAndGet((int) currentBucketIndex);
                return true;
            }
            return false;
        } finally {
            updateLock.unlock();
        }
    }
}
