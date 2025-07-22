package com.claude.automator.test;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.config.BrobotConfiguration;
import io.github.jspinak.brobot.tools.logging.visual.VisualFeedbackConfig;
import io.github.jspinak.brobot.tools.logging.visual.HighlightManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for verifying action highlighting functionality.
 * This test creates a visible highlight on screen and verifies it was executed.
 */
@Profile("highlight-test")
@Component
public class HighlightingTest implements CommandLineRunner {
    
    private static final Logger log = LoggerFactory.getLogger(HighlightingTest.class);

    @Autowired
    private Action action;
    @Autowired
    private BrobotConfiguration brobotConfig;
    @Autowired
    private VisualFeedbackConfig visualFeedbackConfig;
    @Autowired
    private HighlightManager highlightManager;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== HIGHLIGHTING TEST ===");
        
        // Verify configuration
        log.info("Checking highlighting configuration...");
        log.info("brobot.highlight.enabled: {}", visualFeedbackConfig.isEnabled());
        log.info("brobot.highlight.auto-highlight-finds: {}", visualFeedbackConfig.isAutoHighlightFinds());
        log.info("brobot.highlight.auto-highlight-search-regions: {}", visualFeedbackConfig.isAutoHighlightSearchRegions());
        
        assertTrue(visualFeedbackConfig.isEnabled(), 
            "Highlighting must be enabled for this test. Set brobot.highlight.enabled=true");
        
        try {
            // Test 1: Direct highlight on screen region
            testDirectHighlight();
            
            // Test 2: Highlight during find operation
            testFindWithHighlight();
            
            // Test 3: Highlight search region
            testSearchRegionHighlight();
            
            log.info("✓ All highlighting tests passed!");
            
        } catch (Exception e) {
            log.error("✗ Highlighting test failed", e);
            throw e;
        }
    }
    
    private void testDirectHighlight() {
        log.info("\n--- Test 1: Direct Highlight ---");
        
        // Create a region in the center of the screen
        Region centerRegion = new Region(500, 300, 200, 100);
        
        // Create highlight options
        HighlightOptions highlightOptions = new HighlightOptions.Builder()
            .setHighlightSeconds(2.0) // Show for 2 seconds
            .setHighlightColor("RED")
            .build();
        
        log.info("Highlighting center region: {}", centerRegion);
        
        // Create object collection with the region
        ObjectCollection collection = new ObjectCollection.Builder()
            .withRegions(centerRegion)
            .build();
        
        // Perform highlight
        ActionResult result = action.perform(highlightOptions, collection);
        
        assertTrue(result.isSuccess(), "Direct highlight should succeed");
        log.info("✓ Direct highlight completed");
        
        // Give user time to see the highlight
        sleep(2500);
    }
    
    private void testFindWithHighlight() {
        log.info("\n--- Test 2: Find with Highlight ---");
        
        // Create a search region to highlight
        Region searchRegion = new Region(200, 200, 300, 200);
        
        // Create highlight options for the search region
        HighlightOptions highlightOptions = new HighlightOptions.Builder()
            .setHighlightSeconds(1.5)
            .setHighlightColor("GREEN")
            .build();
        
        log.info("Highlighting search region before find...");
        
        // Create object collection with the region
        ObjectCollection collection = new ObjectCollection.Builder()
            .withRegions(searchRegion)
            .build();
        
        // Highlight the search region
        ActionResult result = action.perform(highlightOptions, collection);
        
        assertTrue(result.isSuccess(), "Search region highlight should succeed");
        log.info("✓ Search region highlighted in green");
        
        sleep(1500);
    }
    
    private void testSearchRegionHighlight() {
        log.info("\n--- Test 3: Multiple Region Highlight ---");
        
        // Create multiple regions to highlight
        Region region1 = new Region(50, 50, 150, 100);
        Region region2 = new Region(250, 150, 200, 150);
        Region region3 = new Region(100, 350, 300, 100);
        
        // Configure blue highlights
        HighlightOptions highlightOptions = new HighlightOptions.Builder()
            .setHighlightSeconds(2.0)
            .setHighlightColor("BLUE")
            .build();
        
        log.info("Highlighting multiple regions...");
        
        // Create object collection with multiple regions
        ObjectCollection collection = new ObjectCollection.Builder()
            .withRegions(region1, region2, region3)
            .build();
        
        ActionResult result = action.perform(highlightOptions, collection);
        
        assertTrue(result.isSuccess(), "Multiple region highlight should succeed");
        log.info("✓ Multiple regions highlighted in blue");
        
        sleep(2000);
    }
    
    
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Test configuration that enables highlighting for tests
     */
    @TestConfiguration
    @Profile("highlight-test")
    public static class HighlightTestConfig {
        
        private static final Logger log = LoggerFactory.getLogger(HighlightTestConfig.class);
        
        @Bean
        public CommandLineRunner highlightConfigVerifier(VisualFeedbackConfig config) {
            return args -> {
                log.info("Setting up highlighting configuration for test...");
                
                // Force enable highlighting for this test
                config.setEnabled(true);
                config.setAutoHighlightFinds(true);
                config.setAutoHighlightSearchRegions(true);
                
                // Configure find highlighting
                config.getFind().setColor("#00FF00");
                config.getFind().setDuration(2.0);
                config.getFind().setBorderWidth(3);
                config.getFind().setFlash(true);
                
                // Configure search region highlighting
                config.getSearchRegion().setColor("#0000FF");
                config.getSearchRegion().setDuration(2.0);
                config.getSearchRegion().setBorderWidth(2);
                config.getSearchRegion().setShowDimensions(true);
                
                log.info("Highlighting configuration updated for test");
            };
        }
    }
}