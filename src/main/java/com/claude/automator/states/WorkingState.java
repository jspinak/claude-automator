package com.claude.automator.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

/**
 * Represents the Working state where Claude is actively processing.
 * 
 * This state is not initial - it becomes active after transitioning from Prompt.
 * In mock mode, this state has a 100% probability of being found,
 * ensuring reliable transitions.
 */
@State
@Getter
@Slf4j
public class WorkingState {
    
    private final StateImage claudeIcon;
    
    public WorkingState() {
        // Create the claude icon images with declarative region definition
        // Do NOT set a fixed ActionHistory as it will override the SearchRegionOnObject
        
        claudeIcon = new StateImage.Builder()
            .addPatterns(
                // Original patterns
                "working/claude-icon-1", 
                "working/claude-icon-2", 
                "working/claude-icon-3", 
                "working/claude-icon-4",
                // 80% scaled patterns (pre-scaled to match 125% Windows scaling)
                "working/claude-icon-1-80",
                "working/claude-icon-2-80", 
                "working/claude-icon-3-80",
                "working/claude-icon-4-80"
            )
            .setName("ClaudeIcon")
            .setHighlightColor("#0000FF")  // Blue color for icon highlighting
            .setFixedForAllPatterns(true)  // Enable fixed region optimization once found
            .setSearchRegionOnObject(SearchRegionOnObject.builder()
                    .setTargetType(StateObject.Type.IMAGE)
                    .setTargetStateName("Prompt")
                    .setTargetObjectName("ClaudePrompt")
                    .setAdjustments(MatchAdjustmentOptions.builder()
                            .setAddX(3)
                            .setAddY(10)
                            .setAddW(30)
                            .setAddH(55)
                            .build())
                    .build())
            .build();
        
        log.info("WorkingState ClaudeIcon search region config: {}", 
                claudeIcon.getSearchRegionOnObject());
        
        // Add mock ActionSnapshots for Find operations
        addMockFindSnapshots();
    }
    
    /**
     * Adds mock ActionSnapshots for Find operations to enable realistic mock behavior.
     * These snapshots simulate successful Find operations with the ClaudeIcon pattern,
     * including multiple matches for ALL strategy and animation variations.
     */
    private void addMockFindSnapshots() {
        // Single icon find (FIRST strategy)
        ActionRecord snapshot1 = new ActionRecord.Builder()
            .setActionConfig(new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .setSimilarity(0.80)
                .build())
            .setMatchList(Collections.singletonList(
                new Match.Builder()
                    .setRegion(103, 610, 25, 25)
                    .setSimScore(0.88)
                    .setName("ClaudeIcon")
                    .build()
            ))
            .setActionSuccess(true)
            .setResultSuccess(true)
            .setDuration(0.15)
            .setTimeStamp(LocalDateTime.now().minusMinutes(8))
            .build();
        
        // Multiple animation frames (ALL strategy)
        ActionRecord snapshot2 = new ActionRecord.Builder()
            .setActionConfig(new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setSimilarity(0.75)
                .build())
            .setMatchList(Arrays.asList(
                new Match.Builder()
                    .setRegion(103, 610, 25, 25)
                    .setSimScore(0.91)
                    .setName("ClaudeIcon-1")
                    .build(),
                new Match.Builder()
                    .setRegion(103, 610, 25, 25)
                    .setSimScore(0.87)
                    .setName("ClaudeIcon-2")
                    .build(),
                new Match.Builder()
                    .setRegion(103, 610, 25, 25)
                    .setSimScore(0.85)
                    .setName("ClaudeIcon-3")
                    .build()
            ))
            .setActionSuccess(true)
            .setResultSuccess(true)
            .setDuration(0.35)
            .setTimeStamp(LocalDateTime.now().minusMinutes(6))
            .build();
        
        // Best match at slightly different position (BEST strategy)
        ActionRecord snapshot3 = new ActionRecord.Builder()
            .setActionConfig(new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .setSimilarity(0.80)
                .build())
            .setMatchList(Collections.singletonList(
                new Match.Builder()
                    .setRegion(108, 615, 24, 24)
                    .setSimScore(0.93)
                    .setName("ClaudeIcon")
                    .build()
            ))
            .setActionSuccess(true)
            .setResultSuccess(true)
            .setDuration(0.22)
            .setTimeStamp(LocalDateTime.now().minusMinutes(3))
            .build();
        
        // Failed find when Working state is not active
        ActionRecord snapshot4 = new ActionRecord.Builder()
            .setActionConfig(new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .setSimilarity(0.80)
                .build())
            .setMatchList(Collections.emptyList())
            .setActionSuccess(false)
            .setResultSuccess(false)
            .setDuration(2.0)
            .setTimeStamp(LocalDateTime.now().minusMinutes(1))
            .build();
        
        // Add all snapshots to all patterns using the new StateImage method
        claudeIcon.addActionSnapshotsToAllPatterns(snapshot1, snapshot2, snapshot3, snapshot4);
        
        log.info("[WorkingState] Added mock ActionSnapshots for Find operations");
    }
}