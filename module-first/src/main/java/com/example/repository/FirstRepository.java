package com.example.repository;

import org.springframework.stereotype.Repository;

@Repository
public class FirstRepository {

    public String findAny() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "First Repository Access!";
    }
}
