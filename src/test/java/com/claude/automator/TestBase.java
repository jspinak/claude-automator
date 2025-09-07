package com.claude.automator;

import io.github.jspinak.brobot.capture.ScreenDimensions;
import io.github.jspinak.brobot.config.core.FrameworkSettings;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Base class for all Claude Automator tests.
 * Configures mock mode for headless environments.
 */
@SpringBootTest(classes = ClaudeAutomatorApplication.class)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
public abstract class TestBase {
    
    @BeforeAll
    public static void setupBeforeAll() {
        // Initialize screen dimensions for mock mode BEFORE Spring context loads
        ScreenDimensions.initialize("MOCK", 1920, 1080);
        
        // Enable mock mode globally
        FrameworkSettings.mock = true;
    }
    
    @BeforeEach
    public void setupTest() {
        // Enable mock mode for headless environments
        FrameworkSettings.mock = true;
        FrameworkSettings.moveMouseDelay = 0.01f;
        FrameworkSettings.pauseBeforeMouseDown = 0.01;
        FrameworkSettings.pauseAfterMouseDown = 0.01;
        FrameworkSettings.pauseBeforeMouseUp = 0.01;
        FrameworkSettings.pauseAfterMouseUp = 0.01;
    }
}