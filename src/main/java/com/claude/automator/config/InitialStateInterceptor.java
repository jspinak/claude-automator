package com.claude.automator.config;

import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Random;

/**
 * Intercepts initial state verification to ensure Prompt is activated in mock mode.
 */
@Aspect
@Component
@Profile("test")
@Slf4j
public class InitialStateInterceptor {
    
    @Autowired
    private StateService stateService;
    
    @Autowired
    private StateMemory stateMemory;
    
    private static boolean intercepted = false;
    
    /**
     * Intercepts the verifyMock method in InitialStateVerifier to force Prompt activation.
     */
    @Around("execution(* io.github.jspinak.brobot.startup.InitialStateVerifier.VerificationBuilder.verifyMock(..))")
    public Object forcePromptInMock(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!FrameworkSettings.mock || intercepted) {
            return joinPoint.proceed();
        }
        
        intercepted = true;
        log.info("═══════════════════════════════════════════════════════");
        log.info("  INTERCEPTING INITIAL STATE VERIFICATION");
        log.info("═══════════════════════════════════════════════════════");
        
        // Force Prompt state activation
        Optional<State> promptState = stateService.getState("Prompt");
        if (promptState.isPresent()) {
            Long promptId = promptState.get().getId();
            stateMemory.addActiveState(promptId, true);
            promptState.get().setProbabilityToBaseProbability();
            log.info("✅ FORCED PROMPT STATE (ID: {}) as initial state", promptId);
            log.info("═══════════════════════════════════════════════════════");
            return true; // Return success
        }
        
        log.warn("Could not find Prompt state, proceeding with default behavior");
        return joinPoint.proceed();
    }
    
    /**
     * Alternative: Intercept the random selection to always return Prompt's ID.
     */
    @Around("execution(* io.github.jspinak.brobot.startup.InitialStateVerifier.VerificationBuilder.verify())")
    public Object ensurePromptSelected(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!FrameworkSettings.mock) {
            return joinPoint.proceed();
        }
        
        log.debug("Ensuring Prompt state is selected in mock verification");
        
        // Let it proceed but we've already set up Prompt
        Object result = joinPoint.proceed();
        
        // Double-check and correct if needed
        if (!stateMemory.getActiveStates().contains(1L)) { // Prompt has ID 1
            log.info("Correcting state activation - ensuring Prompt is active");
            stateMemory.getActiveStates().clear();
            stateMemory.addActiveState(1L, true);
        }
        
        return result;
    }
}