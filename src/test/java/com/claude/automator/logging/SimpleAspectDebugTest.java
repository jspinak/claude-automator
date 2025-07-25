package com.claude.automator.logging;

import com.claude.automator.ClaudeAutomatorApplication;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.logging.unified.LogEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

@SpringBootTest(classes = ClaudeAutomatorApplication.class)
@TestPropertySource(properties = {
    "brobot.logging.verbosity=QUIET",
    "brobot.logging.console.capture-enabled=false",
    "brobot.framework.mock=true",
    "logging.level.root=INFO"
})
public class SimpleAspectDebugTest {
    
    @Autowired
    private BrobotLogger brobotLogger;
    
    @Test
    void testDirectLogging() {
        // Capture console output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
        
        try {
            // Test direct logging with all information
            brobotLogger.log()
                .type(LogEvent.Type.ACTION)
                .level(LogEvent.Level.INFO)
                .action("FIND_COMPLETE")
                .target("TestState.TestImage")
                .success(false)
                .duration(234L)
                .log();
                
            System.out.flush();
            String output = outputStream.toString();
            
            // Restore output
            System.setOut(originalOut);
            
            System.out.println("=== Direct Logging Test ===");
            System.out.println("Expected: ✗ Find TestState.TestImage • 234ms");
            System.out.println("Actual  : " + output.trim());
            
            // Check if output is correct
            if (output.trim().equals("✗ Find TestState.TestImage • 234ms")) {
                System.out.println("✅ PASS: Direct logging works correctly");
            } else {
                System.out.println("❌ FAIL: Direct logging output incorrect");
                System.out.println("Raw output bytes: " + java.util.Arrays.toString(output.getBytes()));
            }
            
        } finally {
            System.setOut(originalOut);
        }
    }
}