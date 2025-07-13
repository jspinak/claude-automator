package com.claude.automator.transitions;

import com.claude.automator.states.PromptState;
import com.claude.automator.states.WorkingState;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.annotations.Transition;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.model.element.Region;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Transition from Prompt state to Working state.
 * Uses the new @Transition annotation for automatic registration.
 */
@Transition(from = PromptState.class, to = WorkingState.class)
@RequiredArgsConstructor
@Slf4j
public class PromptToWorkingTransition {

    private final PromptState promptState;
    private final Action action;
    
    @Autowired(required = false)
    private BrobotLogger brobotLogger;

    /**
     * Executes the transition from Prompt to Working state.
     * Demonstrates the fluent API with action chaining:
     * Finds prompt -> Clicks on it -> Types "continue" with Enter.
     */
    public boolean execute() {
        try (var operation = brobotLogger != null ? 
                brobotLogger.operation("PromptToWorkingTransition") : null) {
            
            log.info("Executing transition from Prompt to Working state");
            log.debug("Using image: {}", promptState.getClaudePrompt().getName());
            
            if (brobotLogger != null) {
                brobotLogger.log()
                    .observation("Starting Prompt to Working transition")
                    .metadata("fromState", "PromptState")
                    .metadata("toState", "WorkingState")
                    .metadata("promptImage", promptState.getClaudePrompt().getName())
                    .metadata("continueCommand", promptState.getContinueCommand().getString())
                    .log();
            }

            // Highlight the search area for debugging
            try (var timer = brobotLogger != null ? 
                    brobotLogger.startTimer("HighlightSearchArea") : null) {
                
                HighlightOptions highlightOptions = new HighlightOptions.Builder()
                        .setPauseAfterEnd(0.5) // Pause after highlighting
                        .build();
                ObjectCollection objColl = new ObjectCollection.Builder()
                        .withRegions(new Region())
                        .build();
                        
                if (brobotLogger != null) {
                    brobotLogger.log()
                        .observation("Highlighting search area")
                        .metadata("pauseAfterEnd", 0.5)
                        .metadata("regionType", "FullScreen")
                        .log();
                }
                
                action.perform(highlightOptions, objColl);
            }
            
            // Using the fluent API to chain actions: find -> click -> type
            PatternFindOptions findClickType = new PatternFindOptions.Builder()
                    .setPauseAfterEnd(0.5) // Pause before clicking
                    .then(new ClickOptions.Builder()
                            .setPauseAfterEnd(0.5) // Pause before typing
                            .build())
                    .then(new TypeOptions.Builder()
                            .build())
                    .build();
            
            // Create target objects for the chained action
            ObjectCollection target = new ObjectCollection.Builder()
                    .withImages(promptState.getClaudePrompt()) // For find & click
                    .withStrings(promptState.getContinueCommand()) // For type (continue with Enter)
                    .build();
            
            if (brobotLogger != null) {
                brobotLogger.log()
                    .observation("Executing chained action sequence")
                    .metadata("actionChain", "FIND -> CLICK -> TYPE")
                    .metadata("findPause", 0.5)
                    .metadata("clickPause", 0.5)
                    .metadata("targetImage", promptState.getClaudePrompt().getName())
                    .metadata("typeText", promptState.getContinueCommand().getString().replace("\n", "\\n"))
                    .log();
            }
            
            // Execute the chained action
            log.debug("Performing chained action: find -> click -> type");
            
            try (var timer = brobotLogger != null ? 
                    brobotLogger.startTimer("ChainedActionExecution") : null) {
                
                ActionResult result = action.perform(findClickType, target);
                log.debug("Action result: success={}, description={}", result.isSuccess(), result.getActionDescription());
                
                if (brobotLogger != null) {
                    brobotLogger.log()
                        .action("CHAIN")
                        .target(promptState.getClaudePrompt())
                        .result(result)
                        .metadata("chainedActions", "FIND->CLICK->TYPE")
                        .metadata("description", result.getActionDescription())
                        .performance("executionTime", timer != null ? timer.stop() : 0)
                        .log();
                }
                
                if (result.isSuccess()) {
                    log.info("Successfully executed transition from Prompt to Working");
                    
                    if (brobotLogger != null) {
                        brobotLogger.log()
                            .transition("PromptState", "WorkingState")
                            .success(true)
                            .metadata("transitionMethod", "ChainedAction")
                            .log();
                    }
                    
                    return true;
                } else {
                    log.warn("Failed to execute transition: {}", result.getActionDescription());
                    
                    if (brobotLogger != null) {
                        brobotLogger.log()
                            .transition("PromptState", "WorkingState")
                            .success(false)
                            .metadata("failureReason", result.getActionDescription())
                            .metadata("transitionMethod", "ChainedAction")
                            .screenshot(captureScreenshotPath())
                            .log();
                    }
                    
                    return false;
                }
            }
            
        } catch (Exception e) {
            log.error("Error during Prompt to Working transition", e);
            
            if (brobotLogger != null) {
                brobotLogger.log()
                    .error(e)
                    .message("Error during Prompt to Working transition")
                    .metadata("errorType", e.getClass().getSimpleName())
                    .metadata("errorMessage", e.getMessage())
                    .screenshot(captureScreenshotPath())
                    .log();
            }
            
            return false;
        }
    }
    
    private String captureScreenshotPath() {
        // In a real implementation, this would capture and save a screenshot
        return "/tmp/transition-error-" + System.currentTimeMillis() + ".png";
    }
}