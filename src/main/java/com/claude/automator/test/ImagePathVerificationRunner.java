package com.claude.automator.test;

import lombok.extern.slf4j.Slf4j;
import org.sikuli.script.ImagePath;
import org.sikuli.script.Pattern;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Verifies that ImagePath configuration is working correctly.
 * Run with: ./gradlew bootRun --args="--spring.profiles.active=verify-images"
 */
@Component
@Profile("verify-images")
@Slf4j
public class ImagePathVerificationRunner implements CommandLineRunner {
    
    @Override
    public void run(String... args) throws Exception {
        log.info("=== Image Path Verification ===");
        
        // Wait for ImagePathConfiguration to run
        Thread.sleep(1000);
        
        log.info("Bundle path: {}", ImagePath.getBundlePath());
        
        // Test loading the images that were failing
        String[] testImages = {
            "working/claude-icon-1.png",
            "working/claude-icon-2.png",
            "prompt/claude-prompt-1.png",
            "prompt/claude-prompt-2.png"
        };
        
        int successCount = 0;
        for (String imagePath : testImages) {
            try {
                Pattern pattern = new Pattern(imagePath);
                if (pattern.getBImage() != null) {
                    log.info("✓ Successfully loaded: {}", imagePath);
                    successCount++;
                } else {
                    log.error("✗ Failed to load: {}", imagePath);
                }
            } catch (Exception e) {
                log.error("✗ Error loading {}: {}", imagePath, e.getMessage());
            }
        }
        
        log.info("=== Verification Complete: {}/{} images loaded ===", successCount, testImages.length);
        
        // Exit after verification
        System.exit(successCount == testImages.length ? 0 : 1);
    }
}