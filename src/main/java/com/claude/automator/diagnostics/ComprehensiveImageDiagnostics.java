package com.claude.automator.diagnostics;

import lombok.extern.slf4j.Slf4j;
import org.sikuli.script.ImagePath;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Comprehensive diagnostics for image loading issues.
 * This component logs detailed information about:
 * - Current working directory
 * - ClassLoader resource paths
 * - SikuliX ImagePath configuration
 * - File system state
 * - Spring Boot timing
 */
@Slf4j
@Component
@Order(1) // Run very early
public class ComprehensiveImageDiagnostics {
    
    @EventListener(ApplicationStartedEvent.class)
    public void runComprehensiveDiagnostics() {
        log.info("================================================================================");
        log.info("                     COMPREHENSIVE IMAGE DIAGNOSTICS");
        log.info("================================================================================");
        
        // 1. JVM and Environment Info
        logEnvironmentInfo();
        
        // 2. Working Directory Analysis
        logWorkingDirectory();
        
        // 3. ClassLoader and Classpath Analysis
        logClasspathInfo();
        
        // 4. SikuliX ImagePath Deep Dive
        logSikuliXConfiguration();
        
        // 5. File System Scan
        scanFileSystem();
        
        // 6. Resource Loading Test
        testResourceLoading();
        
        // 7. Direct Image Load Test
        testDirectImageLoading();
        
        // 8. Timing Analysis
        logTimingInfo();
        
        log.info("================================================================================");
        log.info("                     END COMPREHENSIVE DIAGNOSTICS");
        log.info("================================================================================");
    }
    
    private void logEnvironmentInfo() {
        log.info("=== ENVIRONMENT INFO ===");
        log.info("Java Version: {}", System.getProperty("java.version"));
        log.info("Java Home: {}", System.getProperty("java.home"));
        log.info("OS: {} {} {}", 
            System.getProperty("os.name"),
            System.getProperty("os.version"),
            System.getProperty("os.arch"));
        log.info("User: {}", System.getProperty("user.name"));
        log.info("User Home: {}", System.getProperty("user.home"));
        log.info("Temp Dir: {}", System.getProperty("java.io.tmpdir"));
        log.info("File Encoding: {}", System.getProperty("file.encoding"));
        log.info("File Separator: '{}'", File.separator);
        log.info("Path Separator: '{}'", File.pathSeparator);
    }
    
    private void logWorkingDirectory() {
        log.info("=== WORKING DIRECTORY ===");
        String userDir = System.getProperty("user.dir");
        log.info("user.dir: {}", userDir);
        
        try {
            Path currentPath = Paths.get("").toAbsolutePath();
            log.info("Current absolute path: {}", currentPath);
            log.info("Current canonical path: {}", currentPath.toFile().getCanonicalPath());
            
            // List immediate subdirectories
            log.info("Subdirectories in working directory:");
            Files.list(currentPath)
                .filter(Files::isDirectory)
                .forEach(dir -> log.info("  DIR: {}", dir.getFileName()));
                
            // List any .png files in working directory
            log.info("PNG files in working directory:");
            Files.list(currentPath)
                .filter(path -> path.toString().endsWith(".png"))
                .forEach(file -> log.info("  PNG: {}", file.getFileName()));
                
        } catch (IOException e) {
            log.error("Error examining working directory", e);
        }
    }
    
