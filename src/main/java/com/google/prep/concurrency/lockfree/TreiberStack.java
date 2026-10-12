package com.google.prep.concurrency.lockfree;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Treiber's Lock-Free Stack (LIFO) using Compare-And-Swap (CAS).
 *
 * <p>Invariants:
 * <ul>
 *   <li>Non-blocking progress: Guarantees system-wide lock-free progress.</li>
 *   <li>Thread-safe push and pop without mutual exclusion locks.</li>
 * </ul>
 *
 * @param <T> Element type
 */
public final class TreiberStack<T> {

    private static final class Node<T> {
        final T value;
        Node<T> next;

        Node(T value) {
            this.value = value;
        }
    }

    private final AtomicReference<Node<T>> head = new AtomicReference<>(null);
    private final AtomicInteger size = new AtomicInteger(0);

    public void push(T value) {
        if (value == null) {
            throw new NullPointerException("Null values not permitted in TreiberStack");
        }
        Node<T> newHead = new Node<>(value);
        while (true) {
            Node<T> currentHead = head.get();
            newHead.next = currentHead;
            if (head.compareAndSet(currentHead, newHead)) {
                size.incrementAndGet();
                return;
            }
            // Thread.onSpinWait() provides CPU hint for hyper-threaded architectures
            Thread.onSpinWait();
        }
    }

    public Optional<T> pop() {
        while (true) {
            Node<T> currentHead = head.get();
            if (currentHead == null) {
                return Optional.empty();
            }
            Node<T> nextHead = currentHead.next;
            if (head.compareAndSet(currentHead, nextHead)) {
                size.decrementAndGet();
                return Optional.of(currentHead.value);
            }
            Thread.onSpinWait();
        }
    }

    public Optional<T> peek() {
        Node<T> current = head.get();
        return (current == null) ? Optional.empty() : Optional.of(current.value);
    }

    public boolean isEmpty() {
        return head.get() == null;
    }

    public int size() {
        return Math.max(0, size.get());
    }
}
