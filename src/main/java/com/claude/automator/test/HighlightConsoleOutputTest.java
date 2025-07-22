package com.claude.automator.test;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.tools.logging.visual.HighlightManager;
import io.github.jspinak.brobot.tools.logging.visual.VisualFeedbackConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;

/**
 * Test that monitors console output to verify highlight actions are being logged.
 * This test captures console output and checks for highlight-related messages.
 */
@Component
@ConditionalOnProperty(name = "test.highlight-console", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class HighlightConsoleOutputTest implements CommandLineRunner {
    
    private final Action action;
    private final HighlightManager highlightManager;
    private final VisualFeedbackConfig visualFeedbackConfig;
    
    @Override
    public void run(String... args) throws Exception {
        // Only run if explicitly requested
        if (args.length > 0 && args[0].equals("--test-highlight-console")) {
            System.out.println("\n=== HIGHLIGHT CONSOLE OUTPUT TEST ===");
            
            // Verify highlighting is enabled
            if (!visualFeedbackConfig.isEnabled()) {
                System.err.println("ERROR: Highlighting is not enabled! Set brobot.highlight.enabled=true");
                System.exit(1);
            }
            
            boolean allTestsPassed = true;
            
            try {
                // Test 1: Direct highlight with Action
                allTestsPassed &= testDirectHighlightWithAction();
                
                // Test 2: Highlight via HighlightManager
                allTestsPassed &= testHighlightViaManager();
                
                // Test 3: Multiple regions highlight
                allTestsPassed &= testMultipleRegionsHighlight();
                
            } catch (Exception e) {
                System.err.println("Test failed with exception: " + e.getMessage());
                e.printStackTrace();
                allTestsPassed = false;
            }
            
            System.out.println("\n=== TEST RESULTS ===");
            if (allTestsPassed) {
                System.out.println("✓ All highlight console output tests PASSED!");
                System.exit(0);
            } else {
                System.err.println("✗ Some highlight console output tests FAILED!");
                System.exit(1);
            }
        }
    }
    
    private boolean testDirectHighlightWithAction() {
        System.out.println("\n--- Test 1: Direct Highlight with Action ---");
        
        // Capture console output
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream captureStream = new PrintStream(baos);
        System.setOut(captureStream);
        
        try {
            // Create a test region
            Region testRegion = new Region(100, 100, 200, 150);
            
            // Create highlight options
            HighlightOptions highlightOptions = new HighlightOptions.Builder()
                .setHighlightSeconds(1.0)
                .setHighlightColor("RED")
                .build();
            
            // Create object collection with the region
            ObjectCollection collection = new ObjectCollection.Builder()
                .withRegions(testRegion)
                .build();
            
            // Perform highlight action
            ActionResult result = action.perform(highlightOptions, collection);
            
            // Flush the stream
            captureStream.flush();
            
            // Restore original output
            System.setOut(originalOut);
            
            // Get captured output
            String consoleOutput = baos.toString();
            
            // Check for highlight-related output
            boolean foundHighlight = consoleOutput.toLowerCase().contains("highlight") ||
                                   consoleOutput.contains("HighlightOptions") ||
                                   consoleOutput.contains("✓") && consoleOutput.contains("Highlight");
            
            System.out.println("Console output captured: " + consoleOutput.length() + " bytes");
            System.out.println("Found highlight action: " + foundHighlight);
            
            if (!foundHighlight) {
                System.err.println("ERROR: No highlight action found in console output!");
                System.err.println("Captured output: " + consoleOutput);
            }
            
            return foundHighlight && result.isSuccess();
            
        } catch (Exception e) {
            System.setOut(originalOut);
            System.err.println("Test 1 failed: " + e.getMessage());
            return false;
        }
    }
    
    private boolean testHighlightViaManager() {
        System.out.println("\n--- Test 2: Highlight via HighlightManager ---");
        
        // Capture console output
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream captureStream = new PrintStream(baos);
        System.setOut(captureStream);
        
        try {
            // Create a test region
            Region testRegion = new Region(200, 200, 150, 150);
            
            // Highlight as search region
            highlightManager.highlightSearchRegions(Collections.singletonList(testRegion));
            
            // Wait a bit for async operation to start
            Thread.sleep(100);
            
            // Create a match and highlight it
            Match testMatch = new Match.Builder()
                .setRegion(testRegion)
                .setSimScore(0.95)
                .build();
            highlightManager.highlightMatches(Collections.singletonList(testMatch));
            
            // Wait for async operations to complete
            Thread.sleep(500);
            
            // Flush the stream
            captureStream.flush();
            
            // Restore original output
            System.setOut(originalOut);
            
            // Get captured output
            String consoleOutput = baos.toString();
            
            // Check for highlight-related output
            boolean foundHighlight = consoleOutput.toLowerCase().contains("highlight") ||
                                   consoleOutput.contains("HighlightOptions") ||
                                   consoleOutput.contains("Highlighting") ||
                                   consoleOutput.contains("Highlighted");
            
            System.out.println("Console output captured: " + consoleOutput.length() + " bytes");
            System.out.println("Found highlight action: " + foundHighlight);
            
            if (!foundHighlight) {
                System.err.println("ERROR: No highlight action found in console output!");
                System.err.println("Captured output: " + consoleOutput);
            }
            
            return foundHighlight;
            
        } catch (Exception e) {
            System.setOut(originalOut);
            System.err.println("Test 2 failed: " + e.getMessage());
            return false;
        }
    }
    
    private boolean testMultipleRegionsHighlight() {
        System.out.println("\n--- Test 3: Multiple Regions Highlight ---");
        
        // Capture console output
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream captureStream = new PrintStream(baos);
        System.setOut(captureStream);
        
        try {
            // Create multiple test regions
            Region region1 = new Region(50, 50, 100, 100);
            Region region2 = new Region(200, 50, 100, 100);
            Region region3 = new Region(350, 50, 100, 100);
            
            // Create highlight options
            HighlightOptions highlightOptions = new HighlightOptions.Builder()
                .setHighlightSeconds(1.5)
                .setHighlightColor("GREEN")
                .build();
            
            // Create object collection with multiple regions
            ObjectCollection collection = new ObjectCollection.Builder()
                .withRegions(region1, region2, region3)
                .build();
            
            // Perform highlight action
            ActionResult result = action.perform(highlightOptions, collection);
            
            // Flush the stream
            captureStream.flush();
            
            // Restore original output
            System.setOut(originalOut);
            
            // Get captured output
            String consoleOutput = baos.toString();
            
            // Check for highlight-related output
            boolean foundHighlight = consoleOutput.toLowerCase().contains("highlight") ||
                                   consoleOutput.contains("HighlightOptions") ||
                                   consoleOutput.contains("✓") && consoleOutput.contains("Highlight");
            
            // For multiple regions, we might see multiple highlight entries
            int highlightCount = countOccurrences(consoleOutput, "HighlightOptions");
            
            System.out.println("Console output captured: " + consoleOutput.length() + " bytes");
            System.out.println("Found highlight action: " + foundHighlight);
            System.out.println("Highlight occurrences: " + highlightCount);
            
            if (!foundHighlight) {
                System.err.println("ERROR: No highlight action found in console output!");
                System.err.println("Captured output: " + consoleOutput);
            }
            
            return foundHighlight && result.isSuccess();
            
        } catch (Exception e) {
            System.setOut(originalOut);
            System.err.println("Test 3 failed: " + e.getMessage());
            return false;
        }
    }
    
    private int countOccurrences(String str, String findStr) {
        int count = 0;
        int lastIndex = 0;
        while ((lastIndex = str.indexOf(findStr, lastIndex)) != -1) {
            count++;
            lastIndex += findStr.length();
        }
        return count;
    }
}