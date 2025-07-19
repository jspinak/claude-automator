package com.claude.automator.automation;

import io.github.jspinak.brobot.navigation.monitoring.StateAwareScheduler;
import io.github.jspinak.brobot.navigation.monitoring.StateAwareScheduler.StateCheckConfiguration;
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

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Enhanced version of ClaudeMonitoringAutomation that uses StateAwareScheduler
 * to ensure proper state context at the beginning of each monitoring cycle.
 * 
 * This demonstrates the single responsibility principle:
 * - StateAwareScheduler handles state checking logic
 * - This class focuses on Claude-specific monitoring logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClaudeMonitoringAutomationV2 {

    private final StateService stateService;
    private final StateMemory stateMemory;
    private final StateNavigator stateNavigator;
    private final Action action;
    private final WorkingState workingState;
    private final PromptState promptState;
    private final StateAwareScheduler stateAwareScheduler;
    
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
    
    @Value("${claude.automator.monitoring.required-states:Prompt,Working}")
    private List<String> requiredStates;
    
    @Value("${claude.automator.monitoring.rebuild-on-mismatch:true}")
    private boolean rebuildOnMismatch;

    @PostConstruct
    public void startMonitoring() {
        log.info("Starting Claude monitoring automation V2 with state awareness");
        log.info("Configuration - Initial delay: {}s, Check interval: {}s, Required states: {}", 
                initialDelay, checkInterval, requiredStates);
        
        running = true;
        
        // Configure state checking behavior
        StateCheckConfiguration stateConfig = new StateCheckConfiguration.Builder()
                .withRequiredStates(requiredStates)
                .withRebuildOnMismatch(rebuildOnMismatch)
                .withSkipIfStatesMissing(false) // Continue even if states missing
                .build();
        
        // Schedule monitoring with state awareness
        stateAwareScheduler.scheduleWithStateCheck(
                scheduler,
                this::getClaudeWorking,
                stateConfig,
                initialDelay,
                checkInterval,
                TimeUnit.SECONDS
        );
    }

    /**
     * Main monitoring task that runs after state validation.
     * State checking has already been performed by StateAwareScheduler.
     */
    private void getClaudeWorking() {
        if (!running) {
            return;
        }

        try {
            if (selectClaudePrompt()) {
                checkClaudeIconStatus();
            }
        } catch (Exception e) {
            log.error("Error in Claude monitoring task", e);
        }
    }

    private boolean selectClaudePrompt() {
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setPauseBeforeBegin(0.5)
                .build();
                
        ActionResult result = action.perform(findOptions, promptState.getClaudePrompt());
        
        if (!result.isSuccess() || result.getBestMatch().isEmpty()) {
            return false;
        }
        
        Region matchRegion = result.getBestMatch().get().getRegion();
        if (claudeIconRegion == null) {
            claudeIconRegion = new Region(matchRegion);
            claudeIconRegion.adjust(3, 10, 30, 55);
            workingState.getClaudeIcon().setSearchRegions(claudeIconRegion);
        }
        
        ObjectCollection clickTarget = new ObjectCollection.Builder()
                .withRegions(matchRegion)
                .build();
                
        ActionResult clickResult = action.perform(new ClickOptions.Builder().build(), clickTarget);
        return clickResult.isSuccess();
    }
    
    private void checkClaudeIconStatus() {
        try {
            if (workingStateId == null) {
                workingStateId = stateService.getStateId("Working");
                if (workingStateId == null) {
                    log.error("Working state not found in StateService");
                    return;
                }
            }
            
            checkIconVisibility();
            
        } catch (Exception e) {
            log.error("Error during Claude icon monitoring", e);
        }
    }
    
    private void checkIconVisibility() {
        if (claudeIconRegion != null) {
            HighlightOptions highlightOptions = new HighlightOptions.Builder()
                    .setHighlightSeconds(2)
                    .build();
            ObjectCollection searchRegion = new ObjectCollection.Builder()
                    .withRegions(claudeIconRegion)
                    .build();
            action.perform(highlightOptions, searchRegion);
        }

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
            HighlightOptions highlightOptions = new HighlightOptions.Builder()
                    .setHighlightSeconds(2)
                    .build();
            ObjectCollection highlightTarget = new ObjectCollection.Builder()
                    .withRegions(result.getMatchList().get(0).getRegion())
                    .build();
            action.perform(highlightOptions, highlightTarget);
        } else {
            log.info("Claude icon disappeared - removing Working state and reopening");
            
            stateMemory.removeInactiveState(workingStateId);
            stateNavigator.openState("Working");
        }
    }
    
    public void stopMonitoring() {
        log.info("Stopping Claude monitoring automation V2");
        
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