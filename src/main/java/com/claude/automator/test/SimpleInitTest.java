package com.claude.automator.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.claude.automator", "io.github.jspinak.brobot"})
@Slf4j
public class SimpleInitTest implements CommandLineRunner {
    
    public static void main(String[] args) {
        System.out.println("Starting SimpleInitTest...");
        SpringApplication.run(SimpleInitTest.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== SIMPLE INIT TEST ===");
        log.info("Spring context initialized successfully");
        System.out.println("Test complete");
        System.exit(0);
    }
}