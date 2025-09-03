package com.claude.automator.diagnostic;

import org.junit.jupiter.api.Test;
import org.sikuli.basics.Settings;

/**
 * Simple diagnostic test that can run without Spring context.
 * This is the EASIEST way to run diagnostics in IntelliJ.
 * 
 * Just click the green arrow next to the test method!
 */
public class SimpleDiagnosticTest {

    @Test
    public void checkCurrentDPISettings() {
        System.out.println("\n==================================================");
        System.out.println("         CURRENT DPI SETTINGS CHECK");
        System.out.println("==================================================\n");
        
        // Show current settings
        System.out.println("Settings.AlwaysResize: " + Settings.AlwaysResize);
        System.out.println("Settings.MinSimilarity: " + Settings.MinSimilarity);
        System.out.println("Settings.CheckLastSeen: " + Settings.CheckLastSeen);
        
        // Test screen detection
        try {
            if (!java.awt.GraphicsEnvironment.isHeadless()) {
                java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
                if (ge != null && ge.getDefaultScreenDevice() != null) {
                    double scaleX = ge.getDefaultScreenDevice()
                        .getDefaultConfiguration()
                        .getDefaultTransform()
                        .getScaleX();
                    double scaleY = ge.getDefaultScreenDevice()
                        .getDefaultConfiguration()
                        .getDefaultTransform()
                        .getScaleY();
                    
                    System.out.println("\nDisplay scaling detected: " + (int)(scaleX * 100) + "% x " + (int)(scaleY * 100) + "%");
                    
                    if (Math.abs(scaleX - 1.25) < 0.01) {
                        System.out.println("You have 125% DPI scaling - recommended Settings.AlwaysResize = 0.8");
                    } else if (Math.abs(scaleX - 1.0) < 0.01) {
                        System.out.println("You have 100% DPI scaling - no adjustment needed");
                    }
                }
                
                // Get screen resolution
                java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
                System.out.println("\nScreen Resolution: " + 
                    toolkit.getScreenSize().width + "x" + 
                    toolkit.getScreenSize().height);
                System.out.println("Screen DPI: " + toolkit.getScreenResolution());
                
            } else {
                System.out.println("\nRunning in headless mode - cannot detect display settings");
            }
        } catch (Exception e) {
            System.out.println("\nError detecting display settings: " + e.getMessage());
        }
        
        System.out.println("\n==================================================\n");
    }
    
    @Test 
    public void testWithDifferentScalingFactors() {
        System.out.println("\n==================================================");
        System.out.println("    TESTING DIFFERENT SCALING FACTORS");
        System.out.println("==================================================\n");
        
        float originalResize = Settings.AlwaysResize;
        
        // Test different values
        float[] testValues = {0f, 0.5f, 0.67f, 0.8f, 1.0f};
        
        for (float value : testValues) {
            Settings.AlwaysResize = value;
            System.out.println("Settings.AlwaysResize = " + value);
            
            // You could add actual pattern matching test here if needed
            // For now, just show what the setting is
            
            if (value == 0f) {
                System.out.println("  -> Scaling DISABLED - patterns used at original size");
            } else if (value < 1.0f) {
                System.out.println("  -> Patterns will be DOWNSCALED by " + value);
            } else if (value == 1.0f) {
                System.out.println("  -> No scaling applied");
            }
            System.out.println();
        }
        
        // Restore original
        Settings.AlwaysResize = originalResize;
        System.out.println("Restored Settings.AlwaysResize to: " + originalResize);
        
        System.out.println("\n==================================================\n");
    }
}