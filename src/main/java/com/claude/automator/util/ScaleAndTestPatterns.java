package com.claude.automator.util;

import com.claude.automator.states.PromptState;
import com.claude.automator.states.WorkingState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Standalone application to scale patterns and test them.
 * Run this to create 80% scaled versions of all patterns.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.claude.automator", "io.github.jspinak.brobot"})
@Slf4j
public class ScaleAndTestPatterns {
    
    public static void main(String[] args) {
        System.setProperty("spring.main.web-application-type", "none");
        SpringApplication.run(ScaleAndTestPatterns.class, args);
    }
    
    @Bean
    public CommandLineRunner scalePatterns() {
        return args -> {
            log.info("╔════════════════════════════════════════════════════════════════════╗");
            log.info("║                  PATTERN SCALING UTILITY                           ║");
            log.info("║         Creating 80% scaled versions of all patterns              ║");
            log.info("╚════════════════════════════════════════════════════════════════════╝");
            
            // Scale prompt patterns
            scalePatternDirectory("images/prompt", 0.8f);
            
            // Scale working patterns
            scalePatternDirectory("images/working", 0.8f);
            
            log.info("\n═══════════════════════════════════════════════════════════════════");
            log.info("                    SCALING COMPLETE!");
            log.info("═══════════════════════════════════════════════════════════════════");
            log.info("\nNEXT STEPS:");
            log.info("1. The scaled patterns have been saved with '-80' suffix");
            log.info("2. Update PromptState.java to include the new patterns");
            log.info("3. Update WorkingState.java to include the new patterns");
            log.info("4. Run the application to test if similarity improves");
            log.info("\nExample: claude-prompt-1.png -> claude-prompt-1-80.png");
        };
    }
    
    private void scalePatternDirectory(String directory, float scaleFactor) {
        File dir = new File(directory);
        if (!dir.exists() || !dir.isDirectory()) {
            log.error("Directory not found: {}", directory);
            return;
        }
        
        File[] files = dir.listFiles((d, name) -> 
            name.toLowerCase().endsWith(".png") && !name.contains("-80"));
            
        if (files == null || files.length == 0) {
            log.warn("No PNG files found in: {}", directory);
            return;
        }
        
        log.info("\nScaling patterns in: {}", directory);
        log.info("Found {} patterns to scale", files.length);
        
        for (File file : files) {
            scaleImage(file, scaleFactor);
        }
    }
    
    private void scaleImage(File inputFile, float scaleFactor) {
        try {
            BufferedImage original = ImageIO.read(inputFile);
            if (original == null) {
                log.error("Could not read image: {}", inputFile.getName());
                return;
            }
            
            // Calculate new dimensions
            int newWidth = Math.round(original.getWidth() * scaleFactor);
            int newHeight = Math.round(original.getHeight() * scaleFactor);
            
            // Create scaled image with same type as original
            int imageType = original.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            
            BufferedImage scaled = new BufferedImage(newWidth, newHeight, imageType);
            Graphics2D g2d = scaled.createGraphics();
            
            // High quality scaling
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
                RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw scaled image
            g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
            g2d.dispose();
            
            // Create output filename
            String name = inputFile.getName();
            String nameWithoutExt = name.substring(0, name.lastIndexOf('.'));
            String outputName = nameWithoutExt + "-80.png";
            
            // Save scaled image
            File outputFile = new File(inputFile.getParent(), outputName);
            boolean saved = ImageIO.write(scaled, "PNG", outputFile);
            
            if (saved) {
                log.info("  ✓ {} ({}x{}) -> {} ({}x{})",
                    name,
                    original.getWidth(), original.getHeight(),
                    outputName,
                    newWidth, newHeight);
            } else {
                log.error("  ✗ Failed to save: {}", outputName);
            }
            
        } catch (IOException e) {
            log.error("Error scaling image: " + inputFile.getName(), e);
        }
    }
}