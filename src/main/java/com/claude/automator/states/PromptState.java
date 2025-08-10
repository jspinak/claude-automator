package com.claude.automator.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateString;
import lombok.Getter;

/**
 * Represents the Prompt state where Claude is waiting for input.
 */
@State(initial = true)
@Getter
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
        
        // Initialize the claude prompt image with search region
        // The normal search region defines a limited area where patterns will be searched for.
        // When the pattern is found and marked as fixed, it will set the fixed region.
        // Until then, it continues to search within the defined search regions.
        claudePrompt = new StateImage.Builder()
            .addPatterns("prompt/claude-prompt-1","prompt/claude-prompt-2","prompt/claude-prompt-3")
            .setName("ClaudePrompt")
            .setSearchRegionForAllPatterns(lowerLeftQuarter)
            .setFixedForAllPatterns(true)  // Mark all patterns as fixed
            .build();
        
        // Debug: verify patterns have search regions
        System.out.println("[PromptState] Created StateImage with " + claudePrompt.getPatterns().size() + " patterns");
        for (io.github.jspinak.brobot.model.element.Pattern p : claudePrompt.getPatterns()) {
            System.out.println("[PromptState] Pattern '" + p.getName() + "':");
            System.out.println("  - Fixed: " + p.isFixed());
            System.out.println("  - Search regions: " + p.getSearchRegions().getRegions());
            System.out.println("  - getRegions(fixed=true): " + p.getRegions());
            System.out.println("  - getRegionsForSearch(): " + p.getRegionsForSearch());
        }
        // Create the continue command as a string
        continueCommand = new StateString.Builder()
            .setName("ContinueCommand")
            .setString("continue\n")
            .build();
    }
}