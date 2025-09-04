package com.claude.automator;

import org.sikuli.basics.Settings;
import org.sikuli.script.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DiagnosticWithCapture {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== PATTERN MATCHING DIAGNOSTIC WITH CAPTURE ===");
        System.out.println("This diagnostic captures matches and compares them to patterns");
        System.out.println("Initial Settings.AlwaysResize: " + Settings.AlwaysResize);
        
        // Set DPI scaling for 125% Windows display
        Settings.AlwaysResize = 0.8f;
        System.out.println("Set Settings.AlwaysResize to: " + Settings.AlwaysResize);
        
        // Configure ImagePath
        String currentDir = new File(".").getAbsolutePath();
        ImagePath.add(currentDir);
        ImagePath.add(currentDir + "/images");
        System.out.println("ImagePath configured: " + ImagePath.getPaths());
        
        // Create debug_captures directory
        File captureDir = new File("debug_captures");
        if (!captureDir.exists()) {
            captureDir.mkdirs();
            System.out.println("Created directory: " + captureDir.getAbsolutePath());
        }
        
        String patternPath = "images/prompt/claude-prompt-3.png";
        String scaledPatternPath = "images/prompt/claude-prompt-3-80.png";
        File patternFile = new File(patternPath);
        File scaledPatternFile = new File(scaledPatternPath);
        
        if (!patternFile.exists()) {
            System.err.println("Pattern file not found: " + patternFile.getAbsolutePath());
            return;
        }
        
        System.out.println("Original pattern: " + patternFile.getAbsolutePath());
        System.out.println("80% scaled pattern: " + (scaledPatternFile.exists() ? "EXISTS" : "NOT FOUND"));
        System.out.println();
        
        // Set similarity threshold
        Settings.MinSimilarity = 0.5;
        Screen screen = new Screen();
        
        // Capture screen
        ScreenImage screenImage = screen.capture();
        System.out.println("Screen captured: " + screenImage.w + "x" + screenImage.h);
        System.out.println("=".repeat(70));
        
        // Load original pattern
        BufferedImage originalPattern = ImageIO.read(patternFile);
        System.out.println("Original pattern size: " + originalPattern.getWidth() + "x" + originalPattern.getHeight());
        
        // Load 80% scaled pattern if exists
        BufferedImage scaledPattern = null;
        if (scaledPatternFile.exists()) {
            scaledPattern = ImageIO.read(scaledPatternFile);
            System.out.println("80% scaled pattern size: " + scaledPattern.getWidth() + "x" + scaledPattern.getHeight());
        }
        
        System.out.println("=".repeat(70));
        
        // Test 1: Find with DPI scaling
        System.out.println("\n1. FINDING WITH DPI SCALING (Settings.AlwaysResize = 0.8):");
        Pattern pattern1 = new Pattern(patternPath).similar(0.5);
        Match match1 = findAndCapture(screen, screenImage, pattern1, "DPI_scaled", captureDir);
        
        if (match1 != null) {
            compareToPatterns(match1, originalPattern, scaledPattern, captureDir, "DPI_scaled");
        }
        
        // Test 2: Find without DPI scaling
        System.out.println("\n2. FINDING WITHOUT DPI SCALING (Settings.AlwaysResize = 1.0):");
        Settings.AlwaysResize = 1.0f;
        Pattern pattern2 = new Pattern(patternPath).similar(0.5);
        Match match2 = findAndCapture(screen, screenImage, pattern2, "No_DPI", captureDir);
        
        if (match2 != null) {
            compareToPatterns(match2, originalPattern, scaledPattern, captureDir, "No_DPI");
        }
        
        // Test 3: Find 80% pattern without DPI
        if (scaledPatternFile.exists()) {
            System.out.println("\n3. FINDING 80% PATTERN WITHOUT DPI:");
            Pattern pattern3 = new Pattern(scaledPatternPath).similar(0.5);
            Match match3 = findAndCapture(screen, screenImage, pattern3, "80pct_pattern", captureDir);
            
            if (match3 != null) {
                compareToPatterns(match3, originalPattern, scaledPattern, captureDir, "80pct_pattern");
            }
        }
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("ANALYSIS COMPLETE!");
        System.out.println("Check the 'debug_captures' directory for:");
        System.out.println("  - Captured match regions");
        System.out.println("  - Pixel-by-pixel comparisons");
        System.out.println("  - Similarity calculations");
        System.out.println("=".repeat(70));
    }
    
    private static Match findAndCapture(Screen screen, ScreenImage screenImage, Pattern pattern, 
                                       String testName, File captureDir) {
        try {
            Finder finder = new Finder(screenImage);
            finder.find(pattern);
            
            if (!finder.hasNext()) {
                System.out.println("   NO MATCH FOUND");
                return null;
            }
            
            Match match = finder.next();
            double score = match.getScore();
            
            System.out.printf("   FOUND at %s with similarity: %.4f%n", 
                match.getTarget(), score);
            System.out.printf("   Match bounds: x=%d, y=%d, w=%d, h=%d%n",
                match.x, match.y, match.w, match.h);
            
            // Check if match has valid dimensions
            if (match.w <= 0 || match.h <= 0) {
                System.err.println("   ERROR: Invalid match dimensions (w=" + match.w + ", h=" + match.h + ")");
                System.err.println("   This indicates pattern scaling resulted in zero-size match");
                return match; // Return match for analysis but skip capture
            }
            
            try {
                // Create region with explicit bounds checking
                Region matchRegion = new Region(match.x, match.y, 
                    Math.max(1, match.w), Math.max(1, match.h));
                
                // Capture the matched region
                BufferedImage capture = screen.capture(matchRegion).getImage();
                String timestamp = dateFormat.format(new Date());
                String captureFileName = String.format("capture_%s_%s_sim%.3f.png", 
                    testName, timestamp, score);
                File captureFile = new File(captureDir, captureFileName);
                
                ImageIO.write(capture, "png", captureFile);
                System.out.println("   Captured match saved to: " + captureFileName);
                System.out.println("   Captured size: " + capture.getWidth() + "x" + capture.getHeight());
            } catch (Exception captureError) {
                System.err.println("   Failed to capture: " + captureError.getMessage());
            }
            
            return match;
            
        } catch (Exception e) {
            System.err.println("   Error: " + e.getMessage());
            return null;
        }
    }
    
    private static void compareToPatterns(Match match, BufferedImage originalPattern, 
                                         BufferedImage scaledPattern, File captureDir, String testName) {
        try {
            // Check if match has valid dimensions
            if (match == null || match.w <= 0 || match.h <= 0) {
                System.err.println("   Cannot compare - invalid match dimensions");
                return;
            }
            
            Screen screen = new Screen();
            Region matchRegion = new Region(match.x, match.y, 
                Math.max(1, match.w), Math.max(1, match.h));
            BufferedImage capturedMatch = screen.capture(matchRegion).getImage();
            
            System.out.println("\n   === COMPARISON ANALYSIS ===");
            
            // Compare to original pattern
            System.out.println("   Comparing captured match to ORIGINAL pattern:");
            double simToOriginal = compareImages(capturedMatch, originalPattern);
            System.out.printf("   Pixel similarity to original (195x80): %.4f%n", simToOriginal);
            
            // Compare to 80% scaled pattern
            if (scaledPattern != null) {
                System.out.println("   Comparing captured match to 80% SCALED pattern:");
                double simToScaled = compareImages(capturedMatch, scaledPattern);
                System.out.printf("   Pixel similarity to 80%% scaled (156x64): %.4f%n", simToScaled);
                
                // Determine which is closer
                if (simToScaled > simToOriginal) {
                    System.out.println("   ✓ Captured match is CLOSER to 80% scaled pattern!");
                    System.out.printf("   Difference: %.4f (%.1f%% better)%n", 
                        simToScaled - simToOriginal, 
                        (simToScaled - simToOriginal) * 100);
                } else {
                    System.out.println("   ✓ Captured match is CLOSER to original pattern");
                    System.out.printf("   Difference: %.4f (%.1f%% better)%n", 
                        simToOriginal - simToScaled,
                        (simToOriginal - simToScaled) * 100);
                }
            }
            
            // Save comparison image
            saveComparisonImage(capturedMatch, originalPattern, scaledPattern, 
                              captureDir, testName, simToOriginal, 
                              scaledPattern != null ? compareImages(capturedMatch, scaledPattern) : 0);
            
        } catch (Exception e) {
            System.err.println("   Comparison failed: " + e.getMessage());
        }
    }
    
    private static double compareImages(BufferedImage img1, BufferedImage img2) {
        // If sizes don't match, resize for comparison
        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
            System.out.printf("   Size mismatch: %dx%d vs %dx%d%n",
                img1.getWidth(), img1.getHeight(),
                img2.getWidth(), img2.getHeight());
            
            // For now, return 0 if sizes don't match
            // In a real comparison, you'd resize one to match the other
            return calculateStructuralSimilarity(img1, img2);
        }
        
        // Calculate pixel-by-pixel similarity
        long totalDiff = 0;
        int pixelCount = img1.getWidth() * img1.getHeight();
        
        for (int x = 0; x < img1.getWidth(); x++) {
            for (int y = 0; y < img1.getHeight(); y++) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);
                
                int r1 = (rgb1 >> 16) & 0xFF;
                int g1 = (rgb1 >> 8) & 0xFF;
                int b1 = rgb1 & 0xFF;
                
                int r2 = (rgb2 >> 16) & 0xFF;
                int g2 = (rgb2 >> 8) & 0xFF;
                int b2 = rgb2 & 0xFF;
                
                totalDiff += Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
            }
        }
        
        // Calculate similarity (1.0 = identical, 0.0 = completely different)
        double maxDiff = pixelCount * 3 * 255;
        return 1.0 - (totalDiff / maxDiff);
    }
    
    private static double calculateStructuralSimilarity(BufferedImage img1, BufferedImage img2) {
        // Simple structural similarity when sizes don't match
        // Compare aspect ratios and average colors
        
        double aspectRatio1 = (double) img1.getWidth() / img1.getHeight();
        double aspectRatio2 = (double) img2.getWidth() / img2.getHeight();
        double aspectSimilarity = 1.0 - Math.abs(aspectRatio1 - aspectRatio2) / Math.max(aspectRatio1, aspectRatio2);
        
        // Compare average colors
        long[] avg1 = getAverageColor(img1);
        long[] avg2 = getAverageColor(img2);
        
        double colorDiff = 0;
        for (int i = 0; i < 3; i++) {
            colorDiff += Math.abs(avg1[i] - avg2[i]);
        }
        double colorSimilarity = 1.0 - (colorDiff / (3 * 255));
        
        // Weighted average of aspect ratio and color similarity
        return aspectSimilarity * 0.3 + colorSimilarity * 0.7;
    }
    
    private static long[] getAverageColor(BufferedImage img) {
        long r = 0, g = 0, b = 0;
        int pixelCount = img.getWidth() * img.getHeight();
        
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                int rgb = img.getRGB(x, y);
                r += (rgb >> 16) & 0xFF;
                g += (rgb >> 8) & 0xFF;
                b += rgb & 0xFF;
            }
        }
        
        return new long[] {r / pixelCount, g / pixelCount, b / pixelCount};
    }
    
    private static void saveComparisonImage(BufferedImage captured, BufferedImage original, 
                                           BufferedImage scaled, File captureDir, String testName,
                                           double simToOriginal, double simToScaled) throws IOException {
        // Create a comparison image showing all three side by side
        int width = captured.getWidth() + original.getWidth() + (scaled != null ? scaled.getWidth() : 0) + 20;
        int height = Math.max(captured.getHeight(), Math.max(original.getHeight(), 
                            scaled != null ? scaled.getHeight() : 0)) + 40;
        
        BufferedImage comparison = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = comparison.createGraphics();
        
        // White background
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, width, height);
        
        // Draw images
        g.setColor(java.awt.Color.BLACK);
        int x = 5;
        
        // Captured match
        g.drawImage(captured, x, 20, null);
        g.drawString(String.format("Captured (%dx%d)", captured.getWidth(), captured.getHeight()), x, 15);
        x += captured.getWidth() + 5;
        
        // Original pattern
        g.drawImage(original, x, 20, null);
        g.drawString(String.format("Original (%.3f)", simToOriginal), x, 15);
        x += original.getWidth() + 5;
        
        // 80% scaled pattern
        if (scaled != null) {
            g.drawImage(scaled, x, 20, null);
            g.drawString(String.format("80%% Scaled (%.3f)", simToScaled), x, 15);
        }
        
        g.dispose();
        
        // Save comparison
        String timestamp = dateFormat.format(new Date());
        String comparisonFileName = String.format("comparison_%s_%s.png", testName, timestamp);
        File comparisonFile = new File(captureDir, comparisonFileName);
        ImageIO.write(comparison, "png", comparisonFile);
        System.out.println("   Comparison image saved: " + comparisonFileName);
    }
}