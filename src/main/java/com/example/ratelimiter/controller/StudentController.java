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

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private RateLimiterService rateLimiterService;

    @Autowired
    private CacheManager cacheManager;

    @GetMapping("/{id}")
    public ResponseEntity<?> getStudent(@PathVariable Long id) {
        // Check cache first
        Cache cache = cacheManager.getCache("studentCache");
        if (cache != null) {
            Student cachedStudent = cache.get(id, Student.class);
            if (cachedStudent != null) {
                return ResponseEntity.ok(cachedStudent);
            }
        }

        // Check rate limiter
        if (rateLimiterService.allowRequest("get-student")) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many requests. Try again later.");
        }

        // Fetch from DB
        Optional<Student> studentOpt = studentService.findStudent(id);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            if (cache != null) {
                cache.put(id, student);
            }
            return ResponseEntity.ok(student);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Student not found with id: " + id);
    }

    @GetMapping
    public ResponseEntity<?> getAllStudents() {
        if (rateLimiterService.allowRequest("get-all-students")) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many requests. Try again later.");
        }

        List<Student> students = studentService.findAllStudents();
        return ResponseEntity.ok(students);
    }

    @PostMapping
    public ResponseEntity<?> createStudent(@RequestBody Student student) {
        if (rateLimiterService.allowRequest("create-student")) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many requests. Try again later.");
        }

        Student savedStudent = studentService.saveStudent(student);

        // Add to cache
        Cache cache = cacheManager.getCache("studentCache");
        if (cache != null) {
            cache.put(savedStudent.getId(), savedStudent);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(savedStudent);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable Long id, @RequestBody Student student) {
        if (rateLimiterService.allowRequest("update-student")) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many requests. Try again later.");
        }

        Optional<Student> updatedStudent = studentService.updateStudent(id, student);
        if (updatedStudent.isPresent()) {
            // Update cache
            Cache cache = cacheManager.getCache("studentCache");
            if (cache != null) {
                cache.put(id, updatedStudent.get());
            }
            return ResponseEntity.ok(updatedStudent.get());
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Student not found with id: " + id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id) {
        if (rateLimiterService.allowRequest("delete-student")) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many requests. Try again later.");
        }

        boolean deleted = studentService.deleteStudent(id);
        if (deleted) {
            // Remove from cache
            Cache cache = cacheManager.getCache("studentCache");
            if (cache != null) {
                cache.evict(id);
            }
            return ResponseEntity.ok("Student deleted successfully");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Student not found with id: " + id);
    }

    @PostMapping("/bulk")
    public ResponseEntity<?> createBulkStudents(@RequestBody List<Student> students) {
        if (rateLimiterService.allowRequest("bulk-create")) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many requests. Try again later.");
        }

        List<Student> savedStudents = studentService.saveAllStudents(students);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedStudents);
    }

    @DeleteMapping("/cache/clear")
    public ResponseEntity<?> clearCache() {
        Cache cache = cacheManager.getCache("studentCache");
        if (cache != null) {
            cache.clear();
            return ResponseEntity.ok("Cache cleared successfully");
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Cache not found");
    }
}