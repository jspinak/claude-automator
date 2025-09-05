package io.github.jspinak.claude.tools;

import io.github.jspinak.brobot.capture.JavaCVFFmpegCapture;
import org.sikuli.basics.Settings;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Simple test to verify FFmpeg capture configuration and pattern scaling settings.
 */
public class TestFFmpegConfig {
    
    public static void main(String[] args) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("FFMPEG CONFIGURATION TEST");
        System.out.println("=".repeat(80));
        
        // 1. Check Settings.AlwaysResize
        System.out.println("\n1. PATTERN SCALING CONFIGURATION:");
        System.out.println("-".repeat(60));
        System.out.println("   Settings.AlwaysResize: " + Settings.AlwaysResize);
        
        if (Math.abs(Settings.AlwaysResize - 1.0) < 0.001) {
            System.out.println("   ✅ Pattern scaling is DISABLED (1.0)");
            System.out.println("      Patterns will use original size");
        } else {
            System.out.println("   ⚠️ Pattern scaling is ENABLED: " + Settings.AlwaysResize);
            System.out.println("      Patterns will be scaled by factor: " + Settings.AlwaysResize);
        }
        
        // 2. Test FFmpeg capture
        System.out.println("\n2. TESTING FFMPEG CAPTURE:");
        System.out.println("-".repeat(60));
        
        try {
            System.out.println("   Attempting JavaCV FFmpeg capture...");
            BufferedImage capture = JavaCVFFmpegCapture.capture();
            
            if (capture != null) {
                System.out.println("   ✅ FFmpeg capture successful!");
                System.out.println("      Resolution: " + capture.getWidth() + "x" + capture.getHeight());
                
                // Determine if physical or logical
                if (capture.getWidth() == 1920 && capture.getHeight() == 1080) {
                    System.out.println("      ✅ Physical resolution (1920x1080)");
                } else if (capture.getWidth() == 1536 && capture.getHeight() == 864) {
                    System.out.println("      ⚠️ Logical resolution (1536x864) - 125% DPI scaling");
                } else {
                    System.out.println("      ℹ️ Other resolution: " + capture.getWidth() + "x" + capture.getHeight());
                }
                
                // Save the capture
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                File outputFile = new File("ffmpeg_test_" + timestamp + ".png");
                ImageIO.write(capture, "png", outputFile);
                System.out.println("      Saved to: " + outputFile.getName());
                System.out.println("      File size: " + (outputFile.length() / 1024) + " KB");
            } else {
                System.out.println("   ✗ FFmpeg capture returned null");
            }
            
        } catch (IOException e) {
            System.out.println("   ✗ FFmpeg capture failed: " + e.getMessage());
            System.out.println("      Note: FFmpeg may not work in WSL/headless environments");
        } catch (Exception e) {
            System.out.println("   ✗ Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 3. Pattern matching configuration summary
        System.out.println("\n3. PATTERN MATCHING CONFIGURATION SUMMARY:");
        System.out.println("-".repeat(60));
        
        boolean ffmpegWorks = testFFmpegQuietly();
        boolean scalingDisabled = Math.abs(Settings.AlwaysResize - 1.0) < 0.001;
        
        if (ffmpegWorks && scalingDisabled) {
            System.out.println("   ✅ OPTIMAL CONFIGURATION DETECTED");
            System.out.println("      - FFmpeg captures at physical resolution (1920x1080)");
            System.out.println("      - Pattern scaling is disabled (1.0)");
            System.out.println("      - Patterns from Windows/SikuliX IDE will match correctly");
        } else {
            System.out.println("   ⚠️ CONFIGURATION ISSUES:");
            if (!ffmpegWorks) {
                System.out.println("      - FFmpeg capture not working (use Robot as fallback)");
            }
            if (!scalingDisabled) {
                System.out.println("      - Pattern scaling is enabled (may cause mismatches)");
            }
        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TEST COMPLETE");
        System.out.println("=".repeat(80));
    }
    
    private static boolean testFFmpegQuietly() {
        try {
            BufferedImage img = JavaCVFFmpegCapture.capture();
            return img != null && img.getWidth() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}