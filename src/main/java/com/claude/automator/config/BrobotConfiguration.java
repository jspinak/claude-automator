package com.claude.automator.config;

import io.github.jspinak.brobot.config.BrobotProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.File;

/**
 * Configuration for Brobot framework settings.
 */
@Configuration
@Slf4j
public class BrobotConfiguration {
    
    @Value("${brobot.framework.mock:false}")
    private boolean mockMode;
    
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "brobot")
    public BrobotProperties brobotProperties() {
        BrobotProperties properties = new BrobotProperties();
        
        // Configure core settings
        BrobotProperties.Core core = properties.getCore();
        core.setMock(mockMode);
        
        // Set image path - try to find the images directory
        String imagePath = findImagesDirectory();
        core.setImagePath(imagePath);
        
        log.info("Configured BrobotProperties - mock: {}, imagePath: {}", 
                core.isMock(), core.getImagePath());
        
        return properties;
    }
    
    private String findImagesDirectory() {
        // Try relative path first
        File relativeImages = new File("images");
        if (relativeImages.exists() && relativeImages.isDirectory()) {
            return relativeImages.getAbsolutePath();
        }
        
        // Try Windows specific path
        String userHome = System.getProperty("user.home");
        File windowsImages = new File(userHome + File.separator + "Documents" + 
                File.separator + "brobot-parent-directory" + 
                File.separator + "claude-automator" + 
                File.separator + "images");
        if (windowsImages.exists() && windowsImages.isDirectory()) {
            return windowsImages.getAbsolutePath();
        }
        
        // Default to "images"
        return "images";
    }
}