package com.example.ratelimiter.controller;

import com.example.ratelimiter.service.RateLimiterService;
import com.example.ratelimiter.service.StudentService;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = "*")
public class StatisticsController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private RateLimiterService rateLimiterService;

    @Autowired
    private CacheManager cacheManager;

    @GetMapping
    public ResponseEntity<?> getStatistics() {

        Map<String, Object> stats = new HashMap<>();

        /* ---------------- DATABASE METRICS ---------------- */
        stats.put("totalStudents", studentService.countStudents());

        /* ---------------- CACHE METRICS ---------------- */
        Cache cache = cacheManager.getCache("students");

        if (cache instanceof CaffeineCache caffeineCache) {

            CacheStats caffeineStats =
                    caffeineCache.getNativeCache().stats();

            long hits = caffeineStats.hitCount();
            long misses = caffeineStats.missCount();
            long total = hits + misses;

            stats.put("cacheHits", hits);
            stats.put("cacheMisses", misses);
            stats.put("cacheRequests", total);
            stats.put(
                    "cacheHitRate",
                    total > 0
                            ? String.format("%.2f%%", (hits * 100.0 / total))
                            : "0%"
            );
        } else {
            stats.put("cacheHits", 0);
            stats.put("cacheMisses", 0);
            stats.put("cacheHitRate", "0%");
        }

        /* ---------------- RATE LIMITER METRICS ---------------- */
        Map<String, Object> rateLimiterStats =
                rateLimiterService.getRateLimiterStats();

        stats.put("totalRequests", rateLimiterStats.get("totalRequests"));
        stats.put("allowedRequests", rateLimiterStats.get("allowedRequests"));
        stats.put("blockedRequests", rateLimiterStats.get("blockedRequests"));
        stats.put("blockRate", rateLimiterStats.get("blockRate"));

        return ResponseEntity.ok(stats);
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetStatistics() {
        rateLimiterService.resetStats();
        return ResponseEntity.ok("Statistics reset successfully");
    }

    @GetMapping("/rate-limiter")
    public ResponseEntity<?> getRateLimiterStatus() {
        return ResponseEntity.ok(rateLimiterService.getRateLimiterStats());
    }

    @GetMapping("/cache")
    public ResponseEntity<?> getCacheStatus() {
        return ResponseEntity.ok(getStatistics().getBody());
    }
}
