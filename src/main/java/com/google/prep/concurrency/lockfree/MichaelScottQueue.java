package com.google.prep.concurrency.lockfree;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Michael-Scott Lock-Free Concurrent FIFO Queue.
 *
 * <p>Core Invariants:
 * <ul>
 *   <li>Sentinel dummy node decouples enqueue (tail) and dequeue (head) operations.</li>
 *   <li>Enqueue performs a 2-step CAS: link new node to tail.next, then swing tail to new node.</li>
 *   <li>Cooperative progress: if an enqueuer notices tail.next is non-null (lagging tail), it assists
 *       by swinging tail forward before proceeding with its own operation.</li>
 * </ul>
 */
public final class MichaelScottQueue<T> {

    private static final class Node<T> {
        final T value;
        final AtomicReference<Node<T>> next;

        Node(T value) {
            this.value = value;
            this.next = new AtomicReference<>(null);
        }
    }

    private final Node<T> sentinel = new Node<>(null);
    private final AtomicReference<Node<T>> head = new AtomicReference<>(sentinel);
    private final AtomicReference<Node<T>> tail = new AtomicReference<>(sentinel);
    private final AtomicInteger size = new AtomicInteger(0);

    public void enqueue(T value) {
        if (value == null) {
            throw new NullPointerException("Null values not permitted in MichaelScottQueue");
        }

        Node<T> newNode = new Node<>(value);
        while (true) {
            Node<T> curTail = tail.get();
            Node<T> tailNext = curTail.next.get();

            // Verify tail hasn't changed
            if (curTail == tail.get()) {
                if (tailNext == null) {
                    // Step 1: Attempt to link new node to the end of the queue
                    if (curTail.next.compareAndSet(null, newNode)) {
                        // Step 2: Attempt to swing tail to the new node (failure is benign)
                        tail.compareAndSet(curTail, newNode);
                        size.incrementAndGet();
                        return;
                    }
                } else {
                    // Tail was lagging behind; help swing it forward
                    tail.compareAndSet(curTail, tailNext);
                }
            }
            Thread.onSpinWait();
        }
    }

    public Optional<T> dequeue() {
        while (true) {
            Node<T> curHead = head.get();
            Node<T> curTail = tail.get();
            Node<T> headNext = curHead.next.get();

            if (curHead == head.get()) {
                if (curHead == curTail) {
                    if (headNext == null) {
                        return Optional.empty(); // Queue is empty
                    }
                    // Tail is lagging behind head; help swing it forward
                    tail.compareAndSet(curTail, headNext);
                } else {
                    if (headNext != null) {
                        T value = headNext.value;
                        // Swing head from dummy node to first real node
                        if (head.compareAndSet(curHead, headNext)) {
                            size.decrementAndGet();
                            return Optional.of(value);
                        }
                    }
                }
            }
            Thread.onSpinWait();
        }
    }

    public boolean isEmpty() {
        return head.get().next.get() == null;
    }

    public int size() {
        return Math.max(0, size.get());
    }
}
