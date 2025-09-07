package com.claude.automator.config;

import org.sikuli.script.ImagePath;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import jakarta.annotation.PostConstruct;
import java.io.File;

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
        
        // Configure ImagePath to find images
        configureImagePath();
    }
    
    private void configureImagePath() {
        try {
            // Reset the ImagePath to clear any previous settings
            ImagePath.reset();
            
            // Get the absolute path to the images directory
            File imagesDir = new File("images");
            if (imagesDir.exists() && imagesDir.isDirectory()) {
                String imagePath = imagesDir.getAbsolutePath();
                
                // Set the bundle path first
                ImagePath.setBundlePath(imagePath);
                System.out.println("[SikuliX] Set bundle path: " + ImagePath.getBundlePath());
                
                // Add the main images directory
                ImagePath.add(imagePath);
                System.out.println("[SikuliX] Added image path: " + imagePath);
                
                // Add subdirectories to ImagePath
                File workingDir = new File(imagesDir, "working");
                File promptDir = new File(imagesDir, "prompt");
                
                if (workingDir.exists()) {
                    ImagePath.add(workingDir.getAbsolutePath());
                    System.out.println("[SikuliX] Added working directory to ImagePath: " + workingDir.getAbsolutePath());
                }
                if (promptDir.exists()) {
                    ImagePath.add(promptDir.getAbsolutePath());
                    System.out.println("[SikuliX] Added prompt directory to ImagePath: " + promptDir.getAbsolutePath());
                }
                
                // Also add the current working directory for backward compatibility
                ImagePath.add(System.getProperty("user.dir"));
                System.out.println("[SikuliX] Added current directory to ImagePath: " + System.getProperty("user.dir"));
                
            } else {
                System.err.println("[SikuliX] Images directory not found at: " + imagesDir.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("[SikuliX] Failed to configure ImagePath: " + e.getMessage());
            e.printStackTrace();
        }
    }
}