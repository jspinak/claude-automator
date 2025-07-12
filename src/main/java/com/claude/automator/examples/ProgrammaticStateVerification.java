package com.claude.automator.examples;

import io.github.jspinak.brobot.startup.InitialStateVerifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;

/**
 * Example of programmatic initial state verification.
 * 
 * This demonstrates how to use InitialStateVerifier directly in code
 * when you need more control than the automatic configuration provides.
 * 
 * Comment out @Component to disable this example.
 */
// @Component  // Uncomment to enable
@Order(2)  // Run after automatic verification
@RequiredArgsConstructor
@Slf4j
public class ProgrammaticStateVerification implements ApplicationRunner {
    
    private final InitialStateVerifier stateVerifier;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Demonstrating programmatic state verification");
        
        // Example 1: Simple verification with state names
        boolean found = stateVerifier.verify(
            "Prompt",
            "Working"
        );
        log.info("Simple verification result: {}", found);
        
        // Example 2: Verification with custom options
        boolean customResult = stateVerifier.builder()
            .withStates("Prompt", "Working")
            .withFallbackSearch(true)      // Search all states if these aren't found
            .activateFirstOnly(true)        // Stop after finding first state
            .verify();
        log.info("Custom verification result: {}", customResult);
        
        // Example 3: Mock testing with probabilities
        // This would be used when brobot.mock = true
        boolean mockResult = stateVerifier.builder()
            .withState("Prompt", 70)    // 70% chance
            .withState("Working", 30)  // 30% chance
            .verify();
        log.info("Mock verification result: {}", mockResult);
    }
}