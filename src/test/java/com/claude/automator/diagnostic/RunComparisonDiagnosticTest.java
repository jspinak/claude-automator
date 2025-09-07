package com.claude.automator.diagnostic;

import com.claude.automator.TestBase;
import com.claude.automator.states.PromptState;
import com.claude.automator.states.WorkingState;
import io.github.jspinak.brobot.tools.diagnostics.SikuliXBrobotComparisonDiagnostic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test class to run the SikuliXBrobotComparisonDiagnostic directly.
 * 
 * Run this test in IntelliJ by:
 * 1. Right-clicking on this file
 * 2. Select "Run 'RunComparisonDiagnosticTest'"
 * 
 * Or run a specific test method by clicking the green arrow next to it.
 */
public class RunComparisonDiagnosticTest extends TestBase {

    @Autowired
    private SikuliXBrobotComparisonDiagnostic comparisonDiagnostic;
    
    @Autowired
    private PromptState promptState;
    
    @Autowired
    private WorkingState workingState;
    
    @Test
    public void runFullComparison() {
        System.out.println("\n==================================================");
        System.out.println("    RUNNING SIKULIX vs BROBOT COMPARISON");
        System.out.println("==================================================\n");
        
        // Test Prompt State patterns
        comparisonDiagnostic.comparePatternMatching(
            promptState.getClaudePrompt(), 
            "PROMPT_STATE"
        );
        
        // Test Working State patterns
        comparisonDiagnostic.comparePatternMatching(
            workingState.getClaudeIcon(), 
            "WORKING_STATE"
        );
        
        System.out.println("\n==================================================");
        System.out.println("    COMPARISON COMPLETE - CHECK OUTPUT ABOVE");
        System.out.println("==================================================\n");
    }
    
    @Test
    public void testPromptPatternsOnly() {
        System.out.println("\n==================================================");
        System.out.println("    TESTING PROMPT PATTERNS ONLY");
        System.out.println("==================================================\n");
        
        comparisonDiagnostic.comparePatternMatching(
            promptState.getClaudePrompt(), 
            "PROMPT_STATE"
        );
    }
    
    @Test
    public void testWorkingPatternsOnly() {
        System.out.println("\n==================================================");
        System.out.println("    TESTING WORKING PATTERNS ONLY");
        System.out.println("==================================================\n");
        
        comparisonDiagnostic.comparePatternMatching(
            workingState.getClaudeIcon(), 
            "WORKING_STATE"
        );
    }
}