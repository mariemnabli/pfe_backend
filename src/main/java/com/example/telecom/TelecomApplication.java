package com.example.telecom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TelecomApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelecomApplication.class, args);
    }

}
