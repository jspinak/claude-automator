package com.claude.automator.automation;

import com.claude.automator.states.WorkingState;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import io.github.jspinak.brobot.model.element.Region;
import org.sikuli.script.Screen;
import io.github.jspinak.brobot.navigation.transition.StateNavigator;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaudeMonitoringAutomation {

    private final StateService stateService;
    private final StateMemory stateMemory;
    private final StateNavigator stateNavigator;
    private final Action action;
    private final WorkingState workingState;
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private volatile boolean running = false;
    private Long workingStateId = null;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private boolean firstCheck = true;

    /**
     * Starts the automation that monitors for Claude icon disappearance.
     * Runs indefinitely until stopped.
     */
    @PostConstruct
    public void startMonitoring() {
        log.info("Starting Claude monitoring automation");
        System.out.println("=== CLAUDE MONITORING: Starting automation ===");
        running = true;
        
        // Get the working state ID after states have been registered
        // We'll retrieve it on first check since states are registered in ApplicationReadyEvent
        
        // Schedule monitoring task to run every 2 seconds
        scheduler.scheduleWithFixedDelay(this::checkClaudeIconStatus, 
                5, // initial delay
                2, // period between checks
                TimeUnit.SECONDS);
    }
    
    /**
     * Checks if Claude icon is visible and handles state transitions accordingly.
     */
    private void checkClaudeIconStatus() {
        if (!running) {
            return;
        }

        // Log check start time
        String timestamp = LocalDateTime.now().format(TIME_FORMAT);
        System.out.println("[" + timestamp + "] === CLAUDE MONITORING: Check started ===");
        
        // Check if running in a proper display environment
        boolean displayAvailable = false;
        try {
            String display = System.getenv("DISPLAY");
            if (display != null && !display.isEmpty()) {
                Screen screen = new Screen();
                if (screen.getBounds().width > 0 && screen.getBounds().height > 0) {
                    displayAvailable = true;
                    System.out.println("[" + timestamp + "] Display detected: " + screen.getBounds());
                } else {
                    System.out.println("[" + timestamp + "] Display has zero dimensions - X11 server may not be running");
                }
            } else {
                System.out.println("[" + timestamp + "] DISPLAY environment variable not set");
            }
        } catch (Exception e) {
            System.out.println("[" + timestamp + "] Cannot access display: " + e.getMessage());
        }
        
        // Only attempt highlight if display is available
        if (displayAvailable) {
            // Highlighting would work here in a proper GUI environment
            System.out.println("[" + timestamp + "] Display available - highlighting would work in GUI environment");
        } else {
            if (firstCheck) {
                System.out.println("[" + timestamp + "] Skipping highlight - no display available");
                System.out.println("[" + timestamp + "] To enable highlighting:");
                System.out.println("  - On Windows: Run directly in Windows (not WSL)");
                System.out.println("  - On WSL: Install and run X11 server (VcXsrv, Xming, etc.)");
                System.out.println("  - On Linux: Ensure X11/Wayland session is active");
                firstCheck = false;
            }
        }
        
        try {
            // Get working state ID if not already retrieved
            if (workingStateId == null) {
                workingStateId = stateService.getStateId("Working");
                if (workingStateId == null) {
                    log.error("Working state not found in StateService - states may not be registered yet");
                    return;
                }
                log.info("Retrieved Working state ID: {}", workingStateId);
            }
            
            // Check if Working state is active
            if (!stateMemory.getActiveStates().contains(workingStateId)) {
                log.info("Working state is not active, attempting to navigate to it");
                
                // Try to navigate to Working state (will trigger Prompt->Working transition if needed)
                boolean success = stateNavigator.openState("Working");
                
                if (success) {
                    log.info("Successfully navigated to Working state");
                } else {
                    log.warn("Failed to navigate to Working state");
                }
                
                return; // Skip icon check this iteration
            }
            
            // Quick check if icon exists (0.5 second timeout)
            PatternFindOptions quickFind = new PatternFindOptions.Builder()
                    .setPauseBeforeBegin(0.5)
                    .build();
            
            boolean iconFound = action.perform(quickFind, workingState.getClaudeIcon()).isSuccess();
            
            if (!iconFound) {
                log.info("Claude icon disappeared - removing Working state and reopening");
                
                // Remove Working as active state
                stateMemory.removeInactiveState(workingStateId);
                
                // Navigate back to Working state on next check
                // (We let the next iteration handle navigation to avoid rapid transitions)
                
            } else {
                log.debug("Claude icon still visible");
            }
            
        } catch (Exception e) {
            log.error("Error during Claude icon monitoring", e);
        }
    }
    
    /**
     * Stops the monitoring automation.
     */
    public void stopMonitoring() {
        log.info("Stopping Claude monitoring automation");
        running = false;
        scheduler.shutdown();
        
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}