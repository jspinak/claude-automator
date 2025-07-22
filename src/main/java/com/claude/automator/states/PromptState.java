package com.claude.automator.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateString;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

/**
 * Represents the Prompt state where Claude is waiting for input.
 * Uses the new @State annotation for automatic registration.
 */
@State(initial = true)
@Getter
@Slf4j
public class PromptState {
    
    private StateImage claudePrompt;
    private StateString continueCommand;
    
    @Autowired(required = false)
    private BrobotLogger brobotLogger;
    
    public PromptState() {
        log.info("Creating PromptState");
        
        // Initialize the claude prompt image
        claudePrompt = new StateImage.Builder()
            .addPatterns("prompt/claude-prompt-1","prompt/claude-prompt-2","prompt/claude-prompt-3")
            .setName("ClaudePrompt")
            .setSearchRegionForAllPatterns(new Region(10, 400, 700, 350))
            .build();
        
        // Create the continue command as a string
        continueCommand = new StateString.Builder()
            .setName("ContinueCommand")
            .setString("continue\n")
            .build();
        
        log.info("PromptState created successfully");
    }
    
    @jakarta.annotation.PostConstruct
    private void init() {
        if (brobotLogger == null) {
            log.warn("BrobotLogger not available for PromptState initialization logging");
            return;
        }
        
        try (var timer = brobotLogger.startTimer("PromptStatePostInit")) {
            brobotLogger.log()
                .observation("PromptState post-initialization")
                .metadata("stateType", "initial")
                .metadata("className", this.getClass().getSimpleName())
                .log();
            
            // Log pattern details
            List<String> patterns = Arrays.asList(
                "prompt/claude-prompt-1",
                "prompt/claude-prompt-2", 
                "prompt/claude-prompt-3"
            );
            
            brobotLogger.log()
                .observation("Claude prompt patterns loaded")
                .metadata("patternCount", patterns.size())
                .metadata("patterns", patterns)
                .metadata("imageName", claudePrompt.getName())
                .metadata("searchRegions", claudePrompt.getAllSearchRegions().size())
                .log();
            
            // Log continue command details
            brobotLogger.log()
                .observation("Continue command details")
                .metadata("commandName", continueCommand.getName())
                .metadata("commandText", continueCommand.getString().replace("\n", "\\n"))
                .log();
            
            // Log state registration info
            brobotLogger.log()
                .observation("PromptState components initialized")
                .metadata("componentsCount", 2)
                .metadata("stateAnnotation", "@State(initial=true)")
                .metadata("initializationTime", timer.stop())
                .log();
        } catch (Exception e) {
            log.error("Error in PromptState post-initialization logging", e);
        }
    }
}