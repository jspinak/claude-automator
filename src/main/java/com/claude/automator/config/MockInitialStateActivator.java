package com.claude.automator.config;

import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Ensures Prompt state is activated as the initial state in mock mode.
 * This runs after the application is fully ready, ensuring all states are registered.
 */
@Component
@Slf4j
public class MockInitialStateActivator {
    
    @Autowired
    private StateService stateService;
    
    @Autowired
    private StateMemory stateMemory;
    
    private static boolean activated = false;
    
    @EventListener(ApplicationReadyEvent.class)
    public void activateInitialState() {
        if (!FrameworkSettings.mock || activated) {
            return;
        }
        
        activated = true;
        
        log.info("═══════════════════════════════════════════════════════");
        log.info("  MOCK INITIAL STATE ACTIVATOR");
        log.info("═══════════════════════════════════════════════════════");
        
        // Clear any wrongly activated states
        try {
            stateMemory.getActiveStates().clear();
            log.info("Cleared existing active states");
        } catch (Exception e) {
            log.warn("Could not clear active states: {}", e.getMessage());
        }
        
        // Activate Prompt state
        stateService.getState("Prompt").ifPresent(promptState -> {
            Long promptId = promptState.getId();
            stateMemory.addActiveState(promptId, true);
            promptState.setProbabilityToBaseProbability();
            log.info("✅ ACTIVATED PROMPT STATE (ID: {}) as initial state", promptId);
        });
        
        // Log final state
        log.info("Active states after correction: {}", stateMemory.getActiveStates());
        log.info("═══════════════════════════════════════════════════════");
    }
}