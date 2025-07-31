package com.claude.automator.debug;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Simple main class to debug screenshot capture without Spring context.
 */
public class ScreenshotDebugMain {
    
    public static void main(String[] args) throws Exception {
        System.out.println("\n=== SCREENSHOT DEBUG (No Spring) ===");
        
        // 1. Environment check
        System.out.println("\n1. Environment:");
        System.out.println("   java.awt.headless: " + System.getProperty("java.awt.headless"));
        System.out.println("   GraphicsEnvironment.isHeadless: " + GraphicsEnvironment.isHeadless());
        
        // 2. Display check
        System.out.println("\n2. Display Configuration:");
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = ge.getScreenDevices();
        System.out.println("   Number of screens: " + devices.length);
        for (int i = 0; i < devices.length; i++) {
            GraphicsDevice device = devices[i];
            System.out.println("   Screen " + i + ": " + device.getIDstring());
            DisplayMode mode = device.getDisplayMode();
            System.out.println("     Resolution: " + mode.getWidth() + "x" + mode.getHeight());
        }
        
        // 3. Try to capture
        System.out.println("\n3. Capture Test:");
        try {
            Robot robot = new Robot();
            Rectangle captureArea = new Rectangle(0, 0, 300, 200);
            System.out.println("   Capturing area: " + captureArea);
            
            BufferedImage capture = robot.createScreenCapture(captureArea);
            
            // Save it
            File outputFile = new File("debug-screenshot.png");
            ImageIO.write(capture, "png", outputFile);
            System.out.println("   Saved to: " + outputFile.getAbsolutePath());
            
            // Analyze
            int blackPixels = 0;
            int totalSamples = 100;
            
            for (int i = 0; i < totalSamples; i++) {
                int x = (int)(Math.random() * capture.getWidth());
                int y = (int)(Math.random() * capture.getHeight());
                int rgb = capture.getRGB(x, y);
                if (rgb == 0xFF000000) {
                    blackPixels++;
                }
            }
            
            double blackPercentage = (blackPixels * 100.0) / totalSamples;
            System.out.println("   Black pixels: " + blackPercentage + "%");
            System.out.println("   File size: " + outputFile.length() + " bytes");
            
            if (blackPercentage > 90) {
                System.out.println("\n   ⚠️  WARNING: Screenshot is BLACK!");
                
                // Print pixel values from corners
                System.out.println("\n   Corner pixel values:");
                System.out.printf("   Top-left (0,0): 0x%08X%n", capture.getRGB(0, 0));
                System.out.printf("   Top-right (%d,0): 0x%08X%n", capture.getWidth()-1, capture.getRGB(capture.getWidth()-1, 0));
                System.out.printf("   Bottom-left (0,%d): 0x%08X%n", capture.getHeight()-1, capture.getRGB(0, capture.getHeight()-1));
                System.out.printf("   Bottom-right (%d,%d): 0x%08X%n", 
                    capture.getWidth()-1, capture.getHeight()-1, 
                    capture.getRGB(capture.getWidth()-1, capture.getHeight()-1));
                
                // Environment variables
                System.out.println("\n   Environment variables:");
                System.out.println("   DISPLAY: " + System.getenv("DISPLAY"));
                System.out.println("   SSH_CONNECTION: " + System.getenv("SSH_CONNECTION"));
                System.out.println("   TERM: " + System.getenv("TERM"));
                
                // Try to find any non-black pixel
                System.out.println("\n   Searching for non-black pixels...");
                boolean foundNonBlack = false;
                for (int y = 0; y < capture.getHeight() && !foundNonBlack; y += 5) {
                    for (int x = 0; x < capture.getWidth() && !foundNonBlack; x += 5) {
                        int rgb = capture.getRGB(x, y);
                        if (rgb != 0xFF000000) {
                            System.out.printf("   Found non-black pixel at (%d,%d): 0x%08X%n", x, y, rgb);
                            foundNonBlack = true;
                        }
                    }
                }
                if (!foundNonBlack) {
                    System.out.println("   All sampled pixels are black!");
                }
                
            } else {
                System.out.println("\n   ✓ Screenshot has content!");
                System.out.printf("   Sample pixel (50,50): 0x%08X%n", capture.getRGB(50, 50));
            }
            
        } catch (Exception e) {
            System.err.println("   ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n=== DEBUG COMPLETE ===");
    }
}