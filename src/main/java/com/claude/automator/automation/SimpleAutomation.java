package com.claude.automator.automation;

import com.claude.automator.states.PromptState;
import com.claude.automator.states.WorkingState;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;

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
        
        ActionResult report = action.perform(options, promptCollection);
        return report.isSuccess();
    }

    private boolean findClaudeIcon() {
        PatternFindOptions options = new PatternFindOptions.Builder()
            .setSimilarity(0.7)
            .build();
        
        ObjectCollection iconCollection = new ObjectCollection.Builder()
            .withImages(workingState.getClaudeIcon())
            .build();

        ActionResult report = action.perform(options, iconCollection);
        return report.isSuccess();
    }
}