package com.claude.automator.automation;

import com.claude.automator.states.PromptState;
import com.claude.automator.states.WorkingState;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SimpleAutomation {

    @Autowired
    private Action action;

    @Autowired
    private PromptState promptState;

    @Autowired
    private WorkingState workingState;

    public void run() {
        boolean foundPrompt = findClaudePrompt();
        
        if (foundPrompt) {
            System.out.println("✓ Claude Prompt");
            boolean foundIcon = findClaudeIcon();
            
            if (foundIcon) {
                System.out.println("✓ Claude Icon");
            } else {
                System.out.println("✗ Claude Icon");
            }
        } else {
            System.out.println("✗ Claude Prompt");
        }
    }

    private boolean findClaudePrompt() {
        PatternFindOptions options = new PatternFindOptions.Builder()
            .setSimilarity(0.7)
            .build();
        
        ObjectCollection promptCollection = new ObjectCollection.Builder()
            .withImages(promptState.getClaudePrompt())
            .build();
        
        ActionResult result = action.perform(options, promptCollection);
        
        // Highlight found matches
        if (result.isSuccess() && !result.getMatchList().isEmpty()) {
            HighlightOptions highlightOptions = new HighlightOptions.Builder()
                .setHighlightSeconds(2.0)
                .setHighlightColor("GREEN")
                .build();
            
            ObjectCollection highlightCollection = new ObjectCollection.Builder()
                .withRegions(result.getMatchList().get(0).getRegion())
                .build();
            
            action.perform(highlightOptions, highlightCollection);
        }
        
        return result.isSuccess();
    }

    private boolean findClaudeIcon() {
        PatternFindOptions options = new PatternFindOptions.Builder()
            .setSimilarity(0.7)
            .build();
        
        ObjectCollection iconCollection = new ObjectCollection.Builder()
            .withImages(workingState.getClaudeIcon())
            .build();

        ActionResult result = action.perform(options, iconCollection);
        
        // Highlight found matches
        if (result.isSuccess() && !result.getMatchList().isEmpty()) {
            HighlightOptions highlightOptions = new HighlightOptions.Builder()
                .setHighlightSeconds(2.0)
                .setHighlightColor("BLUE")
                .build();
            
            ObjectCollection highlightCollection = new ObjectCollection.Builder()
                .withRegions(result.getMatchList().get(0).getRegion())
                .build();
            
            action.perform(highlightOptions, highlightCollection);
        }
        
        return result.isSuccess();
    }
}