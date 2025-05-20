package com.example.ratelimiter.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public boolean allowRequest(String key) {
        TokenBucket bucket = buckets.computeIfAbsent(key, k -> new TokenBucket(5, 60_000));
        return bucket.allowRequest();
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
