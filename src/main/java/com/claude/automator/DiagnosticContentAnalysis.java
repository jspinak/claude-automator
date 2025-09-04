package com.claude.automator;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class DiagnosticContentAnalysis {
    public static void main(String[] args) throws IOException {
        System.out.println("=== CONTENT ANALYSIS DIAGNOSTIC ===");
        System.out.println("Analyzing the actual content size within captured images\n");
        
        File captureDir = new File("debug_captures");
        if (!captureDir.exists()) {
            System.err.println("No debug_captures directory found. Run capture diagnostic first!");
            return;
        }
        
        // Analyze captured images
        File[] captures = captureDir.listFiles((dir, name) -> 
            name.startsWith("capture_") && name.endsWith(".png"));
        
        if (captures == null || captures.length == 0) {
            System.err.println("No captured images found!");
            return;
        }
        
        // Also analyze original and 80% scaled patterns
        File originalPattern = new File("images/prompt/claude-prompt-3.png");
        File scaledPattern = new File("images/prompt/claude-prompt-3-80.png");
        
        System.out.println("=".repeat(70));
        
        // Analyze original
        if (originalPattern.exists()) {
            System.out.println("ORIGINAL PATTERN (195x80):");
            analyzeImage(originalPattern);
        }
        
        // Analyze 80% scaled
        if (scaledPattern.exists()) {
            System.out.println("\n80% SCALED PATTERN (156x64):");
            analyzeImage(scaledPattern);
        }
        
        // Analyze captures
        for (File capture : captures) {
            System.out.println("\nCAPTURED IMAGE: " + capture.getName());
            analyzeImage(capture);
            
            // Compare content bounds
            if (originalPattern.exists() && scaledPattern.exists()) {
                compareContentSize(capture, originalPattern, scaledPattern);
            }
        }
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("INTERPRETATION:");
        System.out.println("- If captured content is smaller than 80% pattern content,");
        System.out.println("  there's additional scaling happening (browser zoom, CSS, etc.)");
        System.out.println("- Black borders indicate the actual pattern is smaller than expected");
        System.out.println("- This explains why similarity is only ~69% instead of ~98%");
    }
    
    private static void analyzeImage(File imageFile) throws IOException {
        BufferedImage img = ImageIO.read(imageFile);
        System.out.println("  Image dimensions: " + img.getWidth() + "x" + img.getHeight());
        
        // Find content bounds (non-black area)
        Rectangle contentBounds = findContentBounds(img);
        System.out.println("  Content bounds: " + contentBounds);
        System.out.println("  Content size: " + contentBounds.width + "x" + contentBounds.height);
        
        // Calculate padding
        int leftPadding = contentBounds.x;
        int topPadding = contentBounds.y;
        int rightPadding = img.getWidth() - (contentBounds.x + contentBounds.width);
        int bottomPadding = img.getHeight() - (contentBounds.y + contentBounds.height);
        
        System.out.println("  Padding: left=" + leftPadding + ", top=" + topPadding + 
                          ", right=" + rightPadding + ", bottom=" + bottomPadding);
        
        // Calculate content percentage
        double contentArea = contentBounds.width * contentBounds.height;
        double totalArea = img.getWidth() * img.getHeight();
        double contentPercentage = (contentArea / totalArea) * 100;
        System.out.printf("  Content fills %.1f%% of image%n", contentPercentage);
        
        // Detect if image has significant black borders
        if (leftPadding > 5 || topPadding > 5 || rightPadding > 5 || bottomPadding > 5) {
            System.out.println("  ⚠️ SIGNIFICANT BLACK BORDERS DETECTED!");
        }
        
        // Calculate actual scale factor
        if (imageFile.getName().contains("capture")) {
            double actualWidth = contentBounds.width;
            double expectedWidth = 195.0; // Original pattern width
            double actualScale = actualWidth / expectedWidth;
            System.out.printf("  Actual scale factor: %.2f (%.0f%% of original)%n", 
                actualScale, actualScale * 100);
        }
    }
    
    private static Rectangle findContentBounds(BufferedImage img) {
        int minX = img.getWidth();
        int minY = img.getHeight();
        int maxX = 0;
        int maxY = 0;
        
        // Define black threshold (accounting for slight variations)
        int blackThreshold = 30; // RGB values below this are considered "black"
        
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                // Check if pixel is not black
                if (r > blackThreshold || g > blackThreshold || b > blackThreshold) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        
        // Handle edge case of all black image
        if (minX > maxX) {
            return new Rectangle(0, 0, 0, 0);
        }
        
        return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }
    
    private static void compareContentSize(File captured, File original, File scaled) throws IOException {
        BufferedImage capImg = ImageIO.read(captured);
        BufferedImage origImg = ImageIO.read(original);
        BufferedImage scaledImg = ImageIO.read(scaled);
        
        Rectangle capBounds = findContentBounds(capImg);
        Rectangle origBounds = findContentBounds(origImg);
        Rectangle scaledBounds = findContentBounds(scaledImg);
        
        System.out.println("\n  CONTENT SIZE COMPARISON:");
        System.out.println("    Original pattern content: " + origBounds.width + "x" + origBounds.height);
        System.out.println("    80% scaled content: " + scaledBounds.width + "x" + scaledBounds.height);
        System.out.println("    Captured content: " + capBounds.width + "x" + capBounds.height);
        
        double expectedScale = (double)scaledBounds.width / origBounds.width;
        double actualScale = (double)capBounds.width / origBounds.width;
        
        System.out.printf("    Expected scale: %.2f (%.0f%%)%n", expectedScale, expectedScale * 100);
        System.out.printf("    Actual scale: %.2f (%.0f%%)%n", actualScale, actualScale * 100);
        
        if (actualScale < expectedScale - 0.05) {
            System.out.println("    ⚠️ CAPTURED CONTENT IS SMALLER THAN EXPECTED!");
            System.out.printf("    Additional scaling factor: %.2f%n", actualScale / expectedScale);
        }
    }
}