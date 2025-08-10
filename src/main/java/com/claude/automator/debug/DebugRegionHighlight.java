package com.claude.automator.debug;

import com.claude.automator.states.PromptState;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import io.github.jspinak.brobot.model.element.Region;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * Debug application to test and visualize region highlighting.
 */
//@SpringBootApplication
@ComponentScan(basePackages = {"com.claude.automator", "io.github.jspinak.brobot"})
public class DebugRegionHighlight {
    
    public static void main(String[] args) {
        SpringApplication.run(DebugRegionHighlight.class, args);
    }
    
    @Bean
    CommandLineRunner debugRegions(Action action, RegionDebugger debugger) {
        return args -> {
            System.out.println("\n" + "=".repeat(80));
            System.out.println("REGION HIGHLIGHTING DEBUG TEST");
            System.out.println("=".repeat(80));
            
            // Test 1: Create and debug the lower left quarter
            System.out.println("\n1. Testing Lower Left Quarter Region Creation:");
            System.out.println("-".repeat(60));
            
            Region lowerLeftQuarter = Region.builder()
                .withScreenPercentage(0.0, 0.5, 0.5, 0.5)
                .build();
            
            System.out.println("Created region: " + lowerLeftQuarter.toString());
            System.out.println("  X: " + lowerLeftQuarter.getX() + " (should be 0)");
            System.out.println("  Y: " + lowerLeftQuarter.getY() + " (should be 540 for 1080p)");
            System.out.println("  Width: " + lowerLeftQuarter.getW() + " (should be 960 for 1920 width)");
            System.out.println("  Height: " + lowerLeftQuarter.getH() + " (should be 540 for 1080p)");
            
            // Test 2: Highlight the region
            System.out.println("\n2. Highlighting the Lower Left Quarter:");
            System.out.println("-".repeat(60));
            
            HighlightOptions options = new HighlightOptions.Builder()
                .setDuration(3.0)  // 3 seconds
                .setColor("red")   // Red for visibility
                .build();
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withRegions(lowerLeftQuarter)
                .build();
            
            System.out.println("Executing highlight action...");
            ActionResult result = action.perform(options, collection);
            
            System.out.println("Highlight result: " + (result.isSuccess() ? "SUCCESS" : "FAILED"));
            System.out.println("Matches found: " + result.getMatchList().size());
            
            // Test 3: Create PromptState to see its initialization
            System.out.println("\n3. Creating PromptState (should show debug output):");
            System.out.println("-".repeat(60));
            
            PromptState promptState = new PromptState();
            System.out.println("PromptState created with ClaudePrompt: " + promptState.getClaudePrompt().getName());
            
            // Test 4: Show all screen quarters
            System.out.println("\n4. All Screen Quarters for Reference:");
            System.out.println("-".repeat(60));
            
            debugger.debugLowerLeftQuarter();
            
            System.out.println("\n" + "=".repeat(80));
            System.out.println("DEBUG TEST COMPLETE");
            System.out.println("=".repeat(80));
            
            System.out.println("\nThe console output above shows:");
            System.out.println("1. RegionBuilder debug logs (if DEBUG level is enabled)");
            System.out.println("2. Detailed region coordinates and calculations");
            System.out.println("3. Visual highlighting feedback");
            System.out.println("4. PromptState initialization with search region");
            
            System.out.println("\nCheck your screen for the red highlight in the lower left quarter.");
            System.out.println("The region should cover:");
            System.out.println("  - Left half of the screen (0 to 960 pixels)");
            System.out.println("  - Bottom half of the screen (540 to 1080 pixels)");
            
            // Keep app running briefly to see the highlight
            Thread.sleep(3000);
            
            System.exit(0);
        };
    }
}