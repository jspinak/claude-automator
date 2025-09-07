package com.claude.automator.diagnostic;

import com.claude.automator.states.PromptState;
import com.claude.automator.states.WorkingState;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateObjectMetadata;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import com.claude.automator.TestBase;
import com.claude.automator.config.TestBeanConfiguration;

import java.util.List;

/**
 * Comprehensive diagnostic test for SearchRegionOnObject resolution.
 * This test adds extensive logging to track exactly why the declarative
 * search region definition is not working.
 */
@Import(TestBeanConfiguration.class)
public class SearchRegionResolutionDiagnostic extends TestBase {
    
    private static final Logger log = LoggerFactory.getLogger(SearchRegionResolutionDiagnostic.class);
    
    @Autowired
    private Action action;
    
    @Autowired
    private PromptState promptState;
    
    @Autowired
    private WorkingState workingState;
    
    @Test
    public void diagnoseSearchRegionResolution() {
        log.info("\n╔════════════════════════════════════════════════════════════════════════════╗");
        log.info("║          SEARCH REGION RESOLUTION DIAGNOSTIC                              ║");
        log.info("╚════════════════════════════════════════════════════════════════════════════╝\n");
        
        // Step 1: Verify initial configuration
        verifyInitialConfiguration();
        
        // Step 2: Search for ClaudePrompt and track its matches
        searchForClaudePrompt();
        
        // Step 3: Check if ClaudePrompt lastMatchesFound is populated
        verifyClaudePromptMatches();
        
        // Step 4: Search for ClaudeIcon and track region resolution
        searchForClaudeIcon();
        
        // Step 5: Analyze results
        analyzeResults();
    }
    
    private void verifyInitialConfiguration() {
        log.info("=== STEP 1: INITIAL CONFIGURATION ===");
        
        StateImage claudePrompt = promptState.getClaudePrompt();
        StateImage claudeIcon = workingState.getClaudeIcon();
        
        log.info("ClaudePrompt configuration:");
        log.info("  - Name: {}", claudePrompt.getName());
        log.info("  - Owner state: {}", claudePrompt.getOwnerStateName());
        log.info("  - Instance ID: {}", System.identityHashCode(claudePrompt));
        log.info("  - Has defined search region: {}", claudePrompt.hasDefinedSearchRegion());
        log.info("  - Number of patterns: {}", claudePrompt.getPatterns().size());
        log.info("  - Initial lastMatchesFound size: {}", claudePrompt.getLastMatchesFound().size());
        
        log.info("\nClaudeIcon configuration:");
        log.info("  - Name: {}", claudeIcon.getName());
        log.info("  - Owner state: {}", claudeIcon.getOwnerStateName());
        log.info("  - Instance ID: {}", System.identityHashCode(claudeIcon));
        log.info("  - Has defined search region: {}", claudeIcon.hasDefinedSearchRegion());
        log.info("  - Number of patterns: {}", claudeIcon.getPatterns().size());
        
        SearchRegionOnObject searchRegionConfig = claudeIcon.getSearchRegionOnObject();
        if (searchRegionConfig != null) {
            log.info("  - SearchRegionOnObject configured:");
            log.info("    - Target state: {}", searchRegionConfig.getTargetStateName());
            log.info("    - Target object: {}", searchRegionConfig.getTargetObjectName());
            log.info("    - Target type: {}", searchRegionConfig.getTargetType());
            log.info("    - Adjustments: x+{}, y+{}, w+{}, h+{}", 
                searchRegionConfig.getAdjustments().getAddX(),
                searchRegionConfig.getAdjustments().getAddY(),
                searchRegionConfig.getAdjustments().getAddW(),
                searchRegionConfig.getAdjustments().getAddH());
        } else {
            log.warn("  - NO SearchRegionOnObject configured!");
        }
        
        log.info("\n");
    }
    
    private void searchForClaudePrompt() {
        log.info("=== STEP 2: SEARCHING FOR CLAUDEPROMPT ===");
        
        StateImage claudePrompt = promptState.getClaudePrompt();
        
        // Log the exact object we're searching for
        log.info("Searching for ClaudePrompt:");
        log.info("  - StateImage instance: {}", System.identityHashCode(claudePrompt));
        log.info("  - Name: {}", claudePrompt.getName());
        log.info("  - lastMatchesFound BEFORE search: {}", claudePrompt.getLastMatchesFound().size());
        
        // Create find options to ensure we save matches
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setSimilarity(0.65)
            .build();
        
        ObjectCollection collection = new ObjectCollection.Builder()
            .withImages(claudePrompt)
            .build();
        
        log.info("Executing find operation for ClaudePrompt...");
        ActionResult result = action.perform(ActionType.FIND, findOptions, collection);
        
        log.info("Find operation completed:");
        log.info("  - Success: {}", result.isSuccess());
        log.info("  - Matches found: {}", result.getMatchList().size());
        
        // Log each match
        for (int i = 0; i < result.getMatchList().size(); i++) {
            Match match = result.getMatchList().get(i);
            log.info("  - Match[{}]:", i);
            log.info("    - Region: {}", match.getRegion());
            log.info("    - Score: {}", match.getScore());
            if (match.getStateObjectData() != null) {
                log.info("    - StateObjectData name: {}", match.getStateObjectData().getStateObjectName());
                log.info("    - StateObjectData owner state: {}", match.getStateObjectData().getOwnerStateName());
            }
        }
        
        // CRITICAL: Check if matches were saved to the StateImage
        log.info("\nChecking if matches were saved to ClaudePrompt:");
        log.info("  - lastMatchesFound AFTER search: {}", claudePrompt.getLastMatchesFound().size());
        if (!claudePrompt.getLastMatchesFound().isEmpty()) {
            log.info("  - First match region: {}", claudePrompt.getLastMatchesFound().get(0).getRegion());
        }
        
        log.info("\n");
    }
    
