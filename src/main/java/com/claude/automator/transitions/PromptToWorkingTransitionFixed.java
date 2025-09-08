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
 * Fixed version of the transition that properly chains find->click->type.
 * 
 * The key insight is that when chaining actions with different object types
 * (StateImage for find/click, StateString for type), we need to either:
 * 1. Split the chain into two parts
 * 2. Use CONFIRM strategy (but this doesn't work well for type)
 * 3. Use a hybrid approach
 */
@Slf4j
@Transition(from = PromptState.class, to = WorkingState.class)
@RequiredArgsConstructor
public class PromptToWorkingTransitionFixed {

    private final PromptState promptState;
    private final Action action;

    /**
     * Executes the transition using the optimal chaining approach.
     * 
     * The solution is to chain find->click (which both use StateImage),
     * then separately execute type (which uses StateString).
     */
    public boolean execute() {
        log.info("=== FIXED TRANSITION: PromptToWorkingTransition.execute() START ===");
        
        try {
            // Step 1: Chain find->click operations (both use StateImage)
            log.info("Step 1: Executing find->click chain...");
            
            PatternFindOptions findClickOptions = new PatternFindOptions.Builder()
                    .setPauseAfterEnd(0.3) // Brief pause after finding
                    .then(new ClickOptions.Builder()
                            .setPauseAfterEnd(0.5) // Pause after clicking to let UI respond
                            .build())
                    .build();
            
            ObjectCollection imageCollection = new ObjectCollection.Builder()
                    .withImages(promptState.getClaudePrompt())
                    .build();
            
            log.info("Executing find->click on ClaudePrompt...");
            ActionResult clickResult = action.perform(findClickOptions, imageCollection);
            
            if (!clickResult.isSuccess()) {
                log.error("Find->Click chain failed");
                log.error("  - Matches found: {}", clickResult.getMatchList().size());
                if (clickResult.getMatchList().isEmpty()) {
                    log.error("  - ClaudePrompt not found on screen");
                }
                log.info("=== FIXED TRANSITION: PromptToWorkingTransition.execute() END - FAILED ===");
                return false;
            }
            
            log.info("Find->Click successful:");
            log.info("  - Found at: {}", clickResult.getMatchList().get(0).getRegion());
            log.info("  - Score: {}", clickResult.getMatchList().get(0).getScore());
            
            // Step 2: Execute type action separately (uses StateString)
            log.info("Step 2: Typing continue command...");
            log.info("  - Text to type: '{}'", promptState.getContinueCommand().getString());
            
            TypeOptions typeOptions = new TypeOptions.Builder()
                    .setPauseBeforeBegin(0.3) // Brief pause before typing
                    .setPauseAfterEnd(0.5) // Pause after typing
                    .build();
            
            ObjectCollection stringCollection = new ObjectCollection.Builder()
                    .withStrings(promptState.getContinueCommand())
                    .build();
            
            ActionResult typeResult = action.perform(typeOptions, stringCollection);
            
            if (!typeResult.isSuccess()) {
                log.error("Type action failed");
                log.error("  - Text attempted: '{}'", promptState.getContinueCommand().getString());
                log.error("  - Text typed: '{}'", typeResult.getText());
                log.info("=== FIXED TRANSITION: PromptToWorkingTransition.execute() END - FAILED ===");
                return false;
            }
            
            log.info("Type successful:");
            log.info("  - Text typed: '{}'", typeResult.getText());
            
            log.info("=== FIXED TRANSITION: PromptToWorkingTransition.execute() END - SUCCESS ===");
            return true;
            
        } catch (Exception e) {
            log.error("Exception during transition execution", e);
            log.info("=== FIXED TRANSITION: PromptToWorkingTransition.execute() END - EXCEPTION ===");
            return false;
        }
    }
}