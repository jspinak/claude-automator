package com.claude.automator.test;

import com.claude.automator.states.PromptState;
import com.claude.automator.transitions.PromptToWorkingTransitionChained;
import io.github.jspinak.brobot.action.Action;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Test to verify different chaining approaches for find->click->type actions.
 * Enable with: test.chained-action=true
 */
@Component
@ConditionalOnProperty(name = "test.chained-action", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class ChainedActionTest implements CommandLineRunner {
    
    private final PromptState promptState;
    private final Action action;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("=== CHAINED ACTION TEST ===");
        log.info("Testing different approaches to chain find->click->type actions");
        
        // Create the transition with chained actions
        PromptToWorkingTransitionChained transition = 
            new PromptToWorkingTransitionChained(promptState, action);
        
        // Test 1: Separate chains approach
        log.info("\n--- TEST 1: Separate Chains ---");
        boolean separateResult = transition.executeWithSeparateChains();
        log.info("Separate chains result: {}", separateResult ? "SUCCESS" : "FAILED");
        
        // Wait a bit between tests
        Thread.sleep(2000);
        
        // Test 2: CONFIRM strategy approach
        log.info("\n--- TEST 2: CONFIRM Strategy ---");
        boolean confirmResult = transition.executeWithConfirmStrategy();
        log.info("CONFIRM strategy result: {}", confirmResult ? "SUCCESS" : "FAILED");
        
        // Wait a bit between tests
        Thread.sleep(2000);
        
        // Test 3: Custom coordination approach
        log.info("\n--- TEST 3: Custom Coordination ---");
        boolean customResult = transition.executeWithCustomCoordination();
        log.info("Custom coordination result: {}", customResult ? "SUCCESS" : "FAILED");
        
        // Summary
        log.info("\n=== TEST SUMMARY ===");
        log.info("Separate chains: {}", separateResult ? "✓" : "✗");
        log.info("CONFIRM strategy: {}", confirmResult ? "✓" : "✗");
        log.info("Custom coordination: {}", customResult ? "✓" : "✗");
        
        log.info("\n=== CHAINED ACTION TEST COMPLETE ===");
    }
}