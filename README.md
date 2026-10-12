# Google Senior Software Engineer (L5/L6) Interview Master Guide
### Spec-Driven Development • Java 25 LTS Preview • Algorithms & Data Structures • Google-Scale System Design • Modern Concurrency • Googleyness & Leadership

[![Java 25 CI](https://github.com/aliturgutbozkurt/google-senior-swe-interview-java25/actions/workflows/ci.yml/badge.svg)](https://github.com/aliturgutbozkurt/google-senior-swe-interview-java25/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Java Version](https://img.shields.io/badge/Java-25%20LTS-orange.svg)](https://openjdk.org/)
[![PDF Guide](https://img.shields.io/badge/PDF-Compiled%20Book-red.svg)](docs/pdf/Google_Senior_SWE_Master_Guide.pdf)

---

## 🎯 Executive Overview

This repository is an end-to-end, production-grade master curriculum designed specifically for engineers preparing for **Google Senior Software Engineer (L5)** and **Staff Software Engineer (L6)** technical interviews.

Unlike junior/mid-level prep that focuses exclusively on LeetCode heuristics, this guide embodies **Spec-Driven Development (SDD)**: formulating mathematical invariants, clarifying ambiguous problem spaces, budgeting asymptotic resources, and authoring clean, idiomatic, high-concurrency Java 25 reference code backed by 100% test coverage.

---

## 📚 Curriculum Structure

```
.
├── specs/                                # Formal Technical Specifications (SDD)
│   ├── SPEC-01-SDD-FRAMEWORK.md          # SDD Interviewing Protocol & Scoring Rubric
│   ├── SPEC-02-ALGORITHMS-AND-DATA-STRUCTURES.md # Asymptotic Bounds & Mathematical Invariants
│   ├── SPEC-03-CONCURRENCY-AND-JAVA25.md # Java 25 Virtual Threads & Lock-Free Specifications
│   ├── SPEC-04-SYSTEM-DESIGN-GOOGLE-SCALE.md # Distributed Systems Blueprints & Math Cheatsheet
│   └── SPEC-05-BEHAVIORAL-AND-GOOGLEYNESS.md # Senior Leadership Competencies & STAR-L Framework
│
├── docs/                                 # Full Educational Lessons & Publication Pipeline
│   ├── lessons/
│   │   ├── tr/                           # Türkçe Markdown Dersleri (Bölüm 1 - 5)
│   │   │   ├── 01-Spec-Driven-Development-Google-Metodolojisi.md
│   │   │   ├── 02-Veri-Yapilari-ve-Ileri-Algoritmalar-Detayli-Rehber.md
│   │   │   ├── 03-Modern-Java-25-Concurrency-ve-Dahili-Yapi.md
│   │   │   ├── 04-Google-Scale-Dagitik-Sistem-Tasarimi-Mimarileri.md
│   │   │   └── 05-Googleyness-Liderlik-ve-Davranissal-Mulakatlar.md
│   │   └── en/                           # English Markdown Lessons (Chapters 1 - 5)
│   │       ├── 01-Spec-Driven-Development-Google-Methodology.md
│   │       ├── 02-Data-Structures-and-Algorithms-Deep-Dive.md
│   │       ├── 03-Modern-Java-25-Concurrency-and-Internals.md
│   │       ├── 04-Google-Scale-System-Design-Blueprints.md
│   │       └── 05-Googleyness-Leadership-and-Behavioral-Rounds.md
│   ├── book.typ                          # English Typst Template
│   ├── book_tr.typ                       # Türkçe Typst Şablonu
│   ├── generate_pdf.py                   # Automated Bilingual PDF Generation Script
│   └── pdf/
│       ├── Google_Senior_SWE_Master_Guide_TR.pdf # Türkçe Derlenmiş Master PDF Kitabı
│       └── Google_Senior_SWE_Master_Guide_EN.pdf # English Compiled Master PDF Book
│
└── src/
    ├── main/java/com/google/prep/
    │   ├── algorithms/                   # Advanced Algorithms & Complex Data Structures
    │   │   ├── graph/                    # Tarjan SCC/Bridges, Dinic Max Flow, IPQ, Dijkstra, TopoSort
    │   │   ├── trees/                    # SegmentTree (Lazy), Fenwick 1D/2D, LCA (Binary Lifting), Trie
    │   │   ├── dp/                       # Bitmask TSP, Monotonic Queue Optimizations
    │   │   ├── strings/                  # KMP, Aho-Corasick Automaton, Rabin-Karp Double Hash
    │   │   └── datastructures/           # O(1) LFU Cache, O(1) LRU Cache, Median Finder
    │   ├── concurrency/                  # Modern Java 25 Concurrency
    │   │   ├── virtualthreads/           # StructuredTaskScope (fan-out/racing), ScopedValue
    │   │   ├── lockfree/                 # Treiber Stack, Michael-Scott Concurrent FIFO Queue
    │   │   ├── ratelimiter/              # Lock-free Token Bucket, Leaky Bucket, Sliding Window
    │   │   └── synchronization/          # Fair ReadWriteLock, Bounded Blocking Queue
    │   └── systemdesign/                 # Google-Scale System Design Architecture Simulations
    │       ├── consistenthash/           # Consistent Hash Ring with Virtual Nodes & Quorums
    │       ├── bloomfilter/              # Scalable Bloom Filter with Kirsch-Mitzenmacher Hashing
    │       ├── lsm/                      # Mini LSM-Tree Engine (MemTable, WAL, SSTable, Compaction)
    │       └── ratelimiter/              # Multi-Region Distributed Quota Leasing Simulator
    │
    └── test/java/com/google/prep/        # Comprehensive JUnit 5 & Stress Test Suites
        ├── algorithms/                   # Graph, Tree, DP, String, Data Structure tests
        ├── concurrency/                  # Multi-threaded concurrent stress tests
        └── systemdesign/                 # Distributed architecture verification
```

---

## 🚀 Quickstart & Verification

### 1. Requirements
* **Java**: OpenJDK 25 LTS (with preview enabled)
* **Build Tool**: Apache Maven 3.9+
* **PDF Compiler** (Optional): [Typst](https://github.com/typst/typst) `0.15+` or Python 3.9+

### 2. Compile & Run All 30 Test Suites
```bash
mvn clean test
```

### 3. Generate the Publication PDF Book
```bash
python3 docs/generate_pdf.py
# Or directly with Typst:
typst compile docs/book.typ docs/pdf/Google_Senior_SWE_Master_Guide.pdf
```
Output: `docs/pdf/Google_Senior_SWE_Master_Guide.pdf`

---

## 🧠 Key Modules Breakdown

### 1. Advanced Algorithms & Data Structures
* **Tarjan's SCC & Bridges**: $O(V + E)$ single-pass DFS extracting maximal strongly connected subgraphs and critical bridges.
* **Dinic's Algorithm**: $O(V^2 E)$ maximum flow with level graph BFS and DFS blocking flows; $O(E \sqrt{V})$ for bipartite matching.
* **Indexed Priority Queue (IPQ) & Dijkstra**: Eliminates $O(E \log E)$ duplicate key bloat with $O(\log V)$ decrease-key and $O(V)$ auxiliary memory.
* **Segment Tree with Lazy Propagation**: $O(\log N)$ range additions, range sums, and range minimums.
* **Fenwick Tree (1D & 2D)**: Bitwise `(x & -x)` prefix sums with zero pointer overhead.
* **Lowest Common Ancestor (Binary Lifting)**: $O(N \log N)$ preprocessing and $O(\log N)$ query.
* **Aho-Corasick Automaton**: Multi-pattern dictionary keyword search in $O(|Text| + \text{matches})$.
* **Rabin-Karp Double Rolling Hash**: Modulo $10^9+7$ and $10^9+9$ with base 257 for collision-free $O(N \log N)$ longest duplicate substring search.
* **O(1) LFU / LRU Caches**: Explicit doubly-linked lists and frequency buckets with zero standard library shortcuts.

### 2. Modern Java 25 Concurrency & Internals
* **Virtual Threads (Project Loom)**: Continuation yielding, carrier thread unmounting, unpark mechanics, and thread pinning prevention.
* **Structured Concurrency (`StructuredTaskScope`)**:
  * `Joiner.awaitAllSuccessfulOrThrow()`: Coordinated fan-out with fail-fast cancellation.
  * `Joiner.anySuccessfulResultOrThrow()`: Hedged/speculative execution racing multiple replicas.
* **Scoped Values (`ScopedValue`)**: Immutable, bounded context propagation replacing leak-prone `ThreadLocal`.
* **Lock-Free Concurrency**:
  * Treiber Stack with `AtomicReference` CAS loops and `Thread.onSpinWait()`.
  * Michael-Scott Concurrent Queue with 2-step CAS and cooperative tail forwarding.
* **Rate Limiting**: Lock-free atomic Token Bucket, constant outflow Leaky Bucket, and Sliding Window Counter.

### 3. Google-Scale System Design Blueprints
Detailed architectural blueprints with capacity math, component diagrams, and failure modes:
1. **Global Multi-Region Distributed Rate Limiter** (Envoy, Redis cluster, Token leasing, Partition fail-open)
2. **Google-Scale Web Crawler** (URL frontier, Host politeness, SimHash, Bloom filters, Bigtable)
3. **Google Spanner Distributed Relational Database** (TrueTime API $[\epsilon \approx 1-7\text{ ms}]$, Paxos groups, 2PC over Paxos, MVCC)
4. **Real-Time Collaborative Document Editor** (Google Docs: CRDT Logoot/LSeq vs OT, WebSockets)
5. **Google Drive / Global Cloud Storage** (4MB chunking, SHA-256 CAS, Resumable upload protocol, Colossus)
6. **YouTube Video Ingestion & Global Streaming** (Transcoding DAG, GOP chunks, DASH/HLS, Multi-tier CDN)
7. **High-Throughput Time-Series Metrics & Monitoring** (Google Monarch: Gorilla compression, TSDB)

### 4. Googleyness & Leadership (L5/L6 Behavioral Mastery)
* Navigating Ambiguity: Taking vague business problems and charting roadmaps.
* Leadership Without Authority: RFC-driven consensus, brown bags, empirical proof-of-concepts.
* Intellectual Humility: "Disagree and Commit" anchored in data over ego.
* Blameless Culture: 5 Whys, post-mortems, and engineering automated systemic safeguards.
* STAR-L Blueprint Bank with tailored senior engineering scenarios.

---

## 📜 License
MIT License. Created by [Ali Turgut Bozkurt](https://github.com/aliturgutbozkurt).
