package com.claude.automator;

import org.sikuli.basics.Settings;
import org.sikuli.script.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class DiagnosticWithDPI {
    public static void main(String[] args) throws Exception {
        // Initialize DPI settings FIRST
        System.out.println("=== PATTERN MATCHING DIAGNOSTIC WITH DPI ===");
        System.out.println("Initial Settings.AlwaysResize: " + Settings.AlwaysResize);
        
        // Set DPI scaling for 125% Windows display
        Settings.AlwaysResize = 0.8f;
        System.out.println("Set Settings.AlwaysResize to: " + Settings.AlwaysResize);
        
        // Configure ImagePath to find patterns
        String currentDir = new File(".").getAbsolutePath();
        System.out.println("Current directory: " + currentDir);
        ImagePath.add(currentDir);
        ImagePath.add(currentDir + "/images");
        System.out.println("ImagePath configured: " + ImagePath.getPaths());
        
        String patternPath = "images/prompt/claude-prompt-3.png";
        File patternFile = new File(patternPath);
        
        if (!patternFile.exists()) {
            System.err.println("Pattern file not found: " + patternFile.getAbsolutePath());
            return;
        }
        
        System.out.println("Pattern file exists: " + patternFile.getAbsolutePath());
        System.out.println();
        
        // Initialize SikuliX
        Settings.MinSimilarity = 0.5;
        Screen screen = new Screen();
        
        // Capture screen once
        ScreenImage screenImage = screen.capture();
        System.out.println("Screen captured: " + screenImage.w + "x" + screenImage.h);
        System.out.println();
        
        // Method 1: Pattern(String) - as SikuliX IDE with DPI scaling
        System.out.println("1. Pattern(String) WITH DPI scaling:");
        Pattern pattern1 = new Pattern(patternPath);
        testPattern(screenImage, pattern1, "String+DPI");
        
        // Method 2: Pattern(BufferedImage) - as Brobot might load
        System.out.println("\n2. Pattern(BufferedImage) WITH DPI scaling:");
        BufferedImage bufferedImage = ImageIO.read(patternFile);
        System.out.println("   BufferedImage type: " + bufferedImage.getType());
        System.out.println("   BufferedImage size: " + bufferedImage.getWidth() + "x" + bufferedImage.getHeight());
        Pattern pattern2 = new Pattern(bufferedImage);
        testPattern(screenImage, pattern2, "BufferedImage+DPI");
        
        // Method 3: Pattern(absolute path)
        System.out.println("\n3. Pattern(absolute path) WITH DPI:");
        Pattern pattern3 = new Pattern(patternFile.getAbsolutePath());
        testPattern(screenImage, pattern3, "AbsolutePath+DPI");
        
        // Method 4: Test without DPI scaling for comparison
        System.out.println("\n4. Testing WITHOUT DPI scaling for comparison:");
        Settings.AlwaysResize = 1.0f;
        System.out.println("   Settings.AlwaysResize reset to: " + Settings.AlwaysResize);
        Pattern pattern4 = new Pattern(patternPath);
        testPattern(screenImage, pattern4, "NoDPI");
        
        // Method 5: Test 80% scaled pattern without DPI
        System.out.println("\n5. Testing 80% scaled pattern WITHOUT DPI:");
        String scaledPath = "images/prompt/claude-prompt-3-80.png";
        File scaledFile = new File(scaledPath);
        if (scaledFile.exists()) {
            Pattern pattern5 = new Pattern(scaledPath);
            testPattern(screenImage, pattern5, "80%Pattern+NoDPI");
        } else {
            System.out.println("   80% scaled pattern not found");
        }
        
        System.out.println("\n=== ANALYSIS ===");
        System.out.println("If Method 1 (String+DPI) matches at high similarity,");
        System.out.println("then DPI scaling is the solution.");
        System.out.println("If Method 2 (BufferedImage+DPI) has lower similarity,");
        System.out.println("then BufferedImage loading is the issue.");
        System.out.println("\n=== DIAGNOSTIC COMPLETE ===");
    }
    
    private static void testPattern(ScreenImage screenImage, Pattern pattern, String method) {
        try {
            Finder finder = new Finder(screenImage);
            finder.find(pattern);
            
            if (finder.hasNext()) {
                Match match = finder.next();
                System.out.printf("  [%s] FOUND at %s with similarity: %.4f ✓%n", 
                    method, match.getTarget(), match.getScore());
            } else {
                System.out.printf("  [%s] NOT FOUND ✗%n", method);
            }
        } catch (Exception e) {
            System.err.printf("  [%s] Error: %s%n", method, e.getMessage());
        }
    }
}