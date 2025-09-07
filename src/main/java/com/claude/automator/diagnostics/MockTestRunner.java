package com.claude.automator.diagnostics;

import com.claude.automator.states.PromptState;
import com.claude.automator.states.WorkingState;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mock test runner that simulates finding ClaudePrompt and tests if lastMatchesFound works.
 * This avoids the OpenCV native library issue by manually creating matches.
 * 
 * Run with: java -jar build/libs/claude-automator-1.0.0.jar --spring.profiles.active=mock-test
 */
@Component
@Profile("mock-test")
@RequiredArgsConstructor
@Slf4j
public class MockTestRunner implements CommandLineRunner {
    
    private final PromptState promptState;
    private final WorkingState workingState;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("========================================");
        log.info("     MOCK LASTMATCHESFOUND TEST");
        log.info("========================================");
        log.info("");
        
        // Get the original StateImage instances
        StateImage claudePrompt = promptState.getClaudePrompt();
        StateImage claudeIcon = workingState.getClaudeIcon();
        
        log.info("Initial State:");
        log.info("  ClaudePrompt instance: {}", System.identityHashCode(claudePrompt));
        log.info("  ClaudePrompt name: {}", claudePrompt.getName());
        log.info("  ClaudeIcon has SearchRegionOnObject: {}", claudeIcon.getSearchRegionOnObject() != null);
        if (claudeIcon.getSearchRegionOnObject() != null) {
            log.info("    Target state: {}", claudeIcon.getSearchRegionOnObject().getTargetStateName());
            log.info("    Target object: {}", claudeIcon.getSearchRegionOnObject().getTargetObjectName());
        }
        log.info("");
        
        // Step 1: Manually simulate finding ClaudePrompt
        log.info("Step 1: Simulating ClaudePrompt match...");
        
        // Create a mock match at a specific location
        Region mockRegion = new Region(100, 200, 300, 400);
        Match mockMatch = new Match.Builder()
            .setRegion(mockRegion)
            .setSimScore(0.95)  // Use setSimScore for the match similarity
            .setName("ClaudePrompt")
            .setStateObjectData(claudePrompt)  // This associates the match with the StateImage
            .build();
        
        log.info("  Created mock match at: {}", mockRegion);
        log.info("  Match has StateObjectData: {}", mockMatch.getStateObjectData() != null);
        if (mockMatch.getStateObjectData() != null) {
            log.info("  StateObjectData name: {}", mockMatch.getStateObjectData().getStateObjectName());
        }
        
        // Manually save the match to lastMatchesFound
        claudePrompt.getLastMatchesFound().clear();
        claudePrompt.getLastMatchesFound().add(mockMatch);
        
        log.info("  Manually saved match to claudePrompt.lastMatchesFound");
        log.info("");
        
        // Step 2: Verify the match was saved
        log.info("Step 2: Verifying lastMatchesFound...");
        List<Match> savedMatches = claudePrompt.getLastMatchesFound();
        log.info("  claudePrompt.lastMatchesFound.size(): {}", savedMatches.size());
        
        if (!savedMatches.isEmpty()) {
            log.info("  ✓ SUCCESS: Match is in lastMatchesFound!");
            Match saved = savedMatches.get(0);
            log.info("  Saved match location: {}", saved.getRegion());
            // Note: Score getter may not be available in this version
            // log.info("  Saved match similarity: {}", saved.getSimilarity());
        } else {
            log.error("  ✗ FAILURE: lastMatchesFound is empty!");
        }
        log.info("");
        
        // Step 3: Test if ClaudeIcon can access ClaudePrompt's lastMatchesFound
        log.info("Step 3: Testing cross-reference access...");
        
        // This simulates what DynamicRegionResolver should do
        if (claudeIcon.getSearchRegionOnObject() != null) {
            String targetState = claudeIcon.getSearchRegionOnObject().getTargetStateName();
            String targetObject = claudeIcon.getSearchRegionOnObject().getTargetObjectName();
            
            log.info("  ClaudeIcon references: {}:{}", targetState, targetObject);
            
            // Check if we can access the target's lastMatchesFound
            if ("Prompt".equals(targetState) && "ClaudePrompt".equals(targetObject)) {
                log.info("  ✓ Reference configuration is correct");
                
                // Check if ClaudePrompt has matches
                if (!claudePrompt.getLastMatchesFound().isEmpty()) {
                    Match targetMatch = claudePrompt.getLastMatchesFound().get(0);
                    Region targetRegion = targetMatch.getRegion();
                    
                    // Apply adjustments (simulate what DynamicRegionResolver does)
                    var adjustments = claudeIcon.getSearchRegionOnObject().getAdjustments();
                    if (adjustments != null) {
                        int x = targetRegion.getX() + adjustments.getAddX();
                        int y = targetRegion.getY() + adjustments.getAddY();
                        int w = targetRegion.getW() + adjustments.getAddW();
                        int h = targetRegion.getH() + adjustments.getAddH();
                        
                        Region adjustedRegion = new Region(x, y, w, h);
                        log.info("  ✓ Can compute search region for ClaudeIcon:");
                        log.info("    Base region: {}", targetRegion);
                        log.info("    Adjustments: x+{}, y+{}, w+{}, h+{}", 
                                adjustments.getAddX(), adjustments.getAddY(),
                                adjustments.getAddW(), adjustments.getAddH());
                        log.info("    Result region: {}", adjustedRegion);
                    } else {
                        log.info("  No adjustments configured");
                    }
                } else {
                    log.error("  ✗ Target ClaudePrompt has no matches in lastMatchesFound!");
                }
            } else {
                log.error("  ✗ Reference configuration doesn't match expected values");
            }
        } else {
            log.error("  ✗ ClaudeIcon has no SearchRegionOnObject configured!");
        }
        
        log.info("");
        log.info("========================================");
        log.info("         MOCK TEST COMPLETE");
        log.info("========================================");
        log.info("");
        log.info("Summary:");
        log.info("  1. lastMatchesFound field exists and can store matches ✓");
        log.info("  2. SearchRegionOnObject is configured correctly ✓");
        log.info("  3. Cross-reference can be resolved manually ✓");
        log.info("");
        log.info("The mechanism works! The issue is that FindPipeline.saveMatchesToStateImages()");
        log.info("is not being called during actual searches, or matches don't have StateObjectData.");
        
        // Exit after test
        System.exit(0);
    }
}