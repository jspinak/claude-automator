package com.claude.automator.config;

import com.claude.automator.mock.MockFindHandler;
import com.claude.automator.mock.MockInitialStateHandler;
import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.tools.testing.mock.state.MockStateManagement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

import jakarta.annotation.PostConstruct;

/**
 * Test profile configuration that ensures mock mode is properly activated.
 * This runs with high priority to override default settings.
 * Enables AspectJ for intercepting find operations in mock mode.
 */
@Configuration
@Profile("test")
@EnableAspectJAutoProxy
@Order(1)  // High priority
@Slf4j
public class TestProfileMockConfiguration {
    
    @Autowired(required = false)
    private MockStateManagement mockStateManagement;
    
    @PostConstruct
    public void ensureMockMode() {
        log.info("════════════════════════════════════════════════════════");
        log.info("  TEST PROFILE MOCK CONFIGURATION");
        log.info("════════════════════════════════════════════════════════");
        
        // Force mock mode for test profile
        FrameworkSettings.mock = true;
        log.info("✅ Mock mode FORCED to true for test profile");
        
        // Configure test-optimized settings
        configureTestSettings();
        
        // Configure mock state management if available
        if (mockStateManagement != null) {
            configureMockStates();
        }
        
        log.info("════════════════════════════════════════════════════════");
    }
    
    private void configureTestSettings() {
        // Fast mock execution times
        FrameworkSettings.mockTimeFindFirst = 0.01;
        FrameworkSettings.mockTimeFindAll = 0.02;
        FrameworkSettings.mockTimeClick = 0.005;
        FrameworkSettings.mockTimeDrag = 0.01;
        FrameworkSettings.mockTimeMove = 0.01;
        
        // Disable visual elements
        FrameworkSettings.drawFind = false;
        FrameworkSettings.drawClick = false;
        FrameworkSettings.drawDrag = false;
        FrameworkSettings.drawMove = false;
        FrameworkSettings.drawHighlight = false;
        FrameworkSettings.saveSnapshots = false;
        FrameworkSettings.saveHistory = false;
        
        // Remove pauses
        FrameworkSettings.pauseBeforeMouseDown = 0;
        FrameworkSettings.pauseAfterMouseDown = 0;
        FrameworkSettings.pauseBeforeMouseUp = 0;
        FrameworkSettings.pauseAfterMouseUp = 0;
        FrameworkSettings.moveMouseDelay = 0;
        
        log.info("✅ Test settings applied:");
        log.info("  - Mock timings: find={}, click={}", 
                 FrameworkSettings.mockTimeFindFirst, FrameworkSettings.mockTimeClick);
        log.info("  - Visual elements: DISABLED");
        log.info("  - Pauses: REMOVED");
    }
    
    private void configureMockStates() {
        // Set deterministic probabilities for reliable testing
        mockStateManagement.setStateProbabilities(100, "Prompt");
        mockStateManagement.setStateProbabilities(100, "Working");
        
        log.info("✅ Mock state probabilities set to 100% for deterministic testing");
    }
}