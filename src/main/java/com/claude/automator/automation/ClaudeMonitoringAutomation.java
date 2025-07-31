package com.claude.automator.automation;

import io.github.jspinak.brobot.navigation.monitoring.StateAwareScheduler;
import io.github.jspinak.brobot.navigation.monitoring.StateAwareScheduler.StateCheckConfiguration;
import io.github.jspinak.brobot.navigation.transition.StateNavigator;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import com.claude.automator.states.WorkingState;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Monitors Claude AI interface states and manages transitions.
 * Uses StateAwareScheduler to ensure proper state verification before each cycle.
 * Relies entirely on Brobot's state management system for navigation.
 */
@Service
@RequiredArgsConstructor
public class ClaudeMonitoringAutomation {

    private final StateNavigator stateNavigator;
    private final StateAwareScheduler stateAwareScheduler;
    private final StateService stateService;
    private final StateMemory stateMemory;
    private final Action action;
    private final WorkingState workingState;
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    @Value("${claude.automator.monitoring.initial-delay:5}")
    private int initialDelay;
    
    @Value("${claude.automator.monitoring.check-interval:2}")
    private int checkInterval;
    
    @Value("${claude.automator.monitoring.icon-timeout:5}")
    private int iconTimeout;
    
    @Value("${claude.automator.monitoring.required-states:Prompt,Working}")
    private List<String> requiredStates;

    @PostConstruct
    public void startMonitoring() {
        
        // Configure state checking - ensures Prompt and Working states exist
        StateCheckConfiguration stateConfig = new StateCheckConfiguration.Builder()
                .withRequiredStates(requiredStates)
                .withRebuildOnMismatch(true)
                .withSkipIfStatesMissing(false)
                .build();
        
        // Schedule monitoring with automatic state verification
        stateAwareScheduler.scheduleWithStateCheck(
                scheduler,
                this::monitorClaudeStates,
                stateConfig,
                initialDelay,
                checkInterval,
                TimeUnit.SECONDS
        );
    }

    /**
     * Monitors Claude states and manages transitions.
     * - If only Prompt is active: navigate to Working (triggers transition)
     * - If Working is active: check if icon still exists
     * - If icon disappears: remove Working state to restart cycle
     */
    private void monitorClaudeStates() {
        var activeStates = stateMemory.getActiveStates();
        Long promptStateId = stateService.getStateId("Prompt");
        Long workingStateId = stateService.getStateId("Working");
        
        if (activeStates.contains(workingStateId)) {
            // Working state is active - check if icon still visible
            checkWorkingIcon(workingStateId);
        } else if (activeStates.contains(promptStateId)) {
            // Only Prompt is active - navigate to Working (triggers transition)
            stateNavigator.openState("Working");
        }
        // If neither state is active, do nothing - let state verification handle it
    }
    
    /**
     * Checks if Claude icon is still visible in Working state.
     * If not found, removes Working state to return to Prompt state only.
     */
    private void checkWorkingIcon(Long workingStateId) {
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setSearchDuration(iconTimeout)
                .setSimilarity(0.85) // Lower threshold since we're getting 0.898 scores
                .build();
        
        ActionResult result = action.perform(findOptions, workingState.getClaudeIcon());
        
        if (!result.isSuccess() || result.getMatchList().isEmpty()) {
            // Icon not found - remove Working state
            // This leaves only Prompt state active, allowing transition to trigger next cycle
            stateMemory.removeInactiveState(workingStateId);
        }
        // If icon found, do nothing - stay in Working state
    }
    
    public void stopMonitoring() {
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