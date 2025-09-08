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
        log.info("=== TRANSITION EXECUTION: PromptToWorkingTransition.execute() START ===");
        System.out.println("=== TRANSITION EXECUTION: PromptToWorkingTransition.execute() CALLED ===");
        
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
            
            // Execute actions separately for better control and debugging
            log.info("Step 2: Clicking on the found location...");
            
            // Click on the location where we found the prompt
            ClickOptions clickOptions = new ClickOptions.Builder()
                    .setPauseAfterEnd(0.5) // Pause after clicking
                    .build();
            
            // Use the region from the find result for clicking
            ObjectCollection clickTarget = new ObjectCollection.Builder()
                    .withRegions(findResult.getMatchList().get(0).getRegion())
                    .build();
            
            ActionResult clickResult = action.perform(clickOptions, clickTarget);
            log.info("Click result: success={}", clickResult.isSuccess());
            
            if (!clickResult.isSuccess()) {
                log.error("Failed to click on found region");
                log.info("=== TRANSITION DEBUG: PromptToWorkingTransition.execute() END - FAILED (Click) ===");
                return false;
            }
            
            // Type the continue command
            log.info("Step 3: Typing continue command...");
            log.info("Command to type: '{}'", promptState.getContinueCommand().getString());
            
            TypeOptions typeOptions = new TypeOptions.Builder()
                    .setPauseBeforeBegin(0.5) // Pause before typing
                    .build();
            
            ObjectCollection typeTarget = new ObjectCollection.Builder()
                    .withStrings(promptState.getContinueCommand())
                    .build();
            
            ActionResult typeResult = action.perform(typeOptions, typeTarget);
            log.info("Type result: success={}, text typed='{}'", 
                    typeResult.isSuccess(), typeResult.getText());
            
            if (!typeResult.isSuccess()) {
                log.error("Failed to type continue command");
                log.info("=== TRANSITION DEBUG: PromptToWorkingTransition.execute() END - FAILED (Type) ===");
                return false;
            }
            
            log.info("=== TRANSITION DEBUG: PromptToWorkingTransition.execute() END - SUCCESS ===");
            
            return true;
            
        } catch (Exception e) {
            log.error("Exception during transition execution", e);
            log.info("=== TRANSITION DEBUG: PromptToWorkingTransition.execute() END - EXCEPTION ===");
            return false;
        }
    }
}