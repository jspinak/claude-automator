package com.claude.automator.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateString;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents the Prompt state where Claude is waiting for input.
 * 
 * This is the initial state of the application.
 * In mock mode, this state has a 100% probability of being found,
 * ensuring reliable transitions.
 */
@State(initial = true)
@Getter
@Slf4j
public class PromptState {
    
    private final StateImage claudePrompt;
    private final StateString continueCommand;
    
    public PromptState() {
        // Create search region for lower left quarter of screen
        // This adapts to any screen resolution
        Region lowerLeftQuarter = Region.builder()
            .withScreenPercentage(0.0, 0.5, 0.5, 0.5)  // x=0%, y=50%, width=50%, height=50%
            .build();
        
        // Debug: verify the region is calculated correctly
        System.out.println("[PromptState] Screen resolution detected for region calculation");
        System.out.println("[PromptState] Lower left quarter region: " + lowerLeftQuarter);
        System.out.println("[PromptState] Region details - x:" + lowerLeftQuarter.x() + 
                          " y:" + lowerLeftQuarter.y() + 
                          " w:" + lowerLeftQuarter.w() + 
                          " h:" + lowerLeftQuarter.h());
        
        // Initialize the claude prompt image with search region and ActionHistory
        // The ActionHistory is required for mock mode finds to work
        // Now including both original and 80% scaled patterns
        claudePrompt = new StateImage.Builder()
            .addPatterns(
                // Original patterns
                "prompt/claude-prompt-1",
                "prompt/claude-prompt-2",
                "prompt/claude-prompt-3",
                // 80% scaled patterns (pre-scaled to match 125% Windows scaling)
                "prompt/claude-prompt-1-80",
                "prompt/claude-prompt-2-80",
                "prompt/claude-prompt-3-80"
            )
            .setName("ClaudePrompt")
            .setSearchRegionForAllPatterns(lowerLeftQuarter)
            .setFixedForAllPatterns(true)  // Mark all patterns as fixed
            .build();
        
        // Debug: verify patterns have search regions and ActionHistory
        System.out.println("[PromptState] Created StateImage with " + claudePrompt.getPatterns().size() + " patterns");
        for (io.github.jspinak.brobot.model.element.Pattern p : claudePrompt.getPatterns()) {
            System.out.println("[PromptState] Pattern '" + p.getName() + "':");
            System.out.println("  - Fixed: " + p.isFixed());
            System.out.println("  - Search regions (getRegions()): " + p.getSearchRegions().getRegions());
            System.out.println("  - Search regions (getRegions(fixed)): " + p.getSearchRegions().getRegions(p.isFixed()));
            System.out.println("  - getRegionsForSearch(): " + p.getRegionsForSearch());
            System.out.println("  - Has ActionHistory: " + (p.getMatchHistory() != null && !p.getMatchHistory().getSnapshots().isEmpty()));
        }
        // Create the continue command as a string
        continueCommand = new StateString.Builder()
            .setName("ContinueCommand")
            .setString("continue\n")
            .build();
    }
    
}