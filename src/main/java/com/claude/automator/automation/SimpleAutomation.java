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
        System.out.println("Starting SimpleApplication automation...");
        
        System.out.println("Looking for Claude Prompt...");
        boolean foundPrompt = findClaudePrompt();
        
        if (foundPrompt) {
            System.out.println("Claude Prompt found!");
            System.out.println("Looking for Claude Icon...");
            boolean foundIcon = findClaudeIcon();
            
            if (foundIcon) {
                System.out.println("Claude Icon found!");
                System.out.println("Both elements found successfully.");
            } else {
                System.out.println("Claude Icon not found.");
            }
        } else {
            System.out.println("Claude Prompt not found.");
        }
        
        System.out.println("SimpleApplication automation completed.");
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
            System.out.println("Highlighting Claude Prompt match...");
            HighlightOptions highlightOptions = new HighlightOptions.Builder()
                .setHighlightSeconds(2.0)
                .setHighlightColor("GREEN")
                .build();
            
            // Create collection with matched regions for highlighting
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
            System.out.println("Highlighting Claude Icon match...");
            HighlightOptions highlightOptions = new HighlightOptions.Builder()
                .setHighlightSeconds(2.0)
                .setHighlightColor("BLUE")
                .build();
            
            // Create collection with matched regions for highlighting
            ObjectCollection highlightCollection = new ObjectCollection.Builder()
                .withRegions(result.getMatchList().get(0).getRegion())
                .build();
            
            action.perform(highlightOptions, highlightCollection);
        }
        
        return result.isSuccess();
    }
}