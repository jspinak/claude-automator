package com.claude.automator.test;

import com.claude.automator.states.PromptState;
import com.claude.automator.states.WorkingState;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.claude.automator", "io.github.jspinak.brobot"})
@Slf4j
public class DeclarativeRegionTest implements CommandLineRunner {
    
    @Autowired
    private Action action;
    
    @Autowired
    private PromptState promptState;
    
    @Autowired
    private WorkingState workingState;
    
    public static void main(String[] args) {
        SpringApplication.run(DeclarativeRegionTest.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        log.info("=== DECLARATIVE REGION TEST ===");
        
        // First, find the ClaudePrompt to establish the reference region
        StateImage claudePrompt = promptState.getClaudePrompt();
        log.info("Finding ClaudePrompt...");
        ActionResult promptResult = action.find(claudePrompt);
        
        if (promptResult.isSuccess()) {
            log.info("✅ Found ClaudePrompt at: {}", 
                    promptResult.getMatchList().get(0).getRegion());
            
            // Now check if ClaudeIcon has its search regions set
            StateImage claudeIcon = workingState.getClaudeIcon();
            log.info("\n=== CLAUDE ICON SEARCH REGIONS ===");
            log.info("SearchRegionOnObject config: {}", 
                    claudeIcon.getSearchRegionOnObject());
            
            // Check each pattern's search regions
            for (Pattern pattern : claudeIcon.getPatterns()) {
                log.info("Pattern: {}", pattern.getName());
                log.info("  Search regions: {}", 
                        pattern.getSearchRegions().getAllRegions());
                log.info("  Fixed region: {}", 
                        pattern.getSearchRegions().getFixedRegion());
            }
            
            // Try to find ClaudeIcon - it should search near ClaudePrompt
            log.info("\n=== FINDING CLAUDE ICON ===");
            ActionResult iconResult = action.find(claudeIcon);
            
            if (iconResult.isSuccess()) {
                log.info("✅ Found ClaudeIcon at: {}", 
                        iconResult.getMatchList().get(0).getRegion());
                
                // Check if the icon was found near the prompt
                int promptX = promptResult.getMatchList().get(0).getRegion().getX();
                int promptY = promptResult.getMatchList().get(0).getRegion().getY();
                int iconX = iconResult.getMatchList().get(0).getRegion().getX();
                int iconY = iconResult.getMatchList().get(0).getRegion().getY();
                
                int distanceX = Math.abs(iconX - promptX);
                int distanceY = Math.abs(iconY - promptY);
                
                log.info("Distance from prompt: X={}, Y={}", distanceX, distanceY);
                
                if (distanceX < 100 && distanceY < 100) {
                    log.info("✅ DECLARATIVE REGIONS WORKING: Icon found near prompt!");
                } else {
                    log.warn("⚠️ Icon found but far from prompt - declarative regions may not be working");
                }
            } else {
                log.error("❌ Could not find ClaudeIcon");
            }
            
        } else {
            log.error("❌ Could not find ClaudePrompt - cannot test declarative regions");
        }
        
        log.info("\n=== TEST COMPLETE ===");
        System.exit(0);
    }
}