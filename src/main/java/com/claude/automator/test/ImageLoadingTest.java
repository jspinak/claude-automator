package com.claude.automator.test;

import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import lombok.extern.slf4j.Slf4j;
import org.sikuli.script.ImagePath;
import org.sikuli.script.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Test component to diagnose image loading issues.
 */
@Component
@ConditionalOnProperty(name = "test.image-loading", havingValue = "true")
@Slf4j
public class ImageLoadingTest implements CommandLineRunner {
    
    @Autowired(required = false)
    private BrobotLogger brobotLogger;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("===== Image Loading Test Starting =====");
        
        // Log current ImagePath configuration
        String[] paths = ImagePath.getPaths();
        log.info("Current ImagePath has {} paths:", paths.length);
        for (String path : paths) {
            log.info("  - {}", path);
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                log.info("    Directory exists with {} files", dir.listFiles() != null ? dir.listFiles().length : 0);
            } else {
                log.info("    Directory does NOT exist");
            }
        }
        log.info("Bundle path: {}", ImagePath.getBundlePath());
        
        // Test loading images with different approaches
        String[] testImages = {
            "working/claude-icon-1",
            "working/claude-icon-1.png",
            "claude-icon-1",
            "claude-icon-1.png"
        };
        
        for (String imagePath : testImages) {
            log.info("\nTesting image: {}", imagePath);
            
            // Test 1: SikuliX Pattern direct
            try {
                Pattern pattern = new Pattern(imagePath);
                BufferedImage bImage = pattern.getBImage();
                if (bImage != null) {
                    log.info("  ✓ SikuliX Pattern loaded successfully ({}x{})", bImage.getWidth(), bImage.getHeight());
                } else {
                    log.warn("  ✗ SikuliX Pattern returned null BufferedImage");
                }
            } catch (Exception e) {
                log.error("  ✗ SikuliX Pattern failed: {}", e.getMessage());
            }
            
            // Test 2: Check if file exists with .png extension
            for (String path : paths) {
                File imageFile = new File(path, imagePath + (imagePath.endsWith(".png") ? "" : ".png"));
                if (imageFile.exists()) {
                    log.info("  ✓ Found file at: {}", imageFile.getAbsolutePath());
                }
            }
        }
        
        // List actual files in working directory
        File workingImages = new File("images/working");
        if (workingImages.exists() && workingImages.isDirectory()) {
            log.info("\nFiles in images/working directory:");
            File[] files = workingImages.listFiles();
            if (files != null) {
                for (File file : files) {
                    log.info("  - {}", file.getName());
                }
            }
        }
        
        // Try to load using Brobot's Pattern class
        log.info("\nTesting Brobot Pattern class:");
        try {
            io.github.jspinak.brobot.model.element.Pattern brobotPattern = 
                new io.github.jspinak.brobot.model.element.Pattern("working/claude-icon-1");
            log.info("  Brobot Pattern created: {}", brobotPattern.getName());
            log.info("  Has image: {}", brobotPattern.getImage() != null);
            if (brobotPattern.getImage() != null) {
                log.info("  Image size: {}x{}", brobotPattern.w(), brobotPattern.h());
            }
        } catch (Exception e) {
            log.error("  Failed to create Brobot Pattern", e);
        }
        
        log.info("===== Image Loading Test Complete =====");
        
        if (brobotLogger != null) {
            brobotLogger.log()
                .observation("Image loading test completed")
                .metadata("imagePathCount", paths.length)
                .metadata("bundlePath", ImagePath.getBundlePath())
                .log();
        }
    }
}