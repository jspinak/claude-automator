package com.claude.automator.test;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.tools.logging.visual.VisualFeedbackConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Simple test to verify highlight console output format.
 */
@Component
@ConditionalOnProperty(name = "test.simple-highlight-console", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class SimpleHighlightConsoleTest implements CommandLineRunner {
    
    private final Action action;
    private final VisualFeedbackConfig visualFeedbackConfig;
    
    @Override
    public void run(String... args) throws Exception {
        // Only run if explicitly requested
        if (args.length > 0 && args[0].equals("--test-simple-highlight")) {
            System.out.println("\n=== SIMPLE HIGHLIGHT CONSOLE TEST ===");
            
            // Verify highlighting is enabled
            System.out.println("Highlight enabled: " + visualFeedbackConfig.isEnabled());
            
            // Create a test region
            Region testRegion = new Region(100, 100, 200, 200);
            System.out.println("Test region: " + testRegion);
            
            // Create highlight options
            HighlightOptions highlightOptions = new HighlightOptions.Builder()
                .setHighlightSeconds(2.0)
                .setHighlightColor("RED")
                .build();
            
            // Create object collection
            ObjectCollection collection = new ObjectCollection.Builder()
                .withRegions(testRegion)
                .build();
            
            System.out.println("\n--- CONSOLE OUTPUT START ---");
            
            // Perform highlight - this should produce console output
            ActionResult result = action.perform(highlightOptions, collection);
            
            System.out.println("--- CONSOLE OUTPUT END ---\n");
            
            System.out.println("Action result: " + (result.isSuccess() ? "SUCCESS" : "FAILED"));
            
            if (result.isSuccess()) {
                System.out.println("✓ Highlight action completed successfully");
            } else {
                System.err.println("✗ Highlight action failed");
            }
            
            System.out.println("=== END SIMPLE HIGHLIGHT CONSOLE TEST ===");
            
            // Exit after test
            System.exit(result.isSuccess() ? 0 : 1);
        }
    }
}