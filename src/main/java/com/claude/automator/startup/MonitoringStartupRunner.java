package com.claude.automator.startup;

import com.claude.automator.automation.ClaudeMonitoringAutomation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Starts the monitoring automation after Spring context is fully initialized.
 * This ensures all beans are properly wired and Brobot is fully initialized
 * before starting the monitoring loop.
 * 
 * <p>Using ApplicationRunner instead of @PostConstruct gives us better control
 * over the initialization order and ensures the monitoring starts after all
 * other initialization is complete.</p>
 */
@Component
@Profile({"default", "windows", "linux"}) // Only run in production profiles
@Order(1000) // Run after Brobot initialization (which is typically at order 100-500)
@RequiredArgsConstructor
@Slf4j
public class MonitoringStartupRunner implements ApplicationRunner {
    
    private final ClaudeMonitoringAutomation monitoringAutomation;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("=== MONITORING STARTUP RUNNER ===");
        log.info("Starting Claude monitoring automation...");
        
        // Add a small delay to ensure everything is fully initialized
        Thread.sleep(2000);
        
        log.info("Initiating monitoring loop...");
        monitoringAutomation.startMonitoring();
        
        log.info("Monitoring startup complete");
    }
}