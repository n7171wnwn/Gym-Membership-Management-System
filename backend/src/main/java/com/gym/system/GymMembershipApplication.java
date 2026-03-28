package com.gym.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GymMembershipApplication {
    public static void main(String[] args) {
        SpringApplication.run(GymMembershipApplication.class, args);
    }
}
