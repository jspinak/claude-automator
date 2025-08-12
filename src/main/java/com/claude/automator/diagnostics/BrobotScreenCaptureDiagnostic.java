package com.claude.automator.diagnostics;

import lombok.extern.slf4j.Slf4j;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Diagnostic component that captures screenshots using the exact same methods
 * that Brobot uses for pattern matching. This helps diagnose any scaling or
 * capture issues.
 */
@Slf4j
@Component
public class BrobotScreenCaptureDiagnostic {
    
    private static final String HISTORY_DIR = "history";
    private static final String SCREENSHOT_SUBDIR = "diagnostic-screenshots";
    
    /**
     * Captures a screenshot using SikuliX Screen.capture() - the same method
     * Brobot uses for pattern matching.
     */
    public void captureDiagnosticScreenshot(String context) {
        log.info("=== DIAGNOSTIC SCREENSHOT CAPTURE ===");
        log.info("Context: {}", context);
        
        try {
            // Method 1: Using SikuliX Screen.capture() - this is what Brobot uses
            captureSikuliXScreenshot(context);
            
            // Method 2: Using Java Robot directly for comparison
            captureJavaRobotScreenshot(context);
            
            // Method 3: Using SikuliX Screen.getScreen()
            captureSikuliXGetScreenshot(context);
            
            // Compare and analyze
            compareScreenshots();
            
        } catch (Exception e) {
            log.error("Failed to capture diagnostic screenshots", e);
        }
    }
    
    /**
     * Captures using SikuliX Screen.capture() method - exactly how Brobot does it
     */
    private void captureSikuliXScreenshot(String context) {
        try {
            log.info("Capturing using SikuliX Screen.capture() method...");
            
            // Create Screen object - same as Brobot
            Screen screen = new Screen();
            
            // Get screen dimensions
            Rectangle screenBounds = screen.getBounds();
            log.info("SikuliX Screen bounds: {}x{} at ({}, {})", 
                screenBounds.width, screenBounds.height, 
                screenBounds.x, screenBounds.y);
            
            // Capture the screen - this is the exact method Brobot uses
            ScreenImage screenImage = screen.capture();
            
            if (screenImage != null) {
                BufferedImage image = screenImage.getImage();
                
                log.info("SikuliX captured image dimensions: {}x{}", 
                    image.getWidth(), image.getHeight());
                
                // Check if image is black/empty
                boolean isBlack = isImageBlack(image);
                if (isBlack) {
                    log.warn("WARNING: SikuliX captured image appears to be BLACK!");
                }
                
                // Save the image
                String filename = saveScreenshot(image, context + "_sikulix_capture");
                log.info("SikuliX screenshot saved to: {}", filename);
                
                // Log pixel analysis
                analyzeImagePixels(image, "SikuliX capture");
            } else {
                log.error("SikuliX Screen.capture() returned null!");
            }
            
        } catch (Exception e) {
            log.error("Failed to capture SikuliX screenshot", e);
        }
    }
    
    /**
     * Captures using SikuliX capture(Rectangle) method
     */
    private void captureSikuliXGetScreenshot(String context) {
        try {
            log.info("Capturing using SikuliX Screen.capture(Rectangle) method...");
            
            Screen screen = new Screen();
            Rectangle bounds = screen.getBounds();
            
            // Use capture with explicit rectangle
            ScreenImage screenImage = screen.capture(bounds);
            
            if (screenImage != null) {
                BufferedImage image = screenImage.getImage();
                
                log.info("SikuliX capture(Rectangle) image dimensions: {}x{}", 
                    image.getWidth(), image.getHeight());
                
                // Save the image
                String filename = saveScreenshot(image, context + "_sikulix_rect");
                log.info("SikuliX capture(Rectangle) screenshot saved to: {}", filename);
                
            } else {
                log.error("SikuliX capture(Rectangle) returned null!");
            }
            
        } catch (Exception e) {
            log.error("Failed to capture SikuliX rectangle screenshot", e);
        }
    }
    
