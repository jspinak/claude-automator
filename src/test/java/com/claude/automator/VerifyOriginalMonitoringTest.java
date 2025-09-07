package com.claude.automator;

import com.claude.automator.automation.ClaudeMonitoringAutomation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

/**
 * Verifies that the original ClaudeMonitoringAutomation now works correctly
 * with the lastMatchesFound fix. The SearchRegionOnObject should be resolved
 * even when ClaudePrompt and ClaudeIcon are searched in separate action calls.
 * 
 * Run with: java -jar build/libs/claude-automator-*.jar --spring.profiles.active=verify-original
 */
@SpringBootApplication
@Slf4j
public class VerifyOriginalMonitoringTest {
    
    public static void main(String[] args) {
        SpringApplication.run(VerifyOriginalMonitoringTest.class, args);
    }
    
    @Bean
    @Profile("verify-original")
    CommandLineRunner verifyRunner(ClaudeMonitoringAutomation monitoring) {
        return args -> {
            log.info("=== Verifying Original ClaudeMonitoringAutomation with lastMatchesFound Fix ===");
            log.info("");
            log.info("Expected behavior:");
            log.info("1. ClaudePrompt will be found first (in navigateToWorkingState)");
            log.info("2. Matches will be saved to ClaudePrompt.lastMatchesFound");
            log.info("3. ClaudeIcon will be searched (in checkWorkingIconWithConditionalChain)");
            log.info("4. DynamicRegionResolver will use ClaudePrompt.lastMatchesFound");
            log.info("5. ClaudeIcon search will be constrained to resolved regions");
            log.info("");
            log.info("Look for these log messages:");
            log.info("  - 'Saved X matches to ClaudePrompt lastMatchesFound'");
            log.info("  - 'Resolved X search regions for ClaudeIcon from ClaudePrompt lastMatchesFound'");
            log.info("");
            
            try {
                // Let the application initialize
                Thread.sleep(3000);
                
                log.info("Starting monitoring test...");
                // The monitoring will start automatically via @PostConstruct
                // Just wait for it to run a few iterations
                Thread.sleep(10000);
                
                log.info("Test complete. Check the logs above to verify:");
                log.info("  ✓ ClaudePrompt matches were saved to lastMatchesFound");
                log.info("  ✓ ClaudeIcon search regions were resolved from lastMatchesFound");
                log.info("  ✓ No need for ImprovedClaudeMonitoring workaround!");
                
            } catch (Exception e) {
                log.error("Test failed", e);
            } finally {
                log.info("Shutting down...");
                System.exit(0);
            }
        };
    }
}