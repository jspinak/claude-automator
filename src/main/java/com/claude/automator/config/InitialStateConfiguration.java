package com.claude.automator.config;

import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.startup.BrobotStartupConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Configures the initial state for Brobot startup.
 * This ensures that the correct initial state (Prompt) is activated
 * when the application starts, especially in mock mode.
 */
@Configuration
@Slf4j
public class InitialStateConfiguration {
    
    /**
     * Configures BrobotStartupConfiguration to verify Prompt as the initial state.
     * This fixes the issue where Working state was being activated instead of Prompt.
     */
    @Bean
    public BrobotStartupConfiguration brobotStartupConfiguration() {
        log.info("Configuring Brobot startup with initial state verification");
        
        BrobotStartupConfiguration config = new BrobotStartupConfiguration();
        
        // Enable initial state verification
        config.setVerifyInitialStates(true);
        
        // Add Prompt as the initial state to verify
        // The state name must match the class name without "State" suffix
        config.getInitialStates().add("Prompt");
        
        // Don't add Working - it should only activate after transition
        
        // Set a small delay to ensure all beans are initialized
        config.setStartupDelay(1);
        
        // Don't fall back to searching all states
        config.setFallbackSearch(false);
        
        // Activate the first found state (Prompt)
        config.setActivateFirstOnly(true);
        
        log.info("Initial state configuration: {}", config.getInitialStates());
        
        return config;
    }
    
    /**
     * Test profile specific configuration that ensures deterministic behavior.
     */
    @Configuration
    @Profile("test")
    @Slf4j
    public static class TestInitialStateConfiguration {
        
        @Bean
        @Primary  // This bean takes precedence in test profile
        public BrobotStartupConfiguration testBrobotStartupConfiguration() {
            log.info("═══════════════════════════════════════════════════════");
            log.info("  TEST INITIAL STATE CONFIGURATION");
            log.info("═══════════════════════════════════════════════════════");
            
            BrobotStartupConfiguration config = new BrobotStartupConfiguration();
            
            // Enable initial state verification
            config.setVerifyInitialStates(true);
            
            // In test/mock mode, always start with Prompt
            config.getInitialStates().add("Prompt");
            
            // No startup delay in tests
            config.setStartupDelay(0);
            
            // Don't fall back to searching all states
            config.setFallbackSearch(false);
            
            // Activate only the first found state
            config.setActivateFirstOnly(true);
            
            log.info("✓ Test profile initial state: Prompt");
            log.info("✓ Initial state verification: ENABLED");
            log.info("✓ Fallback search: DISABLED");
            log.info("✓ Startup delay: 0 seconds");
            
            log.info("═══════════════════════════════════════════════════════");
            
            return config;
        }
    }
}