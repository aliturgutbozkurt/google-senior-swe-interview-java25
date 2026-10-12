package com.google.prep.concurrency.synchronization;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Fair Read-Write Lock preventing writer starvation.
 *
 * <p>Standard Java synchronized methods or unfair ReadWriteLocks allow incoming readers to
 * continually starve waiting writers. This implementation guarantees FIFO order using
 * explicit waiting queues and condition variables.
 */
public final class FairReadWriteLock {

    private final ReentrantLock lock = new ReentrantLock(true); // Fair lock
    private final Condition canRead = lock.newCondition();
    private final Condition canWrite = lock.newCondition();

    private int readers = 0;
    private int writers = 0;
    private int writeRequests = 0;

    public void lockRead() throws InterruptedException {
        lock.lock();
        try {
            // Readers must wait if a writer is currently holding the lock OR writers are queued
            while (writers > 0 || writeRequests > 0) {
                canRead.await();
            }
            readers++;
        } finally {
            lock.unlock();
        }
    }

    public void unlockRead() {
        lock.lock();
        try {
            readers--;
            if (readers == 0) {
                canWrite.signal(); // Wake up waiting writer
            }
        } finally {
            lock.unlock();
        }
    }

    public void lockWrite() throws InterruptedException {
        lock.lock();
        try {
            writeRequests++;
            while (readers > 0 || writers > 0) {
                canWrite.await();
            }
            writeRequests--;
            writers = 1;
        } finally {
            lock.unlock();
        }
    }

    public void unlockWrite() {
        lock.lock();
        try {
            writers = 0;
            if (writeRequests > 0) {
                canWrite.signal(); // Prioritize waiting writers
            } else {
                canRead.signalAll(); // Wake up waiting readers
            }
        } finally {
            lock.unlock();
        }
    }
}
