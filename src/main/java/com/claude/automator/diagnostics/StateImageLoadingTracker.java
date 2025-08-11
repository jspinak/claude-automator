package com.claude.automator.diagnostics;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.sikuli.script.ImagePath;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Aspect that tracks all image loading attempts to diagnose loading issues.
 */
@Slf4j
@Aspect
@Component
public class StateImageLoadingTracker {
    
    private static int imageLoadCount = 0;
    
    /**
     * Intercept Pattern constructor calls to log image loading attempts
     */
    @Before("execution(org.sikuli.script.Pattern.new(String))")
    public void trackPatternCreation(JoinPoint joinPoint) {
        imageLoadCount++;
        String imagePath = (String) joinPoint.getArgs()[0];
        
        log.info("=== IMAGE LOAD ATTEMPT #{} ===", imageLoadCount);
        log.info("Pattern constructor called with: '{}'", imagePath);
        log.info("Stack trace:");
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (int i = 0; i < Math.min(10, stack.length); i++) {
            StackTraceElement element = stack[i];
            if (element.getClassName().contains("claude") || 
                element.getClassName().contains("brobot") ||
                element.getClassName().contains("State")) {
                log.info("  {}", element);
            }
        }
        
        // Log current ImagePath state
        String bundlePath = ImagePath.getBundlePath();
        log.info("Current SikuliX Bundle Path: '{}'", bundlePath != null ? bundlePath : "NULL");
        
        // Check if file exists in various locations
        checkFileExistence(imagePath);
    }
    
    /**
     * Intercept StateImage creation
     */
    @Before("execution(io.github.jspinak.brobot.model.state.StateImage.new(..))")
    public void trackStateImageCreation(JoinPoint joinPoint) {
        log.info("=== STATE IMAGE CREATION ===");
        log.info("StateImage constructor called");
        log.info("Arguments: {}", joinPoint.getArgs().length);
        
        // Log timing
        log.info("Time since app start: {} ms", System.currentTimeMillis() - getStartTime());
    }
    
    /**
     * Intercept BufferedImageUtilities.getBuffImgFromFile calls
     */
    @Before("execution(* io.github.jspinak.brobot.util.image.core.BufferedImageUtilities.getBuffImgFromFile(String))")
    public void trackBufferedImageLoad(JoinPoint joinPoint) {
        String path = (String) joinPoint.getArgs()[0];
        log.info("=== BUFFERED IMAGE LOAD ===");
        log.info("BufferedImageUtilities.getBuffImgFromFile: '{}'", path);
        checkFileExistence(path);
    }
    
    private void checkFileExistence(String imagePath) {
        log.info("Checking file existence for: '{}'", imagePath);
        
        // Direct file check
        File directFile = new File(imagePath);
        log.info("  Direct file '{}': exists={}, absolute={}",
            imagePath,
            directFile.exists(),
            directFile.getAbsolutePath());
            
        // Check with common prefixes
        String[] prefixes = {"", "images/", "src/main/resources/", "src/main/resources/images/"};
        for (String prefix : prefixes) {
            File file = new File(prefix + imagePath);
            if (file.exists()) {
                log.info("  ✓ FOUND with prefix '{}': {}", prefix, file.getAbsolutePath());
            }
        }
        
        // Check if it's in the classpath
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl.getResource(imagePath) != null) {
            log.info("  ✓ Found as classpath resource: {}", imagePath);
        }
        
        // Check parent directories
        File parent = directFile.getParentFile();
        if (parent != null) {
            log.info("  Parent directory '{}': exists={}", 
                parent.getPath(), parent.exists());
            if (parent.exists()) {
                File[] files = parent.listFiles();
                if (files != null && files.length > 0) {
                    log.info("  Files in parent directory:");
                    for (File f : files) {
                        if (f.getName().endsWith(".png")) {
                            log.info("    - {}", f.getName());
                        }
                    }
                }
            }
        }
    }
    
    private static final long START_TIME = System.currentTimeMillis();
    
    private long getStartTime() {
        return START_TIME;
    }
}