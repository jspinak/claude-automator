package com.claude.automator.config;

import io.github.jspinak.brobot.config.ExecutionEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

/**
 * Configures the ExecutionEnvironment for claude-automator.
 * This ensures the correct environment settings are applied based on the active profile.
 */
@Configuration
@Slf4j
public class ExecutionEnvironmentConfig {
    
    @Value("${brobot.framework.mock:false}")
    private boolean mockMode;
    
    @Value("${brobot.framework.force-headless:#{null}}")
    private Boolean forceHeadless;
    
    @Value("${brobot.framework.allow-screen-capture:true}")
    private boolean allowScreenCapture;
    
    @PostConstruct
    public void configureEnvironment() {
        log.info("Configuring ExecutionEnvironment - mockMode: {}, forceHeadless: {}, allowScreenCapture: {}", 
                mockMode, forceHeadless, allowScreenCapture);
        
        ExecutionEnvironment newEnv = ExecutionEnvironment.builder()
                .mockMode(mockMode)
                .forceHeadless(forceHeadless)
                .allowScreenCapture(allowScreenCapture)
                .verboseLogging(true)
                .build();
        
        ExecutionEnvironment.setInstance(newEnv);
        
        log.info("ExecutionEnvironment configured: {}", newEnv.getEnvironmentInfo());
        log.info("  - useRealFiles: {}", newEnv.useRealFiles());
        log.info("  - shouldSkipSikuliX: {}", newEnv.shouldSkipSikuliX());
        log.info("  - hasDisplay: {}", newEnv.hasDisplay());
        log.info("  - canCaptureScreen: {}", newEnv.canCaptureScreen());
    }
}