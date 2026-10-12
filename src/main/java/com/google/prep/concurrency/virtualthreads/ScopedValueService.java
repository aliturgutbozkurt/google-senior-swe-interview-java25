package com.google.prep.concurrency.virtualthreads;

import java.util.concurrent.Callable;

/**
 * Demonstrates Modern Java 25 Scoped Values (JEP 429 / 446 / 481 / 487).
 *
 * <p>Why Google Senior Engineers choose ScopedValue over ThreadLocal:
 * <ul>
 *   <li>Immutable binding: Value cannot be arbitrarily mutated by called methods.</li>
 *   <li>Bounded lifetime: Binding is strictly confined to the dynamic lexical scope.</li>
 *   <li>Thread-shareable: Virtual threads and child subtasks inherit values with O(1) pointer copies,
 *       preventing ThreadLocal map replication and GC memory leaks.</li>
 * </ul>
 */
public final class ScopedValueService {

    public record SecurityContext(String traceId, String tenantId, String principal) {}

    public static final ScopedValue<SecurityContext> CURRENT_CONTEXT = ScopedValue.newInstance();

    /**
     * Executes {@code action} within the bound security context.
     */
    public <T> T runWithContext(SecurityContext context, Callable<T> action) throws Throwable {
        return ScopedValue.where(CURRENT_CONTEXT, context)
                          .call(action::call);
    }

    /**
     * Inspects the current bound context or throws if unbound.
     */
    public SecurityContext getContext() {
        if (!CURRENT_CONTEXT.isBound()) {
            throw new IllegalStateException("SecurityContext is not bound in current thread scope");
        }
        return CURRENT_CONTEXT.get();
    }
}
