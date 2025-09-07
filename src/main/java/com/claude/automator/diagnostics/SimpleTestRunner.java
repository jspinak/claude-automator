package com.claude.automator.diagnostics;

import com.claude.automator.states.PromptState;
import com.claude.automator.states.WorkingState;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Simple test runner that tests the lastMatchesFound mechanism without diagnostics.
 * This bypasses the pattern splitting issue in the diagnostic code.
 * 
 * Run with: java -jar build/libs/claude-automator-1.0.0.jar --spring.profiles.active=simple-test
 */
@Component
@Profile("simple-test")
@RequiredArgsConstructor
@Slf4j
public class SimpleTestRunner implements CommandLineRunner {
    
    private final Action action;
    private final PromptState promptState;
    private final WorkingState workingState;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("========================================");
        log.info("       SIMPLE LASTMATCHESFOUND TEST");
        log.info("========================================");
        log.info("");
        
        // Get the original StateImage instances (not split)
        StateImage claudePrompt = promptState.getClaudePrompt();
        StateImage claudeIcon = workingState.getClaudeIcon();
        
        log.info("Initial State:");
        log.info("  ClaudePrompt instance: {}", System.identityHashCode(claudePrompt));
        log.info("  ClaudePrompt name: {}", claudePrompt.getName());
        log.info("  ClaudePrompt patterns: {}", claudePrompt.getPatterns().size());
        log.info("  ClaudeIcon configured: {}", claudeIcon.getSearchRegionOnObject() != null);
        log.info("");
        
        // Step 1: Search for ClaudePrompt
        log.info("Step 1: Searching for ClaudePrompt...");
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setSearchDuration(3)
            .build();
        
        ActionResult result = action.perform(findOptions, claudePrompt);
        
        log.info("  Found: {}", result.isSuccess());
        log.info("  Matches: {}", result.size());
        if (result.isSuccess() && !result.getMatchList().isEmpty()) {
            Match firstMatch = result.getMatchList().get(0);
            log.info("  First match location: {}", firstMatch.getRegion());
            log.info("  Match has StateObjectData: {}", 
                    firstMatch.getStateObjectData() != null);
            if (firstMatch.getStateObjectData() != null) {
                log.info("  StateObjectData name: {}", 
                        firstMatch.getStateObjectData().getStateObjectName());
            }
        }
        log.info("");
        
        // Step 2: Check if matches were saved to lastMatchesFound
        log.info("Step 2: Checking lastMatchesFound...");
        log.info("  ClaudePrompt.lastMatchesFound.size(): {}", 
                claudePrompt.getLastMatchesFound().size());
        
        if (!claudePrompt.getLastMatchesFound().isEmpty()) {
            log.info("  ✓ SUCCESS: Matches were saved to lastMatchesFound!");
            Match savedMatch = claudePrompt.getLastMatchesFound().get(0);
            log.info("  Saved match location: {}", savedMatch.getRegion());
        } else {
            log.error("  ✗ FAILURE: lastMatchesFound is empty!");
            log.error("  This means FindPipeline.saveMatchesToStateImages is not working");
        }
        log.info("");
        
        // Step 3: Test if ClaudeIcon search regions are resolved
        log.info("Step 3: Testing ClaudeIcon search region resolution...");
        log.info("  ClaudeIcon.hasDefinedSearchRegion() BEFORE: {}", 
                claudeIcon.hasDefinedSearchRegion());
        
        // Search for ClaudeIcon - this should trigger DynamicRegionResolver
        ActionResult iconResult = action.perform(findOptions, claudeIcon);
        
        log.info("  ClaudeIcon.hasDefinedSearchRegion() AFTER: {}", 
                claudeIcon.hasDefinedSearchRegion());
        
        if (!claudeIcon.getPatterns().isEmpty() && 
            !claudeIcon.getPatterns().get(0).getSearchRegions().getAllRegions().isEmpty()) {
            log.info("  ✓ Search regions were set: {}", 
                    claudeIcon.getPatterns().get(0).getSearchRegions().getAllRegions());
        } else {
            log.warn("  ✗ No search regions set for ClaudeIcon");
        }
        
        log.info("  ClaudeIcon found: {}", iconResult.isSuccess());
        if (iconResult.isSuccess()) {
            log.info("  ClaudeIcon location: {}", 
                    iconResult.getMatchList().get(0).getRegion());
        }
        
        log.info("");
        log.info("========================================");
        log.info("         TEST COMPLETE");
        log.info("========================================");
        
        // Exit after test
        System.exit(0);
    }
}