package com.claude.automator.test;

import org.sikuli.script.Screen;
import org.sikuli.script.Region;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Direct test using SikuliX highlighting to bypass framework layers.
 */
@Component
@Slf4j
public class DirectHighlightTest implements CommandLineRunner {
    
    @Override
    public void run(String... args) throws Exception {
        // Only run if explicitly requested
        if (args.length > 0 && args[0].equals("--test-sikuli-highlight")) {
            System.out.println("\n=== DIRECT SIKULI HIGHLIGHT TEST ===");
            
            try {
                Screen screen = new Screen();
                
                // Test 1: Highlight full screen
                System.out.println("Test 1: Highlighting full screen with red border...");
                Region fullScreen = new Region(screen.getBounds());
                fullScreen.highlight(2, "red"); // 2 seconds, red color
                
                Thread.sleep(500);
                
                // Test 2: Highlight center area
                System.out.println("Test 2: Highlighting center area with blue border...");
                int centerX = screen.getBounds().width / 2 - 200;
                int centerY = screen.getBounds().height / 2 - 150;
                Region centerRegion = new Region(centerX, centerY, 400, 300);
                centerRegion.highlight(2, "blue");
                
                Thread.sleep(500);
                
                // Test 3: Multiple regions
                System.out.println("Test 3: Highlighting multiple regions...");
                Region topLeft = new Region(50, 50, 300, 200);
                Region topRight = new Region(screen.getBounds().width - 350, 50, 300, 200);
                Region bottomCenter = new Region(centerX, screen.getBounds().height - 250, 400, 200);
                
                topLeft.highlight(3, "green");
                topRight.highlight(3, "yellow");
                bottomCenter.highlight(3, "magenta");
                
                System.out.println("\nIf you see colored borders on screen, SikuliX highlighting is working!");
                System.out.println("If not, there may be a display/permission issue.");
                
            } catch (Exception e) {
                System.err.println("Direct highlight test failed: " + e.getMessage());
                e.printStackTrace();
            }
            
            System.out.println("\n=== END DIRECT SIKULI HIGHLIGHT TEST ===\n");
            
            // Exit after test
            System.exit(0);
        }
    }
}