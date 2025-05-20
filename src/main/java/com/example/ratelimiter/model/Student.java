package com.example.ratelimiter.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;
    private String result;

    public Student() {}

    public Student(long id, String name, String result) {
        this.id = id;
        this.name = name;
        this.result = result;
    }

    public Student(long id, String studentName) {
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResult(){
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }


}
