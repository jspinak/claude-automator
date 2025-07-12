package com.claude.automator.startup;

import com.claude.automator.states.PromptState;
import com.claude.automator.states.WorkingState;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Verifies which states are actually active at startup by checking for their StateImages.
 * This ensures that states marked as active in StateMemory actually exist on screen.
 */
@Component
@Order(5)  // Run after Brobot initialization
@RequiredArgsConstructor
@Slf4j
public class ActiveStateVerifier implements ApplicationRunner {
    
    private final StateMemory stateMemory;
    private final StateService stateService;
    private final Action action;
    private final PromptState promptState;
    private final WorkingState workingState;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Verifying active states at startup...");
        
        // Clear any pre-existing active states
        stateMemory.getActiveStates().clear();
        
        // Check for Working state
        if (verifyWorkingState()) {
            // Get state ID from StateService
            Long workingStateId = stateService.getStateId("Working");
            if (workingStateId != null) {
                stateMemory.addActiveState(workingStateId);
                log.info("Working state verified and marked as active");
            } else {
                log.error("Working state ID not found in StateService");
            }
        } else {
            log.info("Working state not found on screen");
        }
        
        // Check for Prompt state
        if (verifyPromptState()) {
            // Get state ID from StateService
            Long promptStateId = stateService.getStateId("Prompt");
            if (promptStateId != null) {
                stateMemory.addActiveState(promptStateId);
                log.info("Prompt state verified and marked as active");
            } else {
                log.error("Prompt state ID not found in StateService");
            }
        } else {
            log.info("Prompt state not found on screen");
        }
        
        log.info("Active states after verification: {}", stateMemory.getActiveStateNames());
    }
    
    private boolean verifyWorkingState() {
        try {
            log.debug("Looking for Working state (claude icon)...");
            
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                    .setPauseBeforeBegin(2.0)  // Wait for UI to stabilize
                    .build();
            
            ObjectCollection target = new ObjectCollection.Builder()
                    .withImages(workingState.getClaudeIcon())
                    .build();
            
            ActionResult result = action.perform(findOptions, target);
            log.debug("Working state search result: {}", result.getActionDescription());
            return result.isSuccess();
            
        } catch (Exception e) {
            log.error("Error verifying Working state", e);
            return false;
        }
    }
    
    private boolean verifyPromptState() {
        try {
            log.debug("Looking for Prompt state (claude prompt)...");
            
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                    .build();
            
            ObjectCollection target = new ObjectCollection.Builder()
                    .withImages(promptState.getClaudePrompt())
                    .build();
            
            ActionResult result = action.perform(findOptions, target);
            log.debug("Prompt state search result: {}", result.getActionDescription());
            return result.isSuccess();
            
        } catch (Exception e) {
            log.error("Error verifying Prompt state", e);
            return false;
        }
    }
    
}