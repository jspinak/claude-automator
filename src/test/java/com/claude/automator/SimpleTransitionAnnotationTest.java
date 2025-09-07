package com.claude.automator;

import com.claude.automator.transitions.PromptToWorkingTransition;
import io.github.jspinak.brobot.annotations.Transition;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple unit test to verify @Transition annotation configuration.
 * This test doesn't require Spring context.
 */
public class SimpleTransitionAnnotationTest {
    
    @Test
    public void testTransitionAnnotationIncludesComponent() {
        System.out.println("\n=== TESTING @Transition ANNOTATION CONFIGURATION ===");
        
        // Check if @Transition annotation is meta-annotated with @Component
        Class<Transition> transitionClass = Transition.class;
        Component componentAnnotation = transitionClass.getAnnotation(Component.class);
        
        System.out.println("1. @Transition meta-annotations:");
        System.out.println("   - Has @Component: " + (componentAnnotation != null));
        
        // Verify the annotation is present
        assertNotNull(componentAnnotation, "@Transition should be meta-annotated with @Component");
        
        // Check if PromptToWorkingTransition has the @Transition annotation
        Class<?> transitionImplClass = PromptToWorkingTransition.class;
        Transition transitionAnnotation = transitionImplClass.getAnnotation(Transition.class);
        
        System.out.println("\n2. PromptToWorkingTransition class:");
        System.out.println("   - Has @Transition: " + (transitionAnnotation != null));
        
        if (transitionAnnotation != null) {
            System.out.println("   - From states: " + java.util.Arrays.toString(transitionAnnotation.from()));
            System.out.println("   - To states: " + java.util.Arrays.toString(transitionAnnotation.to()));
            System.out.println("   - Method: " + transitionAnnotation.method());
            System.out.println("   - Priority: " + transitionAnnotation.priority());
        }
        
        // Use AnnotationUtils to handle meta-annotations
        Component derivedComponent = AnnotationUtils.findAnnotation(transitionImplClass, Component.class);
        System.out.println("   - Has @Component (derived): " + (derivedComponent != null));
        
        // Verify
        assertNotNull(transitionAnnotation, "PromptToWorkingTransition should have @Transition annotation");
        assertNotNull(derivedComponent, "PromptToWorkingTransition should be detected as a Spring component");
        
        System.out.println("\n=== RESULT ===");
        System.out.println("@Transition annotation is properly configured with @Component meta-annotation.");
        System.out.println("Classes annotated with @Transition should be automatically detected as Spring beans.");
        System.out.println("\nHowever, if Spring's getBeansWithAnnotation() is not finding them,");
        System.out.println("it's likely the same issue we had with @State annotations:");
        System.out.println("getBeansWithAnnotation() doesn't reliably detect meta-annotations.");
        System.out.println("\nSOLUTION: Implement a BeanPostProcessor for @Transition similar to StateAnnotationBeanPostProcessor.");
    }
}