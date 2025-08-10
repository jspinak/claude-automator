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
        
        // Create the continue command as a string
        continueCommand = new StateString.Builder()
            .setName("ContinueCommand")
            .setString("continue\n")
            .build();
    }
}