package com.claude.automator.config;

import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import lombok.extern.slf4j.Slf4j;
import org.sikuli.script.ImagePath;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Configuration class to set up SikuliX ImagePath for loading images.
 * This ensures images can be found both during development and when running from JAR.
 * Uses InitializingBean to ensure it runs before other beans that might need images.
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class ImagePathConfig implements InitializingBean {
    
    @Autowired(required = false)
    private BrobotLogger brobotLogger;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        configureImagePath();
    }
    
    private void configureImagePath() {
        log.info("Configuring SikuliX ImagePath for image loading");
        
        try {
            // Clear existing paths to start fresh
            ImagePath.reset();
            
            // Try multiple strategies to find the images directory
            boolean pathAdded = false;
            
            // Strategy 1: Check for images directory relative to working directory
            File workingDirImages = new File("images");
            if (workingDirImages.exists() && workingDirImages.isDirectory()) {
                String absolutePath = workingDirImages.getAbsolutePath();
                ImagePath.add(absolutePath);
                log.info("Added images directory from working directory: {}", absolutePath);
                pathAdded = true;
            }
            
            // Strategy 2: Check for images directory relative to JAR location
            if (!pathAdded) {
                try {
                    // Get the location of this class
                    URL location = ImagePathConfig.class.getProtectionDomain().getCodeSource().getLocation();
                    File jarFile = new File(location.toURI());
                    File jarDir = jarFile.getParentFile();
                    
                    // Look for images directory next to the JAR
                    File jarDirImages = new File(jarDir, "images");
                    if (jarDirImages.exists() && jarDirImages.isDirectory()) {
                        String absolutePath = jarDirImages.getAbsolutePath();
                        ImagePath.add(absolutePath);
                        log.info("Added images directory relative to JAR: {}", absolutePath);
                        pathAdded = true;
                    }
                } catch (Exception e) {
                    log.warn("Could not determine JAR location: {}", e.getMessage());
                }
            }
            
            // Strategy 3: Check common locations
            if (!pathAdded) {
                String[] commonPaths = {
                    System.getProperty("user.dir") + File.separator + "images",
                    System.getProperty("user.home") + File.separator + "Documents" + File.separator + "brobot-parent-directory" + File.separator + "claude-automator" + File.separator + "images",
                    "/home/jspinak/brobot-parent-directory/claude-automator/images",
                    "C:\\Users\\Joshua\\Documents\\brobot-parent-directory\\claude-automator\\images"
                };
                
                for (String path : commonPaths) {
                    File imagesDir = new File(path);
                    if (imagesDir.exists() && imagesDir.isDirectory()) {
                        String absolutePath = imagesDir.getAbsolutePath();
                        ImagePath.add(absolutePath);
                        log.info("Added images directory from common location: {}", absolutePath);
                        pathAdded = true;
                        break;
                    }
                }
            }
            
            // Strategy 4: If nothing else worked, add the current directory
            if (!pathAdded) {
                String currentDir = System.getProperty("user.dir");
                ImagePath.add(currentDir);
                log.warn("No images directory found, adding current directory to ImagePath: {}", currentDir);
            }
            
            // Also set the bundle path for SikuliX
            if (pathAdded) {
                // Get the first path we added
                List<ImagePath.PathEntry> pathEntries = ImagePath.getPaths();
                if (!pathEntries.isEmpty()) {
                    String bundlePath = pathEntries.get(0).getPath();
                    ImagePath.setBundlePath(bundlePath);
                    log.info("Set SikuliX bundle path to: {}", bundlePath);
                }
            }
            
            // Log final configuration
            List<ImagePath.PathEntry> pathEntries = ImagePath.getPaths();
            log.info("SikuliX ImagePath configured with {} path(s):", pathEntries.size());
            for (ImagePath.PathEntry entry : pathEntries) {
                log.info("  - {}", entry.getPath());
            }
            
            // Log with BrobotLogger if available
            if (brobotLogger != null) {
                String[] pathStrings = pathEntries.stream()
                    .map(ImagePath.PathEntry::getPath)
                    .toArray(String[]::new);
                brobotLogger.log()
                    .observation("SikuliX ImagePath configured")
                    .metadata("pathCount", pathEntries.size())
                    .metadata("paths", pathStrings)
                    .metadata("bundlePath", ImagePath.getBundlePath())
                    .log();
            }
            
            // Verify a test image can be loaded
            verifyImageLoading();
            
        } catch (Exception e) {
            log.error("Failed to configure ImagePath", e);
            if (brobotLogger != null) {
                brobotLogger.error("ImagePath configuration failed", e);
            }
        }
    }
    
    private void verifyImageLoading() {
        try {
            // Try to load a test image using the same mechanism as Pattern
            String[] testImages = {
                "working/claude-icon-1",
                "working/claude-icon-1.png",
                "prompt/claude-prompt-1",
                "prompt/claude-prompt-1.png"
            };
            
            for (String testImage : testImages) {
                try {
                    org.sikuli.script.Pattern testPattern = new org.sikuli.script.Pattern(testImage);
                    
                    if (testPattern.getBImage() != null) {
                        log.info("Successfully verified image loading with test image: {}", testImage);
                        if (brobotLogger != null) {
                            brobotLogger.observation("Image loading verification successful for: " + testImage);
                        }
                        return; // Success!
                    } else {
                        log.warn("Failed to load test image: {}", testImage);
                    }
                } catch (Exception e) {
                    log.warn("Exception loading test image {}: {}", testImage, e.getMessage());
                }
            }
            
            log.error("Failed to load any test images!");
            log.error("Make sure the images directory contains the expected structure:");
            log.error("  images/working/claude-icon-1.png");
            log.error("  images/prompt/claude-prompt-1.png");
            
            if (brobotLogger != null) {
                brobotLogger.error("Image loading verification failed for all test images", new RuntimeException("No test images could be loaded"));
            }
        } catch (Exception e) {
            log.error("Error during image loading verification", e);
        }
    }
}