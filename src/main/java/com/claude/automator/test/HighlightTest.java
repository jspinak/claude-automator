package com.claude.automator.test;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import io.github.jspinak.brobot.model.element.Region;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Simple test to verify highlight functionality.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HighlightTest implements CommandLineRunner {
    
    private final Action action;
    
    @Override
    public void run(String... args) throws Exception {
        // Only run if explicitly requested
        if (args.length > 0 && args[0].equals("--test-highlight")) {
            System.out.println("\n=== HIGHLIGHT TEST ===");
            
            try {
                // Create a small region to highlight
                Region testRegion = new Region(100, 100, 200, 200);
                ObjectCollection testObj = new ObjectCollection.Builder()
                        .withRegions(testRegion)
                        .build();
                
                HighlightOptions options = new HighlightOptions.Builder()
                        .setHighlightSeconds(2.0) // 2 seconds
                        .setHighlightColor("red")
                        .build();
                
                System.out.println("Attempting to highlight region: " + testRegion);
                action.perform(options, testObj);
                System.out.println("Highlight completed successfully!");
                
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