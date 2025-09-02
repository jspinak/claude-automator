package com.claude.automator;

import com.claude.automator.config.ImagePathConfiguration;
import org.junit.jupiter.api.Test;
import org.sikuli.script.ImagePath;
import org.sikuli.script.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ClaudeAutomatorApplication.class)
@TestPropertySource(properties = {
    "brobot.logging.verbosity=VERBOSE",
    "brobot.core.image-path=images"
})
public class ImagePathConfigurationTest {
    
    @Autowired
    private ImagePathConfiguration imagePathConfiguration;
    
    @Test
    public void testImagePathConfigured() {
        System.out.println("=== Testing ImagePath Configuration ===");
        
        // Check that ImagePath is configured
        String bundlePath = ImagePath.getBundlePath();
        assertNotNull(bundlePath, "Bundle path should be set");
        System.out.println("Bundle path: " + bundlePath);
        
        // Test loading patterns with the expected paths used by StateImage
        String[] testPatterns = {
            "working/claude-icon-1",
            "working/claude-icon-1.png",
            "prompt/claude-prompt-1",
            "prompt/claude-prompt-1.png"
        };
        
        for (String patternPath : testPatterns) {
            try {
                Pattern pattern = new Pattern(patternPath);
                boolean loaded = pattern.getBImage() != null;
                System.out.println("Pattern '" + patternPath + "' loaded: " + loaded);
                
                // At least the .png versions should work
                if (patternPath.endsWith(".png")) {
                    assertTrue(loaded, "Should be able to load: " + patternPath);
                }
            } catch (Exception e) {
                System.out.println("Error loading '" + patternPath + "': " + e.getMessage());
                if (patternPath.endsWith(".png")) {
                    fail("Should not throw exception for: " + patternPath);
                }
            }
        }
    }
}