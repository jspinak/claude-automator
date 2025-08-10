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
        
        // Initialize the claude prompt image
        // Build first, then set fixed search region to ensure all patterns share the same region
        // This prevents multiple overlapping highlights
        StateImage tempImage = new StateImage.Builder()
            .addPatterns("prompt/claude-prompt-1","prompt/claude-prompt-2","prompt/claude-prompt-3")
            .setName("ClaudePrompt")
            .build();
        
        // Set fixed search region after building to avoid duplicate regions
        tempImage.setFixedSearchRegion(lowerLeftQuarter);
        claudePrompt = tempImage;
        
        // Create the continue command as a string
        continueCommand = new StateString.Builder()
            .setName("ContinueCommand")
            .setString("continue\n")
            .build();
    }
}