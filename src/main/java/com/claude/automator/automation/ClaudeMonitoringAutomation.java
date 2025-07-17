package com.claude.automator.automation;

import com.claude.automator.states.PromptState;
import com.claude.automator.states.WorkingState;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.SearchRegions;
import io.github.jspinak.brobot.navigation.transition.StateNavigator;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateMemory;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Monitors Claude AI interface for icon disappearance and manages conversation flow.
 * Relies on Brobot library's built-in logging capabilities - configure verbose mode
 * in application.properties for detailed action logging.
 */
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
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private volatile boolean running = false;
    private Long workingStateId = null;
    private Region claudeIconRegion = null;
    
    @Value("${claude.automator.monitoring.initial-delay:5}")
    private int initialDelay;
    
    @Value("${claude.automator.monitoring.check-interval:2}")
    private int checkInterval;
    
    @Value("${claude.automator.monitoring.icon-timeout:5}")
    private int iconTimeout;

    /**
     * Starts the automation that monitors for Claude icon disappearance.
     * All action logging is handled by Brobot's ConsoleActionReporter and BrobotLogger
     * based on configured verbosity level.
     */
    @PostConstruct
    public void startMonitoring() {
        log.info("Starting Claude monitoring automation (initial delay: {}s, check interval: {}s)", 
                initialDelay, checkInterval);
        
        running = true;
        
        // Schedule monitoring task
        scheduler.scheduleWithFixedDelay(this::getClaudeWorking,
                initialDelay,
                checkInterval,
                TimeUnit.SECONDS);
    }

    private void getClaudeWorking() {
        if (!running) {
            return;
        }

        if (selectClaudePrompt()) {
            checkClaudeIconStatus();
        }
    }

    private boolean selectClaudePrompt() {
        // Simple find action - library handles all logging based on verbosity
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setPauseBeforeBegin(0.5)
                .build();
                
        ActionResult result = action.perform(findOptions, promptState.getClaudePrompt());
        
        if (!result.isSuccess() || result.getBestMatch().isEmpty()) {
            // Library logs the failure details in verbose mode
            return false;
        }
        
        // Set up search region for icon based on prompt location
        Region matchRegion = result.getBestMatch().get().getRegion();
        if (claudeIconRegion == null) {
            claudeIconRegion = new Region(matchRegion);
            claudeIconRegion.adjust(3, 10, 30, 55);
            workingState.getClaudeIcon().setSearchRegions(claudeIconRegion);
        }
        
        // Click the prompt
        ObjectCollection clickTarget = new ObjectCollection.Builder()
                .withRegions(matchRegion)
                .build();
                
        ActionResult clickResult = action.perform(new ClickOptions.Builder().build(), clickTarget);
        return clickResult.isSuccess();
    }
    
    /**
     * Checks if Claude icon is visible and handles state transitions.
     * All action results are logged by the library based on verbose settings.
     */
    private void checkClaudeIconStatus() {
        try {
            // Get working state ID if needed
            if (workingStateId == null) {
                workingStateId = stateService.getStateId("Working");
                if (workingStateId == null) {
                    log.error("Working state not found in StateService");
                    return;
                }
            }
            
            // Check icon visibility
            checkIconVisibility();
            
        } catch (Exception e) {
            log.error("Error during Claude icon monitoring", e);
        }
    }
    
    private void checkIconVisibility() {
        // Highlight search region (visual feedback handled by library)
        if (claudeIconRegion != null) {
            HighlightOptions highlightOptions = new HighlightOptions.Builder()
                    .setHighlightSeconds(2)
                    .build();
            ObjectCollection searchRegion = new ObjectCollection.Builder()
                    .withRegions(claudeIconRegion)
                    .build();
            action.perform(highlightOptions, searchRegion);
        }

        // Check if icon exists - library logs all details in verbose mode
        SearchRegions searchRegions = new SearchRegions();
        if (claudeIconRegion != null) {
            searchRegions.addSearchRegions(claudeIconRegion);
        }
        
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setPauseBeforeBegin(0.5)
                .setSearchDuration(iconTimeout)
                .setSearchRegions(searchRegions)
                .build();
        
        ActionResult result = action.perform(findOptions, workingState.getClaudeIcon());
        
        if (result.isSuccess() && !result.getMatchList().isEmpty()) {
            // Icon found - highlight it
            HighlightOptions highlightOptions = new HighlightOptions.Builder()
                    .setHighlightSeconds(2)
                    .build();
            ObjectCollection highlightTarget = new ObjectCollection.Builder()
                    .withRegions(result.getMatchList().get(0).getRegion())
                    .build();
            action.perform(highlightOptions, highlightTarget);
        } else {
            // Icon disappeared - remove state and reopen
            log.info("Claude icon disappeared - removing Working state and reopening");
            
            stateMemory.removeInactiveState(workingStateId);
            
            // Navigate back to Working state (triggers prompt->working transition)
            stateNavigator.openState("Working");
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