package com.claude.automator;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Runner class that starts the Claude Automator application with the test profile
 * for profile-based mock execution.
 * 
 * This demonstrates running the full application in mock mode using Spring profiles
 * instead of runtime delegation.
 */
public class ProfileBasedMockRunner {
    
    public static void main(String[] args) {
        System.out.println("════════════════════════════════════════════════════════");
        System.out.println("  PROFILE-BASED MOCK RUNNER");
        System.out.println("  Starting Claude Automator with test profile");
        System.out.println("════════════════════════════════════════════════════════");
        
        // Create Spring application
        SpringApplication app = new SpringApplication(ClaudeAutomatorApplication.class);
        
        // Set the test profile to activate mock mode
        app.setAdditionalProfiles("test");
        
        // Add shutdown hook for clean exit
        app.setRegisterShutdownHook(true);
        
        // Run the application
        ConfigurableApplicationContext context = app.run(args);
        
        // Log active profiles
        ConfigurableEnvironment env = context.getEnvironment();
        System.out.println("\nActive profiles: " + String.join(", ", env.getActiveProfiles()));
        System.out.println("Mock mode enabled: " + env.getProperty("brobot.framework.mock"));
        
        // The application will run until interrupted
        System.out.println("\n✅ Claude Automator running in profile-based mock mode");
        System.out.println("Press Ctrl+C to exit\n");
    }
}