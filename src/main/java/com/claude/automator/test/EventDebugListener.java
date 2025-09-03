package com.claude.automator.test;

import io.github.jspinak.brobot.annotations.StatesRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Debug listener to trace event flow for StatesRegisteredEvent.
 * This helps identify if events are being published and received properly.
 * 
 * DISABLED: Commented out to reduce verbose logging in production.
 * Uncomment @Component annotation to re-enable for debugging.
 */
// @Component  // DISABLED - Too verbose for normal operation
@Slf4j
public class EventDebugListener {
    
    /**
     * Highest priority listener - should be called first
     */
    @EventListener(StatesRegisteredEvent.class)
    @Order(1)
    public void onStatesRegisteredFirst(StatesRegisteredEvent event) {
        log.info("[EVENT TRACE] EventDebugListener @Order(1): Received StatesRegisteredEvent");
        log.info("[EVENT TRACE] Event details: {} states, {} transitions", 
                event.getStateCount(), event.getTransitionCount());
        log.info("[EVENT TRACE] Event source: {}", event.getSource().getClass().getName());
        log.info("[EVENT TRACE] Current thread: {}", Thread.currentThread().getName());
        log.info("[EVENT TRACE] Stack trace: ");
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (element.toString().contains("io.github.jspinak") || 
                element.toString().contains("com.claude") ||
                element.toString().contains("springframework")) {
                log.info("[EVENT TRACE]   -> {}", element);
            }
        }
    }
    
    /**
     * Medium priority listener
     */
    @EventListener(StatesRegisteredEvent.class)
    @Order(50)
    public void onStatesRegisteredMiddle(StatesRegisteredEvent event) {
        log.info("[EVENT TRACE] EventDebugListener @Order(50): Event still processing");
    }
    
    /**
     * Lowest priority listener - should be called last
     */
    @EventListener(StatesRegisteredEvent.class)
    @Order(100)
    public void onStatesRegisteredLast(StatesRegisteredEvent event) {
        log.info("[EVENT TRACE] EventDebugListener @Order(100): Event processing complete");
        log.info("[EVENT TRACE] All listeners should have been called by now");
    }
    
    /**
     * No order specified - default priority
     */
    @EventListener(StatesRegisteredEvent.class)
    public void onStatesRegisteredDefault(StatesRegisteredEvent event) {
        log.info("[EVENT TRACE] EventDebugListener (no @Order): Default priority listener called");
    }
}