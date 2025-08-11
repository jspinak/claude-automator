package com.claude.automator.config;

import lombok.extern.slf4j.Slf4j;
import org.sikuli.script.ImagePath;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Verifies image setup and provides helpful diagnostics on startup.
 */
@Slf4j
@Component
public class ImageSetupVerifier {
    
    @EventListener(ApplicationReadyEvent.class)
    public void verifyImageSetup() {
        log.info("=== IMAGE SETUP VERIFICATION ===");
        
        // Check ImagePath configuration
        String bundlePath = ImagePath.getBundlePath();
        log.info("SikuliX Bundle Path: {}", bundlePath != null ? bundlePath : "NOT SET");
        
        // Check expected directories
        List<String> expectedDirs = Arrays.asList("images", "images/working", "images/prompt");
        for (String dir : expectedDirs) {
            File dirFile = new File(dir);
            if (dirFile.exists()) {
                log.info("✓ Directory exists: {}", dirFile.getAbsolutePath());
                
                // List PNG files in directory
                File[] pngFiles = dirFile.listFiles((d, name) -> name.endsWith(".png"));
                if (pngFiles != null && pngFiles.length > 0) {
                    log.info("  Found {} PNG files:", pngFiles.length);
                    for (File png : pngFiles) {
                        log.info("    - {}", png.getName());
                    }
                } else {
                    log.warn("  No PNG files found in {}", dir);
                }
            } else {
                log.warn("✗ Directory missing: {} (absolute: {})", dir, dirFile.getAbsolutePath());
            }
        }
        
        // Check specific expected images
        List<String> expectedImages = Arrays.asList(
            "images/working/claude-icon-1.png",
            "images/working/claude-icon-2.png",
            "images/working/claude-icon-3.png",
            "images/working/claude-icon-4.png",
            "images/prompt/claude-prompt-1.png",
            "images/prompt/claude-prompt-2.png",
            "images/prompt/claude-prompt-3.png"
        );
        
        log.info("Checking for expected images:");
        for (String imagePath : expectedImages) {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                log.info("  ✓ {}", imagePath);
            } else {
                log.warn("  ✗ MISSING: {} (looking in: {})", imagePath, imageFile.getAbsolutePath());
                
                // Try to find it without the images prefix
                String withoutPrefix = imagePath.replace("images/", "");
                File altFile = new File(withoutPrefix);
                if (altFile.exists()) {
                    log.info("    Found at: {}", altFile.getAbsolutePath());
                }
            }
        }
        
        // Suggest setup commands if images are missing
        boolean anyMissing = expectedImages.stream()
            .map(File::new)
            .anyMatch(f -> !f.exists());
            
        if (anyMissing) {
            log.warn("\n=== IMAGE SETUP REQUIRED ===");
            log.warn("Images are missing. Please ensure you have:");
            log.warn("1. Created the 'images' directory in the project root");
            log.warn("2. Created 'images/working' and 'images/prompt' subdirectories");
            log.warn("3. Placed the pattern images (PNG files) in the appropriate subdirectories");
            log.warn("4. Named them correctly (e.g., claude-icon-1.png, claude-prompt-1.png)");
            log.warn("\nProject root: {}", System.getProperty("user.dir"));
        }
        
        log.info("=== END IMAGE VERIFICATION ===");
    }
}