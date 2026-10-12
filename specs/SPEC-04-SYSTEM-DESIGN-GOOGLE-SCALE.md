# SPEC-04: Google-Scale Distributed Systems Design Blueprints

## 1. System Design Assessment Framework (L5/L6 Rubric)

Google Senior System Design rounds (45 minutes) evaluate a candidate's ability to architect systems handling millions of queries per second (QPS), petabytes/exabytes of data, multi-region failovers, and subtle consistency trade-offs.

```
+---------------------------------------------------------------------------------+
| Minute 00 - 05: Scope, Functional / Non-Functional Requirements & API Design   |
+---------------------------------------------------------------------------------+
                                         |
                                         v
+---------------------------------------------------------------------------------+
| Minute 05 - 10: Back-of-the-Envelope Capacity Estimations (QPS, IOPS, Storage)  |
+---------------------------------------------------------------------------------+
                                         |
                                         v
+---------------------------------------------------------------------------------+
| Minute 10 - 25: High-Level Architecture, Data Flow, Storage Models & Schemas    |
+---------------------------------------------------------------------------------+
                                         |
                                         v
+---------------------------------------------------------------------------------+
| Minute 25 - 40: Deep-Dive into Choke Points, Failure Modes & Distributed Invariants|
+---------------------------------------------------------------------------------+
                                         |
                                         v
+---------------------------------------------------------------------------------+
| Minute 40 - 45: Trade-offs, Operational Observability, Disaster Recovery & Wrap |
+---------------------------------------------------------------------------------+
```

---

## 2. Distributed Systems Math & Latency Cheatsheet

### 2.1 Latency Numbers Every Google Engineer Knows
| Hardware / Network Operation | Typical Latency | Human Scale (Multiplied by $10^9$) |
| :--- | :--- | :--- |
| L1 Cache Reference | $0.5 \text{ ns}$ | $0.5 \text{ seconds}$ |
| Branch Mispredict | $5 \text{ ns}$ | $5 \text{ seconds}$ |
| L2 Cache Reference | $7 \text{ ns}$ | $7 \text{ seconds}$ |
| Mutex Lock / Unlock | $25 \text{ ns}$ | $25 \text{ seconds}$ |
| Main Memory (RAM) Reference | $100 \text{ ns}$ | $1.7 \text{ minutes}$ |
| Compress 1KB with Snappy | $2,000 \text{ ns} = 2 \mu\text{s}$ | $33 \text{ minutes}$ |
| Read 1 MB sequentially from RAM | $3,000 \text{ ns} = 3 \mu\text{s}$ | $50 \text{ minutes}$ |
| Read 1 MB sequentially from NVMe SSD | $250 \mu\text{s}$ | $2.9 \text{ days}$ |
| Round trip within same Datacenter | $500 \mu\text{s}$ | $5.8 \text{ days}$ |
| Read 1 MB sequentially from Magnetic HDD | $20,000 \mu\text{s} = 20 \text{ ms}$ | $7.7 \text{ months}$ |
| Send packet US East to US West | $40 \text{ ms}$ | $1.3 \text{ years}$ |
| Send packet US East to Europe (RTT) | $100 \text{ ms}$ | $3.2 \text{ years}$ |

### 2.2 Capacity Estimations Rule-of-Thumb
- 1 Million requests/day $\approx 12 \text{ QPS}$.
- 100 Million requests/day $\approx 1,200 \text{ QPS} \approx 1.2\text{k QPS}$ (Peak $\approx 2.5\text{k QPS}$).
- 1 Billion requests/day $\approx 12,000 \text{ QPS} \approx 12\text{k QPS}$ (Peak $\approx 25\text{k - } 30\text{k QPS}$).
- 1 byte $\times 10^9 = 1\text{ GB}$; 1 byte $\times 10^{12} = 1\text{ TB}$; 1 byte $\times 10^{15} = 1\text{ PB}$.

---

## 3. The 7 Google-Scale System Design Blueprints

### Blueprint 1: Global Multi-Region Distributed Rate Limiter
- **Scale**: 10M QPS across 5 continents, $< 2\text{ ms}$ evaluation latency, tolerance to inter-datacenter WAN partitions.
- **Key Techniques**:
  - Local Token Bucket in memory of edge proxy (Envoy / GFE).
  - Asynchronous batch synchronization with local Redis cluster.
  - Gossip protocol (SWIM) or CRDT counters for inter-cluster convergence.
  - Fail-open strategy on complete consensus partition to protect user availability.

### Blueprint 2: Google-Scale Web Crawler
- **Scale**: 5 Billion web pages indexed per day, continuous politeness, duplicate elimination.
- **Key Techniques**:
  - Distributed URL Frontier (Priority Queue + Host-based Politeness Queues).
  - DNS resolution caching with prefetching and TTL management.
  - Content deduplication via 64-bit SimHash / MinHash and Bloom Filters.
  - Crawled document repository stored in Colossus (GFS successor) and Bigtable.

### Blueprint 3: Globally-Distributed Relational Database (Google Spanner Architecture)
- **Scale**: Multi-datacenter, strict external consistency (linearizability), ACID transactions.
- **Key Techniques**:
  - TrueTime API: Hardware GPS receivers and Atomic clocks bounded by uncertainty window $[\epsilon \approx 1-7 \text{ ms}]$.
  - Paxos Groups for state machine replication per shard (directory).
  - Two-Phase Commit (2PC) over Paxos for cross-shard distributed transactions.
  - Multi-Version Concurrency Control (MVCC) enabling lock-free point-in-time reads.

### Blueprint 4: Real-Time Collaborative Document Editor (Google Docs)
- **Scale**: 100 concurrent typists per document, sub-50ms keystroke echo, offline sync.
- **Key Techniques**:
  - Conflict Resolution: Operational Transformation (OT) vs Conflict-free Replicated Data Types (CRDT - Logoot / LSeq).
  - WebSocket Edge Gateways with sticky session state routing.
  - Snapshotting + Append-only Operation Log for fast document hydration.

### Blueprint 5: Global Cloud Storage & File Synchronization (Google Drive)
- **Scale**: 1 Billion active users, petabytes uploaded hourly, byte-level deduplication.
- **Key Techniques**:
  - Content-Addressable Storage (CAS): Files chunked into 4MB blocks, SHA-256 hashed.
  - Resumable Upload Protocol with idempotency keys and chunk offset validation.
  - Metadata DB (Spanner) storing block-to-file mapping separate from chunk blob storage.

### Blueprint 6: YouTube Distributed Video Ingestion & Streaming
- **Scale**: 500 hours of video uploaded per minute, 2 Billion daily viewers.
- **Key Techniques**:
  - Video Ingestion DAG: Split video into 5-second GOP (Group of Pictures) chunks.
  - Asynchronous Transcoding Cluster: Worker pools converting chunks to multiple resolutions (1080p, 4K, AV1, VP9, H.264).
  - Adaptive Bitrate Streaming (DASH / HLS) via Global CDN edge servers with Byte-Range HTTP requests.

### Blueprint 7: High-Throughput Time-Series Metrics & Monitoring (Google Monarch / Borgmon)
- **Scale**: Tens of millions of target metrics collected per second, sub-second query evaluation.
- **Key Techniques**:
  - Delta-of-delta and XOR timestamp/value compression (Gorilla compression format).
  - Two-tier storage: In-memory circular buffer for last 2 hours; downsampled columnar storage on disk.
  - Sharding by metric name and target labels with Consistent Hashing.