    /**
     * Captures using Java Robot directly for comparison
     */
    private void captureJavaRobotScreenshot(String context) {
        try {
            log.info("Capturing using Java Robot directly...");
            
            Robot robot = new Robot();
            
            // Get screen dimensions
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            log.info("Java Toolkit screen size: {}x{}", 
                screenSize.width, screenSize.height);
            
            // Get graphics configuration
            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice device = env.getDefaultScreenDevice();
            GraphicsConfiguration config = device.getDefaultConfiguration();
            Rectangle bounds = config.getBounds();
            
            log.info("Graphics device bounds: {}x{} at ({}, {})", 
                bounds.width, bounds.height, bounds.x, bounds.y);
            
            // Check for scaling
            java.awt.geom.AffineTransform transform = config.getDefaultTransform();
            double scaleX = transform.getScaleX();
            double scaleY = transform.getScaleY();
            log.info("Display scaling detected: X={}, Y={}", scaleX, scaleY);
            
            // Capture with Robot
            BufferedImage image = robot.createScreenCapture(
                new Rectangle(0, 0, screenSize.width, screenSize.height)
            );
            
            log.info("Java Robot captured image dimensions: {}x{}", 
                image.getWidth(), image.getHeight());
            
            // Check if image is black
            boolean isBlack = isImageBlack(image);
            if (isBlack) {
                log.warn("WARNING: Java Robot captured image appears to be BLACK!");
            }
            
            // Save the image
            String filename = saveScreenshot(image, context + "_java_robot");
            log.info("Java Robot screenshot saved to: {}", filename);
            
            // Log pixel analysis
            analyzeImagePixels(image, "Java Robot");
            
        } catch (Exception e) {
            log.error("Failed to capture Java Robot screenshot", e);
        }
    }
    
    /**
     * Analyzes image pixels to detect if it's black or has content
     */
    private boolean isImageBlack(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        // Sample pixels across the image
        int sampleSize = 100;
        int blackPixels = 0;
        int totalSampled = 0;
        
        for (int x = 0; x < width; x += width / 10) {
            for (int y = 0; y < height; y += height / 10) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                if (r < 10 && g < 10 && b < 10) {
                    blackPixels++;
                }
                totalSampled++;
                
                if (totalSampled >= sampleSize) break;
            }
            if (totalSampled >= sampleSize) break;
        }
        
        double blackPercentage = (double) blackPixels / totalSampled * 100;
        log.info("Black pixel percentage: {}% ({}/{} sampled pixels)", 
            String.format("%.1f", blackPercentage), blackPixels, totalSampled);
        
        return blackPercentage > 95; // Consider image black if >95% black pixels
    }
    
    /**
     * Analyzes and logs detailed pixel information
     */
    private void analyzeImagePixels(BufferedImage image, String source) {
        log.info("=== PIXEL ANALYSIS for {} ===", source);
        
        // Get corners and center
        int width = image.getWidth();
        int height = image.getHeight();
        
        logPixel(image, 0, 0, "Top-left");
        logPixel(image, width-1, 0, "Top-right");
        logPixel(image, 0, height-1, "Bottom-left");
        logPixel(image, width-1, height-1, "Bottom-right");
        logPixel(image, width/2, height/2, "Center");
        
        // Calculate average brightness
        long totalBrightness = 0;
        int sampleSize = 1000;
        for (int i = 0; i < sampleSize; i++) {
            int x = (int)(Math.random() * width);
            int y = (int)(Math.random() * height);
            int rgb = image.getRGB(x, y);
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;
            totalBrightness += (r + g + b) / 3;
        }
        
        log.info("Average brightness: {}/255", totalBrightness / sampleSize);
    }
    
    /**
     * Logs a specific pixel's RGB values
     */
    private void logPixel(BufferedImage image, int x, int y, String location) {
        int rgb = image.getRGB(x, y);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        log.info("  {} ({}, {}): RGB({}, {}, {})", location, x, y, r, g, b);
    }
    
    /**
     * Saves a screenshot to the history directory
     */
    private String saveScreenshot(BufferedImage image, String prefix) throws IOException {
        // Create directory if needed
        Path dir = Paths.get(HISTORY_DIR, SCREENSHOT_SUBDIR);
        Files.createDirectories(dir);
        
        // Generate filename with timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
        String timestamp = sdf.format(new Date());
        String filename = String.format("%s_%s.png", prefix, timestamp);
        
        File outputFile = dir.resolve(filename).toFile();
        ImageIO.write(image, "PNG", outputFile);
        
        return outputFile.getAbsolutePath();
    }
    
    /**
     * Compares the different screenshot methods
     */
    private void compareScreenshots() {
        log.info("=== SCREENSHOT COMPARISON ===");
        
        // Get actual display information
        GraphicsDevice device = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getDefaultScreenDevice();
        DisplayMode displayMode = device.getDisplayMode();
        
        log.info("Display Mode: {}x{} @ {}Hz, {} bit", 
            displayMode.getWidth(), 
            displayMode.getHeight(),
            displayMode.getRefreshRate(),
            displayMode.getBitDepth());
        
        // Get all screen devices (for multi-monitor)
        GraphicsDevice[] devices = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getScreenDevices();
        log.info("Number of screens: {}", devices.length);
        
        for (int i = 0; i < devices.length; i++) {
            GraphicsDevice gd = devices[i];
            Rectangle bounds = gd.getDefaultConfiguration().getBounds();
            log.info("  Screen {}: {}x{} at ({}, {})", 
                i, bounds.width, bounds.height, bounds.x, bounds.y);
        }
        
        log.info("=== END SCREENSHOT COMPARISON ===");
    }
}