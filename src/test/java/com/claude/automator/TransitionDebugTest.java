package com.claude.automator;

import com.claude.automator.states.PromptState;
import com.claude.automator.states.WorkingState;
import io.github.jspinak.brobot.model.transition.StateTransitionStore;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.navigation.transition.StateTransitionsJointTable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TransitionDebugTest {

    @Autowired
    private StateService stateService;
    
    @Autowired
    private StateTransitionService transitionService;
    
    @Autowired
    private StateTransitionStore transitionStore;
    
    @Autowired
    private StateTransitionsJointTable jointTable;
    
    @Test
    public void testTransitionRegistration() {
        System.out.println("\n=== TRANSITION DEBUG TEST ===\n");
        
        // Get state IDs
        Long promptId = stateService.getState("Prompt")
            .map(state -> state.getId())
            .orElse(null);
        Long workingId = stateService.getState("Working")
            .map(state -> state.getId())
            .orElse(null);
            
        System.out.println("Prompt state ID: " + promptId);
        System.out.println("Working state ID: " + workingId);
        
        assertNotNull(promptId, "Prompt state should exist");
        assertNotNull(workingId, "Working state should exist");
        
        // Check if transitions exist in the repository
        System.out.println("\n=== Checking StateTransitionStore ===");
        System.out.println("All state IDs in repository: " + transitionStore.getAllStateIds());
        
        Optional<StateTransitions> promptTransitions = transitionStore.get(promptId);
        System.out.println("Transitions for Prompt (" + promptId + "): " + promptTransitions.isPresent());
        
        if (promptTransitions.isPresent()) {
            StateTransitions transitions = promptTransitions.get();
            System.out.println("  - State ID in transitions object: " + transitions.getStateId());
            System.out.println("  - Number of transitions: " + transitions.getTransitions().size());
            transitions.getTransitions().forEach(t -> {
                System.out.println("  - Transition activates: " + t.getActivate());
            });
        }
        
        // Check joint table
        System.out.println("\n=== Checking StateTransitionsJointTable ===");
        Set<Long> fromPrompt = jointTable.getStatesWithTransitionsFrom(promptId);
        System.out.println("States reachable from Prompt (" + promptId + "): " + fromPrompt);
        
        Set<Long> toWorking = jointTable.getStatesWithTransitionsTo(workingId);
        System.out.println("States that can reach Working (" + workingId + "): " + toWorking);
        
        // Check if transition path exists
        boolean canReachWorking = fromPrompt.contains(workingId);
        boolean promptCanReachWorking = toWorking.contains(promptId);
        
        System.out.println("\n=== Path Analysis ===");
        System.out.println("Can Prompt reach Working? " + canReachWorking);
        System.out.println("Is Prompt listed as parent of Working? " + promptCanReachWorking);
        
        // Print full joint table state
        System.out.println("\n=== Full Joint Table State ===");
        System.out.println("Outgoing transitions: " + jointTable.getOutgoingTransitions());
        System.out.println("Incoming transitions: " + jointTable.getIncomingTransitions());
        
        // Verify the transition exists
        assertTrue(promptTransitions.isPresent(), 
                  "StateTransitions should exist for Prompt state");
        assertTrue(canReachWorking || promptCanReachWorking, 
                  "There should be a path from Prompt to Working");
    }
}