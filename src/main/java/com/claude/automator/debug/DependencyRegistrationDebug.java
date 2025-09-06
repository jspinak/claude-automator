package com.claude.automator.debug;

import com.claude.automator.states.PromptState;
import com.claude.automator.states.WorkingState;
import io.github.jspinak.brobot.action.internal.region.DynamicRegionResolver;
import io.github.jspinak.brobot.action.internal.region.SearchRegionDependencyRegistry;
import io.github.jspinak.brobot.annotations.StatesRegisteredEvent;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.navigation.service.StateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.ComponentScan;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@ComponentScan(basePackages = {"com.claude.automator", "io.github.jspinak.brobot"})
@Slf4j
public class DependencyRegistrationDebug implements CommandLineRunner {
    
    @Autowired
    private StateService stateService;
    
    @Autowired
    private DynamicRegionResolver dynamicRegionResolver;
    
    @Autowired
    private SearchRegionDependencyRegistry dependencyRegistry;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Autowired(required = false)
    private PromptState promptState;
    
    @Autowired(required = false)
    private WorkingState workingState;
    
    public static void main(String[] args) {
        SpringApplication.run(DependencyRegistrationDebug.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        log.info("=== DEPENDENCY REGISTRATION DEBUG ===");
        
        // Check if states are available
        log.info("PromptState bean available: {}", promptState != null);
        log.info("WorkingState bean available: {}", workingState != null);
        
        // Check registered states
        List<State> allStates = stateService.getAllStates();
        log.info("Total states in StateService: {}", allStates.size());
        
        for (State state : allStates) {
            log.info("State: {} with {} images", state.getName(), state.getStateImages().size());
            
            // Check each StateImage for SearchRegionOnObject
            for (StateImage image : state.getStateImages()) {
                if (image.getSearchRegionOnObject() != null) {
                    log.info("  - Image {} has SearchRegionOnObject: {}", 
                            image.getName(), image.getSearchRegionOnObject());
                } else {
                    log.info("  - Image {} has NO SearchRegionOnObject", image.getName());
                }
            }
        }
        
        // Manually register dependencies to test if that works
        log.info("\n=== MANUAL REGISTRATION TEST ===");
        List<StateObject> allObjects = new ArrayList<>();
        for (State state : allStates) {
            allObjects.addAll(state.getStateImages());
            allObjects.addAll(state.getStateLocations());
            allObjects.addAll(state.getStateRegions());
        }
        
        log.info("Manually registering {} objects", allObjects.size());
        dynamicRegionResolver.registerDependencies(allObjects);
        
        // Check what's in the dependency registry
        log.info("\n=== DEPENDENCY REGISTRY CHECK ===");
        
        // Try to get dependents for ClaudePrompt
        var dependents = dependencyRegistry.getDependents("Prompt", "ClaudePrompt");
        log.info("Dependents on Prompt:ClaudePrompt: {}", dependents.size());
        for (var dependent : dependents) {
            log.info("  - {}", dependent.getStateObject().getName());
        }
        
        // Force publish StatesRegisteredEvent to see if StateInitializationOrchestrator runs
        log.info("\n=== FORCE TRIGGER EVENT ===");
        StatesRegisteredEvent event = new StatesRegisteredEvent(this, allStates.size(), 0);
        eventPublisher.publishEvent(event);
        
        log.info("\n=== DEBUG COMPLETE ===");
        System.exit(0);
    }
}