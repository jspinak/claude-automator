package com.claude.automator.test;

import io.github.jspinak.brobot.tools.logging.visual.HighlightManager;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.Collections;

/**
 * Simple test to verify highlight functionality.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HighlightTest implements CommandLineRunner {
    
    private final HighlightManager highlightManager;
    
    @Override
    public void run(String... args) throws Exception {
        // Only run if explicitly requested
        if (args.length > 0 && args[0].equals("--test-highlight")) {
            System.out.println("\n=== HIGHLIGHT TEST ===");
            
            try {
                // Test 1: Create a test region
                Region testRegion = new Region(100, 100, 200, 200);
                System.out.println("Using test region: " + testRegion);
                
                // Test 2: Highlight as a search region
                System.out.println("Test 2: Highlighting as search region");
                highlightManager.highlightSearchRegions(Collections.singletonList(testRegion));
                Thread.sleep(1500);
                
                // Test 3: Create a match and highlight it
                System.out.println("Test 3: Highlighting as a match");
                Match testMatch = new Match.Builder()
                        .setRegion(testRegion)
                        .setSimScore(0.95) // 95% confidence
                        .build();
                highlightManager.highlightMatches(Collections.singletonList(testMatch));
                Thread.sleep(2500);
                
                // Test 4: Highlight a click location
                System.out.println("Test 4: Highlighting click location");
                // Use center of the region for click highlight
                int centerX = testRegion.x() + testRegion.w() / 2;
                int centerY = testRegion.y() + testRegion.h() / 2;
                highlightManager.highlightClick(centerX, centerY);
                Thread.sleep(1000);
                
                System.out.println("All highlight tests completed successfully!");
                
            } catch (Exception e) {
                System.err.println("Highlight test failed: " + e.getMessage());
                e.printStackTrace();
            }
            
            System.out.println("=== END HIGHLIGHT TEST ===\n");
            
            // Exit after test
            System.exit(0);
        }
    }
}