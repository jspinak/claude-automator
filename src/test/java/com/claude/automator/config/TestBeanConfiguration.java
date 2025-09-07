package com.claude.automator.config;

import io.github.jspinak.brobot.core.services.SikuliScreenCapture;
import io.github.jspinak.brobot.util.image.capture.DirectRobotCapture;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Test configuration that provides mock implementations of capture services
 * that fail in headless environments.
 */
@TestConfiguration
@Profile("test")
public class TestBeanConfiguration {
    
    @Bean
    @Primary
    public SikuliScreenCapture sikuliScreenCapture() {
        // Return a mock to avoid SikuliX initialization in headless
        return Mockito.mock(SikuliScreenCapture.class);
    }
    
    @Bean
    @Primary
    public DirectRobotCapture directRobotCapture() {
        // Return a mock to avoid Robot initialization in headless
        return Mockito.mock(DirectRobotCapture.class);
    }
}