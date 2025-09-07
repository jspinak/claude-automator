package com.claude.automator;

import io.github.jspinak.brobot.annotations.AnnotationProcessor;
import io.github.jspinak.brobot.navigation.service.StateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ManualAnnotationProcessingTest extends TestBase {
    
    @Autowired
    private AnnotationProcessor annotationProcessor;
    
    @Autowired
    private StateService stateService;
    
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
    }
}