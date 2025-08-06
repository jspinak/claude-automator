package com.claude.automator.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateObject;
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
            .setSearchRegionOnObject(SearchRegionOnObject.builder()
                    .targetType(StateObject.Type.IMAGE)
                    .targetStateName("Prompt")
                    .targetObjectName("ClaudePrompt")
                    .adjustments(MatchAdjustmentOptions.builder()
                            .addX(3)
                            .addY(10)
                            .addW(30)
                            .addH(55)
                            .build())
                    .build())
            .build();
    }
}