# SPEC-03: Senior Concurrency, Memory Model & Modern Java 25 Architecture

## 1. Scope & Interview Objective
Google L5/L6 coding rounds frequently evaluate concurrency primitives, thread safety, lock contention, memory consistency guarantees (JMM), and high-throughput non-blocking architectures. This specification details the contracts and invariants for modern Java 25 concurrency.

---

## 2. Java 25 Modern Concurrency Specifications

### 2.1 Structured Concurrency (`StructuredTaskScope`)
- **Problem**: Thread leaks, orphan threads, cancelled tasks continuing to consume CPU/IO in asynchronous code (`CompletableFuture` / `ExecutorService`).
- **Invariant**: Subtasks fork within an explicit lexical scope. The enclosing scope blocks at `join()` until all subtasks finish, succeed, or fail. Cancellation cascades deterministically.
- **Patterns**:
  1. `ShutdownOnFailure`: Fan-out queries to multiple microservices; if any critical service fails, cancel all others immediately.
  2. `ShutdownOnSuccess`: Speculative execution (hedged requests) across multiple replicas; return the fastest response and cancel remaining in-flight tasks.

### 2.2 Scoped Values (`ScopedValue`)
- **Problem**: `ThreadLocal` introduces unbounded memory footprint, GC leaks in thread pools, and mutability hazards.
- **Invariant**: `ScopedValue<T>` is strictly immutable and bounded to the execution of a specific lambda or `StructuredTaskScope`. Carrier and virtual threads inherit scoped bindings with near-zero allocation cost.

---

## 3. Lock-Free & Non-Blocking Data Structures

### 3.1 Treiber Lock-Free Stack
- **Problem**: Thread-safe LIFO stack without mutual exclusion locks.
- **Invariants**:
  1. Stack state is represented by a single `AtomicReference<Node<T>> head`.
  2. `push(val)`: Prepare new node with `node.next = head.get()`; attempt CAS `head.compareAndSet(oldHead, node)`. Retry in loop on collision.
  3. `pop()`: Fetch `head`; if null return empty; attempt CAS `head.compareAndSet(oldHead, oldHead.next)`.
  4. Non-blocking Progress: System-wide progress is guaranteed (Lock-Free). At least one thread succeeds in each contention round.

### 3.2 Michael-Scott Lock-Free Queue
- **Problem**: Concurrent FIFO queue supporting concurrent non-blocking enqueues and dequeues.
- **Invariants**:
  1. Guarded with dummy sentinel node to decouple head and tail.
  2. `head` always points to the sentinel or first consumable item.
  3. `tail` points to the last node or second-to-last node (lagging tail).
  4. Enqueue uses 2-step CAS: link node to `tail.next`, then swing `tail` to the new node. If tail is lagging, other threads help swing `tail` forward before attempting their own enqueue.

---

## 4. Production-Grade Rate Limiting Algorithms

### 4.1 Lock-Free Token Bucket
- **Problem**: High-throughput thread-safe rate limiter permitting bursts up to $B$ tokens and sustained rate of $R$ tokens/second without lock contention.
- **Invariants**:
  1. Avoids background timer threads; replenishes tokens on-demand via elapsed time calculation.
  2. State: Packed into `AtomicReference<State>` or atomic primitive fields to prevent race conditions during concurrent `tryAcquire(tokens)`.
  3. Tokens available: $\min(B, \text{current} + \Delta t \times R)$.

### 4.2 Sliding Window Counter Rate Limiter
- **Problem**: Eliminate edge-of-window burst vulnerabilities present in Fixed Window Counter while maintaining lower memory footprint than Sliding Window Log.
- **Invariants**:
  1. Divides current window into sub-bucket segments (e.g., 60 1-second buckets for a 1-minute window).
  2. Thread-safe atomic counter arrays or ring buffers with time-stamped epoch tracking.

---

## 5. Custom Synchronization Primitives

### 5.1 Fair ReadWriteLock
- **Problem**: Prevent writer starvation in read-heavy workloads where readers continuously acquire shared locks.
- **Invariants**:
  1. Read lock allowed only if `writers == 0` AND `writeWaiters == 0`.
  2. Write lock allowed only if `writers == 0` AND `readers == 0`.
  3. Uses explicit `ReentrantLock` with `Condition` queues to enforce FIFO order.

### 5.2 Bounded Blocking Queue
- **Problem**: Thread-safe Producer-Consumer queue with bounded capacity $C$.
- **Invariants**:
  1. Producers wait on `notFull` condition when `size == capacity`.
  2. Consumers wait on `notEmpty` condition when `size == 0`.
  3. Array-based circular ring buffer with separate `head`, `tail`, and `count` pointers.
