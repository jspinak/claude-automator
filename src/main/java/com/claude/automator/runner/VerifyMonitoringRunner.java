package com.claude.automator.runner;

import com.claude.automator.automation.ClaudeMonitoringAutomation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Runner to verify the ClaudeMonitoringAutomation with lastMatchesFound fix.
 * 
 * Run with: java -jar build/libs/claude-automator-*.jar --spring.profiles.active=verify-original
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class VerifyMonitoringRunner {
    
    private final ClaudeMonitoringAutomation monitoring;
    
    @Bean
    @Profile("verify-original")
    CommandLineRunner verifyRunner() {
        return args -> {
            log.info("=== VERIFY MONITORING RUNNER STARTED ===");
            log.info("");
            log.info("Verifying Original ClaudeMonitoringAutomation with lastMatchesFound Fix");
            log.info("");
            log.info("Expected behavior:");
            log.info("1. ClaudePrompt will be found first (in navigateToWorkingState)");
            log.info("2. Matches will be saved to ClaudePrompt.lastMatchesFound");
            log.info("3. ClaudeIcon will be searched (in checkWorkingIconWithConditionalChain)");
            log.info("4. DynamicRegionResolver will use ClaudePrompt.lastMatchesFound");
            log.info("5. ClaudeIcon search will be constrained to resolved regions");
            log.info("");
            log.info("Look for these log messages:");
            log.info("  - 'NAVIGATE TO WORKING STATE'");
            log.info("  - 'ClaudePrompt FOUND'");
            log.info("  - 'Saved X matches to ClaudePrompt lastMatchesFound'");
            log.info("  - 'CHECK WORKING ICON'");
            log.info("  - 'Resolved X search regions for ClaudeIcon from ClaudePrompt lastMatchesFound'");
            log.info("");
            
            // Let Spring context fully initialize
            Thread.sleep(2000);
            
            log.info("Starting monitoring...");
            monitoring.startMonitoring();
            
            log.info("");
            log.info("=== MONITORING STARTED - CHECK LOGS ABOVE ===");
        };
    }
}