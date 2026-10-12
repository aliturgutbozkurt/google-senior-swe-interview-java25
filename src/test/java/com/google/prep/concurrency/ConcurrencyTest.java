package com.google.prep.concurrency;

import com.google.prep.concurrency.lockfree.MichaelScottQueue;
import com.google.prep.concurrency.lockfree.TreiberStack;
import com.google.prep.concurrency.ratelimiter.LeakyBucketRateLimiter;
import com.google.prep.concurrency.ratelimiter.SlidingWindowRateLimiter;
import com.google.prep.concurrency.ratelimiter.TokenBucketRateLimiter;
import com.google.prep.concurrency.synchronization.BoundedBlockingQueue;
import com.google.prep.concurrency.synchronization.FairReadWriteLock;
import com.google.prep.concurrency.virtualthreads.ScopedValueService;
import com.google.prep.concurrency.virtualthreads.StructuredTaskScopeDemo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class ConcurrencyTest {

    @Test
    @DisplayName("TreiberStack: Concurrent multi-threaded push and pop stress test")
    void testTreiberStackConcurrent() throws InterruptedException {
        TreiberStack<Integer> stack = new TreiberStack<>();
        int threads = 8;
        int opsPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            executor.submit(() -> {
                try {
                    for (int i = 0; i < opsPerThread; i++) {
                        stack.push(i);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        assertThat(stack.size()).isEqualTo(threads * opsPerThread);

        AtomicInteger poppedCount = new AtomicInteger(0);
        CountDownLatch popLatch = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            executor.submit(() -> {
                try {
                    for (int i = 0; i < opsPerThread; i++) {
                        if (stack.pop().isPresent()) {
                            poppedCount.incrementAndGet();
                        }
                    }
                } finally {
                    popLatch.countDown();
                }
            });
        }

        popLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(poppedCount.get()).isEqualTo(threads * opsPerThread);
        assertThat(stack.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("MichaelScottQueue: Non-blocking FIFO queue concurrent stress test")
    void testMichaelScottQueue() throws InterruptedException {
        MichaelScottQueue<Integer> queue = new MichaelScottQueue<>();
        int numThreads = 6;
        int itemsPerThread = 500;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        for (int t = 0; t < numThreads; t++) {
            final int base = t * itemsPerThread;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < itemsPerThread; i++) {
                        queue.enqueue(base + i);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        assertThat(queue.size()).isEqualTo(numThreads * itemsPerThread);

        AtomicInteger consumed = new AtomicInteger(0);
        CountDownLatch consumeLatch = new CountDownLatch(numThreads);

        for (int t = 0; t < numThreads; t++) {
            executor.submit(() -> {
                try {
                    for (int i = 0; i < itemsPerThread; i++) {
                        if (queue.dequeue().isPresent()) {
                            consumed.incrementAndGet();
                        }
                    }
                } finally {
                    consumeLatch.countDown();
                }
            });
        }

        consumeLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(consumed.get()).isEqualTo(numThreads * itemsPerThread);
        assertThat(queue.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("TokenBucketRateLimiter: Burst capacity and token replenishment")
    void testTokenBucketRateLimiter() throws InterruptedException {
        // Capacity 5, refill 10 per sec
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(5.0, 10.0);

        // Can acquire burst of 5 immediately
        for (int i = 0; i < 5; i++) {
            assertThat(limiter.tryAcquire()).isTrue();
        }
        // 6th fails
        assertThat(limiter.tryAcquire()).isFalse();

        // Wait 250ms -> refills ~2.5 tokens
        Thread.sleep(250);
        assertThat(limiter.tryAcquire()).isTrue();
        assertThat(limiter.tryAcquire()).isTrue();
    }

    @Test
    @DisplayName("LeakyBucketRateLimiter: Constant outflow rate limiting")
    void testLeakyBucketRateLimiter() {
        LeakyBucketRateLimiter limiter = new LeakyBucketRateLimiter(3.0, 1.0);
        assertThat(limiter.tryAcquire(2.0)).isTrue();
        assertThat(limiter.tryAcquire(1.0)).isTrue();
        assertThat(limiter.tryAcquire(1.0)).isFalse(); // Capacity 3 exceeded
    }

    @Test
    @DisplayName("SlidingWindowRateLimiter: Sub-window sliding rate enforcement")
    void testSlidingWindowRateLimiter() {
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(1000, 10, 3);
        assertThat(limiter.tryAcquire()).isTrue();
        assertThat(limiter.tryAcquire()).isTrue();
        assertThat(limiter.tryAcquire()).isTrue();
        assertThat(limiter.tryAcquire()).isFalse();
    }

    @Test
    @DisplayName("FairReadWriteLock: Readers share lock, writers have mutual exclusion")
    void testFairReadWriteLock() throws InterruptedException {
        FairReadWriteLock rwLock = new FairReadWriteLock();
        rwLock.lockRead();
        rwLock.lockRead(); // Concurrent readers allowed
        rwLock.unlockRead();
        rwLock.unlockRead();

        rwLock.lockWrite();
        rwLock.unlockWrite();
    }

    @Test
    @DisplayName("BoundedBlockingQueue: Thread-safe producer-consumer blocking")
    void testBoundedBlockingQueue() throws InterruptedException {
        BoundedBlockingQueue<String> queue = new BoundedBlockingQueue<>(2);
        queue.put("item1");
        queue.put("item2");
        assertThat(queue.size()).isEqualTo(2);

        assertThat(queue.take()).isEqualTo("item1");
        queue.put("item3");
        assertThat(queue.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("StructuredTaskScopeDemo: Fan-out join and speculative racing in Java 25")
    void testStructuredTaskScope() throws Throwable {
        StructuredTaskScopeDemo demo = new StructuredTaskScopeDemo();
        var userData = demo.fetchUserData("user-42", Duration.ofSeconds(2));
        assertThat(userData.profile().name()).isEqualTo("Alice Smith");
        assertThat(userData.orders().orders()).contains("ORD-101", "ORD-102");

        List<Callable<String>> replicas = List.of(
                () -> { Thread.sleep(150); return "Replica-Slow"; },
                () -> { Thread.sleep(20); return "Replica-Fast"; }
        );
        String fastest = demo.raceReplicas(replicas);
        assertThat(fastest).isEqualTo("Replica-Fast");
    }

    @Test
    @DisplayName("ScopedValueService: Immutable dynamic context propagation in Java 25")
    void testScopedValue() throws Throwable {
        ScopedValueService service = new ScopedValueService();
        var ctx = new ScopedValueService.SecurityContext("trace-999", "tenant-alpha", "admin");

        String result = service.runWithContext(ctx, () -> {
            var activeCtx = service.getContext();
            return activeCtx.tenantId() + ":" + activeCtx.principal();
        });

        assertThat(result).isEqualTo("tenant-alpha:admin");
    }
}
