package io.github.jspinak.claude.tools;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Creates logical resolution versions of patterns by scaling them by 0.8
 * This matches the 125% DPI scaling (1920x1080 physical -> 1536x864 logical)
 */
public class CreateLogicalPatterns {
    
    public static void main(String[] args) throws Exception {
        System.out.println("Creating logical resolution patterns (scaled by 0.8)...\n");
        
        // Scale factor for 125% DPI (1/1.25 = 0.8)
        double scaleFactor = 0.8;
        
        // Process Windows pattern
        processPattern("images/prompt/windows.png", 
                      "images/prompt/windows-scaled.png", 
                      scaleFactor);
        
        // Process FFmpeg pattern  
        processPattern("images/prompt/ffmpeg.png",
                      "images/prompt/ffmpeg-scaled.png",
                      scaleFactor);
        
        System.out.println("\nLogical resolution patterns created successfully!");
        System.out.println("Use these patterns when Brobot captures at logical resolution (1536x864)");
        System.out.println("\nTo use scaled patterns, update your StateImage creation:");
        System.out.println("  .withImage(\"windows-scaled\", \"ffmpeg-scaled\")");
    }
    
    private static void processPattern(String inputPath, String outputPath, double scale) throws Exception {
        File inputFile = new File(inputPath);
        if (!inputFile.exists()) {
            System.out.println("File not found: " + inputPath);
            return;
        }
        
        BufferedImage original = ImageIO.read(inputFile);
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();
        
        int scaledWidth = (int)(originalWidth * scale);
        int scaledHeight = (int)(originalHeight * scale);
        
        System.out.println("Processing: " + inputPath);
        System.out.println("  Original: " + originalWidth + "x" + originalHeight);
        System.out.println("  Scaled:   " + scaledWidth + "x" + scaledHeight);
        
        // Create scaled image with high quality
        BufferedImage scaled = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaled.createGraphics();
        
        // Set high quality rendering hints
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw scaled image
        g2d.drawImage(original, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();
        
        // Save scaled image
        File outputFile = new File(outputPath);
        ImageIO.write(scaled, "png", outputFile);
        System.out.println("  Saved to: " + outputPath);
    }
}