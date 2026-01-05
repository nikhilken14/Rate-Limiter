package com.example.ratelimiter.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RateLimiterService {

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    // Statistics tracking
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong allowedRequests = new AtomicLong(0);
    private final AtomicLong blockedRequests = new AtomicLong(0);
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);

    public boolean allowRequest(String key) {
        totalRequests.incrementAndGet();
        TokenBucket bucket = buckets.computeIfAbsent(key, k -> new TokenBucket(5, 60_000));
        boolean allowed = bucket.allowRequest();

        if (allowed) {
            allowedRequests.incrementAndGet();
        } else {
            blockedRequests.incrementAndGet();
        }

        return !allowed;
    }

    public void recordCacheHit() {
        cacheHits.incrementAndGet();
    }

    public void recordCacheMiss() {
        cacheMisses.incrementAndGet();
    }

    public Map<String, Object> getRateLimiterStats() {
        Map<String, Object> stats = new HashMap<>();
        long total = totalRequests.get();
        long blocked = blockedRequests.get();

        stats.put("totalRequests", total);
        stats.put("allowedRequests", allowedRequests.get());
        stats.put("blockedRequests", blocked);
        stats.put("blockRate", total > 0 ? String.format("%.2f%%", (blocked * 100.0 / total)) : "0%");

        return stats;
    }

    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        long total = hits + misses;

        stats.put("hits", hits);
        stats.put("misses", misses);
        stats.put("total", total);
        stats.put("hitRate", total > 0 ? String.format("%.2f%%", (hits * 100.0 / total)) : "0%");

        return stats;
    }

    public void resetStats() {
        totalRequests.set(0);
        allowedRequests.set(0);
        blockedRequests.set(0);
        cacheHits.set(0);
        cacheMisses.set(0);
    }

    private static class TokenBucket {
        private final int capacity;
        private final long refillIntervalMillis;
        private int tokens;
        private long lastRefillTimestamp;

        TokenBucket(int capacity, long refillIntervalMillis) {
            this.capacity = capacity;
            this.refillIntervalMillis = refillIntervalMillis;
            this.tokens = capacity;
            this.lastRefillTimestamp = Instant.now().toEpochMilli();
        }

        synchronized boolean allowRequest() {
            refill();
            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }

        private void refill() {
            long now = Instant.now().toEpochMilli();
            long elapsed = now - lastRefillTimestamp;
            if (elapsed > refillIntervalMillis) {
                tokens = capacity;
                lastRefillTimestamp = now;
            }
        }
    }
}