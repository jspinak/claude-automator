package com.claude.automator.mock;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.config.core.FrameworkSettings;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Intercepts Action.find() calls in mock mode and uses MockFindHandler
 * to return ActionHistory-based results instead of failing.
 */
@Aspect
@Component
@Slf4j
public class MockActionInterceptor {

    @Autowired
    private MockFindHandler mockFindHandler;

    /**
     * Intercepts find operations and redirects to mock handler in mock mode.
     */
    @Around("execution(* io.github.jspinak.brobot.action.Action.find(..)) && args(objects)")
    public Object interceptFind(ProceedingJoinPoint joinPoint, Object objects) throws Throwable {
        if (!FrameworkSettings.mock) {
            // Not in mock mode, proceed normally
            return joinPoint.proceed();
        }

        log.debug("Intercepting find operation in mock mode");

        // Handle different parameter types
        ObjectCollection objectCollection = null;

        if (objects instanceof ObjectCollection) {
            objectCollection = (ObjectCollection) objects;
        } else if (objects instanceof ObjectCollection[]) {
            ObjectCollection[] array = (ObjectCollection[]) objects;
            if (array.length > 0) {
                objectCollection = array[0];
            }
        }

        if (objectCollection == null) {
            log.warn("Could not extract ObjectCollection from parameters");
            return joinPoint.proceed();
        }

        // Use our mock handler
        ActionResult mockResult = mockFindHandler.performMockFind(objectCollection);

        if (mockResult != null && !mockResult.getMatchList().isEmpty()) {
            log.info("Mock find interceptor returned {} matches",
                    mockResult.getMatchList().size());
            return mockResult;
        }

        // If mock handler didn't find anything, let original proceed
        // (which will also fail, but maintains expected behavior)
        log.debug("Mock handler found no matches, proceeding with original");
        return joinPoint.proceed();
    }

    /**
     * Intercepts click operations to simulate success in mock mode.
     */
    @Around("execution(* io.github.jspinak.brobot.action.Action.click(..)) && args(objects)")
    public Object interceptClick(ProceedingJoinPoint joinPoint, Object objects) throws Throwable {
        if (!FrameworkSettings.mock) {
            return joinPoint.proceed();
        }

        log.debug("Intercepting click operation in mock mode");

        // In mock mode, first try to find the object
        ObjectCollection objectCollection = null;

        if (objects instanceof ObjectCollection) {
            objectCollection = (ObjectCollection) objects;
        } else if (objects instanceof ObjectCollection[]) {
            ObjectCollection[] array = (ObjectCollection[]) objects;
            if (array.length > 0) {
                objectCollection = array[0];
            }
        }

        if (objectCollection != null) {
            // Check if we can find it with ActionHistory
            ActionResult findResult = mockFindHandler.performMockFind(objectCollection);
            if (findResult != null && !findResult.getMatchList().isEmpty()) {
                // Found it, simulate successful click
                ActionResult clickResult = new ActionResult();
                clickResult.setSuccess(true);
                clickResult.add(findResult.getMatchList().get(0));
                log.info("✓ Mock click successful on {} matches",
                        findResult.getMatchList().size());
                return clickResult;
            }
        }

        // Let original proceed
        return joinPoint.proceed();
    }
}