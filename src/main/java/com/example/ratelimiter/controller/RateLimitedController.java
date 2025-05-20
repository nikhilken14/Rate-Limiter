package com.example.ratelimiter.controller;

import com.example.ratelimiter.model.Student;
import com.example.ratelimiter.service.RateLimiterService;
import com.example.ratelimiter.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api")
public class RateLimitedController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private RateLimiterService rateLimiterService;

    @Autowired
    private CacheManager cacheManager;

    @GetMapping("/{id}")
    public ResponseEntity<?> getStudent(@PathVariable long id) {
        Cache cache = cacheManager.getCache("studentCache");
        if (cache != null) {
            Student cachedStudent = cache.get(id, Student.class);
            if (cachedStudent != null) {
                return ResponseEntity.ok(cachedStudent);
            }
        }

        if (!rateLimiterService.allowRequest("student-api")) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many requests. Try again later.");
        }

        Optional<Student> studentOpt = studentService.findStudent(id);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            if (cache != null) {
                cache.put(id, student);
            }
            return ResponseEntity.ok(student);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found.");
    }
}