    private void logClasspathInfo() {
        log.info("=== CLASSPATH ANALYSIS ===");
        
        // Get classpath
        String classpath = System.getProperty("java.class.path");
        log.info("Classpath entries: {}", classpath.split(File.pathSeparator).length);
        
        // Check for JAR files that might contain images
        Arrays.stream(classpath.split(File.pathSeparator))
            .filter(entry -> entry.contains("claude-automator"))
            .forEach(entry -> log.info("  Claude-automator entry: {}", entry));
            
        // Check ClassLoader
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        log.info("Context ClassLoader: {}", cl.getClass().getName());
        
        // Try to find resources
        try {
            URL imagesUrl = cl.getResource("images");
            log.info("ClassLoader resource 'images': {}", imagesUrl);
            
            URL workingUrl = cl.getResource("images/working");
            log.info("ClassLoader resource 'images/working': {}", workingUrl);
            
            URL promptUrl = cl.getResource("images/prompt");
            log.info("ClassLoader resource 'images/prompt': {}", promptUrl);
            
            // Try without images prefix
            URL workingDirect = cl.getResource("working");
            log.info("ClassLoader resource 'working': {}", workingDirect);
            
            URL promptDirect = cl.getResource("prompt");
            log.info("ClassLoader resource 'prompt': {}", promptDirect);
            
        } catch (Exception e) {
            log.error("Error checking classpath resources", e);
        }
    }
    
    private void logSikuliXConfiguration() {
        log.info("=== SIKULI-X CONFIGURATION ===");
        
        try {
            // Get bundle path
            String bundlePath = ImagePath.getBundlePath();
            log.info("SikuliX Bundle Path: '{}'", bundlePath != null ? bundlePath : "NULL");
            
            // Use reflection to get internal ImagePath state
            try {
                Field pathListField = ImagePath.class.getDeclaredField("pathList");
                pathListField.setAccessible(true);
                Object pathList = pathListField.get(null);
                log.info("SikuliX pathList type: {}", pathList != null ? pathList.getClass().getName() : "NULL");
                log.info("SikuliX pathList content: {}", pathList);
            } catch (Exception e) {
                log.warn("Could not access ImagePath.pathList via reflection: {}", e.getMessage());
            }
            
            // Try to call ImagePath methods
            try {
                Method getPathsMethod = ImagePath.class.getMethod("getPaths");
                Object paths = getPathsMethod.invoke(null);
                log.info("ImagePath.getPaths() result: {}", paths);
                
                if (paths instanceof List) {
                    List<?> pathList = (List<?>) paths;
                    log.info("ImagePath has {} paths", pathList.size());
                    for (int i = 0; i < pathList.size(); i++) {
                        Object pathEntry = pathList.get(i);
                        log.info("  Path[{}]: {} (type: {})", i, pathEntry, 
                            pathEntry != null ? pathEntry.getClass().getName() : "null");
                    }
                }
            } catch (Exception e) {
                log.warn("Could not call ImagePath.getPaths(): {}", e.getMessage());
            }
            
            // Try to reset and add paths
            log.info("Attempting to reset and configure ImagePath...");
            ImagePath.reset();
            log.info("ImagePath.reset() called");
            
            String testPath = "images";
            ImagePath.setBundlePath(testPath);
            log.info("ImagePath.setBundlePath('{}') called", testPath);
            log.info("Bundle path after set: '{}'", ImagePath.getBundlePath());
            
            ImagePath.add(testPath);
            log.info("ImagePath.add('{}') called", testPath);
            
        } catch (Exception e) {
            log.error("Error inspecting SikuliX configuration", e);
        }
    }
    
    private void scanFileSystem() {
        log.info("=== FILE SYSTEM SCAN ===");
        
        List<String> searchPaths = Arrays.asList(
            "images",
            "images/working",
            "images/prompt",
            "src/main/resources/images",
            "src/main/resources/images/working",
            "src/main/resources/images/prompt",
            "working",
            "prompt",
            "../images",
            "claude-automator/images"
        );
        
        for (String searchPath : searchPaths) {
            File dir = new File(searchPath);
            if (dir.exists()) {
                log.info("✓ EXISTS: {} -> {}", searchPath, dir.getAbsolutePath());
                
                if (dir.isDirectory()) {
                    File[] files = dir.listFiles();
                    if (files != null) {
                        log.info("  Contains {} files/dirs", files.length);
                        for (File file : files) {
                            if (file.getName().endsWith(".png")) {
                                log.info("    PNG: {} ({}KB)", file.getName(), file.length() / 1024);
                            }
                        }
                    }
                }
            } else {
                log.info("✗ NOT FOUND: {} (would be: {})", searchPath, dir.getAbsolutePath());
            }
        }
        
        // Deep scan for any PNG files
        log.info("Deep scanning for PNG files from current directory...");
        try {
            Path start = Paths.get(".");
            List<Path> pngFiles = Files.walk(start, 3) // Limit depth to 3
                .filter(path -> path.toString().endsWith(".png"))
                .collect(Collectors.toList());
                
            if (!pngFiles.isEmpty()) {
                log.info("Found {} PNG files within 3 levels:", pngFiles.size());
                pngFiles.forEach(png -> log.info("  {}", start.relativize(png)));
            } else {
                log.warn("No PNG files found within 3 directory levels!");
            }
        } catch (IOException e) {
            log.error("Error during deep scan", e);
        }
    }
    
