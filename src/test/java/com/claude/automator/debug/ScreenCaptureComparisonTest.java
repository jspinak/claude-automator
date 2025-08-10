package com.claude.automator.debug;

import org.junit.jupiter.api.Test;
import org.sikuli.script.Screen;
import org.sikuli.script.Region;
import org.sikuli.script.ScreenImage;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Test to compare what SikuliX captures vs what it highlights.
 * This will help identify if there's a mismatch between the capture and display coordinate systems.
 */
public class ScreenCaptureComparisonTest {
    
    @Test
    public void compareCapatureVsHighlight() throws Exception {
        System.out.println("\n================================================================================");
        System.out.println("SCREEN CAPTURE VS HIGHLIGHT COMPARISON");
        System.out.println("================================================================================\n");
        
        Screen screen = new Screen();
        
        // Test 1: Capture specific regions and see what we get
        System.out.println("--- Test 1: Capturing Specific Regions ---");
        
        // Define test regions
        Region topLeft = new Region(0, 0, 200, 200);
        Region bottomLeft = new Region(0, 880, 200, 200);  // Near bottom for 1080p
        Region center = new Region(860, 440, 200, 200);
        Region lowerLeftQuarter = new Region(0, 540, 960, 540);
        
        // Capture and save each region
        captureAndSave(screen, topLeft, "top_left");
        captureAndSave(screen, bottomLeft, "bottom_left");
        captureAndSave(screen, center, "center");
        captureAndSave(screen, lowerLeftQuarter, "lower_left_quarter");
        
        // Test 2: Highlight then capture to see if highlight appears in capture
        System.out.println("\n--- Test 2: Highlight Then Capture ---");
        
        Region testRegion = new Region(100, 100, 300, 300);
        System.out.printf("Highlighting region: %s%n", testRegion);
        testRegion.highlight(); // Start persistent highlight
        
        Thread.sleep(500); // Let highlight appear
        
        // Capture the area where we highlighted
        System.out.println("Capturing the highlighted area...");
        ScreenImage capture = screen.capture(testRegion);
        BufferedImage image = capture.getImage();
        
        File outputFile = new File("highlighted_region_capture.png");
        ImageIO.write(image, "png", outputFile);
        System.out.printf("Saved capture to: %s%n", outputFile.getAbsolutePath());
        
        testRegion.highlightOff(); // Turn off highlight
        
        // Test 3: Capture full screen and analyze
        System.out.println("\n--- Test 3: Full Screen Capture Analysis ---");
        
        ScreenImage fullCapture = screen.capture();
        BufferedImage fullImage = fullCapture.getImage();
        
        System.out.printf("Full screen capture size: %dx%d%n", 
                fullImage.getWidth(), fullImage.getHeight());
        
        File fullScreenFile = new File("full_screen_capture.png");
        ImageIO.write(fullImage, "png", fullScreenFile);
        System.out.printf("Saved full screen to: %s%n", fullScreenFile.getAbsolutePath());
        
        // Test 4: Check if screen coordinate (0,0) matches image coordinate (0,0)
        System.out.println("\n--- Test 4: Coordinate Origin Verification ---");
        
        // Highlight at (0,0)
        Region originRegion = new Region(0, 0, 100, 100);
        originRegion.highlight(3, "red");
        
        System.out.println("A RED highlight should appear at what SikuliX thinks is (0,0)");
        System.out.println("Check the captured images to see if they match what you see on screen.");
        
        // Test 5: Create visual markers
        System.out.println("\n--- Test 5: Creating Visual Reference Grid ---");
        
        // Create a grid of highlights
        for (int x = 0; x < screen.w; x += 200) {
            for (int y = 0; y < screen.h; y += 200) {
                Region gridPoint = new Region(x, y, 10, 10);
                gridPoint.highlight(0.1);
                Thread.sleep(50);
            }
        }
        
        System.out.println("Brief grid flash completed. This shows SikuliX's coordinate grid.");
        
        // Test 6: Mouse position vs highlight position
        System.out.println("\n--- Test 6: Mouse vs Highlight Coordinates ---");
        
        try {
            org.sikuli.script.Location mouseLoc = new org.sikuli.script.Location(
                java.awt.MouseInfo.getPointerInfo().getLocation()
            );
            
            System.out.printf("Mouse location: %s%n", mouseLoc);
            
            // Create region around mouse
            Region mouseRegion = new Region(
                mouseLoc.x - 50, 
                mouseLoc.y - 50, 
                100, 100
            );
            
            System.out.printf("Highlighting around mouse at: %s%n", mouseRegion);
            mouseRegion.highlight(3, "green");
            
            // Capture that area
            ScreenImage mouseCapture = screen.capture(mouseRegion);
            File mouseFile = new File("mouse_region_capture.png");
            ImageIO.write(mouseCapture.getImage(), "png", mouseFile);
            System.out.printf("Saved mouse region to: %s%n", mouseFile.getAbsolutePath());
            
        } catch (Exception e) {
            System.err.println("Mouse test failed: " + e.getMessage());
        }
        
        System.out.println("\n================================================================================");
        System.out.println("COMPARISON COMPLETE");
        System.out.println("Check the saved PNG files in the current directory.");
        System.out.println("Compare what you saw on screen with what was captured.");
        System.out.println("================================================================================\n");
    }
    
    private void captureAndSave(Screen screen, Region region, String name) {
        try {
            System.out.printf("Capturing %s: %s%n", name, region);
            
            // First highlight it so we know where it should be
            region.highlight(2, "yellow");
            Thread.sleep(500);
            
            // Then capture it
            ScreenImage capture = screen.capture(region);
            BufferedImage image = capture.getImage();
            
            File outputFile = new File(name + "_capture.png");
            ImageIO.write(image, "png", outputFile);
            
            System.out.printf("  Captured image size: %dx%d%n", 
                    image.getWidth(), image.getHeight());
            System.out.printf("  Saved to: %s%n", outputFile.getAbsolutePath());
            
            Thread.sleep(1500); // Wait for highlight to finish
            
        } catch (Exception e) {
            System.err.printf("Failed to capture %s: %s%n", name, e.getMessage());
        }
    }
}