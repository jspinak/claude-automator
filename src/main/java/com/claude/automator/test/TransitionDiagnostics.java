package com.claude.automator.test;

import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Diagnostic component to check what transitions are actually registered.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TransitionDiagnostics {
    
    private final StateTransitionService transitionService;
    private final StateService stateService;
    
    @EventListener(ApplicationReadyEvent.class)
    public void diagnoseTransitions() {
        log.info("=== TRANSITION DIAGNOSTICS ===");
        
        // List all registered states
        List<State> allStates = stateService.getAllStates();
        log.info("Registered states: {}", allStates.size());
        for (State state : allStates) {
            log.info("  State: {} (ID: {})", state.getName(), state.getId());
        }
        
        // List all transitions
        log.info("All registered transitions:");
        List<StateTransitions> allTransitions = transitionService.getAllStateTransitions();
        log.info("Total transition containers: {}", allTransitions.size());
        
        for (StateTransitions transitions : allTransitions) {
            log.info("  From state: {} (ID: {})", 
                    transitions.getStateName(), transitions.getStateId());
            if (transitions.getTransitions() != null) {
                transitions.getTransitions().forEach(transition -> {
                    log.info("    -> To states: {}", transition.getActivate());
                    log.info("       Type: {}", transition.getClass().getSimpleName());
                    log.info("       Score: {}", transition.getScore());
                });
            }
        }
        
        // Check specific transitions
        Optional<State> promptState = stateService.getState("Prompt");
        Optional<State> workingState = stateService.getState("Working");
        
        if (promptState.isPresent() && workingState.isPresent()) {
            log.info("Checking transition from Prompt ({}) to Working ({})", 
                    promptState.get().getId(), workingState.get().getId());
            
            Optional<StateTransitions> promptTransitions = 
                transitionService.getTransitions(promptState.get().getId());
            
            if (promptTransitions.isPresent()) {
                log.info("Prompt state has {} transitions", 
                        promptTransitions.get().getTransitions().size());
            } else {
                log.error("No transitions found for Prompt state!");
            }
        }
        
        log.info("=== END TRANSITION DIAGNOSTICS ===");
    }
}