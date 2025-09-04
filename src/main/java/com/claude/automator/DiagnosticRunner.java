package com.claude.automator;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@Profile("diagnostic")
public class DiagnosticRunner implements CommandLineRunner {
    
    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "diagnostic");
        SpringApplication.run(DiagnosticRunner.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("Starting diagnostic from Spring Boot context...");
        SimpleDiagnostic.main(args);
        System.exit(0);
    }
}