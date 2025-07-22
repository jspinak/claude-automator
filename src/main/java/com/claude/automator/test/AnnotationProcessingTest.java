package com.claude.automator.test;

import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.model.state.State;
import org.springframework.context.event.EventListener;
import io.github.jspinak.brobot.annotations.StatesRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Test component to verify annotation processing is working correctly.
 */
@Component
@ConditionalOnProperty(name = "test.annotation-processing", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class AnnotationProcessingTest {
    
    private final StateService stateService;
    
    @EventListener(StatesRegisteredEvent.class)
    public void onStatesRegistered(StatesRegisteredEvent event) {
        System.out.println("\n=== RECEIVED STATES REGISTERED EVENT ===");
        System.out.println("Event reports: " + event.getStateCount() + " states, " + 
                event.getTransitionCount() + " transitions");
        
        System.out.println("\n=== ANNOTATION PROCESSING VERIFICATION ===");
        
        // Get all registered states
        Collection<State> allStates = stateService.getAllStates();
        System.out.println("Total states registered: " + allStates.size());
        
        // Print details of each state
        for (State state : allStates) {
            System.out.println("\nState: " + state.getName());
            System.out.println("  - State Images: " + state.getStateImages().size());
            System.out.println("  - State Strings: " + state.getStateStrings().size());
            
            // Print image details
            state.getStateImages().forEach(img -> 
                System.out.println("    - Image: " + img.getName() + " (index: " + img.getIndex() + ")")
            );
            
            // Print string details
            state.getStateStrings().forEach(str -> 
                System.out.println("    - String: " + str.getName() + " = \"" + str.getString() + "\"")
            );
        }
        
        System.out.println("\n=== END VERIFICATION ===\n");
    }
}