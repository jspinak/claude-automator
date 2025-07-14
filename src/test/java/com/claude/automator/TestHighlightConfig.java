package com.claude.automator;

import io.github.jspinak.brobot.tools.logging.visual.VisualFeedbackConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class TestHighlightConfig {
    
    public static void main(String[] args) {
        // Set the active profile to windows
        System.setProperty("spring.profiles.active", "windows");
        
        ConfigurableApplicationContext context = SpringApplication.run(TestHighlightConfig.class, args);
        
        // Get the visual feedback config
        VisualFeedbackConfig config = context.getBean(VisualFeedbackConfig.class);
        
        System.out.println("Visual Feedback Configuration:");
        System.out.println("- Enabled: " + config.isEnabled());
        System.out.println("- Auto Highlight Finds: " + config.isAutoHighlightFinds());
        System.out.println("- Auto Highlight Search Regions: " + config.isAutoHighlightSearchRegions());
        System.out.println("- Find Highlight Color: " + config.getFind().getColor());
        System.out.println("- Find Highlight Duration: " + config.getFind().getDuration());
        System.out.println("- Search Region Highlight Color: " + config.getSearchRegion().getColor());
        System.out.println("- Search Region Show Dimensions: " + config.getSearchRegion().isShowDimensions());
        
        context.close();
    }
}