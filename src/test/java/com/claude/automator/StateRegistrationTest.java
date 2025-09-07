package com.claude.automator;

import com.claude.automator.states.PromptState;
import com.claude.automator.states.WorkingState;
import io.github.jspinak.brobot.annotations.AnnotationProcessor;
import io.github.jspinak.brobot.annotations.AnnotatedStateBuilder;
import io.github.jspinak.brobot.annotations.StateRegistrationService;
import io.github.jspinak.brobot.navigation.service.StateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class StateRegistrationTest extends TestBase {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private StateService stateService;
    
    @Autowired(required = false)
    private PromptState promptState;
    
    @Autowired(required = false)
    private WorkingState workingState;
    
    @Autowired(required = false)
    private AnnotationProcessor annotationProcessor;
    
    @Autowired(required = false)
    private AnnotatedStateBuilder annotatedStateBuilder;
    
    @Autowired(required = false)
    private StateRegistrationService stateRegistrationService;
    
    @Test
    public void testStatesAreRegistered() {
        // Check if AnnotationProcessor and its dependencies are created
        System.out.println("\n=== ANNOTATION PROCESSOR CHECK ===");
        System.out.println("AnnotationProcessor bean exists: " + (annotationProcessor != null));
        System.out.println("AnnotatedStateBuilder bean exists: " + (annotatedStateBuilder != null));
        System.out.println("StateRegistrationService bean exists: " + (stateRegistrationService != null));
        
        // Manually trigger annotation processing if not done
        if (annotationProcessor != null) {
            System.out.println("\n=== MANUALLY TRIGGERING ANNOTATION PROCESSOR ===");
            annotationProcessor.processAnnotations();
        }
        
        // Check if state beans exist
        System.out.println("\n=== STATE BEAN VERIFICATION ===");
        System.out.println("PromptState bean exists: " + (promptState != null));
        System.out.println("WorkingState bean exists: " + (workingState != null));
        
        // Check if beans are in the application context
        boolean hasPromptStateBean = applicationContext.containsBean("promptState");
        boolean hasWorkingStateBean = applicationContext.containsBean("workingState");
        System.out.println("ApplicationContext contains promptState: " + hasPromptStateBean);
        System.out.println("ApplicationContext contains workingState: " + hasWorkingStateBean);
        
        // List all beans with "state" in their name
        System.out.println("\n=== ALL STATE-RELATED BEANS ===");
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String name : beanNames) {
            if (name.toLowerCase().contains("state")) {
                System.out.println("  - " + name + " (" + applicationContext.getBean(name).getClass().getSimpleName() + ")");
            }
        }
        
        // Check StateService
        System.out.println("\n=== STATE SERVICE CHECK ===");
        System.out.println("StateService exists: " + (stateService != null));
        if (stateService != null) {
            System.out.println("States in StateService: " + stateService.getAllStates().size());
            stateService.getAllStates().forEach(state -> {
                System.out.println("  - " + state.getName() + ": " + state.getClass().getSimpleName());
            });
        }
        
        // Check AnnotationProcessor
        System.out.println("\n=== ANNOTATION PROCESSOR CHECK ===");
        System.out.println("AnnotationProcessor exists: " + (annotationProcessor != null));
        
        // Assertions
        assertNotNull(promptState, "PromptState should be a Spring bean");
        assertNotNull(workingState, "WorkingState should be a Spring bean");
        assertNotNull(stateService, "StateService should be available");
        
        // The states should be registered in StateService
        assertTrue(stateService.getAllStates().size() > 0, "StateService should have registered states");
    }
}