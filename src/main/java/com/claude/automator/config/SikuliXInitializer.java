package com.claude.automator.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import jakarta.annotation.PostConstruct;

/**
 * Forces SikuliX to use its bundled OpenCV instead of JavaCV's.
 * This must run before any SikuliX or Brobot code.
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SikuliXInitializer {
    
    static {
        try {
            // Force SikuliX to use its bundled OpenCV 4.3.0
            // This prevents it from trying to load opencv_java490
            System.setProperty("sikuli.opencv_core", "430");
            System.setProperty("sikuli.opencv_version", "430");
            System.setProperty("sikuli.useJavaCV", "false");
            System.setProperty("sikuli.Debug", "0");
            
            System.out.println("[SikuliX] Configured to use bundled OpenCV 4.3.0");
            
        } catch (Exception e) {
            System.err.println("[SikuliX] Failed to configure SikuliX: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @PostConstruct
    public void init() {
        System.out.println("[SikuliX] Using bundled OpenCV to avoid conflicts with JavaCV");
    }
}