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
        // Initialize the claude prompt image
        claudePrompt = new StateImage.Builder()
            .addPatterns("prompt/claude-prompt-1","prompt/claude-prompt-2","prompt/claude-prompt-3")
            .setName("ClaudePrompt")
            .setSearchRegionForAllPatterns(new Region(10, 400, 700, 350))
            .build();
        
        // Create the continue command as a string
        continueCommand = new StateString.Builder()
            .setName("ContinueCommand")
            .setString("continue\n")
            .build();
    }
}