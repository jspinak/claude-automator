package com.claude.automator;

import io.github.jspinak.brobot.annotations.AnnotationProcessor;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.navigation.transition.StateTransitionsJointTable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ManualAnnotationProcessingTest extends TestBase {
    
    @Autowired
    private AnnotationProcessor annotationProcessor;
    
    @Autowired
    private StateService stateService;
    
    @Autowired
    private StateTransitionService transitionService;
    
    @Autowired
    private StateTransitionsJointTable jointTable;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Test
    public void testManualAnnotationProcessing() {
        // Check initial state
        System.out.println("\n=== BEFORE MANUAL PROCESSING ===");
        System.out.println("States in StateService: " + stateService.getAllStates().size());
        stateService.getAllStates().forEach(state -> {
            System.out.println("  - " + state.getName());
        });
        
        // Manually trigger annotation processing
        System.out.println("\n=== MANUALLY TRIGGERING ANNOTATION PROCESSING ===");
        assertNotNull(annotationProcessor, "AnnotationProcessor should be available");
        annotationProcessor.processAnnotations();
        
        // Check after processing
        System.out.println("\n=== AFTER MANUAL PROCESSING ===");
        System.out.println("States in StateService: " + stateService.getAllStates().size());
        stateService.getAllStates().forEach(state -> {
            System.out.println("  - " + state.getName());
        });
        
        // Verify states were registered
        assertTrue(stateService.getAllStates().size() > 1, 
            "Should have more than just the default 'unknown' state after processing");
        
        // Check for our specific states
        assertTrue(stateService.getState("Prompt").isPresent(), "Prompt state should be registered");
        assertTrue(stateService.getState("Working").isPresent(), "Working state should be registered");
        
        // Check if transitions were detected
        System.out.println("\n=== CHECKING TRANSITIONS ===");
        
        // Check using applicationContext.getBeansWithAnnotation directly
        var transitionBeans = applicationContext.getBeansWithAnnotation(
            io.github.jspinak.brobot.annotations.Transition.class);
        System.out.println("Transition beans found by getBeansWithAnnotation: " + transitionBeans.size());
        transitionBeans.forEach((name, bean) -> {
            System.out.println("  - " + name + " (" + bean.getClass().getName() + ")");
        });
        
        // Check if transitions were registered in the joint table
        Long promptStateId = stateService.getState("Prompt").get().getId();
        Long workingStateId = stateService.getState("Working").get().getId();
        
        System.out.println("\nPrompt state ID: " + promptStateId);
        System.out.println("Working state ID: " + workingStateId);
        
        // Check if there are transitions from Prompt state
        var promptTransitions = transitionService.getTransitions(promptStateId);
        System.out.println("Transitions from Prompt state: " + 
            (promptTransitions.isPresent() ? promptTransitions.get().getTransitions().size() : 0));
        
        // Check joint table
        var parentsOfWorking = jointTable.getStatesWithTransitionsTo(workingStateId);
        System.out.println("States with transitions to Working: " + parentsOfWorking);
        
        // Verify transitions are registered
        assertTrue(transitionBeans.size() > 0, "Should have at least one transition bean");
        assertTrue(promptTransitions.isPresent() && promptTransitions.get().getTransitions().size() > 0,
            "Should have transitions from Prompt state");
        assertTrue(parentsOfWorking.contains(promptStateId),
            "Prompt state should have transition to Working state");
    }
}