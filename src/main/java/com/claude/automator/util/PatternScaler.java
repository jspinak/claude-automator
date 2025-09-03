package com.claude.automator.util;

import lombok.extern.slf4j.Slf4j;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility to scale pattern images and save them.
 * This helps test if pre-scaled patterns match better than runtime scaling.
 */
@Slf4j
public class PatternScaler {
    
    /**
     * Scale all patterns in a directory and save them with a suffix
     */
    public static void scalePatterns(String sourceDir, float scaleFactor, String suffix) {
        try {
            Path sourcePath = Paths.get(sourceDir);
            if (!Files.exists(sourcePath)) {
                log.error("Source directory does not exist: {}", sourceDir);
                return;
            }
            
            Files.walk(sourcePath)
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().toLowerCase().endsWith(".png"))
                .filter(p -> !p.getFileName().toString().contains(suffix)) // Don't re-scale already scaled
                .forEach(path -> scaleAndSaveImage(path, scaleFactor, suffix));
                
            log.info("Pattern scaling complete for directory: {}", sourceDir);
            
        } catch (IOException e) {
            log.error("Error scaling patterns in directory: " + sourceDir, e);
        }
    }
    
    /**
     * Scale a single image and save it with a suffix
     */
    private static void scaleAndSaveImage(Path imagePath, float scaleFactor, String suffix) {
        try {
            File inputFile = imagePath.toFile();
            BufferedImage original = ImageIO.read(inputFile);
            
            if (original == null) {
                log.warn("Could not read image: {}", imagePath);
                return;
            }
            
            // Calculate new dimensions
            int newWidth = Math.round(original.getWidth() * scaleFactor);
            int newHeight = Math.round(original.getHeight() * scaleFactor);
            
            // Create scaled image
            BufferedImage scaled = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = scaled.createGraphics();
            
            // Set high quality rendering hints
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw scaled image
            g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
            g2d.dispose();
            
            // Generate output filename
            String originalName = inputFile.getName();
            String nameWithoutExt = originalName.substring(0, originalName.lastIndexOf('.'));
            String extension = originalName.substring(originalName.lastIndexOf('.'));
            String outputName = nameWithoutExt + suffix + extension;
            
            // Save in same directory
            File outputFile = new File(inputFile.getParent(), outputName);
            ImageIO.write(scaled, "PNG", outputFile);
            
            log.info("Scaled {} ({}x{}) -> {} ({}x{}) at {}%", 
                originalName, 
                original.getWidth(), original.getHeight(),
                outputName, 
                newWidth, newHeight,
                (int)(scaleFactor * 100));
                
        } catch (IOException e) {
            log.error("Error scaling image: " + imagePath, e);
        }
    }
    
    /**
     * Main method to scale all patterns for claude-automator
     */
    public static void main(String[] args) {
        log.info("Starting pattern scaling at 80%...");
        
        // Scale prompt patterns
        scalePatterns("images/prompt", 0.8f, "-80");
        
        // Scale working patterns  
        scalePatterns("images/working", 0.8f, "-80");
        
        log.info("Pattern scaling complete!");
        log.info("Remember to update PromptState and WorkingState to include the new patterns.");
    }
}