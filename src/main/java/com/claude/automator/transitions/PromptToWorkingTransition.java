package com.claude.automator.transitions;

import com.claude.automator.states.PromptState;
import com.claude.automator.states.WorkingState;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.annotations.Transition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Transition from Prompt state to Working state.
 */
@Slf4j
@Transition(from = PromptState.class, to = WorkingState.class)
@RequiredArgsConstructor
public class PromptToWorkingTransition {

    private final PromptState promptState;
    private final Action action;

    /**
     * Executes the transition from Prompt to Working state.
     * Uses fluent API to chain: find -> click -> type.
     */
    public boolean execute() {
        log.info("=== TRANSITION DEBUG: PromptToWorkingTransition.execute() START ===");
        
        try {
            // Log the prompt state details
            log.info("PromptState details:");
            log.info("  - ClaudePrompt: {}", promptState.getClaudePrompt());
            log.info("  - ContinueCommand: '{}'", promptState.getContinueCommand());
            
            // First, try to find the prompt separately to debug
            log.info("Step 1: Finding ClaudePrompt...");
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                    .build();
            
            ObjectCollection findTarget = new ObjectCollection.Builder()
                    .withImages(promptState.getClaudePrompt())
                    .build();
            
            ActionResult findResult = action.perform(findOptions, findTarget);
            log.info("Find result: success={}, matches={}, best score={}", 
                    findResult.isSuccess(), 
                    findResult.getMatchList().size(),
                    findResult.getMatchList().isEmpty() ? "N/A" : 
                        findResult.getMatchList().get(0).getScore());
            
            if (!findResult.isSuccess()) {
                log.error("Failed to find ClaudePrompt - transition cannot proceed");
                log.info("=== TRANSITION DEBUG: PromptToWorkingTransition.execute() END - FAILED (Find) ===");
                return false;
            }
            
            // Now try the full chained action
            log.info("Step 2: Executing chained action (find -> click -> type)...");
            
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
            
            log.info("Executing chained action with targets:");
            log.info("  - Images: {}", target.getStateImages());
            log.info("  - Strings: {}", target.getStateStrings());
            
            // Execute the chained action
            ActionResult result = action.perform(findClickType, target);
            
            log.info("Chained action result:");
            log.info("  - Success: {}", result.isSuccess());
            log.info("  - Matches: {}", result.getMatchList().size());
            log.info("  - Text typed: '{}'", result.getText());
            log.info("  - Duration: {}ms", result.getDuration());
            
            if (!result.isSuccess()) {
                log.error("Chained action failed. Checking individual action results...");
                // Log more details about what failed
                if (result.getMatchList().isEmpty()) {
                    log.error("  - No matches found for ClaudePrompt");
                }
                if (result.getText() == null || result.getText().isEmpty()) {
                    log.error("  - Text was not typed");
                }
            }
            
            log.info("=== TRANSITION DEBUG: PromptToWorkingTransition.execute() END - {} ===", 
                    result.isSuccess() ? "SUCCESS" : "FAILED");
            
            return result.isSuccess();
            
        } catch (Exception e) {
            log.error("Exception during transition execution", e);
            log.info("=== TRANSITION DEBUG: PromptToWorkingTransition.execute() END - EXCEPTION ===");
            return false;
        }
    }
}