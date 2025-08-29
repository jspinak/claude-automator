package com.claude.automator.config;

import io.github.jspinak.brobot.control.ExecutionController;
import io.github.jspinak.brobot.control.ThreadSafeExecutionController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration to resolve Spring bean conflicts.
 * Specifies which ExecutionController implementation to use as primary.
 */
@Configuration
public class BeanConfiguration {
    
    /**
     * Designates ThreadSafeExecutionController as the primary ExecutionController bean.
     * This resolves the conflict between ThreadSafeExecutionController and ReactiveAutomator.
     */
    @Bean
    @Primary
    public ExecutionController primaryExecutionController(ThreadSafeExecutionController threadSafeExecutionController) {
        return threadSafeExecutionController;
    }
}