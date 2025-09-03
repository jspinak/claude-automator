package com.claude.automator.debug;

import com.claude.automator.states.PromptState;
import com.claude.automator.states.WorkingState;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.config.core.BrobotAutoConfiguration;
import io.github.jspinak.brobot.tools.diagnostics.PatternMatchingDiagnostics;
import lombok.extern.slf4j.Slf4j;
import org.sikuli.basics.Settings;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * Standalone diagnostic application for testing pattern matching with DPI scaling.
 * Run this to get detailed diagnostics about why patterns may not be matching.
 * 
 * Usage: Run as a Spring Boot application
 * 
 * This will:
 * 1. Log all DPI and scaling settings
 * 2. Test pattern matching with different similarity thresholds
 * 3. Test with different AlwaysResize values
 * 4. Generate visual comparison images
 * 5. Provide specific recommendations
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.claude.automator", "io.github.jspinak.brobot"})
@Import(BrobotAutoConfiguration.class)
@Slf4j
public class StandalonePatternDiagnostic {

    public static void main(String[] args) {
        // Force debug output
        System.setProperty("sikuli.Debug", "3");
        System.setProperty("sikuli.Silence", "false");
        System.setProperty("java.awt.headless", "false");
        
        log.info("Starting Pattern Matching Diagnostic Tool...");
        log.info("DPI configuration will be handled by Brobot ApplicationContextInitializer");
        SpringApplication.run(StandalonePatternDiagnostic.class, args);
    }

    @Bean
    public CommandLineRunner diagnosticRunner(
            PatternMatchingDiagnostics diagnostics,
            PromptState promptState,
            WorkingState workingState,
            Action action) {
        
        return args -> {
            log.info("╔════════════════════════════════════════════════════════════════════╗");
            log.info("║          STANDALONE PATTERN MATCHING DIAGNOSTIC TOOL               ║");
            log.info("╚════════════════════════════════════════════════════════════════════╝");
            
            // Log current configuration
            logCurrentConfiguration();
            
            // Test Prompt State patterns
            log.info("\n");
            log.info("═══════════════════════════════════════════════════════════════════");
            log.info("                    TESTING PROMPT STATE PATTERNS");
            log.info("═══════════════════════════════════════════════════════════════════");
            diagnostics.diagnoseStateImage(promptState.getClaudePrompt(), "PROMPT_STATE");
            
            // Test Working State patterns
            log.info("\n");
            log.info("═══════════════════════════════════════════════════════════════════");
            log.info("                    TESTING WORKING STATE PATTERNS");
            log.info("═══════════════════════════════════════════════════════════════════");
            diagnostics.diagnoseStateImage(workingState.getClaudeIcon(), "WORKING_STATE");
            
            // Test with different AlwaysResize values
            testWithDifferentScalingFactors(diagnostics, promptState, workingState);
            
            // Final recommendations
            provideFinalRecommendations();
            
            log.info("\n");
            log.info("═══════════════════════════════════════════════════════════════════");
            log.info("                    DIAGNOSTIC COMPLETE");
            log.info("═══════════════════════════════════════════════════════════════════");
            log.info("Check the 'pattern-matching-debug' directory for visual comparisons");
            log.info("═══════════════════════════════════════════════════════════════════");
        };
    }
    
    private void logCurrentConfiguration() {
        log.info("");
        log.info("=== CURRENT CONFIGURATION ===");
        log.info("Settings.AlwaysResize: {}", Settings.AlwaysResize);
        log.info("Settings.MinSimilarity: {}", Settings.MinSimilarity);
        log.info("Settings.CheckLastSeen: {}", Settings.CheckLastSeen);
        log.info("Settings.WaitScanRate: {}", Settings.WaitScanRate);
        log.info("Settings.ObserveScanRate: {}", Settings.ObserveScanRate);
        
        // Log JVM and system info
        log.info("");
        log.info("=== SYSTEM INFO ===");
        log.info("OS: {} {}", System.getProperty("os.name"), System.getProperty("os.version"));
        log.info("Java Version: {}", System.getProperty("java.version"));
        log.info("Screen Resolution: {}x{}", 
            java.awt.Toolkit.getDefaultToolkit().getScreenSize().width,
            java.awt.Toolkit.getDefaultToolkit().getScreenSize().height);
        log.info("Screen DPI: {}", java.awt.Toolkit.getDefaultToolkit().getScreenResolution());
    }
    
