package com.example.ratelimiter.config;

import com.example.ratelimiter.model.Student;
import com.example.ratelimiter.repo.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private StudentRepository studentRepository;

    @Override
    public void run(String... args) throws Exception {
        // Check if data already exists
        if (studentRepository.count() == 0) {
            Student[] students = {
                    new Student(0, "Alice Johnson", "A+"),
                    new Student(0, "Bob Smith", "B+"),
                    new Student(0, "Charlie Brown", "A"),
                    new Student(0, "Diana Prince", "A+"),
                    new Student(0, "Eve Wilson", "B"),
                    new Student(0, "Frank Miller", "C+"),
                    new Student(0, "Grace Lee", "A"),
                    new Student(0, "Henry Adams", "B+"),
                    new Student(0, "Ivy Chen", "A+"),
                    new Student(0, "Jack Davis", "B")
            };

            studentRepository.saveAll(Arrays.asList(students));
            System.out.println("✅ Sample data initialized: " + students.length + " students added");
        } else {
            System.out.println("✅ Database already contains data. Skipping initialization.");
        }
    }
}