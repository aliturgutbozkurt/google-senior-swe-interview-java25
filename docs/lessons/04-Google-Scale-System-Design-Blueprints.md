# Chapter 4: Google-Scale System Design Blueprints

## 1. Google System Design Assessment Framework

In a 45-minute Google Senior (L5/L6) System Design round, you are the technical lead driving the architecture. The interviewer will judge your ability to handle ambiguous scale, choose proper storage engines, reason about failure modes, and evaluate trade-offs.

```
+-----------------------------------------------------------------------------------+
| 00:00 - 05:00: Scope Clarification, Functional/Non-Functional Specs, Public API  |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
| 05:00 - 10:00: Back-of-the-Envelope Capacity Estimations (QPS, IOPS, Storage, BW) |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
| 10:00 - 25:00: High-Level Architecture, Core Components, Data Flow & Schemas      |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
| 25:00 - 40:00: Deep-Dive into Bottlenecks, Distributed Invariants & Failure Modes |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
| 40:00 - 45:00: Observability (SLOs/SLAs), Disaster Recovery, Wrap-up & Trade-offs |
+-----------------------------------------------------------------------------------+
```

---

## 2. Distributed Systems Math & Hardware Latencies

### 2.1 Latency Numbers Every Google Engineer Knows
* **L1 Cache**: $0.5 \text{ ns}$
* **L2 Cache**: $7 \text{ ns}$
* **RAM Access**: $100 \text{ ns}$
* **Read 1 MB sequentially from RAM**: $3 \mu\text{s}$
* **Read 1 MB sequentially from NVMe SSD**: $250 \mu\text{s}$
* **Datacenter Intra-Rack Round Trip (RTT)**: $500 \mu\text{s}$
* **Cross-Datacenter RTT (US East to US West)**: $40 \text{ ms}$
* **Transatlantic RTT (US East to Europe)**: $100 \text{ ms}$

### 2.2 Capacity Estimations Rule-of-Thumb
* $10^6$ requests/day $\approx 12 \text{ QPS}$
* $10^8$ requests/day $\approx 1,200 \text{ QPS}$ (Peak $\approx 2,500 \text{ QPS}$)
* $10^9$ requests/day $\approx 12,000 \text{ QPS}$ (Peak $\approx 30,000 \text{ QPS}$)
* Storage multipliers:
  * 1 Byte $\times 10^9 \approx 1 \text{ GB}$
  * 1 Byte $\times 10^{12} \approx 1 \text{ TB}$
  * 1 Byte $\times 10^{15} \approx 1 \text{ PB}$

---

## 3. The 7 Google-Scale System Design Blueprints

### 3.1 Global Multi-Region Distributed Rate Limiter
* **Challenge**: Protect APIs against DDoS and noisy neighbors across 5 continents at 10M QPS with sub-millisecond evaluation latency.
* **Architecture**:
  1. **Edge Proxies (Google Frontend / Envoy)**: Maintain in-memory local token buckets for $O(1)$ zero-network evaluations.
  2. **Batch Quota Leasing**: Proxies do not hit a central database on every request. They request token allocations in batches (e.g. 500 tokens at a time) from an in-region Redis cluster.
  3. **Global Synchronization**: Inter-region quotas are synchronized via asynchronous gossip or lightweight Raft groups.
  4. **Fail-Open Strategy**: If a regional network partition isolates an edge proxy from Redis, the proxy falls back to a safe local degraded rate rather than returning 500 errors to valid users.

---

### 3.2 Google-Scale Web Crawler
* **Challenge**: Crawl and index 5 Billion web pages daily, honoring host politeness and eliminating duplicate content.
* **Architecture**:
  1. **URL Frontier**:
     * **Priority Queue**: Ranks URLs by PageRank and freshness.
     * **Politeness Queues**: One queue per hostname (e.g. `domain.com`). A worker thread crawls from a host queue only after a configurable delay (e.g. 1 second).
  2. **DNS Prefetching & Caching**: Custom DNS resolver avoiding OS DNS resolution bottlenecks.
  3. **Content Deduplication (SimHash & Bloom Filters)**:
     * 64-bit SimHash generates fingerprint of page text. Two pages with Hamming distance $\le 3$ are flagged as near-duplicates.
     * Scalable Bloom filters prevent re-fetching previously crawled URLs.
  4. **Storage Tier**: Raw HTML stored compressed in Colossus (GFS) with metadata in Google Bigtable.

---

