package com.claude.automator.diagnostics;

import com.claude.automator.states.PromptState;
import com.claude.automator.states.WorkingState;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.internal.region.DynamicRegionResolver;
import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Comprehensive diagnostic for the lastMatchesFound mechanism.
 * This diagnostic verifies every step of the process to identify where it's failing.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LastMatchesFoundDiagnostic {
    
    private final Action action;
    private final PromptState promptState;
    private final WorkingState workingState;
    private final StateMemory stateMemory;
    private final DynamicRegionResolver dynamicRegionResolver;
    
    public void runComprehensiveDiagnostic() {
        log.info("========================================================================");
        log.info("           LASTMATCHESFOUND COMPREHENSIVE DIAGNOSTIC");
        log.info("========================================================================");
        log.info("");
        
        // Step 1: Verify initial state
        verifyInitialState();
        
        // Step 2: Search for ClaudePrompt
        ActionResult promptResult = searchForClaudePrompt();
        
        // Step 3: Verify matches were saved
        verifyMatchesSaved(promptResult);
        
        // Step 4: Verify ClaudeIcon configuration
        verifyClaudeIconConfiguration();
        
        // Step 5: Test DynamicRegionResolver directly
        testDynamicRegionResolver();
        
        // Step 6: Search for ClaudeIcon
        searchForClaudeIcon();
        
        log.info("");
        log.info("========================================================================");
        log.info("                    DIAGNOSTIC COMPLETE");
        log.info("========================================================================");
    }
    
    private void verifyInitialState() {
        log.info("=== STEP 1: VERIFY INITIAL STATE ===");
        
        // Check ClaudePrompt
        StateImage claudePrompt = promptState.getClaudePrompt();
        log.info("ClaudePrompt StateImage:");
        log.info("  - Instance ID: {}", System.identityHashCode(claudePrompt));
        log.info("  - Name: {}", claudePrompt.getName());
        log.info("  - Owner state: {}", claudePrompt.getOwnerStateName());
        log.info("  - Number of patterns: {}", claudePrompt.getPatterns().size());
        log.info("  - Has SearchRegionOnObject: {}", claudePrompt.getSearchRegionOnObject() != null);
        log.info("  - Initial lastMatchesFound size: {}", claudePrompt.getLastMatchesFound().size());
        
        // Check ClaudeIcon
        StateImage claudeIcon = workingState.getClaudeIcon();
        log.info("");
        log.info("ClaudeIcon StateImage:");
        log.info("  - Instance ID: {}", System.identityHashCode(claudeIcon));
        log.info("  - Name: {}", claudeIcon.getName());
        log.info("  - Owner state: {}", claudeIcon.getOwnerStateName());
        log.info("  - Number of patterns: {}", claudeIcon.getPatterns().size());
        
        SearchRegionOnObject searchConfig = claudeIcon.getSearchRegionOnObject();
        if (searchConfig != null) {
            log.info("  - SearchRegionOnObject configured:");
            log.info("    - Target state: {}", searchConfig.getTargetStateName());
            log.info("    - Target object: {}", searchConfig.getTargetObjectName());
            log.info("    - Target type: {}", searchConfig.getTargetType());
            log.info("    - Has adjustments: {}", searchConfig.getAdjustments() != null);
            if (searchConfig.getAdjustments() != null) {
                log.info("    - Adjustment X: {}", searchConfig.getAdjustments().getAddX());
                log.info("    - Adjustment Y: {}", searchConfig.getAdjustments().getAddY());
                log.info("    - Adjustment W: {}", searchConfig.getAdjustments().getAddW());
                log.info("    - Adjustment H: {}", searchConfig.getAdjustments().getAddH());
            }
        } else {
            log.warn("  - NO SearchRegionOnObject configured!");
        }
        
        log.info("");
    }
    
    private ActionResult searchForClaudePrompt() {
        log.info("=== STEP 2: SEARCH FOR CLAUDEPROMPT ===");
        
        StateImage claudePrompt = promptState.getClaudePrompt();
        log.info("Searching for ClaudePrompt (instance: {})...", System.identityHashCode(claudePrompt));
        
        // Create find options
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setSearchDuration(3)
            .build();
        
        // Search for ClaudePrompt
        ActionResult result = action.perform(findOptions, claudePrompt);
        
        log.info("Search result:");
        log.info("  - Success: {}", result.isSuccess());
        log.info("  - Number of matches: {}", result.getMatchList().size());
        
        if (result.isSuccess()) {
            for (int i = 0; i < result.getMatchList().size(); i++) {
                Match match = result.getMatchList().get(i);
                log.info("  - Match[{}]: Region={}, Score={}", 
                    i, match.getRegion(), match.getScore());
                if (match.getStateObjectData() != null) {
                    log.info("    - StateObject name: {}", match.getStateObjectData().getStateObjectName());
                    log.info("    - Owner state: {}", match.getStateObjectData().getOwnerStateName());
                }
            }
        }
        
        log.info("");
        return result;
    }
    
    private void verifyMatchesSaved(ActionResult promptResult) {
        log.info("=== STEP 3: VERIFY MATCHES WERE SAVED TO LASTMATCHESFOUND ===");
        
        StateImage claudePrompt = promptState.getClaudePrompt();
        List<Match> lastMatches = claudePrompt.getLastMatchesFound();
        
        log.info("ClaudePrompt.lastMatchesFound after search:");
        log.info("  - StateImage instance: {}", System.identityHashCode(claudePrompt));
        log.info("  - Size: {}", lastMatches.size());
        
        if (lastMatches.isEmpty()) {
            log.error("  ❌ PROBLEM: lastMatchesFound is EMPTY!");
            log.error("     This means FindPipeline.saveMatchesToStateImages() is not working!");
            
            // Let's manually check if the StateImage from the ObjectCollection is the same instance
            log.info("");
            log.info("Debugging: Let's check StateImage instances...");
            
            // Create a new ObjectCollection to see what instance is used
            ObjectCollection testCollection = new ObjectCollection.Builder()
                .withImages(claudePrompt)
                .build();
            
            for (StateImage img : testCollection.getStateImages()) {
                log.info("  - ObjectCollection StateImage instance: {}", System.identityHashCode(img));
                log.info("    - Name: {}", img.getName());
                log.info("    - Same as promptState.getClaudePrompt(): {}", img == claudePrompt);
            }
        } else {
            log.info("  ✓ lastMatchesFound contains {} matches", lastMatches.size());
            for (int i = 0; i < lastMatches.size(); i++) {
                Match match = lastMatches.get(i);
                log.info("  - LastMatch[{}]: Region={}", i, match.getRegion());
            }
        }
        
        log.info("");
    }
    
    private void verifyClaudeIconConfiguration() {
        log.info("=== STEP 4: VERIFY CLAUDEICON CONFIGURATION ===");
        
        StateImage claudeIcon = workingState.getClaudeIcon();
        log.info("ClaudeIcon search region status:");
        log.info("  - Instance: {}", System.identityHashCode(claudeIcon));
        log.info("  - Has defined search region: {}", claudeIcon.hasDefinedSearchRegion());
        log.info("  - Number of patterns: {}", claudeIcon.getPatterns().size());
        
        if (!claudeIcon.getPatterns().isEmpty()) {
            log.info("  - First pattern search regions:");
            var firstPattern = claudeIcon.getPatterns().get(0);
            log.info("    - Fixed region: {}", firstPattern.getSearchRegions().getFixedRegion());
            log.info("    - All regions: {}", firstPattern.getSearchRegions().getAllRegions());
        }
        
        log.info("");
    }
    
    private void testDynamicRegionResolver() {
        log.info("=== STEP 5: TEST DYNAMICREGIONRESOLVER DIRECTLY ===");
        
        StateImage claudeIcon = workingState.getClaudeIcon();
        StateImage claudePrompt = promptState.getClaudePrompt();
        
        log.info("Testing if DynamicRegionResolver can resolve regions...");
        log.info("  - ClaudePrompt lastMatchesFound size: {}", claudePrompt.getLastMatchesFound().size());
        log.info("  - ClaudeIcon has SearchRegionOnObject: {}", claudeIcon.getSearchRegionOnObject() != null);
        
        // Create a dummy ActionResult and test
        ActionResult dummyResult = new ActionResult();
        List<StateObject> objects = List.of(claudeIcon);
        
        log.info("Calling DynamicRegionResolver.updateSearchRegionsForObjects...");
        dynamicRegionResolver.updateSearchRegionsForObjects(objects, dummyResult);
        
        log.info("After DynamicRegionResolver:");
        log.info("  - ClaudeIcon has defined search region: {}", claudeIcon.hasDefinedSearchRegion());
        if (!claudeIcon.getPatterns().isEmpty()) {
            var firstPattern = claudeIcon.getPatterns().get(0);
            log.info("  - First pattern search regions: {}", firstPattern.getSearchRegions().getAllRegions());
        }
        
        log.info("");
    }
    
    private void searchForClaudeIcon() {
        log.info("=== STEP 6: SEARCH FOR CLAUDEICON ===");
        
        StateImage claudeIcon = workingState.getClaudeIcon();
        log.info("Searching for ClaudeIcon...");
        log.info("  - Instance: {}", System.identityHashCode(claudeIcon));
        log.info("  - Current search regions defined: {}", claudeIcon.hasDefinedSearchRegion());
        
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setSearchDuration(3)
            .build();
        
        ActionResult result = action.perform(findOptions, claudeIcon);
        
        log.info("Search result:");
        log.info("  - Success: {}", result.isSuccess());
        log.info("  - Number of matches: {}", result.getMatchList().size());
        
        if (!result.isSuccess()) {
            log.info("  - ClaudeIcon not found (this is expected if search regions are not being resolved)");
        } else {
            for (Match match : result.getMatchList()) {
                log.info("  - Found at: {}", match.getRegion());
            }
        }
        
        log.info("");
    }
    
    /**
     * Run a quick test to verify the basic flow.
     */
    public void runQuickTest() {
        log.info("=== QUICK LASTMATCHESFOUND TEST ===");
        
        StateImage claudePrompt = promptState.getClaudePrompt();
        StateImage claudeIcon = workingState.getClaudeIcon();
        
        // Search for prompt
        log.info("1. Searching for ClaudePrompt...");
        ActionResult promptResult = action.find(claudePrompt);
        log.info("   - Found: {}, Matches: {}", promptResult.isSuccess(), promptResult.size());
        
        // Check if matches were saved
        log.info("2. Checking lastMatchesFound...");
        log.info("   - ClaudePrompt.lastMatchesFound.size(): {}", claudePrompt.getLastMatchesFound().size());
        
        if (claudePrompt.getLastMatchesFound().isEmpty()) {
            log.error("   ❌ CRITICAL: lastMatchesFound is empty after find!");
            log.error("   This indicates FindPipeline.saveMatchesToStateImages is not being called");
        } else {
            log.info("   ✓ Matches saved to lastMatchesFound");
        }
        
        // Check if regions can be resolved
        log.info("3. Checking if ClaudeIcon search regions can be resolved...");
        log.info("   - ClaudeIcon.hasDefinedSearchRegion(): {}", claudeIcon.hasDefinedSearchRegion());
        
        log.info("");
    }
}