package com.google.prep.systemdesign.lsm;

import com.google.prep.systemdesign.bloomfilter.ScalableBloomFilter;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Log-Structured Merge-Tree (LSM-Tree) Engine Simulation.
 *
 * <p>Underpins storage engines for Google Bigtable, RocksDB, Cassandra, and Spanner:
 * <ul>
 *   <li>Fast In-Memory Writes: Written to Write-Ahead Log (WAL) and concurrent MemTable (SkipList).</li>
 *   <li>Immutable SSTables: Flushed to disk when MemTable reaches threshold size.</li>
 *   <li>Bloom Filters: Avoids disk scans for keys not in the SSTable.</li>
 *   <li>Tombstone Deletion: Records soft deletes as tombstones purged during compaction.</li>
 *   <li>Merge Compaction: Consolidates multiple SSTables into a sorted run.</li>
 * </ul>
 */
public final class MiniLsmTree {

    public record Entry(String key, String value, boolean isTombstone, long timestamp) {}

    public static final class SsTable {
        private final int id;
        private final List<Entry> entries;
        private final ScalableBloomFilter filter;

        public SsTable(int id, List<Entry> entries) {
            this.id = id;
            this.entries = Collections.unmodifiableList(new ArrayList<>(entries));
            this.filter = new ScalableBloomFilter(Math.max(10, entries.size()), 0.01);
            for (Entry e : entries) {
                filter.put(e.key());
            }
        }

        public Optional<Entry> get(String key) {
            if (!filter.mightContain(key)) {
                return Optional.empty(); // Skipped via Bloom filter!
            }
            // Binary search in sorted entries
            int low = 0, high = entries.size() - 1;
            while (low <= high) {
                int mid = (low + high) >>> 1;
                Entry e = entries.get(mid);
                int cmp = e.key().compareTo(key);
                if (cmp == 0) {
                    return Optional.of(e);
                } else if (cmp < 0) {
                    low = mid + 1;
                } else {
                    high = mid - 1;
                }
            }
            return Optional.empty();
        }

        public List<Entry> getEntries() {
            return entries;
        }

        public int getId() {
            return id;
        }
    }

    private final int memTableThreshold;
    private final ConcurrentSkipListMap<String, Entry> memTable = new ConcurrentSkipListMap<>();
    private final CopyOnWriteArrayList<SsTable> ssTables = new CopyOnWriteArrayList<>();
    private final AtomicInteger tableIdGen = new AtomicInteger(1);

    public MiniLsmTree(int memTableThreshold) {
        if (memTableThreshold <= 0) {
            throw new IllegalArgumentException("Threshold must be positive: " + memTableThreshold);
        }
        this.memTableThreshold = memTableThreshold;
    }

    public synchronized void put(String key, String value) {
        if (key == null) throw new NullPointerException("Key cannot be null");
        Entry entry = new Entry(key, value, false, System.currentTimeMillis());
        memTable.put(key, entry);

        if (memTable.size() >= memTableThreshold) {
            flushMemTable();
        }
    }

    public synchronized void delete(String key) {
        if (key == null) throw new NullPointerException("Key cannot be null");
        Entry tombstone = new Entry(key, "", true, System.currentTimeMillis());
        memTable.put(key, tombstone);

        if (memTable.size() >= memTableThreshold) {
            flushMemTable();
        }
    }

    public Optional<String> get(String key) {
        if (key == null) return Optional.empty();

        // 1. Check in-memory active MemTable first
        Entry memEntry = memTable.get(key);
        if (memEntry != null) {
            return memEntry.isTombstone() ? Optional.empty() : Optional.of(memEntry.value());
        }

        // 2. Search SSTables in reverse chronological order (newest to oldest)
        for (int i = ssTables.size() - 1; i >= 0; i--) {
            SsTable table = ssTables.get(i);
            Optional<Entry> entryOpt = table.get(key);
            if (entryOpt.isPresent()) {
                Entry e = entryOpt.get();
                return e.isTombstone() ? Optional.empty() : Optional.of(e.value());
            }
        }

        return Optional.empty();
    }

    public synchronized void flushMemTable() {
        if (memTable.isEmpty()) return;
        List<Entry> sortedEntries = new ArrayList<>(memTable.values());
        SsTable newTable = new SsTable(tableIdGen.getAndIncrement(), sortedEntries);
        ssTables.add(newTable);
        memTable.clear();
    }

    public synchronized void compact() {
        if (ssTables.size() <= 1) return;

        // Merge sort all SSTables into a single compacted SSTable
        Map<String, Entry> merged = new TreeMap<>();
        for (SsTable table : ssTables) {
            for (Entry e : table.getEntries()) {
                Entry existing = merged.get(e.key());
                if (existing == null || e.timestamp() >= existing.timestamp()) {
                    merged.put(e.key(), e);
                }
            }
        }

        // Purge tombstones during major compaction
        List<Entry> compactedEntries = new ArrayList<>();
        for (Entry e : merged.values()) {
            if (!e.isTombstone()) {
                compactedEntries.add(e);
            }
        }

        SsTable compactedTable = new SsTable(tableIdGen.getAndIncrement(), compactedEntries);
        ssTables.clear();
        ssTables.add(compactedTable);
    }

    public int getSsTableCount() {
        return ssTables.size();
    }

    public int getMemTableSize() {
        return memTable.size();
    }
}
