package com.claude.automator.config;

import lombok.extern.slf4j.Slf4j;
import org.sikuli.script.ImagePath;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.File;

/**
 * Configures the SikuliX ImagePath very early in the Spring lifecycle.
 * This runs before state beans are created to ensure images can be found.
 */
@Slf4j
@Configuration
public class ImagePathConfiguration {
    
    @Value("${brobot.core.image-path:images}")
    private String imagePath;
    
    /**
     * Static initializer runs even before Spring context is fully ready.
     * This ensures ImagePath is set for any static image loading.
     */
    static {
        // Set default image path immediately
        String defaultPath = "images";
        log.info("Setting initial SikuliX ImagePath to: {}", defaultPath);
        ImagePath.setBundlePath(defaultPath);
        
        // Also add common subdirectories if they exist
        File workingDir = new File("images/working");
        if (workingDir.exists()) {
            ImagePath.add(workingDir.getPath());
            log.info("Added working directory to ImagePath: {}", workingDir.getPath());
        }
        
        File promptDir = new File("images/prompt");
        if (promptDir.exists()) {
            ImagePath.add(promptDir.getPath());
            log.info("Added prompt directory to ImagePath: {}", promptDir.getPath());
        }
    }
    
    @PostConstruct
    public void configureImagePath() {
        log.info("Configuring ImagePath with property value: {}", imagePath);
        
        // Update with configured value if different from default
        if (!imagePath.equals("images")) {
            ImagePath.setBundlePath(imagePath);
            log.info("Updated ImagePath to configured value: {}", imagePath);
        }
        
        // Ensure the directory exists
        File imageDir = new File(imagePath);
        if (!imageDir.exists()) {
            log.warn("Image directory does not exist, creating: {}", imageDir.getAbsolutePath());
            imageDir.mkdirs();
        }
        
        // Log current configuration
        log.info("ImagePath bundle path: {}", ImagePath.getBundlePath());
    }
}