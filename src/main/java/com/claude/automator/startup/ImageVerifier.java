package com.claude.automator.startup;

import lombok.extern.slf4j.Slf4j;
import org.sikuli.script.ImagePath;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Verifies that images are accessible on startup.
 * Creates symbolic links if needed when running from different directories.
 */
@Component
@Order(1) // Run early in startup
@Slf4j
public class ImageVerifier implements ApplicationRunner {
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("=== Image Verification Starting ===");
        
        // Get current working directory
        String workingDir = System.getProperty("user.dir");
        log.info("Working directory: {}", workingDir);
        
        // Check if images directory exists in working directory
        File imagesInWorkingDir = new File(workingDir, "images");
        
        if (!imagesInWorkingDir.exists()) {
            log.warn("Images directory not found in working directory: {}", imagesInWorkingDir.getAbsolutePath());
            
            // Try to find the actual images directory
            String[] possiblePaths = {
                "/home/jspinak/brobot-parent-directory/claude-automator/images",
                System.getProperty("user.home") + "/Documents/brobot-parent-directory/claude-automator/images",
                System.getProperty("user.home") + "/brobot-parent-directory/claude-automator/images"
            };
            
            File actualImagesDir = null;
            for (String path : possiblePaths) {
                File dir = new File(path);
                if (dir.exists() && dir.isDirectory()) {
                    actualImagesDir = dir;
                    log.info("Found images directory at: {}", dir.getAbsolutePath());
                    break;
                }
            }
            
            if (actualImagesDir != null) {
                // For Windows, we can't create symbolic links easily, so add to ImagePath instead
                ImagePath.add(actualImagesDir.getAbsolutePath());
                log.info("Added images directory to SikuliX ImagePath: {}", actualImagesDir.getAbsolutePath());
                
                // Also add the parent directory so relative paths work
                ImagePath.add(actualImagesDir.getParentFile().getAbsolutePath());
                log.info("Added parent directory to ImagePath: {}", actualImagesDir.getParentFile().getAbsolutePath());
            } else {
                log.error("Could not find images directory in any expected location!");
                log.error("Please ensure the images directory exists at one of these locations:");
                for (String path : possiblePaths) {
                    log.error("  - {}", path);
                }
            }
        } else {
            log.info("Images directory found in working directory: {}", imagesInWorkingDir.getAbsolutePath());
            
            // Verify specific images exist
            String[] requiredImages = {
                "working/claude-icon-1.png",
                "working/claude-icon-2.png", 
                "working/claude-icon-3.png",
                "working/claude-icon-4.png",
                "prompt/claude-prompt.png"
            };
            
            boolean allImagesFound = true;
            for (String imagePath : requiredImages) {
                File imageFile = new File(imagesInWorkingDir, imagePath);
                if (!imageFile.exists()) {
                    log.error("Required image missing: {}", imageFile.getAbsolutePath());
                    allImagesFound = false;
                } else {
                    log.debug("Found required image: {}", imageFile.getName());
                }
            }
            
            if (allImagesFound) {
                log.info("All required images verified successfully!");
            } else {
                log.error("Some required images are missing. Please check the images directory.");
            }
        }
        
        // Log final ImagePath configuration
        List<ImagePath.PathEntry> pathEntries = ImagePath.getPaths();
        log.info("Final SikuliX ImagePath configuration ({} paths):", pathEntries.size());
        for (ImagePath.PathEntry entry : pathEntries) {
            log.info("  - {}", entry.getPath());
        }
        
        log.info("=== Image Verification Complete ===");
    }
}