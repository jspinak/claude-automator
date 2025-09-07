package com.claude.automator;

import com.claude.automator.states.PromptState;
import com.claude.automator.states.WorkingState;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.action.internal.region.DynamicRegionResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ManualStateRegistrationTest extends TestBase {
    
    @Autowired
    private StateService stateService;
    
    @Autowired
    private PromptState promptState;
    
    @Autowired
    private WorkingState workingState;
    
    @Autowired
    private DynamicRegionResolver dynamicRegionResolver;
    
    @Test
    public void testManualStateRegistration() {
        System.out.println("\n=== MANUAL STATE REGISTRATION TEST ===");
        
        // Check initial state
        System.out.println("Initial states in StateService: " + stateService.getAllStates().size());
        stateService.getAllStates().forEach(s -> 
            System.out.println("  - " + s.getName() + " with " + s.getStateImages().size() + " images"));
        
        // Manually create and register Prompt state
        State promptStateObj = new State();
        promptStateObj.setName("Prompt");
        promptStateObj.getStateImages().add(promptState.getClaudePrompt());
        stateService.save(promptStateObj);
        System.out.println("Registered Prompt state");
        
        // Manually create and register Working state
        State workingStateObj = new State();
        workingStateObj.setName("Working");
        workingStateObj.getStateImages().add(workingState.getClaudeIcon());
        stateService.save(workingStateObj);
        System.out.println("Registered Working state");
        
        // Check states after registration
        System.out.println("\nStates after registration: " + stateService.getAllStates().size());
        stateService.getAllStates().forEach(s -> 
            System.out.println("  - " + s.getName() + " with " + s.getStateImages().size() + " images"));
        
        // Test that states can be found
        assertTrue(stateService.getState("Prompt").isPresent(), "Prompt state should be registered");
        assertTrue(stateService.getState("Working").isPresent(), "Working state should be registered");
        
        // Now register the SearchRegionOnObject dependencies
        System.out.println("\n=== REGISTERING SEARCHREGION DEPENDENCIES ===");
        
        // Get all objects that need to be registered
        List<StateObject> objectsToRegister = new ArrayList<>();
        
        // Add all StateImages from both states (StateImage extends StateObject)
        stateService.getState("Prompt").ifPresent(state -> 
            objectsToRegister.addAll(state.getStateImages()));
        stateService.getState("Working").ifPresent(state -> 
            objectsToRegister.addAll(state.getStateImages()));
        
        System.out.println("Registering " + objectsToRegister.size() + " objects with DynamicRegionResolver");
        dynamicRegionResolver.registerDependencies(objectsToRegister);
        
        // Check if ClaudeIcon has dependency on ClaudePrompt
        StateImage claudeIcon = workingState.getClaudeIcon();
        System.out.println("\n=== VERIFYING DEPENDENCIES ===");
        System.out.println("ClaudeIcon patterns: " + claudeIcon.getPatterns().size());
        claudeIcon.getPatterns().forEach(p -> {
            System.out.println("  Pattern: " + p.getName());
            System.out.println("    - Fixed: " + p.isFixed());
            System.out.println("    - Search regions: " + p.getSearchRegions().getRegions());
        });
        
        System.out.println("\n=== TEST COMPLETE ===");
    }
}