package com.claude.automator.debug;

import io.github.jspinak.brobot.model.element.Region;
import lombok.extern.slf4j.Slf4j;

/**
 * Highlighter that compensates for DPI scaling and Wayland coordinate issues.
 */
@Slf4j
public class CoordinateFixHighlighter {
    
    // Detected scaling factors from your environment
    private static final double X_SCALE = 1.333333333333333;
    private static final double Y_SCALE = 1.336842105263158;
    
    /**
     * Highlights a region with coordinate compensation for DPI scaling.
     * 
     * @param region The Brobot region to highlight
     * @param duration Duration in seconds
     * @param useScalingFix Whether to apply scaling compensation
     */
    public static void highlightWithFix(Region region, double duration, boolean useScalingFix) {
        try {
            int x = region.getX();
            int y = region.getY();
            int w = region.getW();
            int h = region.getH();
            
            if (useScalingFix) {
                // Apply inverse scaling to compensate for DPI scaling
                x = (int)(x / X_SCALE);
                y = (int)(y / Y_SCALE);
                w = (int)(w / X_SCALE);
                h = (int)(h / Y_SCALE);
                
                log.info("Applied scaling fix: Original ({},{},{},{}) -> Adjusted ({},{},{},{})",
                        region.getX(), region.getY(), region.getW(), region.getH(),
                        x, y, w, h);
            }
            
            org.sikuli.script.Region sikuliRegion = new org.sikuli.script.Region(x, y, w, h);
            log.info("Highlighting region: {}", sikuliRegion);
            sikuliRegion.highlight(duration);
            
        } catch (Exception e) {
            log.error("Failed to highlight region with fix: {}", e.getMessage());
        }
    }
    
    /**
     * Highlights a region at a specific screen position, ignoring the region's coordinates.
     * Useful for debugging when highlights appear in wrong locations.
     * 
     * @param forcedX X coordinate to force
     * @param forcedY Y coordinate to force  
     * @param width Width of highlight
     * @param height Height of highlight
     * @param duration Duration in seconds
     */
    public static void highlightAtPosition(int forcedX, int forcedY, int width, int height, double duration) {
        try {
            org.sikuli.script.Region sikuliRegion = new org.sikuli.script.Region(
                forcedX, forcedY, width, height
            );
            log.info("Force highlighting at position: {}", sikuliRegion);
            sikuliRegion.highlight(duration);
        } catch (Exception e) {
            log.error("Failed to highlight at position: {}", e.getMessage());
        }
    }
    
    /**
     * Tests different coordinate interpretations to find where highlights actually appear.
     */
    public static void debugHighlightPositions(Region region) {
        log.info("\n=== HIGHLIGHT POSITION DEBUG ===");
        log.info("Original region: {}", region);
        
        // Test 1: Direct coordinates
        log.info("Test 1: Direct coordinates");
        highlightWithFix(region, 1, false);
        
        try { Thread.sleep(1500); } catch (InterruptedException e) {}
        
        // Test 2: With scaling fix
        log.info("Test 2: With DPI scaling compensation");
        highlightWithFix(region, 1, true);
        
        try { Thread.sleep(1500); } catch (InterruptedException e) {}
        
        // Test 3: Inverted Y coordinate (in case of coordinate system flip)
        log.info("Test 3: Inverted Y coordinate");
        int invertedY = 1080 - region.getY() - region.getH();
        highlightAtPosition(region.getX(), invertedY, region.getW(), region.getH(), 1);
        
        try { Thread.sleep(1500); } catch (InterruptedException e) {}
        
        log.info("=== DEBUG COMPLETE ===\n");
    }
}