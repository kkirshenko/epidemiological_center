package com.sanepidcenter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Sanitary-Epidemiological Center Management System.
 * 
 * Technology Stack:
 * - Server Language: Java 17+
 * - Server Framework: Spring Boot
 * - Client Side: HTML, CSS, JavaScript (Thymeleaf templates)
 * - Database: PostgreSQL
 * - ORM: Spring Data JPA / Hibernate
 * - Build Tool: Gradle
 */
@SpringBootApplication
public class SanepidcenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(SanepidcenterApplication.class, args);
    }
}
