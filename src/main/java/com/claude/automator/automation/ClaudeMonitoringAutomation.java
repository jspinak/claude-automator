package com.claude.automator.automation;

import io.github.jspinak.brobot.navigation.monitoring.StateAwareScheduler;
import io.github.jspinak.brobot.navigation.monitoring.StateAwareScheduler.StateCheckConfiguration;
import io.github.jspinak.brobot.navigation.transition.StateNavigator;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.vanish.VanishOptions;
import io.github.jspinak.brobot.action.ConditionalActionChain;
import io.github.jspinak.brobot.action.ObjectCollection;
import com.claude.automator.states.WorkingState;
import com.claude.automator.states.PromptState;
import com.claude.automator.diagnostics.BrobotScreenCaptureDiagnostic;
import io.github.jspinak.brobot.util.image.debug.CaptureDebugger;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Positions;
import io.github.jspinak.brobot.model.element.Region;

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
 * 
 * <p>
 * This class demonstrates modern Brobot patterns including:
 * </p>
 * <ul>
 * <li>StateAwareScheduler for automatic state verification</li>
 * <li>ConditionalActionChain for elegant UI interactions</li>
 * <li>Configuration-driven behavior via application.properties</li>
 * <li>Clean separation of monitoring logic from state definitions</li>
 * </ul>
 * 
 * @see WorkingState
 * @see PromptState
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClaudeMonitoringAutomation {

    private final StateNavigator stateNavigator;
    private final StateAwareScheduler stateAwareScheduler;
    private final StateMemory stateMemory;
    private final Action action;
    private final WorkingState workingState;
    private final PromptState promptState;

    // Optional debugger - not required for normal operation
    @Autowired(required = false)
    private CaptureDebugger captureDebugger;
    
    // Diagnostic component for screenshot capture
    @Autowired(required = false)
    private BrobotScreenCaptureDiagnostic screenCaptureDiagnostic;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> scheduledTask;

    @Value("${claude.automator.monitoring.initial-delay:5}")
    private int initialDelay;

    @Value("${claude.automator.monitoring.check-interval:2}")
    private int checkInterval;

    @Value("${claude.automator.monitoring.icon-timeout:5}")
    private int iconTimeout;

    // Only require Prompt state initially - Working will be found through transition
    // This ensures ClaudePrompt is found first, establishing the search region for ClaudeIcon
    @Value("${claude.automator.monitoring.required-states:Prompt}")
    private List<String> requiredStates;

    @Value("${claude.automator.monitoring.max-iterations:2}")
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
                TimeUnit.SECONDS);
        
        // Capture diagnostic screenshot after scheduling is done
        if (screenCaptureDiagnostic != null) {
            log.info("Capturing diagnostic screenshot after scheduling...");
            scheduler.schedule(() -> {
                screenCaptureDiagnostic.captureDiagnosticScreenshot("after_scheduling");
            }, initialDelay + 1, TimeUnit.SECONDS); // Capture 1 second after initial delay
        }

        // Schedule a separate task to stop monitoring after max iterations time
        // This ensures stopMonitoring is called even if the scheduler doesn't complete
        // normally
        long totalDuration = initialDelay + (checkInterval * maxIterations);
        scheduler.schedule(() -> {
            log.info("Monitoring duration reached ({} seconds), stopping", totalDuration);
            stopMonitoring();
        }, totalDuration + 2, TimeUnit.SECONDS); // Add 2 seconds buffer
    }

    /**
     * Monitors Claude states and manages transitions using modern patterns.
     * 
     * <p>
     * State flow:
     * </p>
     * <ol>
     * <li>If Working state is active: check if icon still exists</li>
     * <li>If Prompt state is active: navigate to Working (triggers transition)</li>
     * <li>If neither state is active: let state verification handle it</li>
     * </ol>
     */
    private void monitorClaudeStates() {
        var activeStates = stateMemory.getActiveStateNames();

        if (activeStates.contains("Working")) {
            // Working state is active - verify icon still visible
            checkWorkingIconWithConditionalChain();
        } else if (activeStates.contains("Prompt")) {
            // Only Prompt is active - navigate to Working (triggers transition)
            navigateToWorkingState();
        }
        // If neither state is active, StateAwareScheduler will handle rebuilding
    }

    /**
     * Navigates from Prompt to Working state using modern pattern.
     */
    private void navigateToWorkingState() {
        log.debug("Prompt state active, attempting to navigate to Working state");

        // Use action.find for verification before navigation
        // State activation happens automatically in the Action framework
        ActionResult promptFound = action.find(promptState.getClaudePrompt());

        if (promptFound.isSuccess()) {
            log.debug("Prompt confirmed at {}, navigating to Working",
                    promptFound.getMatchList().get(0).getRegion());
            boolean success = stateNavigator.openState("Working");
            log.debug("Navigation to Working state: {}", success ? "SUCCESS" : "FAILED");
        } else {
            log.debug("Prompt not found, skipping navigation");
        }
    }

    /**
     * Checks if Claude icon is still visible using ConditionalActionChain.
     * 
     * <p>
     * This method demonstrates the modern conditional chain pattern for
     * cleaner error handling and state management.
     * </p>
     */
    private void checkWorkingIconWithConditionalChain() {
        // Build find options with configuration from properties
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setSearchDuration(iconTimeout)
                .setSimilarity(0.85) // Configured for Claude icon detection
                .build();

        // Use ConditionalActionChain for elegant conditional execution
        ConditionalActionChain
                .find(findOptions)
                .ifFoundDo(result -> {
                    log.debug("Claude icon found with {} matches at {}",
                            result.getMatchList().size(),
                            result.getMatchList().get(0).getRegion());
                })
                .ifNotFoundDo(result -> {
                    log.debug("Claude icon not found, transitioning back to Prompt state");
                    handleIconDisappearance();
                })
                .perform(action, new ObjectCollection.Builder()
                        .withImages(workingState.getClaudeIcon())
                        .build());
    }

    /**
     * Handles the disappearance of the Claude icon.
     * 
     * <p>
     * This method encapsulates the logic for when the Working state
     * should transition back to Prompt state.
     * </p>
     */
    private void handleIconDisappearance() {
        // Run debug capture if enabled
        if (debugCaptureEnabled && captureDebugger != null) {
            runDebugCapture();
        }

        // Remove Working state to return to Prompt state
        // This allows the transition cycle to restart
        stateMemory.removeInactiveState("working");
        log.info("Removed Working state, returning to Prompt state");
    }

    /**
     * Runs debug capture for pattern matching diagnostics.
     */
    private void runDebugCapture() {
        log.info("Running capture debug to diagnose pattern matching issue");

        // Get the search region from the icon's configuration
        Region searchRegion = workingState.getClaudeIcon().getPatterns().stream()
                .map(p -> p.getSearchRegions().getFixedRegion())
                .filter(Region::isDefined)
                .findFirst()
                .orElse(Region.builder().fullScreen().build());

        String patternPath = "images/working/claude-icon-1.png";
        captureDebugger.debugCapture(searchRegion, patternPath);
    }

    /**
     * Alternative implementation using VanishOptions for icon monitoring.
     * 
     * <p>
     * This method demonstrates using VanishOptions to detect when
     * an element disappears from the screen.
     * </p>
     * 
     * @deprecated Use checkWorkingIconWithConditionalChain instead
     */
    @Deprecated
    private void checkWorkingIconWithVanish() {
        // VanishOptions waits for an element to disappear
        VanishOptions vanishOptions = new VanishOptions.Builder()
                .setSearchDuration(iconTimeout) // Use setSearchDuration instead of setWaitTime
                .build();

        ActionResult vanishResult = action.perform(vanishOptions, workingState.getClaudeIcon());

        if (vanishResult.isSuccess()) {
            log.debug("Claude icon vanished after {} ms", vanishResult.getDuration());
            handleIconDisappearance();
        } else {
            log.debug("Claude icon still present after {} seconds", iconTimeout);
        }
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
     * Moves the mouse to the center of the primary screen using idiomatic Brobot
     * patterns.
     * 
     * <p>
     * This method demonstrates the proper use of Positions.Name for screen
     * positioning,
     * which automatically adapts to any screen resolution.
     * </p>
     */
    private void moveMouseToCenter() {
        try {
            // Use idiomatic Brobot pattern for screen center
            // This automatically calculates center based on current screen dimensions
            Location centerLocation = new Location(Positions.Name.MIDDLEMIDDLE);

            log.info("Moving mouse to center of screen using Positions.Name.MIDDLEMIDDLE");

            // Use the Action.perform convenience method with ActionType.MOVE
            ActionResult moveResult = action.perform(ActionType.MOVE, centerLocation);

            if (moveResult.isSuccess()) {
                log.info("Successfully moved mouse to center of screen at {}", centerLocation);
            } else {
                log.warn("Failed to move mouse to center of screen");
            }
        } catch (Exception e) {
            log.error("Error moving mouse to center of screen", e);
        }
    }

    /**
     * Alternative implementation using Region builder for center calculation.
     * 
     * <p>
     * This shows how to use Region builder to create screen-aware regions
     * that adapt to different resolutions.
     * </p>
     */
    private void moveMouseToCenterUsingRegion() {
        // Create a region at the center of the screen
        Region centerRegion = Region.builder()
                .withScreenPercentage(0.45, 0.45, 0.1, 0.1) // 10% region centered at screen
                .build();

        // Calculate center point of the region
        int centerX = centerRegion.x() + centerRegion.w() / 2;
        int centerY = centerRegion.y() + centerRegion.h() / 2;

        Location centerLocation = new Location(centerX, centerY);

        ActionResult moveResult = action.perform(ActionType.MOVE, centerLocation);

        if (moveResult.isSuccess()) {
            log.info("Mouse moved to center: {}", centerLocation);
        } else {
            log.warn("Failed to move mouse to center");
        }
    }
}