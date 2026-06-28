package com.quan.diabetes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(exclude = { org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration.class })
public class DiabetesApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiabetesApplication.class, args);
    }

}
