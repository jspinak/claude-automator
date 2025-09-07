package com.claude.automator;

import io.github.jspinak.brobot.annotations.AnnotationProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class AnnotationProcessorDebugTest extends TestBase {
    
    @Autowired
    private ApplicationContext context;
    
    @Test
    public void testAnnotationProcessorExists() {
        System.out.println("=== ANNOTATION PROCESSOR BEAN CHECK ===");
        
        // Check if AnnotationProcessor is in the context
        boolean hasAnnotationProcessor = context.containsBean("annotationProcessor");
        System.out.println("Context contains 'annotationProcessor' bean: " + hasAnnotationProcessor);
        
        // Try to get it by type
        try {
            AnnotationProcessor processor = context.getBean(AnnotationProcessor.class);
            System.out.println("AnnotationProcessor bean retrieved by type: " + (processor != null));
            
            // Try to manually trigger processing
            System.out.println("Manually triggering annotation processing...");
            processor.processAnnotations();
            System.out.println("Manual trigger completed");
        } catch (Exception e) {
            System.out.println("Could not get AnnotationProcessor bean: " + e.getMessage());
        }
        
        // Check all annotation-related beans
        System.out.println("\nAll annotation-related beans:");
        String[] beanNames = context.getBeanDefinitionNames();
        for (String name : beanNames) {
            if (name.toLowerCase().contains("annotation")) {
                System.out.println("  Found: " + name + " (" + context.getBean(name).getClass().getSimpleName() + ")");
            }
        }
        
        // Check if StateAnnotationBeanPostProcessor is there
        System.out.println("\nBeanPostProcessor check:");
        try {
            var postProcessor = context.getBean("stateAnnotationBeanPostProcessor");
            System.out.println("  StateAnnotationBeanPostProcessor found: " + (postProcessor != null));
        } catch (Exception e) {
            System.out.println("  StateAnnotationBeanPostProcessor not found: " + e.getMessage());
        }
        
        // This test just logs, no assertions to see what's happening
        assertTrue(true);
    }
}