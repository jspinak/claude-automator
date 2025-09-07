package com.claude.automator.diagnostics;

import com.claude.automator.TestBase;
import io.github.jspinak.brobot.tools.diagnostics.ComprehensiveMatchingDiagnostic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.sikuli.basics.Settings;
import org.springframework.test.context.TestPropertySource;

import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Run comprehensive diagnostic to identify why SikuliX IDE matches at 0.99
 * while Brobot only achieves 0.69 similarity.
 * 
 * Run this test in IntelliJ on Windows to diagnose the matching discrepancy.
 */
@TestPropertySource(properties = {
    "brobot.core.mock=false",
    "brobot.dpi.resize-factor=0.8",  // Test with your DPI setting
    "brobot.logging.verbosity=VERBOSE"
})
public class RunComprehensiveDiagnostic extends TestBase {

    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true")
    public void runDiagnosticForClaudePatterns() {
        // Skip if headless
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Skipping diagnostic in headless environment");
            return;
        }

        System.out.println("\n=== RUNNING COMPREHENSIVE DIAGNOSTIC FOR CLAUDE PATTERNS ===");
        System.out.println("Current Settings.AlwaysResize: " + Settings.AlwaysResize);
        
        ComprehensiveMatchingDiagnostic diagnostic = new ComprehensiveMatchingDiagnostic();
        
        // Test each claude prompt pattern
        String[] patterns = {
            "images/prompt/claude-prompt-1.png",
            "images/prompt/claude-prompt-2.png",
            "images/prompt/claude-prompt-3.png"
        };
        
        for (String pattern : patterns) {
            File patternFile = new File(pattern);
            if (patternFile.exists()) {
                System.out.println("\n\n================================================");
                System.out.println("Testing pattern: " + pattern);
                System.out.println("================================================");
                diagnostic.runFullDiagnosis(patternFile.getAbsolutePath());
            } else {
                System.err.println("Pattern not found: " + patternFile.getAbsolutePath());
            }
        }
        
        System.out.println("\n=== DIAGNOSTIC COMPLETE ===");
        System.out.println("\nKey things to check:");
        System.out.println("1. Does 'Direct SikuliX (as IDE)' show ~0.99 similarity?");
        System.out.println("2. Does 'Brobot-Loaded Pattern' show ~0.69 similarity?");
        System.out.println("3. What does 'Pixel Analysis' show about loading differences?");
        System.out.println("4. Which color space conversion gives best results?");
        System.out.println("5. Which image type gives best results?");
    }
    
    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true")
    public void testSpecificPattern() {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Skipping diagnostic in headless environment");
            return;
        }

        // Test a specific pattern in detail
        String patternPath = "images/prompt/claude-prompt-3.png";
        File patternFile = new File(patternPath);
        
        if (!patternFile.exists()) {
            System.err.println("Pattern not found: " + patternFile.getAbsolutePath());
            return;
        }
        
        ComprehensiveMatchingDiagnostic diagnostic = new ComprehensiveMatchingDiagnostic();
        
        // Test with different Settings.AlwaysResize values
        double[] resizeFactors = {1.0, 0.8, 0.667, 0.5};
        
        for (double factor : resizeFactors) {
            Settings.AlwaysResize = (float) factor;
            System.out.println("\n\n================================================");
            System.out.println("Testing with Settings.AlwaysResize = " + factor);
            System.out.println("================================================");
            diagnostic.runFullDiagnosis(patternFile.getAbsolutePath());
        }
    }
}