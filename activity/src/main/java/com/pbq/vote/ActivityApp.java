package com.pbq.vote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class ActivityApp {
    public static void main(String[] args) {
        SpringApplication.run(ActivityApp.class, args);
    }
}
