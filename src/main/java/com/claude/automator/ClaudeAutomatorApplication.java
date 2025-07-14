package com.claude.automator;

import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = {"com.claude.automator", "io.github.jspinak.brobot"})
public class ClaudeAutomatorApplication {

    private static BrobotLogger brobotLogger;

    public static void main(String[] args) {
        log.info("Starting Claude Automator Application...");
        
        // Set headless mode based on environment before AWT initializes
        configureHeadlessMode();
        
        try {
            // Start Spring Boot application
            ConfigurableApplicationContext context = SpringApplication.run(ClaudeAutomatorApplication.class, args);
            
            // Get BrobotLogger from context
            brobotLogger = context.getBean(BrobotLogger.class);
            
            // Create a session for the entire application run
            try (var session = brobotLogger.session("claude-automator-" + System.currentTimeMillis())) {
                brobotLogger.observation("Claude Automator started successfully");
                brobotLogger.log()
                    .observation("Application context initialized")
                    .metadata("beanCount", context.getBeanDefinitionCount())
                    .metadata("activeProfiles", context.getEnvironment().getActiveProfiles())
                    .metadata("javaVersion", System.getProperty("java.version"))
                    .metadata("osName", System.getProperty("os.name"))
                    .log();
                
                // Log component scan results
                logComponentScanResults(context);
                
                // Keep application running
                log.info("Claude Automator is running. Press Ctrl+C to stop.");
                brobotLogger.observation("Application ready and monitoring started");
                
                // Add shutdown hook for cleanup logging
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    brobotLogger.observation("Shutdown initiated");
                    log.info("Shutting down Claude Automator...");
                }));
                
                // Keep the main thread alive
                Thread.currentThread().join();
            }
        } catch (Exception e) {
            log.error("Failed to start Claude Automator", e);
            if (brobotLogger != null) {
                brobotLogger.error("Application startup failed", e);
            }
            System.exit(1);
        }
    }
    
    private static void logComponentScanResults(ConfigurableApplicationContext context) {
        brobotLogger.log()
            .observation("Component scan completed")
            .metadata("automationBeans", context.getBeansOfType(com.claude.automator.automation.ClaudeMonitoringAutomation.class).size())
            .metadata("stateBeans", context.getBeansOfType(io.github.jspinak.brobot.model.state.State.class).size())
            .metadata("springBeans", context.getBeanDefinitionNames().length)
            .log();
    }
    
    private static void configureHeadlessMode() {
        String os = System.getProperty("os.name", "").toLowerCase();
        boolean isWindows = os.contains("windows");
        boolean isWSL = System.getenv("WSL_DISTRO_NAME") != null || 
                       System.getenv("WSL_INTEROP") != null;
        
        // Check for explicit profile override
        String profile = System.getProperty("spring.profiles.active");
        boolean hasWindowsProfile = profile != null && profile.contains("windows");
        boolean hasLinuxProfile = profile != null && profile.contains("linux");
        
        if (hasWindowsProfile) {
            // Explicitly using Windows profile - disable headless
            System.setProperty("java.awt.headless", "false");
            log.info("Windows profile detected - Setting java.awt.headless=false");
        } else if (hasLinuxProfile || isWSL || !isWindows) {
            // Linux/WSL environment - enable headless
            System.setProperty("java.awt.headless", "true");
            log.info("Linux/WSL environment detected - Setting java.awt.headless=true");
        } else if (isWindows) {
            // Windows environment without explicit profile - disable headless
            System.setProperty("java.awt.headless", "false");
            log.info("Windows environment detected - Setting java.awt.headless=false");
        }
        
        // Log the final decision
        log.info("Headless mode configuration: os.name={}, WSL={}, profile={}, java.awt.headless={}",
                os, isWSL, profile, System.getProperty("java.awt.headless"));
    }
}