package com.claude.automator.test;

import io.github.jspinak.brobot.model.transition.StateTransitionStore;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.navigation.transition.StateNavigator;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.navigation.transition.StateTransitionsJointTable;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

/**
 * Verifies that transitions are properly registered and can be executed.
 * Run with: ./gradlew bootRun -Pargs="--transition.verification.enabled=true"
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "transition.verification.enabled", havingValue = "true")
public class TransitionVerificationRunner implements CommandLineRunner {

    private final StateService stateService;
    private final StateTransitionService transitionService;
    private final StateTransitionStore transitionStore;
    private final StateTransitionsJointTable jointTable;
    private final StateNavigator navigation;
    private final StateMemory stateMemory;

    @Override
    public void run(String... args) throws Exception {
        log.info("\n\n=== TRANSITION VERIFICATION RUNNER ===\n");
        
        // Get state IDs
        Long promptId = stateService.getState("Prompt")
            .map(state -> state.getId())
            .orElse(null);
        Long workingId = stateService.getState("Working")
            .map(state -> state.getId())
            .orElse(null);
            
        log.info("Prompt state ID: {}", promptId);
        log.info("Working state ID: {}", workingId);
        
        if (promptId == null || workingId == null) {
            log.error("States not found!");
            return;
        }
        
        // Check if transitions exist in the repository
        log.info("\n=== Checking StateTransitionStore ===");
        log.info("All state IDs in repository: {}", transitionStore.getAllStateIds());
        
        Optional<StateTransitions> promptTransitions = transitionStore.get(promptId);
        log.info("Transitions for Prompt ({}): {}", promptId, promptTransitions.isPresent());
        
        if (promptTransitions.isPresent()) {
            StateTransitions transitions = promptTransitions.get();
            log.info("  - State ID in transitions object: {}", transitions.getStateId());
            log.info("  - Number of transitions: {}", transitions.getTransitions().size());
            transitions.getTransitions().forEach(t -> {
                log.info("  - Transition activates: {}", t.getActivate());
            });
        }
        
        // Check joint table
        log.info("\n=== Checking StateTransitionsJointTable ===");
        Set<Long> fromPrompt = jointTable.getStatesWithTransitionsFrom(promptId);
        log.info("States reachable from Prompt ({}): {}", promptId, fromPrompt);
        
        Set<Long> toWorking = jointTable.getStatesWithTransitionsTo(workingId);
        log.info("States that can reach Working ({}): {}", workingId, toWorking);
        
        // Check if transition path exists
        boolean canReachWorking = fromPrompt.contains(workingId);
        boolean promptCanReachWorking = toWorking.contains(promptId);
        
        log.info("\n=== Path Analysis ===");
        log.info("Can Prompt reach Working? {}", canReachWorking);
        log.info("Is Prompt listed as parent of Working? {}", promptCanReachWorking);
        
        // Try to navigate from Prompt to Working
        log.info("\n=== Attempting Navigation ===");
        
        // First, we need to be in Prompt state - directly set it as active
        log.info("Setting Prompt as active state...");
        stateMemory.addActiveState(promptId);
        log.info("Active states after setting Prompt: {}", stateMemory.getActiveStates());
        
        // Now attempt navigation to Working
        log.info("Attempting to navigate from Prompt to Working...");
        boolean navigationResult = navigation.openState("Working");
        log.info("Navigation result: {}", navigationResult);
        
        log.info("Active states after navigation: {}", stateMemory.getActiveStates());
        
        if (navigationResult) {
            log.info("✅ Navigation successful! Transition executed.");
        } else {
            log.error("❌ Navigation failed! Check the logs for transition execution issues.");
        }
        
        log.info("\n=== VERIFICATION COMPLETE ===\n");
    }
}