### 3.3 Google Spanner: Globally-Distributed Relational Database
* **Challenge**: Multi-region distributed transactions with ACID semantics and external consistency (linearizability).
* **Architecture**:
  1. **TrueTime API**: Hardware synchronized clocks utilizing GPS receivers and atomic rubidium clocks. Returns time intervals $[t_{\text{earliest}}, t_{\text{latest}}]$ with uncertainty bounded to $\epsilon \approx 1 - 7 \text{ ms}$.
  2. **Commit Wait Invariant**: Before committing a write transaction with timestamp $t_{\text{commit}}$, the leader sleeps until $\text{now}().\text{earliest} > t_{\text{commit}}$. This guarantees any subsequent transaction receives a strictly greater timestamp.
  3. **Paxos Replication**: Each table shard (Directory) is replicated across 3-5 regions via a Paxos group with an elected leader lease.
  4. **Two-Phase Commit (2PC) over Paxos**: Cross-shard distributed transactions coordinate multiple Paxos leader groups using 2PC.
  5. **MVCC (Multi-Version Concurrency Control)**: Reads at timestamp $t$ execute lock-free on local replicas without stalling writes.

---

### 3.4 Real-Time Collaborative Document Editor (Google Docs)
* **Challenge**: Support 100+ concurrent authors typing into the same document with sub-50ms latency and guaranteed convergence.
* **Architecture**:
  1. **Conflict Resolution Model**:
     * **Operational Transformation (OT)**: Central server sequences all operations. Operations are transformed against concurrent operations based on revision numbers.
     * **CRDT (Conflict-free Replicated Data Type)**: Characters receive globally unique, fractional position identifiers (Logoot / LSeq). Merging is mathematically associative and commutative, allowing peer-to-peer convergence without a central transformer.
  2. **WebSocket Gateway Cluster**: Terminate persistent bi-directional WebSockets at edge proxies with sticky affinity to document shard coordinators.
  3. **Hydration & Compaction**: Append-only operation log with periodic document snapshots in Spanner.

---

### 3.5 Google Drive / Global Cloud Storage
* **Challenge**: Petabyte file ingestion, byte-level deduplication, resumable uploads, and instant synchronization.
* **Architecture**:
  1. **Block Chunking**: Client splits files into 4MB chunks and computes SHA-256 hashes.
  2. **Resumable Upload Protocol**: Client obtains an upload token. Before uploading chunk $i$, client queries the block metadata service. If the SHA-256 hash already exists in storage, the chunk is skipped (deduplication saved 40%+ network bandwidth).
  3. **Metadata Database**: Spanner stores file trees, permissions, and chunk pointers.
  4. **Blob Storage Engine**: Chunks stored in Colossus across erasure-coded disks ($8+4$ Reed-Solomon encoding).

---

### 3.6 YouTube Distributed Video Ingestion & Streaming
* **Challenge**: Ingest 500 hours of video per minute; stream to 2 Billion viewers worldwide.
* **Architecture**:
  1. **Ingestion & Transcoding DAG**:
     * Uploaded video is split into 5-second GOP (Group of Pictures) chunks.
     * Distributed worker pool transcodes chunks concurrently into target resolutions (1080p, 4K, 60fps) and modern codecs (AV1, VP9, H.264).
  2. **Adaptive Bitrate Streaming (DASH / HLS)**: Client continuously measures network bandwidth and requests optimal chunk bitrate via HTTP byte-range requests.
  3. **Multi-Tier CDN Caching**:
     * **Edge Points of Presence (PoPs)**: Cache the top 10% hottest videos (serving 80% of views).
     * **Regional Datacenters**: Cache the long-tail content.
  4. **View Count Aggregator**: Streaming view events aggregated via Pub/Sub and Apache Flink / MillWheel to prevent hot partition bottlenecks.

---

### 3.7 High-Throughput Metrics & Alerting Engine (Google Monarch / Borgmon)
* **Challenge**: Collect tens of millions of metric time-series per second across Google's fleet; evaluate alerting rules with sub-second latency.
* **Architecture**:
  1. **Time-Series Compression (Gorilla Compression)**:
     * Timestamps stored using delta-of-delta encoding (reducing timestamp to $\approx 1.37$ bits).
     * Metric float values stored using XOR delta compression against previous values.
  2. **Two-Tier Storage**:
     * **In-Memory Ring Buffer**: Stores the most recent 2 hours of metrics for instant alert rule evaluation.
     * **Columnar Disk Store**: Downsampled aggregates (1m, 1h, 1d) persisted for long-term capacity trend analysis.
  3. **Consistent Hashing**: Metric collection shards distributed via consistent hash rings with virtual nodes.
