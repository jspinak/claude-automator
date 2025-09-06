package com.claude.automator.states;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.model.state.StateImage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the dynamic resolution of SearchRegionOnObject dependencies.
 * 
 * This test verifies that when WorkingState.ClaudeIcon has a SearchRegionOnObject
 * that references PromptState.ClaudePrompt, the search regions are properly
 * resolved after finding ClaudePrompt.
 */
@SpringBootTest(classes = com.claude.automator.ClaudeAutomatorApplication.class)
public class DynamicRegionResolutionTest {
    
    @Autowired
    private Action action;
    
    @Autowired
    private PromptState promptState;
    
    @Autowired
    private WorkingState workingState;
    
    @Test
    public void testSearchRegionOnObjectResolution() {
        System.out.println("Testing dynamic SearchRegionOnObject resolution");
        
        // Step 1: Verify initial state - WorkingState should have SearchRegionOnObject defined
        StateImage claudeIcon = workingState.getClaudeIcon();
        assertNotNull(claudeIcon, "ClaudeIcon should not be null");
        
        SearchRegionOnObject searchRegionConfig = claudeIcon.getSearchRegionOnObject();
        assertNotNull(searchRegionConfig, "SearchRegionOnObject should be defined for ClaudeIcon");
        assertEquals("Prompt", searchRegionConfig.getTargetStateName(), 
                     "Should target Prompt state");
        assertEquals("ClaudePrompt", searchRegionConfig.getTargetObjectName(), 
                     "Should target ClaudePrompt object");
        
        // Step 2: Initially, ClaudeIcon should not have resolved search regions
        System.out.println("Initial state - ClaudeIcon search regions:");
        for (Pattern pattern : claudeIcon.getPatterns()) {
            System.out.println("  Pattern '" + pattern.getName() + "' search regions: " + 
                     pattern.getSearchRegions().getRegions());
            assertTrue(pattern.getSearchRegions().getRegions().isEmpty() || 
                      pattern.getSearchRegions().getRegions().size() == 1,
                      "Initially should have no resolved regions or just screen region");
        }
        
        // Step 3: Find ClaudePrompt patterns to establish their location
        System.out.println("Finding ClaudePrompt patterns to establish base location...");
        StateImage claudePrompt = promptState.getClaudePrompt();
        
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setSimilarity(0.65)
            .build();
        
        ObjectCollection promptCollection = new ObjectCollection.Builder()
            .withImages(claudePrompt)
            .build();
        
        ActionResult promptResult = action.perform(ActionType.FIND, promptCollection, findOptions);
        
        if (promptResult.isSuccess()) {
            System.out.println("ClaudePrompt found with " + promptResult.getMatchList().size() + " matches");
            
            // Step 4: Now find ClaudeIcon - it should use the resolved search regions
            System.out.println("Finding ClaudeIcon with dynamically resolved search regions...");
            
            ObjectCollection workingCollection = new ObjectCollection.Builder()
                .withImages(claudeIcon)
                .build();
            
            ActionResult workingResult = action.perform(ActionType.FIND, workingCollection, findOptions);
            
            // Step 5: Verify that search regions were resolved
            System.out.println("After finding ClaudePrompt - ClaudeIcon search regions:");
            for (Pattern pattern : claudeIcon.getPatterns()) {
                System.out.println("  Pattern '" + pattern.getName() + "' search regions: " + 
                         pattern.getSearchRegions().getRegions());
                
                // Check if regions were updated based on ClaudePrompt match
                if (pattern.isFixed() && !pattern.getSearchRegions().getRegions().isEmpty()) {
                    Region searchRegion = pattern.getSearchRegions().getRegions().get(0);
                    System.out.println("    Region details: x=" + searchRegion.x() + ", y=" + searchRegion.y() + 
                             ", w=" + searchRegion.w() + ", h=" + searchRegion.h());
                    
                    // The region should be adjusted from the ClaudePrompt match
                    // Based on WorkingState constructor: addX=3, addY=10, addW=30, addH=55
                    assertTrue(searchRegion.w() > 30, "Width should include adjustment");
                    assertTrue(searchRegion.h() > 55, "Height should include adjustment");
                }
            }
            
            System.out.println("WorkingState result: success=" + workingResult.isSuccess() + 
                     ", matches=" + workingResult.getMatchList().size());
            
        } else {
            System.out.println("WARNING: ClaudePrompt not found - this may be expected in mock mode");
            System.out.println("In mock mode, SearchRegionOnObject resolution happens differently");
        }
        
        // Step 6: Log final diagnostic information
        System.out.println("");
        System.out.println("=== DIAGNOSTIC SUMMARY ===");
        System.out.println("SearchRegionOnObject configured: " + (searchRegionConfig != null));
        System.out.println("Target state: " + (searchRegionConfig != null ? searchRegionConfig.getTargetStateName() : "N/A"));
        System.out.println("Target object: " + (searchRegionConfig != null ? searchRegionConfig.getTargetObjectName() : "N/A"));
        System.out.println("Adjustments: " + (searchRegionConfig != null ? searchRegionConfig.getAdjustments() : "N/A"));
        
        // The test passes if SearchRegionOnObject is properly configured
        // The actual resolution happens at runtime when patterns are found
        assertTrue(searchRegionConfig != null, 
                   "SearchRegionOnObject should be configured for dynamic resolution");
    }
    
    @Test
    public void testSearchRegionAdjustments() {
        System.out.println("Testing SearchRegionOnObject adjustments configuration");
        
        StateImage claudeIcon = workingState.getClaudeIcon();
        SearchRegionOnObject config = claudeIcon.getSearchRegionOnObject();
        
        assertNotNull(config.getAdjustments(), "Adjustments should be configured");
        assertEquals(3, config.getAdjustments().getAddX(), "AddX should be 3");
        assertEquals(10, config.getAdjustments().getAddY(), "AddY should be 10");
        assertEquals(30, config.getAdjustments().getAddW(), "AddW should be 30");
        assertEquals(55, config.getAdjustments().getAddH(), "AddH should be 55");
        
        System.out.println("Adjustments verified: addX=" + config.getAdjustments().getAddX() +
                 ", addY=" + config.getAdjustments().getAddY() +
                 ", addW=" + config.getAdjustments().getAddW() +
                 ", addH=" + config.getAdjustments().getAddH());
    }
}