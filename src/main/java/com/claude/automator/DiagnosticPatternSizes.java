package com.claude.automator;

import org.sikuli.basics.Settings;
import org.sikuli.script.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class DiagnosticPatternSizes {
    public static void main(String[] args) throws Exception {
        System.out.println("=== PATTERN SIZE DIAGNOSTIC ===");
        System.out.println("Investigating why match regions have zero dimensions\n");
        
        // Test patterns
        String originalPath = "images/prompt/claude-prompt-3.png";
        String scaledPath = "images/prompt/claude-prompt-3-80.png";
        
        File originalFile = new File(originalPath);
        File scaledFile = new File(scaledPath);
        
        if (!originalFile.exists()) {
            System.err.println("Original pattern not found: " + originalFile.getAbsolutePath());
            return;
        }
        
        // Load patterns
        BufferedImage originalImg = ImageIO.read(originalFile);
        System.out.println("Original pattern dimensions: " + originalImg.getWidth() + "x" + originalImg.getHeight());
        
        if (scaledFile.exists()) {
            BufferedImage scaledImg = ImageIO.read(scaledFile);
            System.out.println("80% scaled pattern dimensions: " + scaledImg.getWidth() + "x" + scaledImg.getHeight());
        }
        
        // Configure ImagePath
        ImagePath.add(new File(".").getAbsolutePath());
        ImagePath.add(new File(".").getAbsolutePath() + "/images");
        
        Screen screen = new Screen();
        System.out.println("\nScreen dimensions: " + screen.w + "x" + screen.h);
        
        // Test with different DPI settings
        testPatternWithDPI(originalPath, 0.8f, "DPI 0.8 (125% scaling)");
        testPatternWithDPI(originalPath, 1.0f, "DPI 1.0 (no scaling)");
        testPatternWithDPI(originalPath, 0.667f, "DPI 0.667 (150% scaling)");
        testPatternWithDPI(originalPath, 0.5f, "DPI 0.5 (200% scaling)");
        
        if (scaledFile.exists()) {
            System.out.println("\n--- Testing 80% pre-scaled pattern ---");
            testPatternWithDPI(scaledPath, 1.0f, "80% pattern, no DPI");
            testPatternWithDPI(scaledPath, 0.8f, "80% pattern, DPI 0.8");
        }
        
        // Test internal Pattern object dimensions
        System.out.println("\n=== PATTERN OBJECT ANALYSIS ===");
        analyzePatternObject(originalPath, 0.8f);
        analyzePatternObject(originalPath, 1.0f);
    }
    
    private static void testPatternWithDPI(String patternPath, float dpiScale, String description) {
        try {
            System.out.println("\n" + description + ":");
            Settings.AlwaysResize = dpiScale;
            System.out.println("  Settings.AlwaysResize = " + Settings.AlwaysResize);
            
            Pattern pattern = new Pattern(patternPath).similar(0.5);
            Image patternImage = pattern.getImage();
            
            System.out.println("  Pattern.getImage() size: " + patternImage.getSize());
            System.out.println("  Pattern.getImage() URL: " + patternImage.getURL());
            
            // Try to find pattern
            Screen screen = new Screen();
            Finder finder = new Finder(screen.capture());
            finder.find(pattern);
            
            if (finder.hasNext()) {
                Match match = finder.next();
                System.out.println("  MATCH FOUND:");
                System.out.println("    Location: " + match.getTarget());
                System.out.println("    Bounds: x=" + match.x + ", y=" + match.y + ", w=" + match.w + ", h=" + match.h);
                System.out.println("    Similarity: " + String.format("%.4f", match.getScore()));
                
                // Calculate expected dimensions
                BufferedImage img = ImageIO.read(new File(patternPath));
                int expectedW = (int)(img.getWidth() * dpiScale);
                int expectedH = (int)(img.getHeight() * dpiScale);
                System.out.println("    Expected size after scaling: " + expectedW + "x" + expectedH);
                
                if (match.w == 0 || match.h == 0) {
                    System.err.println("    WARNING: Match has zero dimensions!");
                    
                    // Try to understand why
                    System.out.println("    Debugging zero-size match:");
                    System.out.println("      Match.getRect(): " + match.getRect());
                    System.out.println("      Match.toString(): " + match.toString());
                    
                    // Try creating a Region from the match
                    try {
                        Region r = new Region(match);
                        System.out.println("      Region from match: " + r);
                    } catch (Exception e) {
                        System.err.println("      Cannot create Region: " + e.getMessage());
                    }
                }
            } else {
                System.out.println("  NO MATCH FOUND");
            }
            
        } catch (Exception e) {
            System.err.println("  Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void analyzePatternObject(String patternPath, float dpiScale) {
        try {
            System.out.println("\nAnalyzing Pattern object with DPI " + dpiScale + ":");
            Settings.AlwaysResize = dpiScale;
            
            // Load pattern different ways
            Pattern p1 = new Pattern(patternPath);
            System.out.println("  Pattern(String):");
            System.out.println("    toString: " + p1.toString());
            
            BufferedImage img = ImageIO.read(new File(patternPath));
            Pattern p2 = new Pattern(img);
            System.out.println("  Pattern(BufferedImage):");
            System.out.println("    toString: " + p2.toString());
            
            // Check if pattern scaling affects the Pattern object itself
            System.out.println("  Pattern dimensions after resize factor " + dpiScale + ":");
            System.out.println("    Original image: " + img.getWidth() + "x" + img.getHeight());
            System.out.println("    Expected after scale: " + (int)(img.getWidth() * dpiScale) + "x" + (int)(img.getHeight() * dpiScale));
            
            // Test what SikuliX returns as pattern size
            Image sikuliImg = p1.getImage();
            if (sikuliImg != null) {
                System.out.println("    SikuliX Image size: " + sikuliImg.getSize());
                BufferedImage bufImg = sikuliImg.get();
                if (bufImg != null) {
                    System.out.println("    BufferedImage from SikuliX: " + bufImg.getWidth() + "x" + bufImg.getHeight());
                }
            }
            
        } catch (Exception e) {
            System.err.println("  Analysis failed: " + e.getMessage());
        }
    }
}