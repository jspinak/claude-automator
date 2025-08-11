package com.claude.automator.debug;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.util.image.debug.CaptureDebugger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Command-line runner to debug capture issues.
 * Enable with: --debug-capture=true
 */
@Component
@ConditionalOnProperty(name = "debug-capture", havingValue = "true")
public class CaptureAnalyzer implements CommandLineRunner {
    
    @Autowired
    private CaptureDebugger captureDebugger;
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("RUNNING CAPTURE DEBUG ANALYSIS");
        System.out.println("=".repeat(80));
        
        // Test region for claude-prompt button
        Region testRegion = new Region(600, 900, 200, 100);
        String patternPath = "images/claude-prompt.png";
        
        // Run comprehensive debug
        captureDebugger.debugCapture(testRegion, patternPath);
        
        System.out.println("\nDebug complete. Check debug-captures directory.");
        System.out.println("Exiting debug mode...");
        
        // Exit after debug
        System.exit(0);
    }
}