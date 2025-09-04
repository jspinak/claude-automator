package com.claude.automator;

import org.sikuli.basics.Settings;
import org.sikuli.script.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class SimpleDiagnostic {
    public static void main(String[] args) throws Exception {
        String patternPath = "images/prompt/claude-prompt-3.png";
        File patternFile = new File(patternPath);
        
        if (!patternFile.exists()) {
            System.err.println("Pattern file not found: " + patternFile.getAbsolutePath());
            System.err.println("Current directory: " + new File(".").getAbsolutePath());
            return;
        }
        
        System.out.println("=== SIMPLE PATTERN MATCHING DIAGNOSTIC ===");
        System.out.println("Pattern: " + patternFile.getAbsolutePath());
        System.out.println("Settings.AlwaysResize: " + Settings.AlwaysResize);
        System.out.println();
        
        // Initialize SikuliX
        Settings.MinSimilarity = 0.5;
        Screen screen = new Screen();
        
        // Capture screen once
        ScreenImage screenImage = screen.capture();
        System.out.println("Screen captured: " + screenImage.w + "x" + screenImage.h);
        
        // Method 1: Pattern(String) - as SikuliX IDE
        System.out.println("\n1. Pattern(String) - as SikuliX IDE loads:");
        Pattern pattern1 = new Pattern(patternPath);
        testPattern(screenImage, pattern1, "String path");
        
        // Method 2: Pattern(BufferedImage) - as Brobot might load
        System.out.println("\n2. Pattern(BufferedImage) - as Brobot might load:");
        BufferedImage bufferedImage = ImageIO.read(patternFile);
        System.out.println("   BufferedImage type: " + bufferedImage.getType());
        System.out.println("   BufferedImage size: " + bufferedImage.getWidth() + "x" + bufferedImage.getHeight());
        Pattern pattern2 = new Pattern(bufferedImage);
        testPattern(screenImage, pattern2, "BufferedImage");
        
        // Method 3: Pattern with similarity threshold
        System.out.println("\n3. Pattern(String).similar(0.5):");
        Pattern pattern3 = new Pattern(patternPath).similar(0.5);
        testPattern(screenImage, pattern3, "With threshold");
        
        System.out.println("\n=== SUMMARY ===");
        System.out.println("If Pattern(String) matches at ~0.99 and Pattern(BufferedImage) at ~0.69,");
        System.out.println("then the issue is in how Brobot loads BufferedImages into Patterns.");
        System.out.println("\n=== DIAGNOSTIC COMPLETE ===");
    }
    
    private static void testPattern(ScreenImage screenImage, Pattern pattern, String method) {
        try {
            Finder finder = new Finder(screenImage);
            finder.find(pattern);
            
            if (finder.hasNext()) {
                Match match = finder.next();
                System.out.printf("  [%s] Found at %s with similarity: %.4f%n", 
                    method, match.getTarget(), match.getScore());
            } else {
                System.out.printf("  [%s] Not found%n", method);
            }
        } catch (Exception e) {
            System.err.printf("  [%s] Error: %s%n", method, e.getMessage());
        }
    }
}