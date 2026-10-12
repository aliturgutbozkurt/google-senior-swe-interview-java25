package com.google.prep.concurrency.synchronization;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe Bounded Blocking Queue (Circular Ring Buffer).
 *
 * <p>Implements the Producer-Consumer pattern using explicit locks and conditions,
 * avoiding object allocations on enqueue/dequeue.
 *
 * @param <T> Element type
 */
public final class BoundedBlockingQueue<T> {

    private final Object[] buffer;
    private final int capacity;
    private int head = 0;
    private int tail = 0;
    private int count = 0;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    public BoundedBlockingQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive: " + capacity);
        }
        this.capacity = capacity;
        this.buffer = new Object[capacity];
    }

    public void put(T item) throws InterruptedException {
        if (item == null) throw new NullPointerException("Null elements not allowed");

        lock.lockInterruptibly();
        try {
            while (count == capacity) {
                notFull.await();
            }
            buffer[tail] = item;
            tail = (tail + 1) % capacity;
            count++;
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public boolean offer(T item, long timeout, TimeUnit unit) throws InterruptedException {
        if (item == null) throw new NullPointerException();

        long nanos = unit.toNanos(timeout);
        lock.lockInterruptibly();
        try {
            while (count == capacity) {
                if (nanos <= 0) return false;
                nanos = notFull.awaitNanos(nanos);
            }
            buffer[tail] = item;
            tail = (tail + 1) % capacity;
            count++;
            notEmpty.signal();
            return true;
        } finally {
            lock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public T take() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (count == 0) {
                notEmpty.await();
            }
            T item = (T) buffer[head];
            buffer[head] = null; // Prevent memory leak
            head = (head + 1) % capacity;
            count--;
            notFull.signal();
            return item;
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }

    public boolean isEmpty() {
        return size() == 0;
    }
}
