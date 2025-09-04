package com.claude.automator;

import org.sikuli.basics.Settings;
import org.sikuli.script.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class DiagnosticReverseMatch {
    public static void main(String[] args) throws Exception {
        System.out.println("=== REVERSE MATCHING DIAGNOSTIC ===");
        System.out.println("This diagnostic uses captured matches as patterns to search the screen\n");
        
        // Check if we have captured images
        File captureDir = new File("debug_captures");
        if (!captureDir.exists()) {
            System.err.println("No debug_captures directory found. Run capture diagnostic first!");
            return;
        }
        
        // Find captured images
        File[] captures = captureDir.listFiles((dir, name) -> 
            name.startsWith("capture_") && name.endsWith(".png"));
        
        if (captures == null || captures.length == 0) {
            System.err.println("No captured images found. Run capture diagnostic first!");
            return;
        }
        
        System.out.println("Found " + captures.length + " captured images");
        System.out.println("=".repeat(70));
        
        // Setup
        Settings.MinSimilarity = 0.5;
        Settings.AlwaysResize = 1.0f; // No scaling for captured images
        Screen screen = new Screen();
        ScreenImage screenImage = screen.capture();
        
        // Test each captured image as a pattern
        for (File captureFile : captures) {
            System.out.println("\n--- Testing: " + captureFile.getName() + " ---");
            
            BufferedImage capturedImg = ImageIO.read(captureFile);
            System.out.println("Captured image size: " + capturedImg.getWidth() + "x" + capturedImg.getHeight());
            
            // Method 1: Use captured image directly as pattern
            System.out.println("\n1. Using captured image as Pattern:");
            Pattern capturedPattern = new Pattern(capturedImg).similar(0.5);
            testPattern(screenImage, capturedPattern, "Captured->Pattern");
            
            // Method 2: Save and load as file (like SikuliX IDE would)
            System.out.println("\n2. Using captured image file path:");
            Pattern filePattern = new Pattern(captureFile.getAbsolutePath()).similar(0.5);
            testPattern(screenImage, filePattern, "Captured->File");
            
            // Method 3: Test with different DPI settings
            System.out.println("\n3. Testing with DPI scaling:");
            Settings.AlwaysResize = 0.8f;
            Pattern scaledPattern = new Pattern(captureFile.getAbsolutePath()).similar(0.5);
            testPattern(screenImage, scaledPattern, "Captured+DPI0.8");
            Settings.AlwaysResize = 1.0f;
        }
        
        // Now test original patterns at the captured size
        System.out.println("\n" + "=".repeat(70));
        System.out.println("TESTING ORIGINAL PATTERNS RESIZED TO 156x64:");
        System.out.println("=".repeat(70));
        
        String originalPath = "images/prompt/claude-prompt-3.png";
        File originalFile = new File(originalPath);
        if (originalFile.exists()) {
            BufferedImage original = ImageIO.read(originalFile);
            
            // Resize original to 156x64 (the captured size)
            BufferedImage resized = resizeImage(original, 156, 64);
            
            System.out.println("\nOriginal pattern manually resized to 156x64:");
            Pattern resizedPattern = new Pattern(resized).similar(0.5);
            testPattern(screenImage, resizedPattern, "ManualResize156x64");
            
            // Save the resized image for inspection
            File resizedFile = new File(captureDir, "original_resized_156x64.png");
            ImageIO.write(resized, "png", resizedFile);
            System.out.println("Saved resized original to: " + resizedFile.getName());
        }
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("CONCLUSIONS:");
        System.out.println("- If captured images match at >90%, the patterns are visually correct");
        System.out.println("- If they match at ~69%, SikuliX's algorithm is the limiting factor");
        System.out.println("- The 30% similarity loss is inherent to SikuliX template matching");
        System.out.println("=".repeat(70));
    }
    
    private static void testPattern(ScreenImage screenImage, Pattern pattern, String method) {
        try {
            Finder finder = new Finder(screenImage);
            finder.find(pattern);
            
            if (!finder.hasNext()) {
                System.out.printf("  [%s] NO MATCH FOUND%n", method);
                return;
            }
            
            Match match = finder.next();
            System.out.printf("  [%s] FOUND at %s with similarity: %.4f %s%n", 
                method, match.getTarget(), match.getScore(),
                match.getScore() > 0.90 ? "✓✓✓ EXCELLENT!" : 
                match.getScore() > 0.70 ? "✓✓ Good" : "✓ Moderate");
            
            // Check for additional matches
            int count = 1;
            while (finder.hasNext() && count < 3) {
                Match next = finder.next();
                count++;
                System.out.printf("  [%s] Also found at %s (%.4f)%n", 
                    method, next.getTarget(), next.getScore());
            }
            
        } catch (Exception e) {
            System.err.printf("  [%s] Error: %s%n", method, e.getMessage());
        }
    }
    
    private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, originalImage.getType());
        java.awt.Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                          java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return resizedImage;
    }
}