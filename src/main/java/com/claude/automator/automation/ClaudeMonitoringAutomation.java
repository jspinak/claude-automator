package com.claude.automator.automation;

import com.claude.automator.states.PromptState;
import com.claude.automator.states.WorkingState;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.tools.logging.visual.HighlightManager;
import org.sikuli.script.Screen;
import io.github.jspinak.brobot.navigation.transition.StateNavigator;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateMemory;

import java.awt.Rectangle;
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
    private final PromptState promptState;
    private final BrobotLogger brobotLogger;
    private final HighlightManager highlightManager;
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private volatile boolean running = false;
    private Long workingStateId = null;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private boolean firstCheck = true;
    private int checkCount = 0;
    private long lastCheckDuration = 0;

    /**
     * Starts the automation that monitors for Claude icon disappearance.
     * Runs indefinitely until stopped.
     */
    @PostConstruct
    public void startMonitoring() {
        log.info("Starting Claude monitoring automation");
        System.out.println("=== CLAUDE MONITORING: Starting automation ===");
        
        brobotLogger.log()
            .observation("Claude monitoring automation starting")
            .metadata("initialDelay", 5)
            .metadata("checkInterval", 2)
            .metadata("timeUnit", "SECONDS")
            .log();
        
        running = true;
        
        // Get the working state ID after states have been registered
        // We'll retrieve it on first check since states are registered in ApplicationReadyEvent
        
        // Schedule monitoring task to run every 2 seconds
        scheduler.scheduleWithFixedDelay(this::getClaudeWorking,
                5, // initial delay
                2, // period between checks
                TimeUnit.SECONDS);
                
        brobotLogger.observation("Monitoring scheduler started successfully");
    }

    private void getClaudeWorking() {
        if (!running) {
            brobotLogger.observation("getClaudeWorking skipped - automation stopped");
            return;
        }

        if (claudePromptIsVisible()) checkClaudeIconStatus();
    }

    private boolean claudePromptIsVisible() {
        PatternFindOptions patternFindOptions = new PatternFindOptions.Builder()
                .setPauseBeforeBegin(0.5)
                .build();
        return action.perform(patternFindOptions, promptState.getClaudePrompt()).isSuccess();
    }
    
    /**
     * Checks if Claude icon is visible and handles state transitions accordingly.
     */
    private void checkClaudeIconStatus() {
        checkCount++;
        long checkStartTime = System.currentTimeMillis();
        
        try (AutoCloseable operation = brobotLogger.operation("ClaudeIconCheck-" + checkCount)) {
            // Log check start time
            String timestamp = LocalDateTime.now().format(TIME_FORMAT);
            System.out.println("[" + timestamp + "] === CLAUDE MONITORING: Check started ===");
            
            brobotLogger.log()
                .observation("Starting monitoring check")
                .metadata("checkNumber", checkCount)
                .metadata("timestamp", timestamp)
                .metadata("previousCheckDuration", lastCheckDuration)
                .log();
            
            // Check if running in a proper display environment
            boolean displayAvailable = checkDisplayEnvironment(timestamp);
            
            // Only attempt highlight if display is available
            if (displayAvailable) {
                // Highlighting would work here in a proper GUI environment
                System.out.println("[" + timestamp + "] Display available - highlighting would work in GUI environment");
                brobotLogger.observation("Display available for visual operations");
            } else {
                if (firstCheck) {
                    System.out.println("[" + timestamp + "] Skipping highlight - no display available");
                    System.out.println("[" + timestamp + "] To enable highlighting:");
                    System.out.println("  - On Windows: Run directly in Windows (not WSL)");
                    System.out.println("  - On WSL: Install and run X11 server (VcXsrv, Xming, etc.)");
                    System.out.println("  - On Linux: Ensure X11/Wayland session is active");
                    
                    brobotLogger.log()
                        .observation("Display not available - visual operations disabled")
                        .metadata("firstCheck", true)
                        .metadata("displayInstructions", "See console output for display setup instructions")
                        .log();
                    
                    firstCheck = false;
                }
            }
            
            try {
                // Get working state ID if not already retrieved
                if (workingStateId == null) {
                    retrieveWorkingStateId();
                }
                
                // BYPASS STATE CHECK FOR NOW - directly check icon visibility
                // This allows us to see highlighting even if state navigation fails
                log.info("Bypassing state check - directly checking icon visibility");
                
                // Perform icon visibility check regardless of state
                checkIconVisibility();
                
            } catch (Exception e) {
                log.error("Error during Claude icon monitoring", e);
                brobotLogger.log()
                    .error(e)
                    .message("Error during Claude icon monitoring")
                    .metadata("checkNumber", checkCount)
                    .metadata("errorType", e.getClass().getSimpleName())
                    .log();
            }
            
            lastCheckDuration = System.currentTimeMillis() - checkStartTime;
            
            brobotLogger.log()
                .observation("Monitoring check completed")
                .metadata("checkDuration", lastCheckDuration)
                .metadata("checkNumber", checkCount)
                .log();
        } catch (Exception e) {
            log.error("Error with operation logging", e);
            brobotLogger.error("Error with operation logging", e);
        }
    }
    
    private boolean checkDisplayEnvironment(String timestamp) {
        boolean displayAvailable = false;
        
        try (var timer = brobotLogger.startTimer("DisplayEnvironmentCheck")) {
            String os = System.getProperty("os.name", "").toLowerCase();
            boolean isWindows = os.contains("windows");
            boolean isWSL = System.getenv("WSL_DISTRO_NAME") != null || 
                           System.getenv("WSL_INTEROP") != null;
            String display = System.getenv("DISPLAY");
            
            brobotLogger.log()
                .observation("Checking display environment")
                .metadata("os", os)
                .metadata("isWindows", isWindows)
                .metadata("isWSL", isWSL)
                .metadata("DISPLAY", display != null ? display : "not set")
                .metadata("java.awt.headless", System.getProperty("java.awt.headless"))
                .log();
            
            // For Windows (not WSL), check directly if we can access screen
            if (isWindows && !isWSL) {
                try {
                    // Get all screens/monitors
                    int numScreens = Screen.getNumberScreens();
                    System.out.println("[" + timestamp + "] Number of screens detected: " + numScreens);
                    
                    // Also check Java's GraphicsEnvironment for comparison
                    java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
                    java.awt.GraphicsDevice[] devices = ge.getScreenDevices();
                    System.out.println("[" + timestamp + "] Java GraphicsEnvironment reports " + devices.length + " devices");
                    
                    for (int i = 0; i < devices.length; i++) {
                        java.awt.Rectangle bounds = devices[i].getDefaultConfiguration().getBounds();
                        double scaleFactor = devices[i].getDefaultConfiguration().getDefaultTransform().getScaleX();
                        System.out.println("[" + timestamp + "] GraphicsDevice " + i + ": " + bounds + 
                                         " (Scale: " + scaleFactor + "x)");
                    }
                    
                    System.out.println("[" + timestamp + "] SikuliX Screen information:");
                    for (int i = 0; i < numScreens; i++) {
                        Screen screen = new Screen(i);
                        Rectangle bounds = screen.getBounds();
                        System.out.println("[" + timestamp + "] Screen " + i + ": " + bounds + 
                                         " (Primary: " + (i == 0) + ")");
                        
                        brobotLogger.log()
                            .observation("Monitor detected")
                            .metadata("screenId", i)
                            .metadata("width", bounds.width)
                            .metadata("height", bounds.height)
                            .metadata("x", bounds.x)
                            .metadata("y", bounds.y)
                            .metadata("isPrimary", i == 0)
                            .log();
                    }
                    
                    // Check primary screen
                    Screen primaryScreen = new Screen(0);
                    int width = primaryScreen.getBounds().width;
                    int height = primaryScreen.getBounds().height;
                    
                    if (width > 0 && height > 0) {
                        displayAvailable = true;
                        System.out.println("[" + timestamp + "] Primary Windows display: " + primaryScreen.getBounds());
                        
                        brobotLogger.log()
                            .observation("Windows display detected successfully")
                            .metadata("primaryScreenWidth", width)
                            .metadata("primaryScreenHeight", height)
                            .metadata("totalScreens", numScreens)
                            .log();
                    }
                } catch (Exception e) {
                    System.out.println("[" + timestamp + "] Cannot access Windows display: " + e.getMessage());
                    brobotLogger.log()
                        .error(e)
                        .message("Cannot access Windows display")
                        .log();
                }
            } 
            // For Linux/WSL, check DISPLAY variable
            else if (display != null && !display.isEmpty()) {
                try {
                    Screen screen = new Screen();
                    int width = screen.getBounds().width;
                    int height = screen.getBounds().height;
                    
                    if (width > 0 && height > 0) {
                        displayAvailable = true;
                        System.out.println("[" + timestamp + "] X11 display detected: " + screen.getBounds());
                        
                        brobotLogger.log()
                            .observation("X11 display detected successfully")
                            .metadata("screenWidth", width)
                            .metadata("screenHeight", height)
                            .metadata("bounds", screen.getBounds().toString())
                            .log();
                    }
                } catch (Exception e) {
                    System.out.println("[" + timestamp + "] X11 display has zero dimensions - X11 server may not be running");
                    brobotLogger.observation("X11 display not accessible");
                }
            } else if (!isWindows || isWSL) {
                System.out.println("[" + timestamp + "] DISPLAY environment variable not set (required for Linux/WSL)");
                brobotLogger.observation("DISPLAY environment variable not set");
            }
            
            // Check if we're in headless mode
            if ("true".equals(System.getProperty("java.awt.headless"))) {
                displayAvailable = false;
                System.out.println("[" + timestamp + "] Running in headless mode - display operations disabled");
                brobotLogger.observation("Running in headless mode");
            }
        } catch (Exception e) {
            System.out.println("[" + timestamp + "] Error checking display environment: " + e.getMessage());
            brobotLogger.error("Error checking display environment", e);
        }
        
        return displayAvailable;
    }
    
    private void retrieveWorkingStateId() {
        try (var timer = brobotLogger.startTimer("RetrieveWorkingStateId")) {
            workingStateId = stateService.getStateId("Working");
            
            if (workingStateId == null) {
                log.error("Working state not found in StateService - states may not be registered yet");
                
                brobotLogger.log()
                    .error(new IllegalStateException("Working state not found"))
                    .message("Working state not found in StateService")
                    .metadata("availableStates", stateService.getAllStates().size())
                    .log();
                
                return;
            }
            
            log.info("Retrieved Working state ID: {}", workingStateId);
            
            brobotLogger.log()
                .observation("Working state ID retrieved successfully")
                .metadata("workingStateId", workingStateId)
                .metadata("stateName", "Working")
                .log();
        }
    }
    
    private void handleInactiveWorkingState() {
        log.info("Working state is not active, attempting to navigate to it");
        
        brobotLogger.log()
            .observation("Working state not active - initiating navigation")
            .metadata("currentActiveStates", stateMemory.getActiveStates())
            .metadata("targetState", "Working")
            .log();
        
        try (var timer = brobotLogger.startTimer("NavigateToWorkingState")) {
            // Try to navigate to Working state (will trigger Prompt->Working transition if needed)
            boolean success = stateNavigator.openState("Working");
            
            if (success) {
                log.info("Successfully navigated to Working state");
                
                brobotLogger.log()
                    .transition("Unknown", "Working")
                    .success(true)
                    .duration(timer.stop())
                    .metadata("navigationMethod", "stateNavigator.openState")
                    .log();
            } else {
                log.warn("Failed to navigate to Working state");
                
                brobotLogger.log()
                    .transition("Unknown", "Working")
                    .success(false)
                    .duration(timer.stop())
                    .metadata("navigationMethod", "stateNavigator.openState")
                    .metadata("issue", "Navigation failed")
                    .log();
            }
        }
    }
    
    private void checkIconVisibility() {
        // Quick check if icon exists (0.5 second timeout)
        PatternFindOptions quickFind = new PatternFindOptions.Builder()
                .setPauseBeforeBegin(0.5)
                .build();
        
        brobotLogger.log()
            .observation("Starting Claude icon visibility check")
            .metadata("pauseBeforeBegin", 0.5)
            .metadata("iconPattern", workingState.getClaudeIcon().getName())
            .log();
        
        // Highlight search regions before find operation
        try {
            // Get search regions from the pattern
            java.util.List<io.github.jspinak.brobot.model.element.Region> searchRegions = 
                workingState.getClaudeIcon().getAllSearchRegions();
            
            // If no specific regions, use full screen
            if (searchRegions.isEmpty()) {
                searchRegions = java.util.Collections.singletonList(
                    new io.github.jspinak.brobot.model.element.Region()
                );
            }
            
            // Log configuration status
            log.info("Highlighting configuration - enabled: {}, mock: {}", 
                highlightManager != null, 
                io.github.jspinak.brobot.config.FrameworkSettings.mock);
            
            // Highlight the search regions
            highlightManager.highlightSearchRegions(searchRegions);
            
            brobotLogger.log()
                .observation("Called highlightSearchRegions")
                .metadata("regionCount", searchRegions.size())
                .metadata("mockMode", io.github.jspinak.brobot.config.FrameworkSettings.mock)
                .log();
                
            // Small pause to see the highlight
            Thread.sleep(500);
        } catch (Exception e) {
            log.warn("Failed to highlight search regions", e);
        }
        
        try (var timer = brobotLogger.startTimer("IconVisibilityCheck")) {
            ActionResult result = action.perform(quickFind, workingState.getClaudeIcon());
            boolean iconFound = result.isSuccess();
            
            // Highlight matches if found
            if (iconFound && !result.getMatchList().isEmpty()) {
                try {
                    highlightManager.highlightMatches(result.getMatchList());
                    
                    brobotLogger.log()
                        .observation("Highlighted found matches")
                        .metadata("matchCount", result.getMatchList().size())
                        .log();
                } catch (Exception e) {
                    log.warn("Failed to highlight matches", e);
                }
            }
            
            brobotLogger.log()
                .action("FIND")
                .target(workingState.getClaudeIcon())
                .result(result)
                .metadata("iconFound", iconFound)
                .metadata("checkNumber", checkCount)
                .performance("findDuration", timer.stop())
                .log();
            
            if (!iconFound) {
                log.info("Claude icon disappeared - removing Working state and reopening");
                
                brobotLogger.log()
                    .observation("Claude icon disappeared - triggering state reset")
                    .metadata("lastSeenCheck", checkCount - 1)
                    .metadata("action", "removeInactiveState")
                    .log();
                
                // Remove Working as active state
                stateMemory.removeInactiveState(workingStateId);
                
                brobotLogger.log()
                    .observation("Working state removed from active states")
                    .metadata("removedStateId", workingStateId)
                    .metadata("remainingActiveStates", stateMemory.getActiveStates())
                    .log();
                
                // Immediately navigate back to Working state
                // This will trigger the PromptToWorkingTransition which types "continue\n"
                handleInactiveWorkingState();
                
            } else {
                log.debug("Claude icon still visible");
                
                if (checkCount % 10 == 0) { // Log every 10th successful check to avoid spam
                    brobotLogger.log()
                        .observation("Claude icon still visible")
                        .metadata("consecutiveChecks", 10)
                        .metadata("totalChecks", checkCount)
                        .log();
                }
            }
        }
    }
    
    /**
     * Stops the monitoring automation.
     */
    public void stopMonitoring() {
        log.info("Stopping Claude monitoring automation");
        
        brobotLogger.log()
            .observation("Stopping Claude monitoring automation")
            .metadata("totalChecks", checkCount)
            .metadata("runningTime", checkCount * 2 + " seconds (approx)")
            .log();
        
        running = false;
        scheduler.shutdown();
        
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
                brobotLogger.observation("Scheduler force shutdown after timeout");
            } else {
                brobotLogger.observation("Scheduler shutdown gracefully");
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
            
            brobotLogger.log()
                .error(e)
                .message("Interrupted during scheduler shutdown")
                .log();
        }
        
        brobotLogger.observation("Claude monitoring automation stopped");
    }
}