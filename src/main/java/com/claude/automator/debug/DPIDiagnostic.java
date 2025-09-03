package com.claude.automator.debug;

import org.sikuli.basics.Settings;
import org.sikuli.script.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Standalone diagnostic for DPI scaling issues.
 * Run this to understand why patterns aren't matching.
 */
public class DPIDiagnostic {
    
    public static void main(String[] args) throws Exception {
        System.out.println("\n========== DPI SCALING DIAGNOSIS ==========\n");
        
        // 1. Check current settings
        System.out.println("1. CURRENT SETTINGS:");
        System.out.println("   Settings.AlwaysResize: " + Settings.AlwaysResize);
        System.out.println("   Settings.MinSimilarity: " + Settings.MinSimilarity);
        
        // 2. Check screen dimensions
        System.out.println("\n2. SCREEN DIMENSIONS:");
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        System.out.println("   Logical screen size: " + screenSize.width + "x" + screenSize.height);
        
        // 3. Capture current screen
        System.out.println("\n3. SCREEN CAPTURE TEST:");
        Screen screen = new Screen();
        ScreenImage capture = screen.capture();
        BufferedImage capturedImage = capture.getImage();
        System.out.println("   Captured dimensions: " + capturedImage.getWidth() + "x" + capturedImage.getHeight());
        
        // 4. Check pattern dimensions
        System.out.println("\n4. PATTERN DIMENSIONS:");
        String[] patterns = {
            "images/prompt/claude-prompt-1.png",
            "images/working/working-1.png"
        };
        
        for (String patternPath : patterns) {
            File patternFile = new File(patternPath);
            if (patternFile.exists()) {
                BufferedImage patternImage = ImageIO.read(patternFile);
                System.out.println("   " + patternPath + ": " + 
                    patternImage.getWidth() + "x" + patternImage.getHeight() + " pixels");
            } else {
                System.out.println("   " + patternPath + ": NOT FOUND");
            }
        }
        
        // 5. Calculate scaling
        System.out.println("\n5. SCALING ANALYSIS:");
        
        // Common resolutions and their DPI scaled versions
        if (capturedImage.getWidth() == 1536 && capturedImage.getHeight() == 864) {
            System.out.println("   ✓ Detected: Logical resolution 1536x864 (125% scaling of 1920x1080)");
            System.out.println("   → Patterns at 1920x1080 need 0.8x scaling (1536/1920 = 0.8)");
            System.out.println("   → SOLUTION: Settings.AlwaysResize = 0.8f");
        } else if (capturedImage.getWidth() == 1280 && capturedImage.getHeight() == 720) {
            System.out.println("   ✓ Detected: Logical resolution 1280x720 (150% scaling of 1920x1080)");
            System.out.println("   → Patterns at 1920x1080 need 0.667x scaling (1280/1920 = 0.667)");
            System.out.println("   → SOLUTION: Settings.AlwaysResize = 0.667f");
        } else if (capturedImage.getWidth() == 1920 && capturedImage.getHeight() == 1080) {
            System.out.println("   ✓ Detected: Physical resolution 1920x1080 (no scaling)");
            System.out.println("   → No scaling needed");
            System.out.println("   → SOLUTION: Settings.AlwaysResize = 1.0f");
        } else {
            System.out.println("   ? Unknown resolution: " + capturedImage.getWidth() + "x" + capturedImage.getHeight());
            float scaleX = capturedImage.getWidth() / 1920.0f;
            float scaleY = capturedImage.getHeight() / 1080.0f;
            System.out.println("   → Estimated scale factor: " + scaleX);
        }
        
        // 6. Test with different factors
        System.out.println("\n6. TESTING PATTERN MATCHING:");
        File testPattern = new File("images/prompt/claude-prompt-1.png");
        if (testPattern.exists()) {
            BufferedImage patternImage = ImageIO.read(testPattern);
            System.out.println("   Testing pattern: " + patternImage.getWidth() + "x" + patternImage.getHeight());
            
            float[] testFactors = {Settings.AlwaysResize, 1.0f, 0.8f, 0.667f, 1.25f};
            for (float factor : testFactors) {
                Settings.AlwaysResize = factor;
                Pattern pattern = new Pattern(patternImage).similar(0.70);
                
                System.out.print("   AlwaysResize=" + factor + ": ");
                try {
                    Match match = screen.find(pattern);
                    System.out.println("✓ FOUND (score: " + String.format("%.3f", match.getScore()) + ")");
                } catch (FindFailed e) {
                    System.out.println("✗ NOT FOUND");
                }
            }
        }
        
        System.out.println("\n========================================\n");
    }
}