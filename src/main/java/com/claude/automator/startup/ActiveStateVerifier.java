package com.claude.automator.startup;

import com.claude.automator.states.PromptState;
import com.claude.automator.states.WorkingState;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    
    @Autowired
    private BrobotLogger brobotLogger;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Verifying active states at startup...");
        
        try (var operation = brobotLogger.operation("ActiveStateVerification")) {
            brobotLogger.log()
                .observation("Starting active state verification")
                .metadata("executionOrder", 5)
                .metadata("verifyingStates", 2)
                .log();
            
            // Clear any pre-existing active states
            int previousActiveCount = stateMemory.getActiveStates().size();
            stateMemory.getActiveStates().clear();
            
            brobotLogger.log()
                .observation("Cleared pre-existing active states")
                .metadata("previousActiveCount", previousActiveCount)
                .log();
            
            // Check for Working state
            boolean workingStateActive = false;
            try (var timer = brobotLogger.startTimer("VerifyWorkingState")) {
                if (verifyWorkingState()) {
                    // Get state ID from StateService
                    Long workingStateId = stateService.getStateId("Working");
                    if (workingStateId != null) {
                        stateMemory.addActiveState(workingStateId);
                        workingStateActive = true;
                        log.info("Working state verified and marked as active");
                        
                        brobotLogger.log()
                            .observation("Working state verified and activated")
                            .metadata("stateId", workingStateId)
                            .metadata("stateName", "Working")
                            .metadata("verificationTime", timer.stop())
                            .log();
                    } else {
                        log.error("Working state ID not found in StateService");
                        
                        brobotLogger.log()
                            .error(new IllegalStateException("Working state ID not found"))
                            .message("Working state ID not found in StateService")
                            .metadata("availableStates", stateService.getAllStates())
                            .log();
                    }
                } else {
                    log.info("Working state not found on screen");
                    
                    brobotLogger.log()
                        .observation("Working state not found on screen")
                        .metadata("verificationTime", timer.stop())
                        .log();
                }
            }
            
            // Check for Prompt state
            boolean promptStateActive = false;
            try (var timer = brobotLogger.startTimer("VerifyPromptState")) {
                if (verifyPromptState()) {
                    // Get state ID from StateService
                    Long promptStateId = stateService.getStateId("Prompt");
                    if (promptStateId != null) {
                        stateMemory.addActiveState(promptStateId);
                        promptStateActive = true;
                        log.info("Prompt state verified and marked as active");
                        
                        brobotLogger.log()
                            .observation("Prompt state verified and activated")
                            .metadata("stateId", promptStateId)
                            .metadata("stateName", "Prompt")
                            .metadata("verificationTime", timer.stop())
                            .log();
                    } else {
                        log.error("Prompt state ID not found in StateService");
                        
                        brobotLogger.log()
                            .error(new IllegalStateException("Prompt state ID not found"))
                            .message("Prompt state ID not found in StateService")
                            .metadata("availableStates", stateService.getAllStates())
                            .log();
                    }
                } else {
                    log.info("Prompt state not found on screen");
                    
                    brobotLogger.log()
                        .observation("Prompt state not found on screen")
                        .metadata("verificationTime", timer.stop())
                        .log();
                }
            }
            
            log.info("Active states after verification: {}", stateMemory.getActiveStateNames());
            
            brobotLogger.log()
                .observation("Active state verification completed")
                .metadata("workingStateActive", workingStateActive)
                .metadata("promptStateActive", promptStateActive)
                .metadata("totalActiveStates", stateMemory.getActiveStates().size())
                .metadata("activeStateNames", stateMemory.getActiveStateNames())
                .log();
        }
    }
    
    private boolean verifyWorkingState() {
        try {
            log.debug("Looking for Working state (claude icon)...");
            
            brobotLogger.log()
                .observation("Searching for Working state")
                .metadata("targetImage", workingState.getClaudeIcon().getName())
                .metadata("pauseBeforeBegin", 2.0)
                .log();
            
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                    .setPauseBeforeBegin(2.0)  // Wait for UI to stabilize
                    .build();
            
            ObjectCollection target = new ObjectCollection.Builder()
                    .withImages(workingState.getClaudeIcon())
                    .build();
            
            try (var timer = brobotLogger.startTimer("WorkingStateSearch")) {
                ActionResult result = action.perform(findOptions, target);
                log.debug("Working state search result: {}", result.getActionDescription());
                
                brobotLogger.log()
                    .action("FIND")
                    .target(workingState.getClaudeIcon())
                    .result(result)
                    .metadata("searchPurpose", "StateVerification")
                    .metadata("searchDuration", timer.stop())
                    .log();
                
                return result.isSuccess();
            }
            
        } catch (Exception e) {
            log.error("Error verifying Working state", e);
            
            brobotLogger.log()
                .error(e)
                .message("Error verifying Working state")
                .metadata("stateName", "Working")
                .log();
            
            return false;
        }
    }
    
    private boolean verifyPromptState() {
        try {
            log.debug("Looking for Prompt state (claude prompt)...");
            
            brobotLogger.log()
                .observation("Searching for Prompt state")
                .metadata("targetImage", promptState.getClaudePrompt().getName())
                .metadata("pauseBeforeBegin", 0)
                .log();
            
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                    .build();
            
            ObjectCollection target = new ObjectCollection.Builder()
                    .withImages(promptState.getClaudePrompt())
                    .build();
            
            try (var timer = brobotLogger.startTimer("PromptStateSearch")) {
                ActionResult result = action.perform(findOptions, target);
                log.debug("Prompt state search result: {}", result.getActionDescription());
                
                brobotLogger.log()
                    .action("FIND")
                    .target(promptState.getClaudePrompt())
                    .result(result)
                    .metadata("searchPurpose", "StateVerification")
                    .metadata("searchDuration", timer.stop())
                    .log();
                
                return result.isSuccess();
            }
            
        } catch (Exception e) {
            log.error("Error verifying Prompt state", e);
            
            brobotLogger.log()
                .error(e)
                .message("Error verifying Prompt state")
                .metadata("stateName", "Prompt")
                .log();
                
            return false;
        }
    }
}