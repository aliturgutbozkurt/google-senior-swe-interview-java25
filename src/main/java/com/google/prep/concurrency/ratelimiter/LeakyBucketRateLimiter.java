package com.google.prep.concurrency.ratelimiter;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Production-grade Leaky Bucket Rate Limiter.
 *
 * <p>Unlike Token Bucket which allows bursts up to capacity, Leaky Bucket enforces
 * a smooth, continuous outflow rate, preventing downstream service overload.
 */
public final class LeakyBucketRateLimiter {

    public record BucketState(double waterLevel, long lastLeakNanos) {}

    private final double capacity;
    private final double leakRatePerSec;
    private final AtomicReference<BucketState> state;

    public LeakyBucketRateLimiter(double capacity, double leakRatePerSec) {
        if (capacity <= 0 || leakRatePerSec <= 0) {
            throw new IllegalArgumentException("Capacity and leak rate must be positive");
        }
        this.capacity = capacity;
        this.leakRatePerSec = leakRatePerSec;
        this.state = new AtomicReference<>(new BucketState(0.0, System.nanoTime()));
    }

    /**
     * Attempts to add {@code volume} of requests to the leaky bucket.
     *
     * @return true if added without overflow; false if bucket is full (rate limit exceeded).
     */
    public boolean tryAcquire(double volume) {
        if (volume <= 0) {
            throw new IllegalArgumentException("Volume must be positive: " + volume);
        }

        while (true) {
            BucketState current = state.get();
            long now = System.nanoTime();
            long elapsedNanos = Math.max(0, now - current.lastLeakNanos());
            double leaked = elapsedNanos * leakRatePerSec / 1_000_000_000.0;
            double currentWater = Math.max(0.0, current.waterLevel() - leaked);

            if (currentWater + volume > capacity) {
                return false; // Bucket overflow
            }

            BucketState next = new BucketState(currentWater + volume, now);
            if (state.compareAndSet(current, next)) {
                return true;
            }
            Thread.onSpinWait();
        }
    }

    public boolean tryAcquire() {
        return tryAcquire(1.0);
    }
}
