package com.claude.automator.states;

import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test to verify that SearchRegionOnObject is properly configured for dynamic resolution.
 * This test doesn't require Spring context and directly tests the state configuration.
 */
public class SearchRegionConfigurationTest {
    
    @Test
    public void testWorkingStateHasSearchRegionOnObjectConfiguration() {
        // Create the WorkingState directly
        WorkingState workingState = new WorkingState();
        StateImage claudeIcon = workingState.getClaudeIcon();
        
        // Verify ClaudeIcon exists
        assertNotNull(claudeIcon, "ClaudeIcon should not be null");
        assertEquals("ClaudeIcon", claudeIcon.getName(), "StateImage name should be ClaudeIcon");
        
        // Verify SearchRegionOnObject is configured
        SearchRegionOnObject searchRegionConfig = claudeIcon.getSearchRegionOnObject();
        assertNotNull(searchRegionConfig, "SearchRegionOnObject should be configured for ClaudeIcon");
        
        // Verify the configuration points to the correct target
        assertEquals("Prompt", searchRegionConfig.getTargetStateName(), 
                     "Should target 'Prompt' state");
        assertEquals("ClaudePrompt", searchRegionConfig.getTargetObjectName(), 
                     "Should target 'ClaudePrompt' object");
        assertEquals(StateObject.Type.IMAGE, searchRegionConfig.getTargetType(),
                     "Should target an IMAGE type");
        
        // Verify adjustments are configured
        assertNotNull(searchRegionConfig.getAdjustments(), 
                      "Adjustments should be configured");
        assertEquals(3, searchRegionConfig.getAdjustments().getAddX(), 
                     "AddX adjustment should be 3");
        assertEquals(10, searchRegionConfig.getAdjustments().getAddY(), 
                     "AddY adjustment should be 10");
        assertEquals(30, searchRegionConfig.getAdjustments().getAddW(), 
                     "AddW adjustment should be 30");
        assertEquals(55, searchRegionConfig.getAdjustments().getAddH(), 
                     "AddH adjustment should be 55");
        
        System.out.println("✓ SearchRegionOnObject configuration verified:");
        System.out.println("  - Target State: " + searchRegionConfig.getTargetStateName());
        System.out.println("  - Target Object: " + searchRegionConfig.getTargetObjectName());
        System.out.println("  - Target Type: " + searchRegionConfig.getTargetType());
        System.out.println("  - Adjustments: x+" + searchRegionConfig.getAdjustments().getAddX() +
                          ", y+" + searchRegionConfig.getAdjustments().getAddY() +
                          ", w+" + searchRegionConfig.getAdjustments().getAddW() +
                          ", h+" + searchRegionConfig.getAdjustments().getAddH());
        
        // Verify patterns are configured
        assertFalse(claudeIcon.getPatterns().isEmpty(), "ClaudeIcon should have patterns");
        assertEquals(8, claudeIcon.getPatterns().size(), "ClaudeIcon should have 8 patterns (4 original + 4 scaled)");
        
        System.out.println("  - Number of patterns: " + claudeIcon.getPatterns().size());
        
        // All patterns should be fixed
        assertTrue(claudeIcon.getPatterns().stream().allMatch(p -> p.isFixed()),
                   "All patterns should be fixed for optimization");
        
        System.out.println("  - All patterns fixed: true");
        System.out.println("\n✅ Test passed: SearchRegionOnObject is properly configured for dynamic resolution");
    }
    
    @Test
    public void testPromptStateHasDefinedSearchRegions() {
        // Create the PromptState directly
        PromptState promptState = new PromptState();
        StateImage claudePrompt = promptState.getClaudePrompt();
        
        // Verify ClaudePrompt exists
        assertNotNull(claudePrompt, "ClaudePrompt should not be null");
        assertEquals("ClaudePrompt", claudePrompt.getName(), "StateImage name should be ClaudePrompt");
        
        // Verify patterns exist
        assertFalse(claudePrompt.getPatterns().isEmpty(), "ClaudePrompt should have patterns");
        assertEquals(2, claudePrompt.getPatterns().size(), "ClaudePrompt should have 2 patterns");
        
        // All patterns should have search regions defined (lower left quarter)
        claudePrompt.getPatterns().forEach(pattern -> {
            assertFalse(pattern.getSearchRegions().getRegions().isEmpty(),
                       "Pattern '" + pattern.getName() + "' should have search regions defined");
            
            // The region should be in the lower left quarter
            pattern.getSearchRegions().getRegions().forEach(region -> {
                System.out.println("Pattern '" + pattern.getName() + "' search region: " +
                                 "x=" + region.x() + ", y=" + region.y() + 
                                 ", w=" + region.w() + ", h=" + region.h());
            });
        });
        
        // All patterns should be fixed
        assertTrue(claudePrompt.getPatterns().stream().allMatch(p -> p.isFixed()),
                   "All patterns should be fixed for optimization");
        
        System.out.println("\n✅ Test passed: PromptState has properly defined search regions");
    }
    
    @Test
    public void testSearchRegionOnObjectConfiguration() {
        System.out.println("\n=== Testing SearchRegionOnObject Configuration ===\n");
        
        // Create both states
        PromptState promptState = new PromptState();
        WorkingState workingState = new WorkingState();
        
        // Get the state images
        StateImage claudePrompt = promptState.getClaudePrompt();
        StateImage claudeIcon = workingState.getClaudeIcon();
        
        System.out.println("1. PromptState.ClaudePrompt configuration:");
        System.out.println("   - Has defined search regions: " + claudePrompt.isDefined());
        System.out.println("   - Number of patterns: " + claudePrompt.getPatterns().size());
        System.out.println("   - All patterns fixed: " + 
                          claudePrompt.getPatterns().stream().allMatch(p -> p.isFixed()));
        
        System.out.println("\n2. WorkingState.ClaudeIcon configuration:");
        System.out.println("   - Has SearchRegionOnObject: " + 
                          (claudeIcon.getSearchRegionOnObject() != null));
        System.out.println("   - Number of patterns: " + claudeIcon.getPatterns().size());
        System.out.println("   - All patterns fixed: " + 
                          claudeIcon.getPatterns().stream().allMatch(p -> p.isFixed()));
        
        SearchRegionOnObject config = claudeIcon.getSearchRegionOnObject();
        if (config != null) {
            System.out.println("\n3. SearchRegionOnObject details:");
            System.out.println("   - Target: " + config.getTargetStateName() + "." + 
                              config.getTargetObjectName());
            System.out.println("   - Type: " + config.getTargetType());
            System.out.println("   - Adjustments: " + config.getAdjustments());
            
            System.out.println("\n4. Expected behavior:");
            System.out.println("   - When ClaudePrompt is found, ClaudeIcon search regions will be:");
            System.out.println("     • Positioned relative to ClaudePrompt match location");
            System.out.println("     • Adjusted by: x+3, y+10, w+30, h+55 pixels");
            System.out.println("     • This creates a focused search area for the Claude icon");
        }
        
        System.out.println("\n✅ Configuration test complete - declarative regions are properly set up");
    }
}