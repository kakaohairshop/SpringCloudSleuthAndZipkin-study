package com.example.repository;

import org.springframework.stereotype.Repository;

@Repository
public class ThirdRepository {

    public String findAny() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Third Repository Access!";
    }
}
