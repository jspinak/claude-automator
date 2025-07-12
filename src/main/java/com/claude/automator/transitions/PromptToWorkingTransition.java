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
import io.github.jspinak.brobot.model.element.Region;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

    /**
     * Executes the transition from Prompt to Working state.
     * Demonstrates the fluent API with action chaining:
     * Finds prompt -> Clicks on it -> Types "continue" with Enter.
     */
    public boolean execute() {
        try {
            log.info("Executing transition from Prompt to Working state");
            log.debug("Using image: {}", promptState.getClaudePrompt().getName());

            // Highlight the search area for debugging
            HighlightOptions highlightOptions = new HighlightOptions.Builder()
                    .setPauseAfterEnd(0.5) // Pause after highlighting
                    .build();
            ObjectCollection objColl = new ObjectCollection.Builder()
                    .withRegions(new Region())
                    .build();
            action.perform(highlightOptions, objColl);
            
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
            
            // Execute the chained action
            log.debug("Performing chained action: find -> click -> type");
            ActionResult result = action.perform(findClickType, target);
            log.debug("Action result: success={}, description={}", result.isSuccess(), result.getActionDescription());
            
            if (result.isSuccess()) {
                log.info("Successfully executed transition from Prompt to Working");
                return true;
            } else {
                log.warn("Failed to execute transition: {}", result.getActionDescription());
                return false;
            }
            
        } catch (Exception e) {
            log.error("Error during Prompt to Working transition", e);
            return false;
        }
    }
}