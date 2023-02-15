package com.pbq.vote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class PlatApp {
    public static void main(String[] args) {
        SpringApplication.run(PlatApp.class, args);
    }
}
