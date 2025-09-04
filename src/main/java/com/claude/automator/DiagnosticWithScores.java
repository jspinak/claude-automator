package com.claude.automator;

import org.sikuli.basics.Settings;
import org.sikuli.script.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;

public class DiagnosticWithScores {
    public static void main(String[] args) throws Exception {
        System.out.println("=== PATTERN MATCHING DIAGNOSTIC - SIMILARITY SCORES ===");
        System.out.println("This diagnostic shows ALL matches and their similarity scores");
        System.out.println("Initial Settings.AlwaysResize: " + Settings.AlwaysResize);
        
        // Set DPI scaling for 125% Windows display
        Settings.AlwaysResize = 0.8f;
        System.out.println("Set Settings.AlwaysResize to: " + Settings.AlwaysResize);
        
        // Configure ImagePath
        String currentDir = new File(".").getAbsolutePath();
        ImagePath.add(currentDir);
        ImagePath.add(currentDir + "/images");
        System.out.println("ImagePath configured: " + ImagePath.getPaths());
        
        String patternPath = "images/prompt/claude-prompt-3.png";
        File patternFile = new File(patternPath);
        
        if (!patternFile.exists()) {
            System.err.println("Pattern file not found: " + patternFile.getAbsolutePath());
            return;
        }
        
        System.out.println("Pattern file: " + patternFile.getAbsolutePath());
        System.out.println();
        
        // Set very low similarity to find ANY matches
        Settings.MinSimilarity = 0.01;
        System.out.println("Settings.MinSimilarity set to: " + Settings.MinSimilarity + " (to find all matches)");
        
        Screen screen = new Screen();
        
        // Capture screen
        ScreenImage screenImage = screen.capture();
        System.out.println("Screen captured: " + screenImage.w + "x" + screenImage.h);
        System.out.println("=".repeat(60));
        
        // Test 1: Pattern(String) with DPI
        System.out.println("\n1. Pattern(String) WITH DPI (Settings.AlwaysResize = 0.8):");
        Pattern pattern1 = new Pattern(patternPath).similar(0.01);
        findAllMatches(screenImage, pattern1, "String+DPI");
        
        // Test 2: Pattern(BufferedImage) with DPI
        System.out.println("\n2. Pattern(BufferedImage) WITH DPI:");
        BufferedImage bufferedImage = ImageIO.read(patternFile);
        System.out.println("   BufferedImage: Type " + bufferedImage.getType() + 
                          ", Size " + bufferedImage.getWidth() + "x" + bufferedImage.getHeight());
        Pattern pattern2 = new Pattern(bufferedImage).similar(0.01);
        findAllMatches(screenImage, pattern2, "BufferedImage+DPI");
        
        // Test 3: Without DPI scaling
        System.out.println("\n3. Pattern(String) WITHOUT DPI (Settings.AlwaysResize = 1.0):");
        Settings.AlwaysResize = 1.0f;
        Pattern pattern3 = new Pattern(patternPath).similar(0.01);
        findAllMatches(screenImage, pattern3, "String+NoDPI");
        
        // Test 4: 80% scaled pattern without DPI
        System.out.println("\n4. 80% scaled pattern WITHOUT DPI:");
        String scaledPath = "images/prompt/claude-prompt-3-80.png";
        File scaledFile = new File(scaledPath);
        if (scaledFile.exists()) {
            Pattern pattern4 = new Pattern(scaledPath).similar(0.01);
            findAllMatches(screenImage, pattern4, "80%Pattern+NoDPI");
        } else {
            System.out.println("   80% scaled pattern not found at: " + scaledPath);
        }
        
        // Test with working directory patterns if they exist
        System.out.println("\n5. Testing with working directory patterns:");
        Settings.AlwaysResize = 0.8f;
        String[] workingPatterns = {
            "images/working/claude-icon-1-80.png",
            "images/working/claude-icon-2-80.png",
            "images/working/claude-icon-3-80.png",
            "images/working/claude-icon-4-80.png"
        };
        
        for (String wp : workingPatterns) {
            File wf = new File(wp);
            if (wf.exists()) {
                System.out.println("\n   Testing: " + wp);
                Pattern pattern = new Pattern(wp).similar(0.01);
                findAllMatches(screenImage, pattern, wf.getName());
            }
        }
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("INTERPRETATION:");
        System.out.println("- Similarity > 0.90: Excellent match (should work reliably)");
        System.out.println("- Similarity 0.70-0.90: Good match (may need threshold adjustment)");
        System.out.println("- Similarity 0.50-0.70: Poor match (significant differences)");
        System.out.println("- Similarity < 0.50: Very poor match (likely wrong pattern/scale)");
        System.out.println("\nIf no matches found, ensure Claude window is open and visible.");
        System.out.println("=== DIAGNOSTIC COMPLETE ===");
    }
    
    private static void findAllMatches(ScreenImage screenImage, Pattern pattern, String method) {
        try {
            Finder finder = new Finder(screenImage);
            finder.find(pattern);
            
            if (!finder.hasNext()) {
                System.out.printf("   [%s] NO MATCHES FOUND%n", method);
                return;
            }
            
            int count = 0;
            double bestScore = 0;
            Match bestMatch = null;
            
            // Find all matches and track the best one
            while (finder.hasNext()) {
                Match match = finder.next();
                double score = match.getScore();
                count++;
                
                if (score > bestScore) {
                    bestScore = score;
                    bestMatch = match;
                }
                
                // Show first 3 matches
                if (count <= 3) {
                    System.out.printf("   [%s] Match #%d: Location %s, Similarity: %.4f%n", 
                        method, count, match.getTarget(), score);
                }
            }
            
            if (count > 3) {
                System.out.printf("   [%s] ... and %d more matches%n", method, count - 3);
            }
            
            if (bestMatch != null) {
                System.out.printf("   [%s] BEST MATCH: Similarity %.4f at %s %s%n", 
                    method, bestScore, bestMatch.getTarget(),
                    bestScore > 0.90 ? "✓✓✓" : bestScore > 0.70 ? "✓✓" : bestScore > 0.50 ? "✓" : "✗");
            }
            
        } catch (Exception e) {
            System.err.printf("   [%s] Error: %s%n", method, e.getMessage());
        }
    }
}