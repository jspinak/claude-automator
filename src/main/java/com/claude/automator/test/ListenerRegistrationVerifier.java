package com.claude.automator.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.ApplicationListenerMethodAdapter;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import java.lang.reflect.Field;
import java.util.Collection;

/**
 * Verifies that event listeners are properly registered with Spring's event system.
 */
@Component
@Slf4j
public class ListenerRegistrationVerifier {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private ApplicationEventMulticaster eventMulticaster;
    
    @PostConstruct
    public void verifyListenerRegistration() {
        log.info("[LISTENER VERIFY] Starting listener registration verification");
        
        try {
            // Use reflection to get the listeners from the multicaster
            Field listenersField = eventMulticaster.getClass().getDeclaredField("defaultRetriever");
            listenersField.setAccessible(true);
            Object retriever = listenersField.get(eventMulticaster);
            
            if (retriever != null) {
                Field applicationListenersField = retriever.getClass().getDeclaredField("applicationListeners");
                applicationListenersField.setAccessible(true);
                Collection<?> listeners = (Collection<?>) applicationListenersField.get(retriever);
                
                log.info("[LISTENER VERIFY] Total registered listeners: {}", listeners.size());
                
                // Check for SearchRegionDependencyInitializer
                boolean foundSearchRegionListener = false;
                for (Object listener : listeners) {
                    String listenerClass = listener.getClass().getName();
                    
                    if (listener instanceof ApplicationListenerMethodAdapter) {
                        ApplicationListenerMethodAdapter adapter = (ApplicationListenerMethodAdapter) listener;
                        Field beanNameField = ApplicationListenerMethodAdapter.class.getDeclaredField("beanName");
                        beanNameField.setAccessible(true);
                        String beanName = (String) beanNameField.get(adapter);
                        
                        if ("searchRegionDependencyInitializer".equals(beanName)) {
                            foundSearchRegionListener = true;
                            log.info("[LISTENER VERIFY] Found SearchRegionDependencyInitializer listener!");
                            
                            // Get more details
                            Field methodField = ApplicationListenerMethodAdapter.class.getDeclaredField("method");
                            methodField.setAccessible(true);
                            Object method = methodField.get(adapter);
                            log.info("[LISTENER VERIFY]   Method: {}", method);
                            
                            Field eventTypesField = ApplicationListenerMethodAdapter.class.getDeclaredField("declaredEventTypes");
                            eventTypesField.setAccessible(true);
                            Object eventTypes = eventTypesField.get(adapter);
                            log.info("[LISTENER VERIFY]   Event types: {}", eventTypes);
                        }
                        
                        // Log all StatesRegisteredEvent listeners
                        if (beanName != null && adapter.supportsEventType(
                                org.springframework.core.ResolvableType.forClass(
                                    io.github.jspinak.brobot.annotations.StatesRegisteredEvent.class))) {
                            log.info("[LISTENER VERIFY] StatesRegisteredEvent listener: {}", beanName);
                        }
                    }
                }
                
                if (!foundSearchRegionListener) {
                    log.warn("[LISTENER VERIFY] SearchRegionDependencyInitializer listener NOT found!");
                }
                
            } else {
                log.warn("[LISTENER VERIFY] Could not access listener retriever");
            }
            
        } catch (Exception e) {
            log.error("[LISTENER VERIFY] Error verifying listeners", e);
        }
        
        // Also check if the bean exists
        if (applicationContext.containsBean("searchRegionDependencyInitializer")) {
            Object bean = applicationContext.getBean("searchRegionDependencyInitializer");
            log.info("[LISTENER VERIFY] SearchRegionDependencyInitializer bean exists: {}", bean.getClass().getName());
        } else {
            log.warn("[LISTENER VERIFY] SearchRegionDependencyInitializer bean does NOT exist!");
        }
    }
}