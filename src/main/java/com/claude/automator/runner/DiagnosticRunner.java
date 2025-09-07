package com.claude.automator.runner;

import com.claude.automator.diagnostics.LastMatchesFoundDiagnostic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Runs the lastMatchesFound diagnostic when the 'diagnostic' profile is active.
 * 
 * Run with: java -jar build/libs/claude-automator-*.jar --spring.profiles.active=diagnostic
 */
@Component
@Profile("diagnostic")
@Order(2000) // Run after Brobot initialization
@RequiredArgsConstructor
@Slf4j
public class DiagnosticRunner implements ApplicationRunner {
    
    private final LastMatchesFoundDiagnostic diagnostic;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Starting diagnostic runner...");
        
        // Wait for everything to initialize
        Thread.sleep(3000);
        
        // Run the comprehensive diagnostic
        diagnostic.runComprehensiveDiagnostic();
        
        // Exit after diagnostic
        log.info("Diagnostic complete. Exiting...");
        System.exit(0);
    }
}