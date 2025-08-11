package com.claude.automator.automation;

import io.github.jspinak.brobot.navigation.monitoring.StateAwareScheduler;
import io.github.jspinak.brobot.navigation.monitoring.StateAwareScheduler.StateCheckConfiguration;
import io.github.jspinak.brobot.navigation.transition.StateNavigator;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.action.ObjectCollection;
import com.claude.automator.states.WorkingState;
import io.github.jspinak.brobot.util.image.debug.CaptureDebugger;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.element.Positions;
import org.sikuli.script.Screen;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Monitors Claude AI interface states and manages transitions.
 * Uses StateAwareScheduler to ensure proper state verification before each cycle.
 * Relies entirely on Brobot's state management system for navigation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClaudeMonitoringAutomation {

    private final StateNavigator stateNavigator;
    private final StateAwareScheduler stateAwareScheduler;
    private final StateService stateService;
    private final StateMemory stateMemory;
    private final Action action;
    private final WorkingState workingState;
    
    // Optional debugger - not required for normal operation
    @Autowired(required = false)
    private CaptureDebugger captureDebugger;
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> scheduledTask;
    
    @Value("${claude.automator.monitoring.initial-delay:5}")
    private int initialDelay;
    
    @Value("${claude.automator.monitoring.check-interval:2}")
    private int checkInterval;
    
    @Value("${claude.automator.monitoring.icon-timeout:5}")
    private int iconTimeout;
    
    // Use LinkedHashSet to maintain order from properties while ensuring uniqueness
    @Value("${claude.automator.monitoring.required-states:Prompt,Working}")
    private List<String> requiredStates;
    
    @Value("${claude.automator.monitoring.max-iterations:5}")
    private int maxIterations;
    
    @Value("${claude.automator.monitoring.debug-capture:false}")
    private boolean debugCaptureEnabled;

    @PostConstruct
    public void startMonitoring() {
        // Log the search region configuration for debugging
        log.info("WorkingState ClaudeIcon search region config: {}", 
                workingState.getClaudeIcon().getSearchRegionOnObject());
        
        log.info("Starting monitoring with max iterations: {}", maxIterations);
        
        // Configure state checking - ensures Prompt and Working states exist
        StateCheckConfiguration stateConfig = new StateCheckConfiguration.Builder()
                .withRequiredStates(requiredStates)
                .withRebuildOnMismatch(true)
                .withSkipIfStatesMissing(false)
                .withMaxIterations(maxIterations)
                .build();
        
        // Schedule monitoring with automatic state verification and iteration limit
        scheduledTask = stateAwareScheduler.scheduleWithStateCheck(
                scheduler,
                this::monitorClaudeStates,
                stateConfig,
                initialDelay,
                checkInterval,
                TimeUnit.SECONDS
        );
        
        // Add shutdown hook to stop when iterations complete
        scheduler.execute(() -> {
            try {
                scheduledTask.get(); // Wait for completion
                log.info("Monitoring completed after {} iterations", maxIterations);
                stopMonitoring();
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("Max iterations reached")) {
                    log.info("Monitoring stopped after reaching max iterations");
                    stopMonitoring();
                }
            }
        });
    }

    /**
     * Monitors Claude states and manages transitions.
     * - If only Prompt is active: navigate to Working (triggers transition)
     * - If Working is active: check if icon still exists
     * - If icon disappears: remove Working state to restart cycle
     */
    private void monitorClaudeStates() {
        var activeStates = stateMemory.getActiveStateNames();
        
        if (activeStates.contains("Working")) {
            // Working state is active - check if icon still visible
            checkWorkingIcon();
        } else if (activeStates.contains("Prompt")) {
            // Only Prompt is active - navigate to Working (triggers transition)
            log.debug("Prompt state active, attempting to navigate to Working state");
            boolean success = stateNavigator.openState("Working");
            log.debug("Navigation to Working state: {}", success ? "SUCCESS" : "FAILED");
        }
        // If neither state is active, do nothing - let state verification handle it
    }
    
    /**
     * Checks if Claude icon is still visible in Working state.
     * If not found, removes Working state to return to Prompt state only.
     */
    private void checkWorkingIcon() {
        // Log search region info
        var searchRegions = workingState.getClaudeIcon().getPatterns().stream()
                .map(p -> p.getSearchRegions().getFixedRegion())
                .filter(r -> r.isDefined())
                .findFirst();
        log.debug("ClaudeIcon search region: {}", searchRegions.orElse(null));
        
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setSearchDuration(iconTimeout)
                .setSimilarity(0.85) // Lower threshold since we're getting 0.898 scores
                .build();
        
        ActionResult result = action.perform(findOptions, workingState.getClaudeIcon());
        
        if (result.isSuccess()) {
            log.debug("Claude icon found with {} matches", result.getMatchList().size());
        } else {
            log.debug("Claude icon not found, removing Working state");
            
            // Run debug capture if enabled and debugger is available
            if (debugCaptureEnabled && captureDebugger != null) {
                log.info("Running capture debug to diagnose pattern matching issue");
                Region searchRegion = searchRegions.orElse(new Region(0, 0, 1920, 1080));
                String patternPath = "images/claude-icon.png";
                captureDebugger.debugCapture(searchRegion, patternPath);
            }
            // Icon not found - remove Working state
            // This leaves only Prompt state active, allowing transition to trigger next cycle
            stateMemory.removeInactiveState("working");
        }
        // If icon found, do nothing - stay in Working state
    }
    
    public void stopMonitoring() {
        // Move mouse to center of screen when monitoring completes
        moveMouseToCenter();
        
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
    
    /**
     * Moves the mouse to the center of the primary screen using Region and Position.
     */
    private void moveMouseToCenter() {
        // Use Position.MIDDLEMIDDLE to represent the center
        Position centerPosition = new Position(Positions.Name.MIDDLEMIDDLE);
        
        // Create a Location from the Region and Position
        Location centerLocation = new Location(new Region(), centerPosition);
        
        // Create move options with descriptive logging through ActionConfig methods
        MouseMoveOptions moveOptions = new MouseMoveOptions.Builder()
                .setMoveMouseDelay(0.5f) // Smooth movement
                .withBeforeActionLog("Moving mouse to center of screen")
                .withSuccessLog("Successfully moved mouse to center of screen")
                .withFailureLog("Failed to move mouse to center of screen")
                .build();
        
        // Create object collection with the center location
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withLocations(centerLocation)
                .build();
        
        // Perform the move action - logging will be handled automatically
        action.perform(moveOptions, objectCollection);
    }
}