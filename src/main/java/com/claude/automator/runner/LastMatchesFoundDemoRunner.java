package com.claude.automator.runner;

import com.claude.automator.automation.ClaudeMonitoringAutomation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Demonstrates that lastMatchesFound mechanism is working.
 * Run with: java -jar build/libs/claude-automator-*.jar --spring.profiles.active=demo
 */
@Component
@Profile("demo")
@Order(2000) // Run after Brobot initialization
@RequiredArgsConstructor
@Slf4j
public class LastMatchesFoundDemoRunner implements ApplicationRunner {
    
    private final ClaudeMonitoringAutomation monitoring;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("==========================================================");
        log.info("         LASTMATCHESFOUND DEMO RUNNER STARTED");
        log.info("==========================================================");
        log.info("");
        log.info("This demo shows the lastMatchesFound mechanism working:");
        log.info("1. ClaudePrompt is searched and its location saved");
        log.info("2. ClaudeIcon uses that saved location for its search region");
        log.info("");
        log.info("Watch for these key log messages:");
        log.info("  ✓ 'Saved X matches to ClaudePrompt lastMatchesFound'");
        log.info("  ✓ 'Resolved X search regions for ClaudeIcon'");
        log.info("");
        
        // Small delay to ensure everything is ready
        Thread.sleep(2000);
        
        log.info(">>> STARTING MONITORING NOW <<<");
        log.info("");
        
        // Start monitoring which will run the test
        monitoring.startMonitoring();
        
        log.info("");
        log.info("==========================================================");
        log.info("              DEMO COMPLETE - CHECK LOGS ABOVE");
        log.info("==========================================================");
    }
}