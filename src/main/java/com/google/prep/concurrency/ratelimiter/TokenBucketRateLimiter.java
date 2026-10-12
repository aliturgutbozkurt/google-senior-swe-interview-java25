package com.google.prep.concurrency.ratelimiter;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Production-grade Lock-Free Token Bucket Rate Limiter.
 *
 * <p>Key Design Considerations for Google Senior Interviews:
 * <ul>
 *   <li>Zero background timer threads: Tokens are replenished lazily on incoming requests based on elapsed nanoseconds.</li>
 *   <li>Immutable record state updated via atomic CAS loop, eliminating lock contention.</li>
 *   <li>Allows burstiness up to capacity, while enforcing average rate.</li>
 * </ul>
 */
public final class TokenBucketRateLimiter {

    public record State(double tokens, long lastRefillNanos) {}

    private final double capacity;
    private final double refillRatePerSec;
    private final AtomicReference<State> state;

    public TokenBucketRateLimiter(double capacity, double refillRatePerSec) {
        if (capacity <= 0 || refillRatePerSec <= 0) {
            throw new IllegalArgumentException("Capacity and refill rate must be strictly positive");
        }
        this.capacity = capacity;
        this.refillRatePerSec = refillRatePerSec;
        this.state = new AtomicReference<>(new State(capacity, System.nanoTime()));
    }

    /**
     * Attempts to acquire {@code tokensRequested} tokens without blocking.
     *
     * @return true if tokens were successfully consumed; false if rate limit exceeded.
     */
    public boolean tryAcquire(double tokensRequested) {
        if (tokensRequested <= 0) {
            throw new IllegalArgumentException("Requested tokens must be positive: " + tokensRequested);
        }

        while (true) {
            State current = state.get();
            long now = System.nanoTime();
            long elapsedNanos = Math.max(0, now - current.lastRefillNanos());
            double newTokens = Math.min(capacity, current.tokens() + (elapsedNanos * refillRatePerSec / 1_000_000_000.0));

            if (newTokens < tokensRequested) {
                return false; // Insufficient tokens
            }

            State nextState = new State(newTokens - tokensRequested, now);
            if (state.compareAndSet(current, nextState)) {
                return true;
            }
            Thread.onSpinWait();
        }
    }

    public boolean tryAcquire() {
        return tryAcquire(1.0);
    }

    public double availableTokens() {
        State current = state.get();
        long now = System.nanoTime();
        long elapsedNanos = Math.max(0, now - current.lastRefillNanos());
        return Math.min(capacity, current.tokens() + (elapsedNanos * refillRatePerSec / 1_000_000_000.0));
    }
}
