package com.claude.automator.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents the Working state where Claude is actively processing.
 * Uses the new @State annotation for automatic registration.
 */
@State
@Getter
@Slf4j
public class WorkingState {
    
    private final StateImage claudeIcon;
    
    public WorkingState() {
        log.info("Creating WorkingState");
        
        // Create the claude icon images
        claudeIcon = new StateImage.Builder()
            .addPatterns("working/claude-icon-1", 
                        "working/claude-icon-2", 
                        "working/claude-icon-3", 
                        "working/claude-icon-4")
            .setName("ClaudeIcon")
            .build();
        
        log.info("WorkingState created successfully");
    }
}