    private void verifyClaudePromptMatches() {
        log.info("=== STEP 3: VERIFYING CLAUDEPROMPT MATCHES ===");
        
        StateImage claudePrompt = promptState.getClaudePrompt();
        List<Match> lastMatches = claudePrompt.getLastMatchesFound();
        
        log.info("ClaudePrompt match verification:");
        log.info("  - StateImage instance: {}", System.identityHashCode(claudePrompt));
        log.info("  - Name: {}", claudePrompt.getName());
        log.info("  - Owner state: {}", claudePrompt.getOwnerStateName());
        log.info("  - lastMatchesFound size: {}", lastMatches.size());
        
        if (lastMatches.isEmpty()) {
            log.error("  ❌ NO MATCHES STORED IN CLAUDEPROMPT!");
            log.error("     This is why SearchRegionOnObject resolution fails!");
            log.error("     The matches are not being saved to the StateImage.");
        } else {
            log.info("  ✓ Matches stored in ClaudePrompt:");
            for (int i = 0; i < lastMatches.size(); i++) {
                Match match = lastMatches.get(i);
                log.info("    - Match[{}]: Region={}, Score={}", 
                    i, match.getRegion(), match.getScore());
            }
        }
        
        log.info("\n");
    }
    
    private void searchForClaudeIcon() {
        log.info("=== STEP 4: SEARCHING FOR CLAUDEICON ===");
        
        StateImage claudeIcon = workingState.getClaudeIcon();
        
        log.info("Pre-search ClaudeIcon state:");
        log.info("  - StateImage instance: {}", System.identityHashCode(claudeIcon));
        log.info("  - Name: {}", claudeIcon.getName());
        log.info("  - Has defined search region: {}", claudeIcon.hasDefinedSearchRegion());
        
        // Check current search regions
        if (!claudeIcon.getPatterns().isEmpty()) {
            claudeIcon.getPatterns().forEach(pattern -> {
                log.info("  - Pattern '{}' search regions: {}", 
                    pattern.getName(), pattern.getSearchRegions().getRegions());
            });
        }
        
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setSimilarity(0.65)
            .build();
        
        ObjectCollection collection = new ObjectCollection.Builder()
            .withImages(claudeIcon)
            .build();
        
        log.info("\nExecuting find operation for ClaudeIcon...");
        log.info("WATCH FOR: DynamicRegionResolver logs should appear here!");
        
        ActionResult result = action.perform(ActionType.FIND, findOptions, collection);
        
        log.info("\nFind operation completed:");
        log.info("  - Success: {}", result.isSuccess());
        log.info("  - Matches found: {}", result.getMatchList().size());
        
        // Check if search regions were updated
        log.info("\nPost-search ClaudeIcon state:");
        log.info("  - Has defined search region: {}", claudeIcon.hasDefinedSearchRegion());
        
        if (!claudeIcon.getPatterns().isEmpty()) {
            claudeIcon.getPatterns().forEach(pattern -> {
                log.info("  - Pattern '{}' search regions after search: {}", 
                    pattern.getName(), pattern.getSearchRegions().getRegions());
            });
        }
        
        log.info("\n");
    }
    
    private void analyzeResults() {
        log.info("=== STEP 5: ANALYSIS ===");
        
        StateImage claudePrompt = promptState.getClaudePrompt();
        StateImage claudeIcon = workingState.getClaudeIcon();
        
        log.info("Final state analysis:");
        log.info("  - ClaudePrompt lastMatchesFound: {}", claudePrompt.getLastMatchesFound().size());
        log.info("  - ClaudeIcon has defined regions: {}", claudeIcon.hasDefinedSearchRegion());
        
        if (claudePrompt.getLastMatchesFound().isEmpty()) {
            log.error("\n❌ ROOT CAUSE: ClaudePrompt matches are not being saved!");
            log.error("   The Find operation finds the patterns but doesn't store them");
            log.error("   in the StateImage's lastMatchesFound list.");
            log.error("   This prevents DynamicRegionResolver from resolving regions.");
        } else if (!claudeIcon.hasDefinedSearchRegion()) {
            log.error("\n❌ ISSUE: DynamicRegionResolver failed to set regions!");
            log.error("   Even though ClaudePrompt has matches, the regions were not");
            log.error("   applied to ClaudeIcon. Check DynamicRegionResolver logs.");
        } else {
            log.info("\n✓ SUCCESS: SearchRegionOnObject resolution worked!");
            log.info("  ClaudeIcon search regions were dynamically set based on ClaudePrompt.");
        }
        
        log.info("\n╚════════════════════════════════════════════════════════════════════════════╝\n");
    }
}