package com.claude.automator.util;

import lombok.extern.slf4j.Slf4j;
import org.sikuli.script.ImagePath;
import org.sikuli.script.Pattern;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Utility class to help diagnose image loading issues.
 */
@Slf4j
public class ImageLoadingDiagnostics {
    
    /**
     * Run comprehensive diagnostics for image loading.
     */
    public static void runDiagnostics() {
        log.info("=== Image Loading Diagnostics ===");
        
        // 1. Current working directory
        String workingDir = System.getProperty("user.dir");
        log.info("Working Directory: {}", workingDir);
        
        // 2. Check if images directory exists
        File imagesDir = new File("images");
        log.info("Images directory (relative): exists={}, absolute={}", 
                imagesDir.exists(), imagesDir.getAbsolutePath());
        
        // 3. List files in images directory if it exists
        if (imagesDir.exists() && imagesDir.isDirectory()) {
            listFilesRecursively(imagesDir, "");
        }
        
        // 4. Check SikuliX ImagePath configuration
        log.info("\n=== SikuliX ImagePath Configuration ===");
        String bundlePath = ImagePath.getBundlePath();
        log.info("Bundle Path: {}", bundlePath);
        
        List<ImagePath.PathEntry> pathEntries = ImagePath.getPaths();
        log.info("Image Paths ({} total):", pathEntries.size());
        for (ImagePath.PathEntry entry : pathEntries) {
            log.info("  - {}", entry.getPath());
        }
        
        // 5. Test loading specific images
        log.info("\n=== Testing Image Loading ===");
        String[] testImages = {
            "working/claude-icon-1",
            "working/claude-icon-1.png",
            "images/working/claude-icon-1",
            "images/working/claude-icon-1.png"
        };
        
        for (String testImage : testImages) {
            testImageLoading(testImage);
        }
        
        // 6. Check absolute path loading
        log.info("\n=== Testing Absolute Path Loading ===");
        File absoluteImage = new File(workingDir, "images/working/claude-icon-1.png");
        if (absoluteImage.exists()) {
            testImageLoading(absoluteImage.getAbsolutePath());
        } else {
            log.warn("Absolute image not found: {}", absoluteImage.getAbsolutePath());
        }
        
        log.info("=== Diagnostics Complete ===");
    }
    
    private static void listFilesRecursively(File dir, String indent) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                log.info("{}|- {}", indent, file.getName());
                if (file.isDirectory()) {
                    listFilesRecursively(file, indent + "  ");
                }
            }
        }
    }
    
    private static void testImageLoading(String imagePath) {
        try {
            log.info("Testing: '{}'", imagePath);
            
            // Test direct file existence
            File file = new File(imagePath);
            log.info("  File exists: {}", file.exists());
            
            // Test with .png extension
            File fileWithPng = new File(imagePath.endsWith(".png") ? imagePath : imagePath + ".png");
            log.info("  File with .png exists: {}", fileWithPng.exists());
            
            // Test SikuliX Pattern loading
            Pattern pattern = new Pattern(imagePath);
            boolean loaded = pattern.getBImage() != null;
            log.info("  SikuliX Pattern loaded: {}", loaded);
            
            if (!loaded) {
                // Try with different variations
                if (!imagePath.endsWith(".png")) {
                    Pattern patternWithPng = new Pattern(imagePath + ".png");
                    boolean loadedWithPng = patternWithPng.getBImage() != null;
                    log.info("  SikuliX Pattern with .png loaded: {}", loadedWithPng);
                }
            }
            
        } catch (Exception e) {
            log.error("  Error testing image: {}", e.getMessage());
        }
    }
    
    /**
     * Get a diagnostic report as a string.
     */
    public static String getDiagnosticReport() {
        StringBuilder report = new StringBuilder();
        report.append("Image Loading Diagnostic Report\n");
        report.append("==============================\n\n");
        
        // Working directory
        report.append("Working Directory: ").append(System.getProperty("user.dir")).append("\n");
        
        // Images directory
        File imagesDir = new File("images");
        report.append("Images Directory: ").append(imagesDir.getAbsolutePath()).append("\n");
        report.append("Images Directory Exists: ").append(imagesDir.exists()).append("\n\n");
        
        // SikuliX configuration
        report.append("SikuliX Configuration:\n");
        report.append("  Bundle Path: ").append(ImagePath.getBundlePath()).append("\n");
        List<ImagePath.PathEntry> pathEntries = ImagePath.getPaths();
        report.append("  Image Paths: ").append(pathEntries.size()).append(" configured\n");
        for (ImagePath.PathEntry entry : pathEntries) {
            report.append("    - ").append(entry.getPath()).append("\n");
        }
        
        return report.toString();
    }
}