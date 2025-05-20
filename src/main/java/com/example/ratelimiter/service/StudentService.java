package com.example.ratelimiter.service;

import com.example.ratelimiter.model.Student;
import com.example.ratelimiter.repo.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    public Optional<Student> findStudent(Long id) {
        return studentRepository.findById(id);
    }
}
