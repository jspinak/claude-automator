package com.claude.automator;

import org.sikuli.basics.Settings;
import org.sikuli.script.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DiagnosticWindowsCapture {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== WINDOWS CAPTURE PATTERN DIAGNOSTIC ===");
        System.out.println("Testing claude-prompt-win.png captured with Windows tool");
        System.out.println("Time: " + dateFormat.format(new Date()));
        System.out.println("=".repeat(70));
        
        // Pattern paths
        String winCapturePath = "images/prompt/claude-prompt-win.png";
        String originalPath = "images/prompt/claude-prompt-3.png";
        String scaled80Path = "images/prompt/claude-prompt-3-80.png";
        
        // Check if files exist
        File winCaptureFile = new File(winCapturePath);
        File originalFile = new File(originalPath);
        File scaled80File = new File(scaled80Path);
        
        if (!winCaptureFile.exists()) {
            System.err.println("ERROR: claude-prompt-win.png not found at: " + winCaptureFile.getAbsolutePath());
            System.err.println("Please ensure the file is in: images/prompt/claude-prompt-win.png");
            return;
        }
        
        // Load and analyze the Windows capture
        BufferedImage winImage = ImageIO.read(winCaptureFile);
        System.out.println("\nWINDOWS CAPTURED PATTERN:");
        System.out.println("  File: " + winCapturePath);
        System.out.println("  Dimensions: " + winImage.getWidth() + "x" + winImage.getHeight());
        System.out.println("  Type: " + getImageTypeString(winImage.getType()));
        
        // Compare to other patterns
        if (originalFile.exists()) {
            BufferedImage origImage = ImageIO.read(originalFile);
            System.out.println("\nORIGINAL PATTERN (SikuliX IDE):");
            System.out.println("  Dimensions: " + origImage.getWidth() + "x" + origImage.getHeight());
        }
        
        if (scaled80File.exists()) {
            BufferedImage scaledImage = ImageIO.read(scaled80File);
            System.out.println("\n80% SCALED PATTERN:");
            System.out.println("  Dimensions: " + scaledImage.getWidth() + "x" + scaledImage.getHeight());
        }
        
        // Configure ImagePath
        ImagePath.add(new File(".").getAbsolutePath());
        ImagePath.add(new File(".").getAbsolutePath() + "/images");
        
        // Initialize screen
        Screen screen = new Screen();
        Settings.MinSimilarity = 0.3; // Very low to find any match
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("TESTING WITHOUT DPI SCALING (Settings.AlwaysResize = 1.0):");
        System.out.println("=".repeat(70));
        
        // Test 1: Windows capture WITHOUT DPI scaling
        Settings.AlwaysResize = 1.0f;
        System.out.println("\n1. WINDOWS CAPTURE - NO DPI SCALING:");
        System.out.println("   Settings.AlwaysResize = " + Settings.AlwaysResize);
        testPattern(screen, winCapturePath, "Win-NoDPI");
        
        // Test 2: For comparison, test with DPI scaling
        Settings.AlwaysResize = 0.8f;
        System.out.println("\n2. WINDOWS CAPTURE - WITH DPI 0.8:");
        System.out.println("   Settings.AlwaysResize = " + Settings.AlwaysResize);
        testPattern(screen, winCapturePath, "Win-DPI0.8");
        
        // Reset to no scaling
        Settings.AlwaysResize = 1.0f;
        
        // Test 3: Compare all patterns without scaling
        System.out.println("\n" + "=".repeat(70));
        System.out.println("COMPARISON: ALL PATTERNS WITHOUT DPI SCALING:");
        System.out.println("=".repeat(70));
        
        System.out.println("\n3. ORIGINAL PATTERN (195x80) - NO SCALING:");
        if (originalFile.exists()) {
            testPattern(screen, originalPath, "Original-NoDPI");
        }
        
        System.out.println("\n4. 80% SCALED PATTERN (156x64) - NO SCALING:");
        if (scaled80File.exists()) {
            testPattern(screen, scaled80Path, "Scaled80-NoDPI");
        }
        
        System.out.println("\n5. WINDOWS CAPTURE - NO SCALING:");
        testPattern(screen, winCapturePath, "WinCapture-NoDPI");
        
        // Test with lower thresholds
        System.out.println("\n" + "=".repeat(70));
        System.out.println("DETAILED ANALYSIS WITH MULTIPLE THRESHOLDS:");
        System.out.println("=".repeat(70));
        
        testWithThresholds(screen, winCapturePath, "Windows Capture");
        
        // Capture comparison if match is found
        System.out.println("\n" + "=".repeat(70));
        System.out.println("ATTEMPTING TO CAPTURE MATCHED REGION:");
        System.out.println("=".repeat(70));
        captureAndCompare(screen, winCapturePath);
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("EXPECTED RESULTS:");
        System.out.println("- Windows capture should match at >90% similarity without DPI scaling");
        System.out.println("- If similarity is still ~69%, the pattern doesn't match what's on screen");
        System.out.println("- If similarity is >90%, this confirms DPI-aware capture is the solution");
        System.out.println("=".repeat(70));
    }
    
    private static void testPattern(Screen screen, String patternPath, String testName) {
        try {
            Pattern pattern = new Pattern(patternPath).similar(0.3);
            ScreenImage screenImage = screen.capture();
            Finder finder = new Finder(screenImage);
            finder.find(pattern);
            
            System.out.println("   Testing: " + testName);
            
            if (!finder.hasNext()) {
                System.out.println("   ❌ NO MATCH FOUND");
                return;
            }
            
            // Find best match
            Match bestMatch = null;
            double bestScore = 0;
            int count = 0;
            
            while (finder.hasNext()) {
                Match match = finder.next();
                count++;
                if (match.getScore() > bestScore) {
                    bestScore = match.getScore();
                    bestMatch = match;
                }
            }
            
            System.out.printf("   ✓ FOUND %d match(es)%n", count);
            System.out.printf("   Best match: Location %s%n", bestMatch.getTarget());
            System.out.printf("   Similarity: %.4f (%.1f%%)%n", bestScore, bestScore * 100);
            System.out.printf("   Match size: %dx%d%n", bestMatch.w, bestMatch.h);
            
            // Interpret results
            if (bestScore > 0.95) {
                System.out.println("   🎯 EXCELLENT MATCH! Pattern is perfect for screen content.");
            } else if (bestScore > 0.85) {
                System.out.println("   ✅ VERY GOOD MATCH! Pattern works well.");
            } else if (bestScore > 0.75) {
                System.out.println("   ⚠️ GOOD MATCH. Acceptable but could be better.");
            } else if (bestScore > 0.65) {
                System.out.println("   ⚠️ MODERATE MATCH. May have reliability issues.");
            } else {
                System.out.println("   ❌ POOR MATCH. Pattern needs adjustment.");
            }
            
        } catch (Exception e) {
            System.err.println("   Error: " + e.getMessage());
        }
    }
    
    private static void testWithThresholds(Screen screen, String patternPath, String patternName) {
        System.out.println("\nTesting " + patternName + " at different thresholds:");
        
        double[] thresholds = {0.95, 0.90, 0.85, 0.80, 0.75, 0.70, 0.65, 0.60, 0.55, 0.50};
        
        for (double threshold : thresholds) {
            try {
                Pattern pattern = new Pattern(patternPath).similar(threshold);
                Match match = screen.exists(pattern, 0.1);
                
                if (match != null) {
                    System.out.printf("   Threshold %.2f: ✓ FOUND (actual: %.4f)%n", 
                        threshold, match.getScore());
                } else {
                    System.out.printf("   Threshold %.2f: ✗ Not found%n", threshold);
                }
            } catch (Exception e) {
                System.err.printf("   Threshold %.2f: Error - %s%n", threshold, e.getMessage());
            }
        }
    }
    
    private static void captureAndCompare(Screen screen, String patternPath) {
        try {
            Settings.AlwaysResize = 1.0f;
            Pattern pattern = new Pattern(patternPath).similar(0.3);
            Match match = screen.exists(pattern, 0.5);
            
            if (match == null) {
                System.out.println("   No match found to capture");
                return;
            }
            
            System.out.println("   Found match to capture:");
            System.out.printf("   Location: %s, Similarity: %.4f%n", 
                match.getTarget(), match.getScore());
            
            // Capture the matched region
            Region matchRegion = new Region(match);
            BufferedImage captured = screen.capture(matchRegion).getImage();
            
            // Save capture
            File captureDir = new File("debug_captures");
            if (!captureDir.exists()) captureDir.mkdirs();
            
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File captureFile = new File(captureDir, 
                String.format("win_capture_test_%s_sim%.3f.png", timestamp, match.getScore()));
            ImageIO.write(captured, "png", captureFile);
            
            System.out.println("   Captured region saved to: " + captureFile.getName());
            System.out.println("   Captured size: " + captured.getWidth() + "x" + captured.getHeight());
            
            // Load Windows capture for comparison
            BufferedImage winPattern = ImageIO.read(new File(patternPath));
            
            System.out.println("\n   COMPARISON:");
            System.out.println("   Windows pattern size: " + winPattern.getWidth() + "x" + winPattern.getHeight());
            System.out.println("   Captured region size: " + captured.getWidth() + "x" + captured.getHeight());
            
            if (winPattern.getWidth() == captured.getWidth() && 
                winPattern.getHeight() == captured.getHeight()) {
                System.out.println("   ✓ Sizes match perfectly!");
            } else {
                System.out.println("   ⚠️ Size mismatch - may indicate scaling issues");
            }
            
        } catch (Exception e) {
            System.err.println("   Capture failed: " + e.getMessage());
        }
    }
    
    private static String getImageTypeString(int type) {
        switch (type) {
            case BufferedImage.TYPE_INT_RGB: return "TYPE_INT_RGB";
            case BufferedImage.TYPE_INT_ARGB: return "TYPE_INT_ARGB";
            case BufferedImage.TYPE_3BYTE_BGR: return "TYPE_3BYTE_BGR";
            case BufferedImage.TYPE_4BYTE_ABGR: return "TYPE_4BYTE_ABGR (with alpha)";
            case BufferedImage.TYPE_BYTE_GRAY: return "TYPE_BYTE_GRAY";
            default: return "TYPE_" + type;
        }
    }
}