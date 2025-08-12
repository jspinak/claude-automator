package com.claude.automator.debug;

import org.junit.jupiter.api.Test;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;
import org.sikuli.script.Region;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Test to compare SikuliX Screen.capture() with direct Robot capture
 * to understand why we're not getting physical pixels.
 */
public class ScreenCaptureComparisonTest {
    
    @Test
    public void compareScreenCaptureMethods() {
        System.out.println("=== SCREEN CAPTURE COMPARISON TEST ===\n");
        
        try {
            // 1. Get screen information
            System.out.println("1. SCREEN INFORMATION:");
            Screen screen = new Screen();
            Rectangle screenBounds = screen.getBounds();
            System.out.println("   SikuliX Screen.getBounds(): " + screenBounds.width + "x" + screenBounds.height);
            
            // 2. Test SikuliX Screen.capture() with full screen
            System.out.println("\n2. SIKULIX SCREEN.CAPTURE() - FULL SCREEN:");
            ScreenImage sikuliCapture = screen.capture();
            if (sikuliCapture != null && sikuliCapture.getImage() != null) {
                BufferedImage sikuliImg = sikuliCapture.getImage();
                System.out.println("   Captured dimensions: " + sikuliImg.getWidth() + "x" + sikuliImg.getHeight());
                System.out.println("   Image type: " + getImageTypeName(sikuliImg.getType()));
            } else {
                System.out.println("   ERROR: SikuliX capture returned null");
            }
            
            // 3. Test SikuliX Screen.capture() with a region
            System.out.println("\n3. SIKULIX SCREEN.CAPTURE() - WITH REGION:");
            Region testRegion = new Region(0, 0, 100, 100);
            ScreenImage regionCapture = screen.capture(testRegion);
            if (regionCapture != null && regionCapture.getImage() != null) {
                BufferedImage regionImg = regionCapture.getImage();
                System.out.println("   Requested region: 100x100");
                System.out.println("   Captured dimensions: " + regionImg.getWidth() + "x" + regionImg.getHeight());
            } else {
                System.out.println("   ERROR: Region capture returned null");
            }
            
            // 4. Test direct Robot capture
            System.out.println("\n4. DIRECT ROBOT CAPTURE:");
            Robot robot = new Robot();
            
            // Get default screen device info
            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice device = env.getDefaultScreenDevice();
            GraphicsConfiguration config = device.getDefaultConfiguration();
            
            // Get logical bounds
            Rectangle logicalBounds = config.getBounds();
            System.out.println("   Logical screen bounds: " + logicalBounds.width + "x" + logicalBounds.height);
            
            // Get DPI scale
            java.awt.geom.AffineTransform transform = config.getDefaultTransform();
            double scaleX = transform.getScaleX();
            double scaleY = transform.getScaleY();
            System.out.println("   DPI Scale: " + (int)(scaleX * 100) + "% (scaleX=" + scaleX + ", scaleY=" + scaleY + ")");
            
            // Calculate physical dimensions
            int physicalWidth = (int)(logicalBounds.width * scaleX);
            int physicalHeight = (int)(logicalBounds.height * scaleY);
            System.out.println("   Calculated physical dimensions: " + physicalWidth + "x" + physicalHeight);
            
            // Try capturing at logical resolution
            System.out.println("\n   4a. Robot capture at logical resolution:");
            BufferedImage logicalCapture = robot.createScreenCapture(new Rectangle(0, 0, logicalBounds.width, logicalBounds.height));
            System.out.println("      Requested: " + logicalBounds.width + "x" + logicalBounds.height);
            System.out.println("      Captured: " + logicalCapture.getWidth() + "x" + logicalCapture.getHeight());
            
            // Try capturing at physical resolution
            System.out.println("\n   4b. Robot capture at physical resolution:");
            BufferedImage physicalCapture = robot.createScreenCapture(new Rectangle(0, 0, physicalWidth, physicalHeight));
            System.out.println("      Requested: " + physicalWidth + "x" + physicalHeight);
            System.out.println("      Captured: " + physicalCapture.getWidth() + "x" + physicalCapture.getHeight());
            
            // 5. Check Toolkit screen size
            System.out.println("\n5. TOOLKIT SCREEN SIZE:");
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            System.out.println("   Toolkit.getScreenSize(): " + screenSize.width + "x" + screenSize.height);
            
            // 6. Check all graphics devices
            System.out.println("\n6. ALL GRAPHICS DEVICES:");
            GraphicsDevice[] devices = env.getScreenDevices();
            for (int i = 0; i < devices.length; i++) {
                GraphicsDevice gd = devices[i];
                DisplayMode dm = gd.getDisplayMode();
                System.out.println("   Device " + i + ": " + dm.getWidth() + "x" + dm.getHeight() + 
                                 " @ " + dm.getRefreshRate() + "Hz");
            }
            
            // 7. Summary
            System.out.println("\n7. SUMMARY:");
            System.out.println("   SikuliX sees: " + screenBounds.width + "x" + screenBounds.height);
            System.out.println("   Toolkit sees: " + screenSize.width + "x" + screenSize.height);
            System.out.println("   Logical resolution: " + logicalBounds.width + "x" + logicalBounds.height);
            System.out.println("   Physical resolution: " + physicalWidth + "x" + physicalHeight);
            System.out.println("   DPI Scaling: " + (int)(scaleX * 100) + "%");
            
            if (screenBounds.width == logicalBounds.width && screenBounds.height == logicalBounds.height) {
                System.out.println("\n   => SikuliX is using LOGICAL resolution (DPI-aware)");
                System.out.println("   => This matches Windows display scaling behavior");
                System.out.println("   => Patterns captured at this resolution will match");
            } else if (screenBounds.width == physicalWidth && screenBounds.height == physicalHeight) {
                System.out.println("\n   => SikuliX is using PHYSICAL resolution (not DPI-aware)");
            } else {
                System.out.println("\n   => UNEXPECTED: SikuliX resolution doesn't match either logical or physical!");
            }
            
        } catch (Exception e) {
            System.err.println("ERROR during test: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String getImageTypeName(int type) {
        switch(type) {
            case BufferedImage.TYPE_INT_RGB: return "TYPE_INT_RGB (1)";
            case BufferedImage.TYPE_INT_ARGB: return "TYPE_INT_ARGB (2)";
            case BufferedImage.TYPE_INT_ARGB_PRE: return "TYPE_INT_ARGB_PRE (3)";
            case BufferedImage.TYPE_INT_BGR: return "TYPE_INT_BGR (4)";
            case BufferedImage.TYPE_3BYTE_BGR: return "TYPE_3BYTE_BGR (5)";
            case BufferedImage.TYPE_4BYTE_ABGR: return "TYPE_4BYTE_ABGR (6)";
            default: return "Type " + type;
        }
    }
}