package com.claude.automator.test;

import lombok.extern.slf4j.Slf4j;
import org.sikuli.script.ImagePath;
import org.sikuli.script.Pattern;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Test class to verify ImagePath configuration.
 * Only runs when the 'test-images' profile is active.
 */
@Component
@Profile("test-images")
@Slf4j
public class ImagePathTest implements CommandLineRunner {
    
    @Override
    public void run(String... args) throws Exception {
        log.info("=== Running ImagePath Test ===");
        
        // Log current ImagePath configuration
        String[] paths = ImagePath.getPaths();
        log.info("Current ImagePath has {} paths:", paths.length);
        for (String path : paths) {
            log.info("  - {}", path);
        }
        
        // Test different ways to load the image
        String[] testPaths = {
            "working/claude-icon-1",
            "working/claude-icon-1.png",
            "images/working/claude-icon-1",
            "images/working/claude-icon-1.png"
        };
        
        for (String testPath : testPaths) {
            log.info("\nTesting path: '{}'", testPath);
            
            // Check file existence
            File file = new File(testPath);
            log.info("  Direct file exists: {}", file.exists());
            
            // Try with ImagePath
            for (String imagePath : paths) {
                File fullPath = new File(imagePath, testPath);
                if (fullPath.exists()) {
                    log.info("  Found in ImagePath: {}", fullPath.getAbsolutePath());
                }
            }
            
            // Try loading with Pattern
            try {
                Pattern pattern = new Pattern(testPath);
                boolean loaded = pattern.getBImage() != null;
                log.info("  Pattern loaded: {}", loaded);
                
                if (loaded) {
                    log.info("  Image dimensions: {}x{}", 
                            pattern.getBImage().getWidth(), 
                            pattern.getBImage().getHeight());
                }
            } catch (Exception e) {
                log.error("  Error loading pattern: {}", e.getMessage());
            }
        }
        
        log.info("\n=== ImagePath Test Complete ===");
    }
}