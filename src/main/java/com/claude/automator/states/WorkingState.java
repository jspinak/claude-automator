package com.claude.automator.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

/**
 * Represents the Working state where Claude is actively processing.
 * Uses the new @State annotation for automatic registration.
 */
@State
@Getter
@Slf4j
public class WorkingState {
    
    private final StateImage claudeIcon;
    
    @Autowired(required = false)
    private BrobotLogger brobotLogger;
    
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
    
    @jakarta.annotation.PostConstruct
    private void init() {
        if (brobotLogger == null) {
            log.warn("BrobotLogger not available for WorkingState initialization logging");
            return;
        }
        
        try (var timer = brobotLogger.startTimer("WorkingStatePostInit")) {
            brobotLogger.log()
                .observation("WorkingState post-initialization")
                .metadata("stateType", "normal")
                .metadata("className", this.getClass().getSimpleName())
                .log();
            
            // Log icon pattern details
            List<String> patterns = Arrays.asList(
                "working/claude-icon-1",
                "working/claude-icon-2",
                "working/claude-icon-3",
                "working/claude-icon-4"
            );
            
            brobotLogger.log()
                .observation("Claude icon patterns loaded")
                .metadata("patternCount", patterns.size())
                .metadata("patterns", patterns)
                .metadata("imageName", claudeIcon.getName())
                .metadata("purpose", "Monitor for icon disappearance")
                .log();
            
            // Log monitoring configuration
            brobotLogger.log()
                .observation("WorkingState monitoring configuration")
                .metadata("monitoringInterval", "2 seconds")
                .metadata("detectionStrategy", "Icon visibility check")
                .metadata("actionOnDisappear", "Remove state and re-navigate")
                .log();
            
            // Log state registration info
            brobotLogger.log()
                .observation("WorkingState components initialized")
                .metadata("componentsCount", 1)
                .metadata("stateAnnotation", "@State")
                .metadata("initializationTime", timer.stop())
                .log();
        } catch (Exception e) {
            log.error("Error in WorkingState post-initialization logging", e);
        }
    }
    
    /**
     * Get the Claude icon with logging.
     */
    public StateImage getClaudeIcon() {
        if (brobotLogger != null) {
            brobotLogger.log()
                .observation("Accessing Claude icon for monitoring")
                .metadata("iconName", claudeIcon.getName())
                .metadata("patternCount", 4)
                .log();
        }
        return claudeIcon;
    }
}