    private void testResourceLoading() {
        log.info("=== RESOURCE LOADING TEST ===");
        
        List<String> testPaths = Arrays.asList(
            "working/claude-icon-1.png",
            "images/working/claude-icon-1.png",
            "/images/working/claude-icon-1.png",
            "claude-icon-1.png",
            "prompt/claude-prompt-1.png",
            "images/prompt/claude-prompt-1.png"
        );
        
        ClassLoader cl = getClass().getClassLoader();
        
        for (String path : testPaths) {
            URL resource = cl.getResource(path);
            if (resource != null) {
                log.info("✓ RESOURCE FOUND: {} -> {}", path, resource);
            } else {
                log.info("✗ RESOURCE NOT FOUND: {}", path);
            }
            
            // Also try as File
            File file = new File(path);
            if (file.exists()) {
                log.info("  ✓ FILE EXISTS: {}", file.getAbsolutePath());
            } else {
                log.info("  ✗ FILE NOT EXISTS: {}", file.getAbsolutePath());
            }
        }
    }
    
    private void testDirectImageLoading() {
        log.info("=== DIRECT IMAGE LOAD TEST ===");
        
        List<String> testPaths = Arrays.asList(
            "images/working/claude-icon-1.png",
            "working/claude-icon-1.png",
            "images/prompt/claude-prompt-1.png",
            "prompt/claude-prompt-1.png"
        );
        
        for (String path : testPaths) {
            log.info("Testing direct load of: {}", path);
            
            // Test with File
            File file = new File(path);
            if (file.exists()) {
                try {
                    BufferedImage img = ImageIO.read(file);
                    if (img != null) {
                        log.info("  ✓ Successfully loaded: {}x{}", img.getWidth(), img.getHeight());
                    } else {
                        log.error("  ✗ ImageIO.read returned null");
                    }
                } catch (IOException e) {
                    log.error("  ✗ Failed to read: {}", e.getMessage());
                }
            } else {
                log.info("  ✗ File does not exist: {}", file.getAbsolutePath());
            }
            
            // Test with SikuliX Pattern
            try {
                org.sikuli.script.Pattern pattern = new org.sikuli.script.Pattern(path);
                if (pattern != null) {
                    log.info("  ✓ SikuliX Pattern created");
                    BufferedImage bimg = pattern.getBImage();
                    if (bimg != null) {
                        log.info("    Image: {}x{}", bimg.getWidth(), bimg.getHeight());
                    } else {
                        log.warn("    Pattern has no BufferedImage");
                    }
                }
            } catch (Exception e) {
                log.error("  ✗ SikuliX Pattern failed: {}", e.getMessage());
            }
        }
    }
    
    private void logTimingInfo() {
        log.info("=== TIMING ANALYSIS ===");
        log.info("Current time: {}", new Date());
        log.info("Application uptime: {} ms", System.currentTimeMillis() - getStartTime());
        
        // Log when this diagnostic runs relative to other Spring components
        log.info("This diagnostic is running in ApplicationStartedEvent");
        log.info("State beans may have already been initialized");
        log.info("Images may have been loaded during state construction");
    }
    
    private static final long START_TIME = System.currentTimeMillis();
    
    private long getStartTime() {
        return START_TIME;
    }
}