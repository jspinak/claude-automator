package com.claude.automator.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.tools.testing.mock.state.MockStateManagement;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

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
    
    @Autowired(required = false)
    private MockStateManagement mockStateManagement;
    
    // Set to 100% for reliable mock transitions
    private static final int MOCK_PROBABILITY = 100;
    
    private final StateImage claudeIcon;
    
    public WorkingState() {
        // Create the claude icon images with declarative region definition
        // Do NOT set a fixed ActionHistory as it will override the SearchRegionOnObject
        
        claudeIcon = new StateImage.Builder()
            .addPatterns("claude-icon-1", 
                        "claude-icon-2", 
                        "claude-icon-3", 
                        "claude-icon-4")
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
    
    @PostConstruct
    public void configureMockProbability() {
        // Only configure if mock mode is enabled and MockStateManagement is available
        if (FrameworkSettings.mock && mockStateManagement != null) {
            mockStateManagement.setStateProbabilities(MOCK_PROBABILITY, "Working");
            log.info("[WORKING STATE] Mock mode enabled - probability set to {}%", MOCK_PROBABILITY);
        } else if (FrameworkSettings.mock) {
            log.warn("[WORKING STATE] Mock mode enabled but MockStateManagement not available");
        } else {
            log.debug("[WORKING STATE] Live mode - no mock probability configuration needed");
        }
    }
}