    private void testWithDifferentScalingFactors(
            PatternMatchingDiagnostics diagnostics,
            PromptState promptState,
            WorkingState workingState) {
        
        log.info("\n");
        log.info("═══════════════════════════════════════════════════════════════════");
        log.info("            TESTING WITH DIFFERENT SCALING FACTORS");
        log.info("═══════════════════════════════════════════════════════════════════");
        
        float originalResize = Settings.AlwaysResize;
        float[] testFactors = {0.5f, 0.67f, 0.75f, 0.8f, 0.9f, 1.0f, 1.1f, 1.25f, 1.5f};
        
        for (float factor : testFactors) {
            Settings.AlwaysResize = factor;
            log.info("\n");
            log.info(">>> Testing with AlwaysResize = {} <<<", factor);
            
            // Quick test - just check if patterns can be found
            testQuickFind(promptState, workingState);
        }
        
        // Restore original
        Settings.AlwaysResize = originalResize;
        log.info("\n");
        log.info("Restored AlwaysResize to original value: {}", originalResize);
    }
    
    private void testQuickFind(PromptState promptState, WorkingState workingState) {
        try {
            // Test prompt patterns
            org.sikuli.script.Screen screen = new org.sikuli.script.Screen();
            
            for (var pattern : promptState.getClaudePrompt().getPatterns()) {
                if (pattern.getImage() != null && pattern.getImage().getBufferedImage() != null) {
                    org.sikuli.script.Pattern sikuliPattern = 
                        new org.sikuli.script.Pattern(pattern.getImage().getBufferedImage());
                    sikuliPattern.similar(0.7);
                    
                    var match = screen.exists(sikuliPattern, 0.1);
                    if (match != null) {
                        log.info("  ✓ PROMPT pattern '{}' FOUND with score {}", 
                            pattern.getName(), match.getScore());
                    } else {
                        log.info("  ✗ PROMPT pattern '{}' NOT FOUND", pattern.getName());
                    }
                }
            }
            
            // Test icon patterns
            for (var pattern : workingState.getClaudeIcon().getPatterns()) {
                if (pattern.getImage() != null && pattern.getImage().getBufferedImage() != null) {
                    org.sikuli.script.Pattern sikuliPattern = 
                        new org.sikuli.script.Pattern(pattern.getImage().getBufferedImage());
                    sikuliPattern.similar(0.7);
                    
                    var match = screen.exists(sikuliPattern, 0.1);
                    if (match != null) {
                        log.info("  ✓ ICON pattern '{}' FOUND with score {}", 
                            pattern.getName(), match.getScore());
                    } else {
                        log.info("  ✗ ICON pattern '{}' NOT FOUND", pattern.getName());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error during quick find test: ", e);
        }
    }
    
    private void provideFinalRecommendations() {
        log.info("\n");
        log.info("═══════════════════════════════════════════════════════════════════");
        log.info("                    FINAL RECOMMENDATIONS");
        log.info("═══════════════════════════════════════════════════════════════════");
        
        log.info("");
        log.info("Based on the diagnostic results:");
        log.info("");
        log.info("1. IF PATTERNS WORK IN SIKULI IDE BUT NOT IN BROBOT:");
        log.info("   - SikuliX IDE may be using different default settings");
        log.info("   - Check if SikuliX IDE has AlwaysResize set to a different value");
        log.info("   - The IDE might be auto-adjusting for your display");
        log.info("");
        log.info("2. FOR 125% WINDOWS SCALING:");
        log.info("   - Use Settings.AlwaysResize = 0.8f");
        log.info("   - Or re-capture patterns at 100% scaling");
        log.info("");
        log.info("3. FOR 150% WINDOWS SCALING:");
        log.info("   - Use Settings.AlwaysResize = 0.67f");
        log.info("");
        log.info("4. FOR 200% WINDOWS SCALING:");
        log.info("   - Use Settings.AlwaysResize = 0.5f");
        log.info("");
        log.info("5. TO MANUALLY OVERRIDE DPI SCALING:");
        log.info("   Add to application.properties:");
        log.info("   brobot.dpi.resize-factor=0.8  # or your desired value");
        log.info("");
        log.info("6. TO DISABLE AUTO-SCALING:");
        log.info("   brobot.dpi.resize-factor=1.0");
        log.info("");
        log.info("7. IF NOTHING WORKS:");
        log.info("   - Re-capture all patterns on the current display");
        log.info("   - Ensure patterns are saved as PNG (not JPG)");
        log.info("   - Check if UI has transparency or anti-aliasing issues");
    }
}