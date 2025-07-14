package com.claude.automator.test;

import com.claude.automator.states.PromptState;
import com.claude.automator.states.WorkingState;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.tools.logging.visual.HighlightManager;
import io.github.jspinak.brobot.model.element.Region;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Direct test to find and highlight Claude UI elements without state navigation.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DirectFindTest implements CommandLineRunner {
    
    private final Action action;
    private final HighlightManager highlightManager;
    private final PromptState promptState;
    private final WorkingState workingState;
    
    @Override
    public void run(String... args) throws Exception {
        // Only run if explicitly requested
        if (args.length > 0 && args[0].equals("--test-direct-find")) {
            System.out.println("\n=== DIRECT FIND TEST ===");
            
            try {
                // Test 1: Search for prompt with highlighting
                System.out.println("\nTest 1: Searching for Claude prompt...");
                
                // Highlight full screen as search region
                Region fullScreen = new Region();
                highlightManager.highlightSearchRegions(Collections.singletonList(fullScreen));
                Thread.sleep(1000);
                
                // Try to find the prompt
                PatternFindOptions findOptions = new PatternFindOptions.Builder()
                        .setPauseBeforeBegin(0.5)
                        .setPauseAfterEnd(1.0)
                        .build();
                
                ActionResult promptResult = action.perform(findOptions, promptState.getClaudePrompt());
                
                if (promptResult.isSuccess()) {
                    System.out.println("✓ Found Claude prompt!");
                    System.out.println("  Matches: " + promptResult.getMatchList().size());
                    
                    // Highlight the matches
                    highlightManager.highlightMatches(promptResult.getMatchList());
                    Thread.sleep(2000);
                } else {
                    System.out.println("✗ Claude prompt not found");
                }
                
                // Test 2: Search for working icon with highlighting
                System.out.println("\nTest 2: Searching for Claude working icon...");
                
                // Highlight search region again
                highlightManager.highlightSearchRegions(Collections.singletonList(fullScreen));
                Thread.sleep(1000);
                
                ActionResult iconResult = action.perform(findOptions, workingState.getClaudeIcon());
                
                if (iconResult.isSuccess()) {
                    System.out.println("✓ Found Claude working icon!");
                    System.out.println("  Matches: " + iconResult.getMatchList().size());
                    
                    // Highlight the matches
                    highlightManager.highlightMatches(iconResult.getMatchList());
                    Thread.sleep(2000);
                } else {
                    System.out.println("✗ Claude working icon not found");
                }
                
                // Test 3: Show where we're looking for images
                System.out.println("\nTest 3: Showing expected image locations...");
                System.out.println("Prompt image: " + promptState.getClaudePrompt().getName());
                System.out.println("Icon image: " + workingState.getClaudeIcon().getName());
                
                // Highlight some common areas where Claude UI might be
                Region topArea = new Region(0, 0, 800, 200);
                Region centerArea = new Region(500, 300, 800, 400);
                
                System.out.println("Highlighting common UI areas...");
                highlightManager.highlightSearchRegions(java.util.Arrays.asList(topArea, centerArea));
                Thread.sleep(2000);
                
            } catch (Exception e) {
                System.err.println("Direct find test failed: " + e.getMessage());
                e.printStackTrace();
            }
            
            System.out.println("\n=== END DIRECT FIND TEST ===\n");
            
            // Exit after test
            System.exit(0);
        }
    }
}