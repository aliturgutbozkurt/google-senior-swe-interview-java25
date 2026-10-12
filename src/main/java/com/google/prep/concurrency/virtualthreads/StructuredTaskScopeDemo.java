package com.google.prep.concurrency.virtualthreads;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;

/**
 * Demonstrates Modern Java 25 Structured Concurrency (JEP 480 / JEP 487):
 * <ul>
 *   <li>{@code StructuredTaskScope.open(Joiner.awaitAllSuccessfulOrThrow())}: Fan-out parallel subtasks,
 *       short-circuits and cancels pending subtasks if any fail.</li>
 *   <li>{@code StructuredTaskScope.open(Joiner.anySuccessfulResultOrThrow())}: Hedged requests / Speculative execution
 *       racing multiple replicas and returning the first successful result.</li>
 * </ul>
 *
 * <p>Crucial Google L5/L6 senior topic: Eliminates thread leaks, orphan tasks, and unhandled cancellations.
 */
public final class StructuredTaskScopeDemo {

    public record UserProfile(String userId, String name, int score) {}
    public record OrderHistory(String userId, List<String> orders) {}
    public record AggregatedUserData(UserProfile profile, OrderHistory orders) {}

    /**
     * Fan-out / Fan-in Pattern with Fail-Fast Semantics.
     * Fetches user profile and order history concurrently. If either fails, the other is cancelled.
     */
    public AggregatedUserData fetchUserData(String userId, Duration timeout) throws Throwable {
        try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.awaitAllSuccessfulOrThrow())) {
            StructuredTaskScope.Subtask<UserProfile> profileTask =
                    scope.fork(() -> simulateProfileService(userId));

            StructuredTaskScope.Subtask<OrderHistory> ordersTask =
                    scope.fork(() -> simulateOrdersService(userId));

            // Block until all succeed or first fails
            scope.join();

            return new AggregatedUserData(profileTask.get(), ordersTask.get());
        }
    }

    /**
     * Hedged / Speculative Request Pattern.
     * Races multiple replicas (e.g. 3 regional endpoints) and returns the fastest successful result,
     * immediately cancelling the slower replicas.
     */
    @SuppressWarnings("unchecked")
    public String raceReplicas(List<Callable<String>> replicaCalls) throws Throwable {
        try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.<String>anySuccessfulResultOrThrow())) {
            for (Callable<String> call : replicaCalls) {
                scope.fork(call);
            }
            return (String) scope.join();
        }
    }

    private UserProfile simulateProfileService(String userId) throws InterruptedException {
        Thread.sleep(20);
        return new UserProfile(userId, "Alice Smith", 980);
    }

    private OrderHistory simulateOrdersService(String userId) throws InterruptedException {
        Thread.sleep(30);
        return new OrderHistory(userId, List.of("ORD-101", "ORD-102"));
    }
}
