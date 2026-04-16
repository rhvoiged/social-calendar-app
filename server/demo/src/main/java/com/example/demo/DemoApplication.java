package com.example.demo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// the entry point of the application

@SpringBootApplication // enables Spring Boot's autoconfiguration and component scanning
@EnableScheduling // enables the background scheduling system
public class DemoApplication { public static void main(String[] args) {SpringApplication.run(DemoApplication.class, args);}} // starts the Spring Boot server
