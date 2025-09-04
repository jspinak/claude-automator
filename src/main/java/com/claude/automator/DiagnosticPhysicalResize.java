package com.claude.automator;

import org.sikuli.basics.Settings;
import org.sikuli.script.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DiagnosticPhysicalResize {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== PHYSICAL SIZE RESIZE DIAGNOSTIC ===");
        System.out.println("Testing if resizing captures to physical pixel dimensions improves matching");
        System.out.println("Time: " + dateFormat.format(new Date()));
        System.out.println("=".repeat(70));
        
        // Pattern paths
        String winCapturePath = "images/prompt/claude-prompt-win.png";
        String originalPath = "images/prompt/claude-prompt-3.png";
        String scaled80Path = "images/prompt/claude-prompt-3-80.png";
        
        File winCaptureFile = new File(winCapturePath);
        File originalFile = new File(originalPath);
        
        if (!winCaptureFile.exists()) {
            System.err.println("ERROR: claude-prompt-win.png not found");
            return;
        }
        
        // Load images
        BufferedImage winCapture = ImageIO.read(winCaptureFile);
        BufferedImage original = originalFile.exists() ? ImageIO.read(originalFile) : null;
        
        System.out.println("ORIGINAL SIZES:");
        System.out.println("  Windows capture: " + winCapture.getWidth() + "x" + winCapture.getHeight() + " (logical pixels)");
        if (original != null) {
            System.out.println("  SikuliX capture: " + original.getWidth() + "x" + original.getHeight() + " (physical pixels)");
        }
        
        // Calculate resize factors
        System.out.println("\nRESIZE CALCULATIONS:");
        System.out.println("  Windows DPI scale: 125% → resize factor 0.8");
        System.out.println("  To convert logical to physical: multiply by 1.25");
        System.out.println("  Windows 103x60 → Physical " + (int)(103 * 1.25) + "x" + (int)(60 * 1.25));
        
        // Create resized versions
        System.out.println("\n" + "=".repeat(70));
        System.out.println("CREATING RESIZED PATTERNS:");
        System.out.println("=".repeat(70));
        
        File debugDir = new File("debug_captures");
        if (!debugDir.exists()) debugDir.mkdirs();
        
        // 1. Resize Windows capture to physical size (125% larger)
        BufferedImage winPhysical = resizeImage(winCapture, 
            (int)(winCapture.getWidth() * 1.25), 
            (int)(winCapture.getHeight() * 1.25));
        File winPhysicalFile = new File(debugDir, "win_capture_physical_129x75.png");
        ImageIO.write(winPhysical, "png", winPhysicalFile);
        System.out.println("\n1. Windows capture → Physical size:");
        System.out.println("   103x60 → " + winPhysical.getWidth() + "x" + winPhysical.getHeight());
        System.out.println("   Saved to: " + winPhysicalFile.getName());
        
        // 2. Resize Windows capture to match SikuliX original size
        BufferedImage winToOrigSize = resizeImage(winCapture, 195, 80);
        File winToOrigFile = new File(debugDir, "win_capture_195x80.png");
        ImageIO.write(winToOrigSize, "png", winToOrigFile);
        System.out.println("\n2. Windows capture → SikuliX original size:");
        System.out.println("   103x60 → 195x80");
        System.out.println("   Saved to: " + winToOrigFile.getName());
        
        // 3. Resize Windows capture to 80% pattern size
        BufferedImage winTo80Size = resizeImage(winCapture, 156, 64);
        File winTo80File = new File(debugDir, "win_capture_156x64.png");
        ImageIO.write(winTo80Size, "png", winTo80File);
        System.out.println("\n3. Windows capture → 80% pattern size:");
        System.out.println("   103x60 → 156x64");
        System.out.println("   Saved to: " + winTo80File.getName());
        
        // 4. If we have original, resize it to Windows logical size
        if (original != null) {
            BufferedImage origToWinSize = resizeImage(original, 103, 60);
            File origToWinFile = new File(debugDir, "original_103x60.png");
            ImageIO.write(origToWinSize, "png", origToWinFile);
            System.out.println("\n4. SikuliX original → Windows capture size:");
            System.out.println("   195x80 → 103x60");
            System.out.println("   Saved to: " + origToWinFile.getName());
        }
        
        // Setup for testing
        ImagePath.add(new File(".").getAbsolutePath());
        ImagePath.add(debugDir.getAbsolutePath());
        Screen screen = new Screen();
        Settings.MinSimilarity = 0.3;
        
        // Test all versions
        System.out.println("\n" + "=".repeat(70));
        System.out.println("TESTING ALL PATTERNS:");
        System.out.println("=".repeat(70));
        
        // Test WITHOUT DPI scaling
        Settings.AlwaysResize = 1.0f;
        System.out.println("\nWITH NO DPI SCALING (Settings.AlwaysResize = 1.0):");
        System.out.println("-".repeat(50));
        
        testPattern(screen, winCapturePath, "Windows Original (103x60)");
        testPattern(screen, winPhysicalFile.getAbsolutePath(), "Windows Physical (129x75)");
        testPattern(screen, winToOrigFile.getAbsolutePath(), "Windows→195x80");
        testPattern(screen, winTo80File.getAbsolutePath(), "Windows→156x64");
        if (original != null) {
            testPattern(screen, originalPath, "SikuliX Original (195x80)");
            testPattern(screen, new File(debugDir, "original_103x60.png").getAbsolutePath(), "SikuliX→103x60");
        }
        
        // Test WITH DPI scaling
        Settings.AlwaysResize = 0.8f;
        System.out.println("\nWITH DPI SCALING 0.8 (Settings.AlwaysResize = 0.8):");
        System.out.println("-".repeat(50));
        
        testPattern(screen, winCapturePath, "Windows Original (103x60)");
        testPattern(screen, winPhysicalFile.getAbsolutePath(), "Windows Physical (129x75)");
        testPattern(screen, winToOrigFile.getAbsolutePath(), "Windows→195x80");
        testPattern(screen, winTo80File.getAbsolutePath(), "Windows→156x64");
        if (original != null) {
            testPattern(screen, originalPath, "SikuliX Original (195x80)");
        }
        
        // Test with different DPI values
        System.out.println("\n" + "=".repeat(70));
        System.out.println("OPTIMAL DPI SEARCH:");
        System.out.println("=".repeat(70));
        
        findOptimalDPI(screen, winCapturePath, "Windows Original (103x60)");
        findOptimalDPI(screen, winPhysicalFile.getAbsolutePath(), "Windows Physical (129x75)");
        findOptimalDPI(screen, winToOrigFile.getAbsolutePath(), "Windows→195x80");
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("RECOMMENDATIONS:");
        System.out.println("=".repeat(70));
        System.out.println("1. Check which resized version achieves highest similarity");
        System.out.println("2. If physical size (129x75) works best → Use that approach");
        System.out.println("3. If 195x80 works best → Resize all Windows captures to match SikuliX");
        System.out.println("4. Note the optimal DPI setting for each approach");
        System.out.println("=".repeat(70));
    }
    
    private static void testPattern(Screen screen, String patternPath, String description) {
        try {
            File f = new File(patternPath);
            if (!f.exists()) {
                System.out.printf("  %-25s: FILE NOT FOUND%n", description);
                return;
            }
            
            Pattern pattern = new Pattern(patternPath).similar(0.3);
            Match match = screen.exists(pattern, 0.5);
            
            if (match != null) {
                System.out.printf("  %-25s: %.1f%% ", description, match.getScore() * 100);
                if (match.getScore() > 0.90) System.out.println("✅ EXCELLENT!");
                else if (match.getScore() > 0.80) System.out.println("✓ Very Good");
                else if (match.getScore() > 0.70) System.out.println("⚠️ Good");
                else if (match.getScore() > 0.60) System.out.println("⚠️ Moderate");
                else System.out.println("❌ Poor");
            } else {
                System.out.printf("  %-25s: NOT FOUND%n", description);
            }
        } catch (Exception e) {
            System.err.printf("  %-25s: ERROR - %s%n", description, e.getMessage());
        }
    }
    
    private static void findOptimalDPI(Screen screen, String patternPath, String description) {
        File f = new File(patternPath);
        if (!f.exists()) return;
        
        System.out.println("\n" + description + " - Finding optimal DPI:");
        
        double bestScore = 0;
        float bestDPI = 1.0f;
        
        float[] dpiValues = {1.0f, 0.9f, 0.8f, 0.75f, 0.667f, 0.6f, 0.5f};
        
        for (float dpi : dpiValues) {
            Settings.AlwaysResize = dpi;
            try {
                Pattern pattern = new Pattern(patternPath).similar(0.3);
                Match match = screen.exists(pattern, 0.1);
                if (match != null && match.getScore() > bestScore) {
                    bestScore = match.getScore();
                    bestDPI = dpi;
                }
                if (match != null) {
                    System.out.printf("  DPI %.3f: %.1f%%%n", dpi, match.getScore() * 100);
                }
            } catch (Exception e) {
                // Skip errors
            }
        }
        
        if (bestScore > 0) {
            System.out.printf("  🎯 BEST: DPI %.3f = %.1f%%%n", bestDPI, bestScore * 100);
        }
    }
    
    private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return resizedImage;
    }
}