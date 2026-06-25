package com.quan.diabetes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DiabetesApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiabetesApplication.class, args);
    }

}
