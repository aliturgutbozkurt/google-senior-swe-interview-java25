package com.google.prep.systemdesign.ratelimiter;

import com.google.prep.concurrency.ratelimiter.TokenBucketRateLimiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Multi-Region Distributed Rate Limiter Simulation.
 *
 * <p>System Design Architecture:
 * <ul>
 *   <li>Edge Gateways (Envoy / GFE) run local token buckets to achieve sub-millisecond evaluation latency.</li>
 *   <li>Local proxies periodically lease batches of tokens from a central quota service (Redis Cluster / Spanner).</li>
 *   <li>Fail-Open Strategy: If WAN partition severs connection to central quota server, edge proxies fall back
 *       to conservative local limits rather than dropping user traffic.</li>
 * </ul>
 */
public final class DistributedRateLimiterSimulator {

    public static final class CentralQuotaServer {
        private final long totalTokensPerSec;
        private final AtomicLong remainingTokens;

        public CentralQuotaServer(long totalTokensPerSec) {
            this.totalTokensPerSec = totalTokensPerSec;
            this.remainingTokens = new AtomicLong(totalTokensPerSec);
        }

        public synchronized long leaseTokens(long requested) {
            long current = remainingTokens.get();
            long granted = Math.min(current, requested);
            remainingTokens.addAndGet(-granted);
            return granted;
        }

        public synchronized void resetPeriod() {
            remainingTokens.set(totalTokensPerSec);
        }

        public long getRemainingTokens() {
            return remainingTokens.get();
        }
    }

    public static final class EdgeProxy {
        private final String region;
        private final CentralQuotaServer centralServer;
        private final TokenBucketRateLimiter localLimiter;
        private final long batchLeaseSize;
        private boolean centralReachable = true;

        public EdgeProxy(String region, CentralQuotaServer centralServer, long batchLeaseSize) {
            this.region = region;
            this.centralServer = centralServer;
            this.batchLeaseSize = batchLeaseSize;
            // Local bucket with burst capacity equal to batch lease size
            this.localLimiter = new TokenBucketRateLimiter(batchLeaseSize, batchLeaseSize);
        }

        public boolean handleRequest() {
            if (localLimiter.tryAcquire(1.0)) {
                return true;
            }

            // Refill batch from central server if local tokens depleted
            if (centralReachable) {
                long granted = centralServer.leaseTokens(batchLeaseSize);
                if (granted > 0) {
                    return true;
                }
            } else {
                // Fail-open degraded quota in case of WAN partition
                return localLimiter.tryAcquire(0.5);
            }

            return false;
        }

        public void setCentralReachable(boolean reachable) {
            this.centralReachable = reachable;
        }

        public String getRegion() {
            return region;
        }
    }

    private final CentralQuotaServer centralServer;
    private final Map<String, EdgeProxy> proxies = new ConcurrentHashMap<>();

    public DistributedRateLimiterSimulator(long globalTokensPerSec) {
        this.centralServer = new CentralQuotaServer(globalTokensPerSec);
    }

    public EdgeProxy registerProxy(String region, long batchSize) {
        EdgeProxy proxy = new EdgeProxy(region, centralServer, batchSize);
        proxies.put(region, proxy);
        return proxy;
    }

    public CentralQuotaServer getCentralServer() {
        return centralServer;
    }
}
