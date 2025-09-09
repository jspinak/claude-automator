package com.claude.automator.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateString;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * Represents the Prompt state where Claude is waiting for input.
 * 
 * This is the initial state of the application.
 * In mock mode, this state has a 100% probability of being found,
 * ensuring reliable transitions.
 */
@State(initial = true)
@Getter
@Slf4j
public class PromptState {
    
    private final StateImage claudePrompt;
    private final StateString continueCommand;
    
    public PromptState() {
        // Create search region for lower left quarter of screen
        // RegionBuilder now automatically uses ScreenResolutionManager
        // to ensure regions match the capture coordinate space
        Region lowerLeftQuarter = Region.builder()
            .withScreenPercentage(0.0, 0.5, 0.5, 0.5)  // x=0%, y=50%, width=50%, height=50%
            .build();
        
        // Debug: verify the region is calculated correctly
        System.out.println("[PromptState] Screen resolution detected for region calculation");
        System.out.println("[PromptState] Lower left quarter region: " + lowerLeftQuarter);
        System.out.println("[PromptState] Region details - x:" + lowerLeftQuarter.x() + 
                          " y:" + lowerLeftQuarter.y() + 
                          " w:" + lowerLeftQuarter.w() + 
                          " h:" + lowerLeftQuarter.h());
        
        // Initialize the claude prompt image with search region and ActionHistory
        // The ActionHistory is required for mock mode finds to work
        // Using original patterns since regions are now in capture coordinate space
        claudePrompt = new StateImage.Builder()
            .addPatterns(
                // Original patterns - regions now match capture resolution
                "prompt/windows", "prompt/ffmpeg"
            )
            .setName("ClaudePrompt")
            .setSearchRegionForAllPatterns(lowerLeftQuarter)
            .setFixedForAllPatterns(true)  // Mark all patterns as fixed
            .build();
        
        // Debug: verify patterns have search regions and ActionHistory
        System.out.println("[PromptState] Created StateImage with " + claudePrompt.getPatterns().size() + " patterns");
        for (io.github.jspinak.brobot.model.element.Pattern p : claudePrompt.getPatterns()) {
            System.out.println("[PromptState] Pattern '" + p.getName() + "':");
            System.out.println("  - Fixed: " + p.isFixed());
            System.out.println("  - Search regions (getRegions()): " + p.getSearchRegions().getRegions());
            System.out.println("  - Search regions (getRegions(fixed)): " + p.getSearchRegions().getRegions(p.isFixed()));
            System.out.println("  - getRegionsForSearch(): " + p.getRegionsForSearch());
            System.out.println("  - Has ActionHistory: " + (p.getMatchHistory() != null && !p.getMatchHistory().getSnapshots().isEmpty()));
        }
        
        // Add mock ActionSnapshots for Find operations
        // This helps the mock framework provide realistic responses
        addMockFindSnapshots();
        // Create the continue command as a string
        continueCommand = new StateString.Builder()
            .setName("ContinueCommand")
            .setString("continue\n")
            .build();
    }
    
    /**
     * Adds mock ActionSnapshots for Find operations to enable realistic mock behavior.
     * These snapshots simulate successful Find operations with the ClaudePrompt pattern
     * at typical locations in the lower left quarter of the screen.
     */
    private void addMockFindSnapshots() {
        // Create successful find snapshot 1
        ActionRecord snapshot1 = new ActionRecord.Builder()
            .setActionConfig(new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .setSimilarity(0.85)
                .build())
            .setMatchList(Collections.singletonList(
                new Match.Builder()
                    .setRegion(100, 600, 150, 30)
                    .setSimScore(0.92)
                    .setName("ClaudePrompt")
                    .build()
            ))
            .setActionSuccess(true)
            .setResultSuccess(true)
            .setDuration(0.25)
            .setTimeStamp(LocalDateTime.now().minusMinutes(10))
            .build();
        
        // Create successful find snapshot 2
        ActionRecord snapshot2 = new ActionRecord.Builder()
            .setActionConfig(new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .setSimilarity(0.85)
                .build())
            .setMatchList(Collections.singletonList(
                new Match.Builder()
                    .setRegion(95, 595, 155, 35)
                    .setSimScore(0.89)
                    .setName("ClaudePrompt")
                    .build()
            ))
            .setActionSuccess(true)
            .setResultSuccess(true)
            .setDuration(0.18)
            .setTimeStamp(LocalDateTime.now().minusMinutes(5))
            .build();
        
        // Create failed find snapshot
        ActionRecord snapshot3 = new ActionRecord.Builder()
            .setActionConfig(new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .setSimilarity(0.85)
                .build())
            .setMatchList(Collections.emptyList())
            .setActionSuccess(false)
            .setResultSuccess(false)
            .setDuration(2.0)
            .setTimeStamp(LocalDateTime.now().minusMinutes(2))
            .build();
        
        // Add all snapshots to all patterns using the new StateImage method
        claudePrompt.addActionSnapshotsToAllPatterns(snapshot1, snapshot2, snapshot3);
        
        log.info("[PromptState] Added mock ActionSnapshots for Find operations");
    }
    
}