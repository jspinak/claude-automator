package com.claude.automator;

import io.github.jspinak.brobot.config.ExecutionEnvironment;
import io.github.jspinak.brobot.config.FrameworkSettings;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify that ExecutionEnvironment correctly reflects mock mode status.
 */
@SpringBootTest
public class ExecutionEnvironmentTest {
    
    @Test
    public void testExecutionEnvironmentMockModeSync() {
        // Wait for Spring initialization to complete
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // ignore
        }
        
        System.out.println("\n=== EXECUTION ENVIRONMENT TEST ===");
        
        // Get the ExecutionEnvironment instance
        ExecutionEnvironment env = ExecutionEnvironment.getInstance();
        
        // Log current state
        System.out.println("ExecutionEnvironment info: " + env.getEnvironmentInfo());
        System.out.println("FrameworkSettings.mock = " + FrameworkSettings.mock);
        System.out.println("ExecutionEnvironment.isMockMode() = " + env.isMockMode());
        
        // Verify they are in sync
        assertEquals(FrameworkSettings.mock, env.isMockMode(), 
                    "ExecutionEnvironment mockMode should match FrameworkSettings.mock");
        
        // Since we have brobot.core.mock=true in application.properties
        assertTrue(env.isMockMode(), "ExecutionEnvironment should report mockMode=true");
        assertTrue(FrameworkSettings.mock, "FrameworkSettings.mock should be true");
        
        // Verify other mock-related settings
        assertFalse(env.canCaptureScreen(), "Should not capture screen in mock mode");
        assertFalse(env.useRealFiles(), "Should not use real files in mock mode");
        assertTrue(env.shouldSkipSikuliX(), "Should skip SikuliX in mock mode");
        
        System.out.println("\n✅ ExecutionEnvironment correctly reflects mock mode status!");
    }
}