package com.claude.automator.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents the Working state where Claude is actively processing.
 * 
 * This state is not initial - it becomes active after transitioning from Prompt.
 * In mock mode, this state has a 100% probability of being found,
 * ensuring reliable transitions.
 */
@State
@Getter
@Slf4j
public class WorkingState {
    
    private final StateImage claudeIcon;
    
    public WorkingState() {
        // Create the claude icon images with declarative region definition
        // Do NOT set a fixed ActionHistory as it will override the SearchRegionOnObject
        
        claudeIcon = new StateImage.Builder()
            .addPatterns("working/claude-icon-1", 
                        "working/claude-icon-2", 
                        "working/claude-icon-3", 
                        "working/claude-icon-4")
            .setName("ClaudeIcon")
            .setFixedForAllPatterns(true)  // Enable fixed region optimization once found
            .setSearchRegionOnObject(SearchRegionOnObject.builder()
                    .setTargetType(StateObject.Type.IMAGE)
                    .setTargetStateName("Prompt")
                    .setTargetObjectName("ClaudePrompt")
                    .setAdjustments(MatchAdjustmentOptions.builder()
                            .setAddX(3)
                            .setAddY(10)
                            .setAddW(30)
                            .setAddH(55)
                            .build())
                    .build())
            .build();
        
        log.info("WorkingState ClaudeIcon search region config: {}", 
                claudeIcon.getSearchRegionOnObject());
    }
}