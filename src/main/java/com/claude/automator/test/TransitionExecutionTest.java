package com.claude.automator.test;

import com.claude.automator.transitions.PromptToWorkingTransition;
import io.github.jspinak.brobot.navigation.transition.StateNavigator;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.transition.StateTransitionsJointTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Test to verify transition execution works
 */
@Component
@ConditionalOnProperty(name = "test.transition-execution", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class TransitionExecutionTest implements CommandLineRunner {
    
    private final StateNavigator stateNavigator;
    private final StateService stateService;
    private final StateTransitionsJointTable jointTable;
    private final PromptToWorkingTransition transition;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("=== TRANSITION EXECUTION TEST ===");
        
        // Check registered transitions
        log.info("Checking transitions in joint table:");
        log.info("Outgoing transitions: {}", jointTable.getOutgoingTransitions());
        log.info("Incoming transitions: {}", jointTable.getIncomingTransitions());
        
        // Get state IDs
        Long promptId = stateService.getStateId("Prompt");
        Long workingId = stateService.getStateId("Working");
        log.info("Prompt state ID: {}", promptId);
        log.info("Working state ID: {}", workingId);
        
        // Check if transition is registered
        if (jointTable.getOutgoingTransitions().containsKey(promptId)) {
            log.info("Prompt has outgoing transitions to: {}", 
                    jointTable.getOutgoingTransitions().get(promptId));
        } else {
            log.warn("Prompt has NO outgoing transitions!");
        }
        
        // Try to execute transition directly
        log.info("Attempting direct transition execution...");
        boolean result = transition.execute();
        log.info("Direct transition execution result: {}", result);
        
        // Try navigation
        log.info("Attempting navigation to Working state...");
        boolean navResult = stateNavigator.openState("Working");
        log.info("Navigation result: {}", navResult);
        
        log.info("=== TEST COMPLETE ===");
    }
}