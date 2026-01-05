package com.example.ratelimiter.service;

import com.example.ratelimiter.model.Student;
import com.example.ratelimiter.repo.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    /**
     * READ PATH (CACHEABLE)
     * ----------------------------------
     * First call → DB
     * Subsequent calls → Caffeine Cache
     */
    @Cacheable(value = "students", key = "#id")
    public Optional<Student> findStudent(Long id) {
        return studentRepository.findById(id);
    }

    /**
     * READ-ALL (NO CACHE)
     * ----------------------------------
     * Intentionally not cached to avoid
     * large dataset memory pressure.
     */
    public List<Student> findAllStudents() {
        return studentRepository.findAll();
    }

    /**
     * WRITE PATH (CACHE PUT)
     * ----------------------------------
     * Ensures cache consistency on insert.
     */
    @CachePut(value = "students", key = "#result.id")
    public Student saveStudent(Student student) {
        return studentRepository.save(student);
    }

    /**
     * BULK WRITE (CACHE EVICT)
     * ----------------------------------
     * Bulk ops invalidate cache to
     * avoid partial stale state.
     */
    @CacheEvict(value = "students", allEntries = true)
    public List<Student> saveAllStudents(List<Student> students) {
        return studentRepository.saveAll(students);
    }

    /**
     * UPDATE PATH (CACHE PUT)
     * ----------------------------------
     * Overwrites cache with latest entity.
     */
    @CachePut(value = "students", key = "#id")
    public Optional<Student> updateStudent(Long id, Student studentDetails) {

        Optional<Student> existingStudent = studentRepository.findById(id);

        if (existingStudent.isPresent()) {
            Student student = existingStudent.get();
            student.setName(studentDetails.getName());
            student.setResult(studentDetails.getResult());
            return Optional.of(studentRepository.save(student));
        }

        return Optional.empty();
    }

    /**
     * DELETE PATH (CACHE EVICT)
     * ----------------------------------
     * Removes entity from cache and DB.
     */
    @CacheEvict(value = "students", key = "#id")
    public boolean deleteStudent(Long id) {

        if (studentRepository.existsById(id)) {
            studentRepository.deleteById(id);
            return true;
        }

        return false;
    }

    /**
     * METRICS SUPPORT
     * ----------------------------------
     * Used by StatisticsController.
     */
    public long countStudents() {
        return studentRepository.count();
    }
}
