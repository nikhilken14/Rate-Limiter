package com.example.ratelimiter.repo;

import com.example.ratelimiter.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    @Query("Select s from Student s where s.id= :id")
    List<Student> findStudent(long id);
}
