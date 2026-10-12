# Chapter 3: Modern Java 25 Concurrency, Memory Model & Internals

## 1. Virtual Threads Architecture in Java 25

Prior to Project Loom, Java threads were 1:1 wrappers around Operating System (OS) kernel threads. A 64-bit JVM process was limited to a few thousand threads due to:
1. **Stack Memory**: OS threads allocate a fixed 1MB virtual memory stack (`-Xss`).
2. **Context Switching Overhead**: Switching between OS kernel threads incurs TLB invalidations and ring-3 to ring-0 transitions ($\approx 1 - 2 \mu\text{s}$).

```
[Virtual Threads (Millions)]
  VT 1   VT 2   VT 3   VT 4   VT 5 ... VT 1,000,000
    \      /      |      |      /
     \    /       |      |     /
    [ForkJoinPool Carrier Threads (Equal to CPU Cores)]
       Carrier 1      Carrier 2      Carrier 3
           |              |              |
    [OS Kernel Thread 1] [OS Kernel Thread 2] [OS Kernel Thread 3]
```

### 1.1 How Virtual Threads Work
* Virtual threads are managed entirely in user-space by the JVM.
* Their stack frames are allocated on the Java heap as **Continuation objects** and grow/shrink dynamically (starting as small as a few hundred bytes).
* When a virtual thread performs a blocking I/O operation (e.g. Socket read, HTTP call, `LockSupport.park()`), the underlying continuation is unmounted from its carrier thread. The carrier thread immediately executes another runnable virtual thread.
* When the I/O completion event fires, the continuation is re-scheduled on any available carrier thread.

### 1.2 Thread Pinning & Critical Guidelines
A virtual thread is **pinned** to its carrier thread if it blocks inside:
* A `synchronized` block/method (resolved in modern JDKs, but best practice is to prefer `ReentrantLock`).
* A native JNI call.
Pinning prevents the carrier thread from executing other virtual threads. Always run JVM with `-Djdk.tracePinnedThreads=full` during stress testing.

---

## 2. Java 25 Structured Concurrency (`StructuredTaskScope`)

Asynchronous code written with raw `Thread`, `ExecutorService`, or `CompletableFuture` frequently suffers from:
* **Thread Leaks**: If an operation times out, orphaned background worker threads continue running and burning CPU/IO.
* **Loss of Observability**: Stack traces in thread dumps are disconnected between parent and child tasks.

Java 25 introduces `StructuredTaskScope` to restore **lexical scope containment**:

```java
try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.awaitAllSuccessfulOrThrow())) {
    var profileTask = scope.fork(() -> fetchProfile(userId));
    var ordersTask  = scope.fork(() -> fetchOrders(userId));

    scope.join(); // Blocks until all finish or any throws
    return new UserData(profileTask.get(), ordersTask.get());
} // Child tasks are guaranteed terminated or cancelled upon exiting try block
```

### Speculative Execution (Hedged Requests)
In distributed systems, the 99th percentile (p99) latency can be 10x the median due to garbage collection pauses or network jitter. Google uses hedged requests to race multiple replicas:

```java
try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.<String>anySuccessfulResultOrThrow())) {
    scope.fork(() -> callReplica("us-east"));
    scope.fork(() -> callReplica("us-central"));
    scope.fork(() -> callReplica("us-west"));

    return (String) scope.join(); // Returns first successful replica response; cancels the rest!
}
```

---

## 3. Scoped Values (`ScopedValue`) vs `ThreadLocal`

| Feature | `ThreadLocal<T>` | `ScopedValue<T>` (Java 25) |
| :--- | :--- | :--- |
| **Mutability** | Mutable (`set()` can be called anywhere). | Strictly immutable within scope. |
| **Lifetime** | Unbounded (must remember to call `remove()`). | Strictly bounded to execution of lambda / scope. |
| **Virtual Thread Scalability** | Heavy (each virtual thread replicates Map entry). | Lightweight (child tasks inherit via parent pointers in $O(1)$). |
| **Memory Leak Risk** | High (static maps retain references in thread pools). | Zero (garbage collected automatically when scope exits). |

```java
public static final ScopedValue<SecurityContext> CTX = ScopedValue.newInstance();

ScopedValue.where(CTX, securityContext).run(() -> {
    // Child calls can safely inspect CTX.get()
    processTransaction();
});
```

---

## 4. Lock-Free Data Structures & The Java Memory Model (JMM)

### 4.1 The Treiber Lock-Free Stack
A LIFO stack implemented with a single `AtomicReference<Node<T>> head`:

```
head ---> [Node A] ---> [Node B] ---> null
```

* **Push**: Prepare new node with `node.next = head.get()`. Attempt CAS: `head.compareAndSet(oldHead, node)`. On collision, spin wait and retry.
* **Pop**: Fetch `head`. Attempt CAS: `head.compareAndSet(oldHead, oldHead.next)`.
* **Lock-Free Guarantee**: At least one concurrent thread makes forward progress on every CAS collision.

### 4.2 Michael-Scott Lock-Free Queue
A FIFO queue requires distinct pointers for both `head` (dequeue) and `tail` (enqueue):
* Uses a **dummy sentinel node** so `head` and `tail` never point to `null`.
* Enqueue requires a 2-step CAS:
  1. Swing `tail.next` from `null` to `newNode`.
  2. Swing `tail` to `newNode`.
* **Cooperation**: If a thread notices `tail.next != null`, it assists by advancing `tail` before continuing its own enqueue.

### 4.3 Java Memory Model (JMM) & Happens-Before
1. **Volatile Semantics**:
   * A write to a `volatile` variable *happens-before* every subsequent read of that same variable.
   * Volatile writes insert a StoreLoad barrier, flushing the CPU store buffer and preventing reordering across memory fences.
2. **Cache Line False Sharing**:
   * Modern CPU architectures load data into L1/L2 cache in **64-byte cache lines**.
   * If two independent variables accessed by different CPU cores reside on the same 64-byte line, core cache invalidations occur continuously, degrading throughput by 90%+.
   * Remedy: Padding with `@jdk.internal.vm.annotation.Contended` or explicit dummy fields.
