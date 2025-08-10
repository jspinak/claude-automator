package com.claude.automator.debug;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.state.StateRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Debug utility to display and highlight regions with detailed console output.
 */
@Component
public class RegionDebugger {
    
    private static final Logger log = LoggerFactory.getLogger(RegionDebugger.class);
    
    @Autowired
    private Action action;
    
    /**
     * Display detailed information about a region and optionally highlight it.
     */
    public void debugRegion(String name, Region region, boolean highlight) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("REGION DEBUG: " + name);
        System.out.println("=".repeat(60));
        
        // Display basic info
        System.out.println("String representation: " + region.toString());
        System.out.println("Coordinates:");
        System.out.println("  X (left edge):   " + region.getX());
        System.out.println("  Y (top edge):    " + region.getY());
        System.out.println("  Width:           " + region.getW());
        System.out.println("  Height:          " + region.getH());
        System.out.println("  Right edge (X2): " + region.x2());
        System.out.println("  Bottom edge (Y2):" + region.y2());
        
        // Calculate screen percentages (assuming 1920x1080)
        int screenWidth = 1920;
        int screenHeight = 1080;
        double xPercent = (region.getX() * 100.0) / screenWidth;
        double yPercent = (region.getY() * 100.0) / screenHeight;
        double widthPercent = (region.getW() * 100.0) / screenWidth;
        double heightPercent = (region.getH() * 100.0) / screenHeight;
        
        System.out.println("\nScreen percentages (assuming 1920x1080):");
        System.out.printf("  X: %.1f%% of screen width%n", xPercent);
        System.out.printf("  Y: %.1f%% of screen height%n", yPercent);
        System.out.printf("  Width: %.1f%% of screen width%n", widthPercent);
        System.out.printf("  Height: %.1f%% of screen height%n", heightPercent);
        
        // Visual representation
        System.out.println("\nVisual representation on 1920x1080 screen:");
        printVisualGrid(region, screenWidth, screenHeight);
        
        // Highlight if requested
        if (highlight) {
            System.out.println("\nHighlighting region...");
            highlightRegion(region);
        }
        
        System.out.println("=".repeat(60) + "\n");
    }
    
    private void printVisualGrid(Region region, int screenWidth, int screenHeight) {
        // Create a simple ASCII representation
        int gridWidth = 40;
        int gridHeight = 20;
        
        // Calculate region position in grid
        int gridX1 = (region.getX() * gridWidth) / screenWidth;
        int gridY1 = (region.getY() * gridHeight) / screenHeight;
        int gridX2 = (region.x2() * gridWidth) / screenWidth;
        int gridY2 = (region.y2() * gridHeight) / screenHeight;
        
        // Ensure bounds
        gridX1 = Math.max(0, Math.min(gridX1, gridWidth - 1));
        gridY1 = Math.max(0, Math.min(gridY1, gridHeight - 1));
        gridX2 = Math.max(0, Math.min(gridX2, gridWidth - 1));
        gridY2 = Math.max(0, Math.min(gridY2, gridHeight - 1));
        
        // Draw grid
        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                if (y == 0 || y == gridHeight - 1) {
                    // Top or bottom border
                    System.out.print("-");
                } else if (x == 0 || x == gridWidth - 1) {
                    // Left or right border
                    System.out.print("|");
                } else if (x >= gridX1 && x <= gridX2 && y >= gridY1 && y <= gridY2) {
                    // Inside the region
                    System.out.print("#");
                } else {
                    // Empty space
                    System.out.print(" ");
                }
            }
            System.out.println();
        }
        
        System.out.println("Legend: # = highlighted region, - = screen edge");
    }
    
    private void highlightRegion(Region region) {
        try {
            StateRegion stateRegion = new StateRegion.Builder()
                .setSearchRegion(region)
                .setName("Debug Region")
                .build();
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withRegions(stateRegion)
                .build();
            
            // Use perform method with highlight action
            ActionResult result = action.perform(
                new io.github.jspinak.brobot.action.basic.highlight.HighlightOptions.Builder()
                    .setDuration(2.0)  // 2 seconds for debugging
                    .setColor("blue")
                    .build(),
                collection
            );
            
            System.out.println("Highlight result: " + (result.isSuccess() ? "SUCCESS" : "FAILED"));
            if (!result.isSuccess()) {
                System.out.println("Highlight matches found: " + result.getMatchList().size());
            }
        } catch (Exception e) {
            System.err.println("Error highlighting region: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Compare the expected lower left quarter with the actual region.
     */
    public void debugLowerLeftQuarter() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("LOWER LEFT QUARTER ANALYSIS");
        System.out.println("=".repeat(60));
        
        // Create the lower left quarter
        Region lowerLeft = Region.builder()
            .withScreenPercentage(0.0, 0.5, 0.5, 0.5)
            .build();
        
        debugRegion("Lower Left Quarter", lowerLeft, true);
        
        // Also show all quarters for comparison
        System.out.println("\nALL SCREEN QUARTERS FOR COMPARISON:");
        System.out.println("-".repeat(40));
        
        Region upperLeft = Region.builder()
            .withScreenPercentage(0.0, 0.0, 0.5, 0.5)
            .build();
        System.out.println("Upper Left:  " + upperLeft.toString());
        
        Region upperRight = Region.builder()
            .withScreenPercentage(0.5, 0.0, 0.5, 0.5)
            .build();
        System.out.println("Upper Right: " + upperRight.toString());
        
        System.out.println("Lower Left:  " + lowerLeft.toString());
        
        Region lowerRight = Region.builder()
            .withScreenPercentage(0.5, 0.5, 0.5, 0.5)
            .build();
        System.out.println("Lower Right: " + lowerRight.toString());
        
        System.out.println("-".repeat(40));
    }
}