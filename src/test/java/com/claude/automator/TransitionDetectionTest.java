package com.claude.automator;

import io.github.jspinak.brobot.annotations.Transition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class TransitionDetectionTest extends TestBase {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Test
    public void testTransitionBeanDetection() {
        System.out.println("\n=== TESTING TRANSITION BEAN DETECTION ===");
        
        // Method 1: Direct getBeansWithAnnotation
        System.out.println("\n1. Using getBeansWithAnnotation(@Transition):");
        Map<String, Object> transitionBeans = applicationContext.getBeansWithAnnotation(Transition.class);
        System.out.println("   Found " + transitionBeans.size() + " beans");
        transitionBeans.forEach((name, bean) -> {
            System.out.println("   - " + name + " (" + bean.getClass().getName() + ")");
        });
        
        // Method 2: Check all beans for @Transition annotation
        System.out.println("\n2. Checking all beans manually:");
        String[] allBeanNames = applicationContext.getBeanDefinitionNames();
        int transitionCount = 0;
        for (String beanName : allBeanNames) {
            if (beanName.toLowerCase().contains("transition")) {
                try {
                    Object bean = applicationContext.getBean(beanName);
                    Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
                    Transition annotation = AnnotationUtils.findAnnotation(targetClass, Transition.class);
                    
                    System.out.println("   Checking " + beanName + " (" + targetClass.getName() + "):");
                    System.out.println("      - Has @Transition: " + (annotation != null));
                    
                    if (annotation != null) {
                        transitionCount++;
                        System.out.println("      - From: " + java.util.Arrays.toString(annotation.from()));
                        System.out.println("      - To: " + java.util.Arrays.toString(annotation.to()));
                    }
                } catch (Exception e) {
                    System.out.println("   Error checking " + beanName + ": " + e.getMessage());
                }
            }
        }
        System.out.println("   Total beans with @Transition found manually: " + transitionCount);
        
        // Method 3: Try to get the specific transition bean directly
        System.out.println("\n3. Getting PromptToWorkingTransition directly:");
        try {
            Object promptToWorking = applicationContext.getBean("promptToWorkingTransition");
            System.out.println("   Found bean: " + promptToWorking.getClass().getName());
            
            Class<?> targetClass = AopProxyUtils.ultimateTargetClass(promptToWorking);
            Transition annotation = AnnotationUtils.findAnnotation(targetClass, Transition.class);
            System.out.println("   Has @Transition annotation: " + (annotation != null));
            
            // Also check if it's detected by getBeansWithAnnotation
            boolean detectedBySpring = transitionBeans.containsValue(promptToWorking) ||
                                      transitionBeans.values().stream()
                                          .anyMatch(bean -> bean.getClass().equals(promptToWorking.getClass()));
            System.out.println("   Detected by getBeansWithAnnotation: " + detectedBySpring);
            
        } catch (Exception e) {
            System.out.println("   Could not get bean: " + e.getMessage());
        }
        
        // Verify results
        System.out.println("\n=== RESULTS ===");
        if (transitionBeans.isEmpty()) {
            System.out.println("WARNING: getBeansWithAnnotation() is NOT detecting @Transition beans!");
            System.out.println("This is the same issue we had with @State annotations.");
            System.out.println("A BeanPostProcessor is needed for @Transition too.");
        } else {
            System.out.println("SUCCESS: @Transition beans are being detected properly.");
        }
    }
}