package com.claude.automator.diagnostics;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.model.state.StateObjectMetadata;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Logs when matches are saved to StateImages to diagnose SearchRegionOnObject issues.
 */
@Aspect
@Component
public class MatchSavingLogger {
    
    private static final Logger log = LoggerFactory.getLogger(MatchSavingLogger.class);
    
    /**
     * Log when setLastMatchesFound is called on a StateImage.
     */
    @Before("execution(* io.github.jspinak.brobot.model.state.StateImage.setLastMatchesFound(..)) && args(matches)")
    public void logSetLastMatchesFound(JoinPoint joinPoint, List<Match> matches) {
        StateImage stateImage = (StateImage) joinPoint.getTarget();
        log.info("[MATCH-SAVE] StateImage.setLastMatchesFound called:");
        log.info("  - StateImage: {} (instance: {})", 
            stateImage.getName(), System.identityHashCode(stateImage));
        log.info("  - Owner state: {}", stateImage.getOwnerStateName());
        log.info("  - Number of matches being saved: {}", matches != null ? matches.size() : 0);
        if (matches != null && !matches.isEmpty()) {
            log.info("  - First match region: {}", matches.get(0).getRegion());
        }
        
        // Log stack trace to see who's calling this
        if (log.isDebugEnabled()) {
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            log.debug("  Call stack:");
            for (int i = 2; i < Math.min(8, stack.length); i++) {
                log.debug("    [{}] {}", i-2, stack[i]);
            }
        }
    }
    
    /**
     * Log when addLastMatchFound is called on a StateImage.
     */
    @Before("execution(* io.github.jspinak.brobot.model.state.StateImage.addLastMatchFound(..)) && args(match)")
    public void logAddLastMatchFound(JoinPoint joinPoint, Match match) {
        StateImage stateImage = (StateImage) joinPoint.getTarget();
        log.info("[MATCH-SAVE] StateImage.addLastMatchFound called:");
        log.info("  - StateImage: {} (instance: {})", 
            stateImage.getName(), System.identityHashCode(stateImage));
        log.info("  - Match region: {}", match != null ? match.getRegion() : null);
        log.info("  - Match score: {}", match != null ? match.getScore() : null);
    }
    
    /**
     * Log when clearLastMatchesFound is called on a StateImage.
     */
    @Before("execution(* io.github.jspinak.brobot.model.state.StateImage.clearLastMatchesFound(..))")
    public void logClearLastMatchesFound(JoinPoint joinPoint) {
        StateImage stateImage = (StateImage) joinPoint.getTarget();
        log.info("[MATCH-SAVE] StateImage.clearLastMatchesFound called:");
        log.info("  - StateImage: {} (instance: {})", 
            stateImage.getName(), System.identityHashCode(stateImage));
        log.info("  - Matches being cleared: {}", stateImage.getLastMatchesFound().size());
    }
    
    /**
     * Log when Find.perform completes to see if matches are saved.
     */
    @AfterReturning(
        pointcut = "execution(* io.github.jspinak.brobot.action.basic.find.Find.perform(..)) && args(actionResult, ..)",
        returning = "result"
    )
    public void logFindPerformComplete(JoinPoint joinPoint, ActionResult actionResult, ActionResult result) {
        log.info("[MATCH-SAVE] Find.perform completed:");
        log.info("  - Matches found: {}", result.getMatchList().size());
        
        // Check if matches have StateObjectData
        for (Match match : result.getMatchList()) {
            if (match.getStateObjectData() != null) {
                StateObjectMetadata metadata = match.getStateObjectData();
                log.info("  - Match has StateObjectMetadata: {}", metadata.getStateObjectName());
                log.info("    - Owner state: {}", metadata.getOwnerStateName());
                
                // Note: We can't access the actual StateImage from metadata,
                // but we can log what it represents
                if (metadata.getStateObjectName().contains("ClaudePrompt")) {
                    log.info("    - This is a ClaudePrompt match");
                }
            } else {
                log.warn("  - Match has NO StateObjectData!");
            }
        }
    }
    
    /**
     * Log when DynamicRegionResolver.updateSearchRegionsForObjects is called.
     */
    @Before("execution(* io.github.jspinak.brobot.action.internal.region.DynamicRegionResolver.updateSearchRegionsForObjects(..)) && args(objects, actionResult)")
    public void logDynamicResolverUpdate(JoinPoint joinPoint, List<StateObject> objects, ActionResult actionResult) {
        log.info("[MATCH-SAVE] DynamicRegionResolver.updateSearchRegionsForObjects called:");
        log.info("  - Number of objects to update: {}", objects.size());
        log.info("  - ActionResult matches: {}", actionResult.getMatchList().size());
        
        for (StateObject obj : objects) {
            if (obj instanceof StateImage) {
                StateImage img = (StateImage) obj;
                log.info("  - Processing StateImage: {} (instance: {})", 
                    img.getName(), System.identityHashCode(img));
                log.info("    - Has SearchRegionOnObject: {}", img.getSearchRegionOnObject() != null);
                if (img.getSearchRegionOnObject() != null) {
                    log.info("    - Target: {}:{}", 
                        img.getSearchRegionOnObject().getTargetStateName(),
                        img.getSearchRegionOnObject().getTargetObjectName());
                }
            }
        }
    }
}