package com.claude.automator.config;

import io.github.jspinak.brobot.capture.ScreenDimensions;
import io.github.jspinak.brobot.config.core.FrameworkSettings;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Test configuration that sets up mock mode before other initializers run.
 */
@Configuration
@Profile("test")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TestConfiguration {
    
    static {
        // Set up mock mode very early, before Spring initializers run
        System.setProperty("java.awt.headless", "true");
        FrameworkSettings.mock = true;
        
        // Pre-initialize screen dimensions to avoid AWT calls
        try {
            // Use reflection to bypass the initialization check
            var field = ScreenDimensions.class.getDeclaredField("initialized");
            field.setAccessible(true);
            field.set(null, false);
            
            // Now initialize with mock dimensions
            ScreenDimensions.initialize("MOCK", 1920, 1080);
        } catch (Exception e) {
            // If reflection fails, try normal initialization
            ScreenDimensions.initialize("MOCK", 1920, 1080);
        }
    }
    
    @PostConstruct
    public void init() {
        // Ensure mock mode is set
        FrameworkSettings.mock = true;
    }
}