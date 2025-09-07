package com.claude.automator;

import io.github.jspinak.brobot.tools.diagnostics.QuickMatchComparison;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import java.awt.GraphicsEnvironment;

/**
 * Run the quick diagnostic as a test to leverage Spring's dependency management
 */
public class RunDiagnosticTest extends TestBase {

    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true")
    public void runQuickDiagnostic() {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Skipping diagnostic in headless environment");
            return;
        }
        
        System.out.println("Running Quick Match Comparison from test context...");
        String patternPath = "images/prompt/claude-prompt-3.png";
        
        try {
            QuickMatchComparison.main(new String[]{patternPath});
        } catch (Exception e) {
            System.err.println("Error running diagnostic: " + e.getMessage());
            e.printStackTrace();
        }
    }
}