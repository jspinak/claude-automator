package com.claude.automator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
@ComponentScan(basePackages = {"com.claude.automator", "io.github.jspinak.brobot"})
public class ClaudeAutomatorApplication {

    public static void main(String[] args) {
        // Force SikuliX debug mode and proper initialization
        System.setProperty("sikuli.Debug", "3");
        System.setProperty("sikuli.Silence", "false");
        System.setProperty("java.awt.headless", "false");
        
        // Additional SikuliX properties for macOS
        System.setProperty("apple.awt.UIElement", "false");
        
        SpringApplication.run(ClaudeAutomatorApplication.class, args);
    }
}