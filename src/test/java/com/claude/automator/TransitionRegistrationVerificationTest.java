package com.claude.automator;

import io.github.jspinak.brobot.annotations.AnnotationProcessor;
import io.github.jspinak.brobot.annotations.TransitionAnnotationBeanPostProcessor;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.navigation.transition.StateTransitionsJointTable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify that @Transition annotations are properly detected and registered
 * using the new TransitionAnnotationBeanPostProcessor.
 */
@SpringBootTest
@ActiveProfiles("test")
public class TransitionRegistrationVerificationTest extends TestBase {
    
    @Autowired
    private TransitionAnnotationBeanPostProcessor transitionBeanPostProcessor;
    
    @Autowired
    private StateService stateService;
    
    @Autowired
    private StateTransitionService transitionService;
    
    @Autowired
    private StateTransitionsJointTable jointTable;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Test
    public void testTransitionBeanPostProcessorDetection() {
        System.out.println("\n=== TESTING TRANSITION BEAN POST PROCESSOR ===");
        
        // Check that the TransitionAnnotationBeanPostProcessor exists
        assertNotNull(transitionBeanPostProcessor, "TransitionAnnotationBeanPostProcessor should be available");
        
        // Check what it has collected
        var transitionBeans = transitionBeanPostProcessor.getTransitionBeans();
        System.out.println("TransitionAnnotationBeanPostProcessor collected " + transitionBeans.size() + " transition beans:");
        transitionBeans.forEach((name, bean) -> {
            System.out.println("  - " + name + " (" + bean.getClass().getName() + ")");
        });
        
        // Verify we have at least one transition bean
        assertTrue(transitionBeans.size() > 0, "Should have at least one @Transition bean");
        
        // Check if PromptToWorkingTransition was detected
        boolean hasPromptToWorking = transitionBeans.values().stream()
            .anyMatch(bean -> bean.getClass().getName().contains("PromptToWorkingTransition"));
        assertTrue(hasPromptToWorking, "PromptToWorkingTransition should be detected");
        
        System.out.println("\n=== VERIFYING TRANSITION REGISTRATION ===");
        
        // Since ApplicationReadyEvent is handled by @PostConstruct in test profile,
        // states and transitions should already be registered
        
        // Check that states exist
        var promptState = stateService.getState("Prompt");
        var workingState = stateService.getState("Working");
        
        assertTrue(promptState.isPresent(), "Prompt state should be registered");
        assertTrue(workingState.isPresent(), "Working state should be registered");
        
        Long promptStateId = promptState.get().getId();
        Long workingStateId = workingState.get().getId();
        
        System.out.println("Prompt state ID: " + promptStateId);
        System.out.println("Working state ID: " + workingStateId);
        
        // Check if transitions are registered
        var promptTransitions = transitionService.getTransitions(promptStateId);
        System.out.println("Transitions from Prompt state: " + 
            (promptTransitions.isPresent() ? promptTransitions.get().getTransitions().size() : 0));
        
        if (promptTransitions.isPresent() && promptTransitions.get().getTransitions().size() > 0) {
            System.out.println("  Transition details:");
            promptTransitions.get().getTransitions().forEach(transition -> {
                System.out.println("    - Score: " + transition.getScore());
                System.out.println("    - Activates: " + transition.getActivate());
            });
        }
        
        // Check joint table
        var parentsOfWorking = jointTable.getStatesWithTransitionsTo(workingStateId);
        System.out.println("States with transitions to Working: " + parentsOfWorking);
        
        // Verify transitions are properly registered
        assertTrue(promptTransitions.isPresent() && promptTransitions.get().getTransitions().size() > 0,
            "Should have transitions from Prompt state");
        assertTrue(parentsOfWorking.contains(promptStateId),
            "Prompt state should have transition to Working state in joint table");
        
        System.out.println("\n=== SUCCESS ===");
        System.out.println("TransitionAnnotationBeanPostProcessor is working correctly!");
        System.out.println("Transitions are being detected and registered properly.");
    }
}