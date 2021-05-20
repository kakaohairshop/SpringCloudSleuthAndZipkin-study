package com.example.repository;

import org.springframework.stereotype.Repository;

@Repository
public class SecondRepository {

    public String findAny() {
        try {
            Thread.sleep(75);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Second Repository Access!";
    }

    public void createError() {
        throw new RuntimeException("sleuth error log test");
    }
}
