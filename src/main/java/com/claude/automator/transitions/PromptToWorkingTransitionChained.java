package com.claude.automator.transitions;

import com.claude.automator.states.PromptState;
import com.claude.automator.states.WorkingState;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.ActionChainOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Fixed version of the transition that properly chains find->click->type actions.
 * This demonstrates the correct way to chain actions that use different object types.
 */
@Slf4j
@RequiredArgsConstructor
public class PromptToWorkingTransitionChained {

    private final PromptState promptState;
    private final Action action;

    /**
     * Executes the transition using properly configured chained actions.
     * 
     * Solution 1: Use separate action chains for image-based and string-based actions
     */
    public boolean executeWithSeparateChains() {
        log.info("=== CHAINED TRANSITION: Separate Chains Approach ===");
        
        try {
            // First chain: Find and Click (both use StateImage)
            log.info("Step 1: Find and Click chain...");
            
            PatternFindOptions findClickOptions = new PatternFindOptions.Builder()
                    .setPauseAfterEnd(0.5)
                    .then(new ClickOptions.Builder()
                            .setPauseAfterEnd(0.5)
                            .build())
                    .build();
            
            ObjectCollection imageCollection = new ObjectCollection.Builder()
                    .withImages(promptState.getClaudePrompt())
                    .build();
            
            ActionResult clickResult = action.perform(findClickOptions, imageCollection);
            
            if (!clickResult.isSuccess()) {
                log.error("Find->Click chain failed");
                return false;
            }
            
            log.info("Find->Click chain successful");
            
            // Second action: Type (uses StateString)
            log.info("Step 2: Typing text...");
            
            TypeOptions typeOptions = new TypeOptions.Builder()
                    .setPauseBeforeBegin(0.5)
                    .build();
            
            ObjectCollection stringCollection = new ObjectCollection.Builder()
                    .withStrings(promptState.getContinueCommand())
                    .build();
            
            ActionResult typeResult = action.perform(typeOptions, stringCollection);
            
            log.info("Type result: success={}, text='{}'", 
                    typeResult.isSuccess(), typeResult.getText());
            
            return typeResult.isSuccess();
            
        } catch (Exception e) {
            log.error("Exception in separate chains approach", e);
            return false;
        }
    }
    
    /**
     * Solution 2: Use CONFIRM strategy which preserves the original ObjectCollection
     */
    public boolean executeWithConfirmStrategy() {
        log.info("=== CHAINED TRANSITION: CONFIRM Strategy Approach ===");
        
        try {
            // Create a chain with CONFIRM strategy
            // This strategy passes the original ObjectCollection to all actions
            ActionChainOptions.Builder chainBuilder = new ActionChainOptions.Builder(
                new PatternFindOptions.Builder()
                    .setPauseAfterEnd(0.5)
                    .build()
            );
            
            chainBuilder.setStrategy(ActionChainOptions.ChainingStrategy.CONFIRM);
            
            chainBuilder.then(new ClickOptions.Builder()
                    .setPauseAfterEnd(0.5)
                    .build());
            
            chainBuilder.then(new TypeOptions.Builder()
                    .setPauseBeforeBegin(0.5)
                    .build());
            
            ActionChainOptions chainOptions = chainBuilder.build();
            
            // Create collection with both images and strings
            ObjectCollection mixedCollection = new ObjectCollection.Builder()
                    .withImages(promptState.getClaudePrompt())
                    .withStrings(promptState.getContinueCommand())
                    .build();
            
            log.info("Executing chain with CONFIRM strategy...");
            log.info("Collection has {} images and {} strings", 
                    mixedCollection.getStateImages().size(),
                    mixedCollection.getStateStrings().size());
            
            // Execute using the Action's chain execution
            ActionResult result = action.perform(chainOptions, mixedCollection);
            
            log.info("Chain result: success={}, matches={}, text='{}'",
                    result.isSuccess(),
                    result.getMatchList().size(),
                    result.getText());
            
            return result.isSuccess();
            
        } catch (Exception e) {
            log.error("Exception in CONFIRM strategy approach", e);
            return false;
        }
    }
    
    /**
     * Solution 3: Custom approach using action results to coordinate
     * This mimics what a properly implemented chain should do
     */
    public boolean executeWithCustomCoordination() {
        log.info("=== CHAINED TRANSITION: Custom Coordination Approach ===");
        
        try {
            // Find the prompt
            ActionResult findResult = action.find(promptState.getClaudePrompt());
            if (!findResult.isSuccess()) {
                log.error("Find failed");
                return false;
            }
            
            // Click at the found location
            ClickOptions clickOptions = new ClickOptions.Builder()
                    .setPauseAfterEnd(0.5)
                    .build();
            
            // Use the region from the find result
            ObjectCollection clickCollection = new ObjectCollection.Builder()
                    .withRegions(findResult.getMatchList().get(0).getRegion())
                    .build();
            
            ActionResult clickResult = action.perform(clickOptions, clickCollection);
            if (!clickResult.isSuccess()) {
                log.error("Click failed");
                return false;
            }
            
            // Type the text
            TypeOptions typeOptions = new TypeOptions.Builder()
                    .setPauseBeforeBegin(0.5)
                    .build();
            
            ObjectCollection typeCollection = new ObjectCollection.Builder()
                    .withStrings(promptState.getContinueCommand())
                    .build();
            
            ActionResult typeResult = action.perform(typeOptions, typeCollection);
            
            log.info("Custom coordination result: {}", typeResult.isSuccess());
            return typeResult.isSuccess();
            
        } catch (Exception e) {
            log.error("Exception in custom coordination approach", e);
            return false;
        }
    }
}