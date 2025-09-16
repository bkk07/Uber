package com.uber.addressservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
@SpringBootApplication
@EnableDiscoveryClient
@EnableMethodSecurity
public class AddressServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AddressServiceApplication.class, args);
    }
}