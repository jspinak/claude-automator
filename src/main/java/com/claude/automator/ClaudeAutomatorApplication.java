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
}