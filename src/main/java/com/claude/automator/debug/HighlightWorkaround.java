package com.claude.automator.debug;

import io.github.jspinak.brobot.model.element.Region;
import org.sikuli.script.Screen;
import lombok.extern.slf4j.Slf4j;
import java.awt.Robot;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Workaround for SikuliX highlighting issues.
 * Since SikuliX highlights appear in wrong locations, this class provides alternatives.
 */
@Slf4j
public class HighlightWorkaround {
    
    /**
     * Alternative 1: Use Robot to draw directly on screen (requires special permissions).
     * This bypasses SikuliX completely.
     */
    public static void highlightWithRobot(Region region, int durationMs) {
        try {
            Robot robot = new Robot();
            
            // Take screenshot
            Rectangle rect = new Rectangle(region.getX(), region.getY(), 
                                         region.getW(), region.getH());
            BufferedImage capture = robot.createScreenCapture(rect);
            
            // Draw border on the image
            Graphics2D g = capture.createGraphics();
            g.setColor(Color.RED);
            g.setStroke(new BasicStroke(3));
            g.drawRect(0, 0, region.getW()-1, region.getH()-1);
            g.dispose();
            
            // Note: Robot can't actually draw on screen, only capture
            // This is just to show the concept
            log.info("Robot would highlight region: {}", region);
            
        } catch (Exception e) {
            log.error("Failed to use Robot: {}", e.getMessage());
        }
    }
    
    /**
     * Alternative 2: Try different Region construction methods to find one that works.
     */
    public static void highlightWithAlternativeConstruction(Region brobotRegion, double duration) {
        try {
            Screen screen = new Screen();
            
            log.info("Trying different region construction methods for: {}", brobotRegion);
            
            // Method 1: Direct SikuliX Region
            org.sikuli.script.Region method1 = new org.sikuli.script.Region(
                brobotRegion.getX(), 
                brobotRegion.getY(), 
                brobotRegion.getW(), 
                brobotRegion.getH()
            );
            log.info("Method 1 (Direct): {}", method1);
            
            // Method 2: Using screen.newRegion
            org.sikuli.script.Region method2 = screen.newRegion(
                brobotRegion.getX(), 
                brobotRegion.getY(), 
                brobotRegion.getW(), 
                brobotRegion.getH()
            );
            log.info("Method 2 (Screen.newRegion): {}", method2);
            
            // Method 3: Using setRect
            screen.setRect(brobotRegion.getX(), brobotRegion.getY(), 
                          brobotRegion.getW(), brobotRegion.getH());
            Rectangle rect = screen.getRect();
            org.sikuli.script.Region method3 = new org.sikuli.script.Region(rect);
            screen.setRect(screen.getBounds()); // Reset
            log.info("Method 3 (From Rectangle): {}", method3);
            
            // Method 4: Using Location
            org.sikuli.script.Location topLeft = new org.sikuli.script.Location(
                brobotRegion.getX(), brobotRegion.getY()
            );
            org.sikuli.script.Region method4 = new org.sikuli.script.Region(
                topLeft.x, topLeft.y, brobotRegion.getW(), brobotRegion.getH()
            );
            log.info("Method 4 (From Location): {}", method4);
            
            // Try highlighting with each method
            log.info("Highlighting with Method 2 (Screen.newRegion)...");
            method2.highlight(duration);
            
        } catch (Exception e) {
            log.error("Alternative construction failed: {}", e.getMessage());
        }
    }
    
    /**
     * Alternative 3: Log detailed information for manual debugging.
     */
    public static void debugRegionDetails(Region brobotRegion) {
        try {
            Screen screen = new Screen();
            
            log.info("\n=== REGION DEBUG DETAILS ===");
            log.info("Brobot Region: {}", brobotRegion);
            log.info("  Coordinates: x={}, y={}, w={}, h={}", 
                    brobotRegion.getX(), brobotRegion.getY(), 
                    brobotRegion.getW(), brobotRegion.getH());
            
            // Convert to SikuliX
            org.sikuli.script.Region sikuliRegion = brobotRegion.sikuli();
            if (sikuliRegion != null) {
                log.info("SikuliX Region: {}", sikuliRegion);
                log.info("  getX()={}, getY()={}", sikuliRegion.getX(), sikuliRegion.getY());
                log.info("  x={}, y={}", sikuliRegion.x, sikuliRegion.y);
                log.info("  getCenter(): {}", sikuliRegion.getCenter());
                log.info("  getScreen(): {}", sikuliRegion.getScreen());
                log.info("  getRect(): {}", sikuliRegion.getRect());
                
                // Check if region is on screen
                boolean isOnScreen = sikuliRegion.x >= 0 && 
                                   sikuliRegion.y >= 0 && 
                                   sikuliRegion.x + sikuliRegion.w <= screen.w &&
                                   sikuliRegion.y + sikuliRegion.h <= screen.h;
                log.info("  Is fully on screen: {}", isOnScreen);
                
                if (!isOnScreen) {
                    log.warn("  Region extends outside screen bounds!");
                }
            }
            
            log.info("=== END DEBUG ===\n");
            
        } catch (Exception e) {
            log.error("Debug failed: {}", e.getMessage());
        }
    }
    
    /**
     * Alternative 4: Try with offset compensation.
     * If highlights consistently appear offset, apply counter-offset.
     */
    public static void highlightWithOffset(Region brobotRegion, int xOffset, int yOffset, double duration) {
        try {
            int adjustedX = brobotRegion.getX() + xOffset;
            int adjustedY = brobotRegion.getY() + yOffset;
            
            log.info("Applying offset: Original ({},{}) -> Adjusted ({},{})",
                    brobotRegion.getX(), brobotRegion.getY(), adjustedX, adjustedY);
            
            org.sikuli.script.Region adjustedRegion = new org.sikuli.script.Region(
                adjustedX, adjustedY, brobotRegion.getW(), brobotRegion.getH()
            );
            
            adjustedRegion.highlight(duration);
            
        } catch (Exception e) {
            log.error("Offset highlight failed: {}", e.getMessage());
        }
    }
    
    /**
     * Alternative 5: Use mouse movement to indicate region boundaries.
     * This is a visual workaround that doesn't rely on highlighting.
     */
    public static void traceRegionWithMouse(Region region) {
        try {
            Robot robot = new Robot();
            
            log.info("Tracing region boundaries with mouse: {}", region);
            
            // Move to each corner
            int[][] corners = {
                {region.getX(), region.getY()},  // Top-left
                {region.getX() + region.getW(), region.getY()},  // Top-right
                {region.getX() + region.getW(), region.getY() + region.getH()},  // Bottom-right
                {region.getX(), region.getY() + region.getH()},  // Bottom-left
                {region.getX(), region.getY()}  // Back to top-left
            };
            
            for (int[] corner : corners) {
                robot.mouseMove(corner[0], corner[1]);
                Thread.sleep(500);
            }
            
            log.info("Mouse trace complete");
            
        } catch (Exception e) {
            log.error("Mouse trace failed: {}", e.getMessage());
        }
    }
}