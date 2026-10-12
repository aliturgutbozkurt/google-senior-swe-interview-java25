#!/usr/bin/env python3
import subprocess
import os
import sys

def main():
    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.abspath(os.path.join(script_dir, ".."))
    pdf_dir = os.path.join(project_root, "docs", "pdf")
    os.makedirs(pdf_dir, exist_ok=True)
    
    typst_file = os.path.join(project_root, "docs", "book.typ")
    output_pdf = os.path.join(pdf_dir, "Google_Senior_SWE_Master_Guide.pdf")

    typst_content = """#set document(
  title: "Google Senior Software Engineer (L5/L6) Interview Master Guide",
  author: "Ali Turgut Bozkurt",
  date: auto,
)

#set page(
  paper: "a4",
  margin: (top: 2.5cm, bottom: 2.5cm, left: 2.5cm, right: 2.5cm),
  header: context {
    if here().page() > 1 {
      smallcaps[Google Senior SWE (L5/L6) Interview Master Guide]
      h(1fr)
      [Java 25 & SDD]
    }
  },
  footer: context {
    if here().page() > 1 {
      align(center)[#here().page()]
    }
  }
)

#set text(
  font: "Helvetica",
  size: 10pt,
  lang: "en",
)

#set par(justify: true, leading: 0.65em)

// Cover Page
#align(center + horizon)[
  #block(
    fill: rgb("#1a73e8"),
    inset: 3em,
    radius: 0.8em,
    width: 100%,
    [
      #text(fill: white, size: 24pt, weight: "bold")[
        Google Senior Software Engineer (L5/L6)
      ]
      #v(0.5em)
      #text(fill: rgb("#e8f0fe"), size: 16pt, weight: "medium")[
        Interview Master Guide
      ]
      #v(1em)
      #text(fill: rgb("#f1f3f4"), size: 11pt)[
        Spec-Driven Development • Java 25 • Advanced Algorithms • Google-Scale System Design • Modern Concurrency • Googleyness & Leadership
      ]
    ]
  )

  #v(3em)
  #text(size: 13pt, weight: "bold")[Author: Ali Turgut Bozkurt] \\
  #text(size: 11pt, fill: rgb("#5f6368"))[Curated for Google Technical Interview Candidates (L5 / L6)]
  
  #v(4em)
  #text(size: 10pt, fill: rgb("#70757a"))[Production Grade • Fully Tested • Java 25 LTS Preview]
]

#pagebreak()

// Table of Contents
#outline(
  title: [Table of Contents],
  indent: 1.5em,
  depth: 3
)

#pagebreak()

= Chapter 1: Spec-Driven Development (SDD) & Google Methodology

== 1.1 The Senior Engineering Paradigm Shift
When interviewing for *Senior Software Engineer (L5)* or *Staff Software Engineer (L6)* at Google, the evaluation bar shifts fundamentally:
- At *L3/L4 (Junior/Mid)*, candidates are evaluated on whether they can take a well-specified prompt and write code passing test cases.
- At *L5/L6 (Senior/Staff)*, interviewers deliberately present *underspecified, ambiguous problems*. Writing code immediately without clarifying scope is an instant disqualifier.

Senior engineers drive the interview like a Tech Lead driving an architectural Design Review or RFC:
1. *Scope boundaries and clarify constraints* before touching code.
2. *Formulate formal mathematical specifications* and state invariants.
3. *Derive strict asymptotic budgets* for time and space.
4. *Construct test contracts* covering edge and degenerate scenarios.
5. *Write idiomatic, modular Java 25 code*.
6. *Verify systematically* with pointer trace tables and proactive trade-off analysis.

== 1.2 The 6-Phase SDD Protocol (45-Minute Breakdown)
- *Phase 1: Ambiguity & Scope (Minutes 0 - 5)*: Clarify data dimensions (N), types, mutability, concurrency, and failure semantics.
- *Phase 2: Formal Specification & State Invariants (Minutes 5 - 10)*: Write down interfaces, pre-conditions, and mathematical invariants.
- *Phase 3: Complexity Budget & Test Contract (Minutes 10 - 15)*: Announce target bounds (_O(N log N)_, _O(N)_) and test edge cases.
- *Phase 4: Idiomatic Java 25 Implementation (Minutes 15 - 35)*: Production-grade code with records, pattern matching, and minimal allocations.
- *Phase 5: Pointer Trace Verification (Minutes 35 - 40)*: Step through code with a concrete trace table.
- *Phase 6: Senior Follow-ups & Trade-offs (Minutes 40 - 45)*: Proactively discuss concurrency, cache lines, and distributed scaling.

== 1.3 Google L5/L6 Evaluation Signals
- *General Cognitive Ability (GCA)*: Deconstructs ambiguity, synthesizes optimal algorithms, navigates trade-offs.
- *Role-Related Knowledge (RRK)*: Deep mastery of Java memory model, data cache locality, concurrency hazards, and algorithmic optimality.
- *Leadership*: Drives technical direction without waiting for instructions; communicates rationale with structured clarity.
- *Googleyness*: Intellectual humility, collaborative spirit, blameless culture, and commitment to long-term engineering excellence.

#pagebreak()

= Chapter 2: Advanced Data Structures & Algorithms Deep Dive (Java 25)

== 2.1 Graph Suite
=== Tarjan's Strongly Connected Components (SCC) & Bridges
- Computes maximal strongly connected subgraphs in directed graphs and critical bridges in undirected graphs in a single _O(V + E)_ DFS pass.
- *Mathematical Invariants*:
  - Discovery order `ids[u]` is strictly increasing.
  - `low[u] = min(ids[u], min_{(u,v) in E} low[v])`.
  - A vertex `u` is root of an SCC iff `ids[u] == low[u]`.
  - An edge `(u, v)` is a critical bridge iff `low[v] > ids[u]`.

=== Dinic's Algorithm for Maximum Network Flow
- Outperforms Edmonds-Karp with _O(V² E)_ general network time and _O(E √V)_ on unit networks / bipartite matching.
- *Two-Phase Loop*:
  1. *BFS Level Graph*: Constructs shortest augmenting paths.
  2. *DFS Blocking Flow*: Pushes flows along level increments using `work[u]` pointers to prune saturated edges.

=== Dijkstra with Indexed Priority Queue (IPQ)
- Standard `PriorityQueue` suffers from _O(E log E)_ time and duplicate key bloat.
- IPQ maintains `heap`, inverse lookup `pos[v]`, and `keys[v]`, achieving _O((V + E) log V)_ time and strictly _O(V)_ auxiliary memory with _O(log V)_ decrease-key.

=== Topological Sort with Directed Cycle Extraction
- Combines Kahn's in-degree queue with DFS 3-coloring (White, Gray, Black) and `parent` tracing to reconstruct the exact cycle path if non-DAG.

== 2.2 Advanced Trees & Range Queries
=== Segment Tree with Lazy Propagation
- Supports _O(log N)_ range additions, range sums, and range minimums over N elements.
- *Lazy Tag Invariant*: Updates to interval `[L, R]` apply immediately to node aggregate; deferred delta is pushed down to children on demand via `pushDown(node)`.

=== Fenwick Tree (Binary Indexed Tree - 1D & 2D)
- Uses bitwise low-bit extraction `(x & -x)` to execute prefix sums and point updates in _O(log N)_ with zero pointer indirection.
- 2D Fenwick Tree computes bounding box sums via inclusion-exclusion in _O(log N log M)_.

=== Lowest Common Ancestor (LCA) via Binary Lifting
- _O(N log N)_ preprocessing computes ancestor table: `up[u][k] = up[up[u][k-1]][k-1]`.
- Queries LCA and tree distance in _O(log N)_ time.

=== Bitwise Trie for Maximum XOR
- 31-bit binary trie finds the maximum XOR pair in an integer stream in _O(31)_ time per query by greedily navigating opposite bit branches.

== 2.3 Dynamic Programming & Monotonic Queues
=== Bitmask DP
- Solves Traveling Salesperson Problem (TSP) in _O(2ⁿ N²)_ time with state memoization `dp(mask, u)` and exact tour path reconstruction.

=== Monotonic Queue DP Optimization
- Optimizes sliding window DP recurrences from _O(N · k)_ to _O(N)_ amortized time using a monotonic decreasing deque.

== 2.4 String Algorithms
- *Knuth-Morris-Pratt (KMP)*: Computes prefix failure function `π` in _O(M)_ time; searches text in _O(N)_ with zero text-pointer backtracking.
- *Aho-Corasick Automaton*: Combines Trie with BFS failure transitions and dictionary suffix links to find all occurrences of multiple patterns simultaneously in _O(|Text| + matches)_.
- *Rabin-Karp Double Rolling Hash*: Uses dual prime moduli (1,000,000,007 and 1,000,000,009) with base 257 to achieve collision-free _O(N log N)_ longest duplicate substring search.

== 2.5 In-Memory Production Data Structures
- *O(1) LFU Cache*: Two-level hash indexing (`keyToNode` and `freqToList`) with _O(1)_ LRU tie-breaking eviction.
- *O(1) LRU Cache*: Doubly linked list + HashMap with zero `LinkedHashMap` crutches.
- *Dual-Heap Streaming Median Finder*: Balanced max-heap and min-heap maintaining `0 <= |maxHeap| - |minHeap| <= 1`.

#pagebreak()

= Chapter 3: Modern Java 25 Concurrency, Memory Model & Internals

== 3.1 Virtual Threads Architecture (Project Loom)
- *User-space Scheduling*: Decouples millions of lightweight virtual threads from OS kernel threads.
- *Continuation Yielding*: When a virtual thread executes blocking I/O, its call stack continuation unmounts from the underlying carrier thread (`ForkJoinPool`), enabling other virtual threads to execute without kernel context switches.
- *Thread Pinning*: Avoid `synchronized` blocks inside long-running I/O to prevent carrier thread starvation; use `ReentrantLock`.

== 3.2 Java 25 Structured Concurrency (`StructuredTaskScope`)
- Replaces uncoordinated asynchronous futures with explicit lexical containment scopes:
  - `StructuredTaskScope.open(Joiner.awaitAllSuccessfulOrThrow())`: Fan-out parallel subtasks with automatic fail-fast cancellation.
  - `StructuredTaskScope.open(Joiner.anySuccessfulResultOrThrow())`: Speculative execution (hedged requests) racing multiple replicas.

== 3.3 Scoped Values (`ScopedValue`)
- Replaces `ThreadLocal` to eliminate memory leaks in thread pools.
- Immutable, bounded lifetime, inherited by virtual thread subtasks in _O(1)_ without map cloning.

== 3.4 Lock-Free Concurrency & Java Memory Model
- *Treiber Stack*: Lock-free LIFO stack using CAS loops on `AtomicReference<Node<T>> head` with `Thread.onSpinWait()` hints.
- *Michael-Scott Queue*: Sentinel-guarded concurrent FIFO queue using 2-step CAS with cooperative tail forwarding.
- *Volatile & Happens-Before*: Volatile writes inject StoreLoad memory barriers, flushing CPU store buffers and preventing reorderings.
- *Cache Line False Sharing*: 64-byte CPU cache lines can degrade concurrent throughput by 90%+ if unrelated variables share the same cache line.

== 3.5 Production Rate Limiting Algorithms
- *Token Bucket*: Lock-free atomic refill without background scheduler threads.
- *Leaky Bucket*: Constant smooth outflow rate limiter.
- *Sliding Window Counter*: Sub-window time-sliced counters smoothing burst spikes.

#pagebreak()

= Chapter 4: Google-Scale Distributed Systems Design Blueprints

== 4.1 Latency Numbers & Math Rules-of-Thumb
- RAM access: 100 ns | NVMe SSD read: 250 µs | Intra-DC RTT: 500 µs | Cross-continent RTT: 40 - 100 ms.
- 100 Million requests/day ≈ 1,200 QPS | 1 Billion requests/day ≈ 12,000 QPS (Peak 30k QPS).

== 4.2 The 7 Google-Scale System Design Blueprints
=== 1. Global Multi-Region Distributed Rate Limiter
- Edge proxies (Envoy / GFE) run local token buckets for sub-millisecond checks.
- Batch quota leasing from in-region Redis cluster.
- Fail-open resilience: If WAN partition severs central connectivity, edge proxies apply degraded local quotas.

=== 2. Google-Scale Web Crawler
- Distributed URL Frontier with PageRank priority queues and per-host politeness delay queues.
- DNS prefetching cache.
- 64-bit SimHash and Scalable Bloom Filters eliminate duplicate content and URLs.
- Raw HTML stored in Colossus (GFS); metadata indexed in Bigtable.

=== 3. Google Spanner: Distributed Database
- TrueTime API: Hardware GPS receivers and atomic rubidium clocks bound clock drift to ε ≈ 1 - 7 ms.
- Commit Wait Invariant ensures linearizable external consistency.
- Shards replicated via Paxos groups; cross-shard distributed transactions use Two-Phase Commit (2PC) over Paxos.
- Multi-Version Concurrency Control (MVCC) enables lock-free point-in-time reads.

=== 4. Real-Time Collaborative Document Editor (Google Docs)
- Conflict Resolution: Operational Transformation (OT) or Conflict-free Replicated Data Types (CRDT - Logoot/LSeq).
- WebSocket edge gateways with sticky document session affinity.
- Append-only operation log with periodic Spanner snapshots.

=== 5. Google Drive / Global Cloud Storage
- 4MB block chunking with SHA-256 content-addressable storage (CAS).
- Resumable upload protocol with chunk deduplication saving 40%+ bandwidth.
- Spanner metadata service + Colossus erasure-coded blob storage.

=== 6. YouTube Video Ingestion & Global Streaming
- Transcoding DAG splits uploaded video into 5-second GOP chunks for parallel multi-resolution encoding (AV1, VP9, H.264).
- Adaptive Bitrate Streaming (DASH/HLS) via multi-tier CDN edge caching.
- Pub/Sub + Apache Flink streaming pipeline for view count aggregation.

=== 7. High-Throughput Metrics & Alerting Engine (Google Monarch)
- Time-series Gorilla compression: Delta-of-delta timestamps (1.37 bits) and XOR float compression.
- In-memory circular buffer for 2-hour alert evaluation; columnar disk persistence for long-term telemetry.

#pagebreak()

= Chapter 5: Googleyness, Leadership & L5/L6 Behavioral Mastery

== 5.1 Senior Behavioral Expectations
- *Navigating Ambiguity*: Taking underspecified mandates and independently driving discovery, stakeholder alignment, and delivery.
- *Leadership Without Authority*: Influencing cross-team roadmaps through RFCs, empirical benchmarks, and empathy.
- *Disagree and Commit*: Championing data-driven debates; fully committing to team decisions once finalized.
- *Blameless Post-Mortems*: Treating production outages as learning opportunities to engineer automated systemic safeguards.

== 5.2 The STAR-L Formula
- *Situation (15%)*: High-stakes technical complexity and business impact.
- *Task (15%)*: The core ambiguity and your specific ownership.
- *Action (50%)*: Concrete technical decisions, consensus-building steps, trade-offs, and risk mitigations.
- *Result (15%)*: Quantifiable engineering metrics (latency, QPS, cloud cost, uptime).
- *Learning (5%)*: Architectural insights and retrospection.

#v(2em)
#align(center)[
  #text(fill: rgb("#1a73e8"), weight: "bold", size: 14pt)[
    Google Interview Preparation Mastered.
  ]
]
"""

    with open(typst_file, "w", encoding="utf-8") as f:
        f.write(typst_content)

    print("Compiling Typst to PDF...")
    cmd = ["/opt/homebrew/bin/typst", "compile", typst_file, output_pdf]
    res = subprocess.run(cmd, capture_output=True, text=True)
    if res.returncode == 0:
        file_size = os.path.getsize(output_pdf)
        print(f"SUCCESS: Generated {output_pdf} ({file_size} bytes)")
    else:
        print("ERROR compiling Typst:", res.stderr)
        sys.exit(1)

if __name__ == "__main__":
    main()
