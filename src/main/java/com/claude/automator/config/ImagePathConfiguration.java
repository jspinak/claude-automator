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
        // Get absolute path to images directory
        File imagesDir = new File("images");
        String absolutePath = imagesDir.getAbsolutePath();
        
        log.info("Setting initial SikuliX ImagePath to absolute path: {}", absolutePath);
        
        // Reset ImagePath first to clear any existing paths
        ImagePath.reset();
        
        // Set the bundle path to the absolute path
        ImagePath.setBundlePath(absolutePath);
        
        // Add the base images directory
        ImagePath.add(absolutePath);
        
        // Also add subdirectories with absolute paths
        File workingDir = new File(imagesDir, "working");
        if (workingDir.exists()) {
            String workingPath = workingDir.getAbsolutePath();
            ImagePath.add(workingPath);
            log.info("Added working directory to ImagePath: {}", workingPath);
        }
        
        File promptDir = new File(imagesDir, "prompt");
        if (promptDir.exists()) {
            String promptPath = promptDir.getAbsolutePath();
            ImagePath.add(promptPath);
            log.info("Added prompt directory to ImagePath: {}", promptPath);
        }
        
        // Log all paths
        log.info("ImagePath configured with paths: {}", ImagePath.getPaths());
    }
    
    @PostConstruct
    public void configureImagePath() {
        log.info("Post-construct ImagePath configuration");
        
        // Get absolute path for the configured image path
        File imageDir = new File(imagePath);
        String absolutePath = imageDir.getAbsolutePath();
        
        // Ensure the directory exists
        if (!imageDir.exists()) {
            log.warn("Image directory does not exist, creating: {}", absolutePath);
            imageDir.mkdirs();
        }
        
        // Only update if different from what was set in static block
        File defaultDir = new File("images");
        if (!imageDir.getAbsolutePath().equals(defaultDir.getAbsolutePath())) {
            log.info("Updating ImagePath to configured value: {}", absolutePath);
            
            // Reset and reconfigure with new path
            ImagePath.reset();
            ImagePath.setBundlePath(absolutePath);
            ImagePath.add(absolutePath);
            
            // Add subdirectories
            File workingDir = new File(imageDir, "working");
            if (workingDir.exists()) {
                ImagePath.add(workingDir.getAbsolutePath());
                log.info("Added working directory: {}", workingDir.getAbsolutePath());
            }
            
            File promptDir = new File(imageDir, "prompt");
            if (promptDir.exists()) {
                ImagePath.add(promptDir.getAbsolutePath());
                log.info("Added prompt directory: {}", promptDir.getAbsolutePath());
            }
        }
        
        // Log final configuration
        log.info("Final ImagePath bundle path: {}", ImagePath.getBundlePath());
        log.info("Final ImagePath paths: {}", ImagePath.getPaths());
    }
}