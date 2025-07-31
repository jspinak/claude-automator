package com.claude.automator.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.Getter;

/**
 * Represents the Working state where Claude is actively processing.
 */
@State
@Getter
public class WorkingState {
    
    private final StateImage claudeIcon;
    
    public WorkingState() {
        // Create the claude icon images with declarative region definition
        // The search region will be dynamically defined relative to the prompt StateImage
        claudeIcon = new StateImage.Builder()
            .addPatterns("working/claude-icon-1", 
                        "working/claude-icon-2", 
                        "working/claude-icon-3", 
                        "working/claude-icon-4")
            .setName("ClaudeIcon")
            .setSearchRegionOnObject(io.github.jspinak.brobot.model.element.SearchRegionOnObject.builder()
                    .targetType(io.github.jspinak.brobot.model.state.StateObject.Type.IMAGE)
                    .targetStateName("Prompt")
                    .targetObjectName("ClaudePrompt")
                    .adjustments(io.github.jspinak.brobot.model.element.SearchRegionOnObject.AdjustOptions.builder()
                            .xAdjust(3)
                            .yAdjust(10)
                            .wAdjust(30)
                            .hAdjust(55)
                            .build())
                    .build())
            .build();
    }
}