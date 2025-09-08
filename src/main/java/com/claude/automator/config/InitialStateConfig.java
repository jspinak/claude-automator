package com.claude.automator.config;

import com.claude.automator.states.PromptState;
import io.github.jspinak.brobot.annotations.StatesRegisteredEvent;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Ensures the correct initial state (Prompt) is active after states are registered.
 * This fixes the issue where Working state was incorrectly being set as active.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(100) // Run after StateInitializationOrchestrator
public class InitialStateConfig {
    
    private final StateService stateService;
    private final StateMemory stateMemory;
    private final PromptState promptState;
    
    @EventListener(StatesRegisteredEvent.class)
    @Order(100) // Run after other initialization
    public void ensureCorrectInitialState(StatesRegisteredEvent event) {
        log.info("=== INITIAL STATE CONFIG ===");
        
        // Log current active states
        log.info("Current active states: {}", stateMemory.getActiveStates());
        log.info("Current active state names: {}", stateMemory.getActiveStateNames());
        
        // Clear any incorrectly set active states
        stateMemory.getActiveStates().clear();
        
        // Get the Prompt state (which should be initial)
        stateService.getState("Prompt").ifPresentOrElse(
            promptStateObj -> {
                log.info("Found Prompt state with ID: {}", promptStateObj.getId());
                
                // Set Prompt as the active state
                stateMemory.addActiveState(promptStateObj.getId());
                log.info("Set Prompt state (ID {}) as active", promptStateObj.getId());
                
                // Verify the change
                log.info("Active states after correction: {}", stateMemory.getActiveStates());
                log.info("Active state names after correction: {}", stateMemory.getActiveStateNames());
            },
            () -> {
                log.error("Could not find Prompt state in StateService!");
                
                // Log all available states for debugging
                log.info("Available states:");
                for (State state : stateService.getAllStates()) {
                    log.info("  - {} (ID: {})", state.getName(), state.getId());
                }
            }
        );
        
        log.info("=== INITIAL STATE CONFIG COMPLETE ===");
    }
}