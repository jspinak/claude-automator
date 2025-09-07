package com.claude.automator.diagnostics;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Aspect to debug the entire Find flow and track where StateObjectData gets lost.
 */
@Aspect
@Component
@Slf4j
public class DebugFindFlow {
    
    private static final ThreadLocal<Integer> depth = ThreadLocal.withInitial(() -> 0);
    
    private String indent() {
        return "  ".repeat(depth.get());
    }
    
    @Around("execution(* io.github.jspinak.brobot.action.basic.find.Find.perform(..))")
    public Object debugFindPerform(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("{}[DEBUG_FIND] >>> Find.perform() INTERCEPTED <<<", indent());
        depth.set(depth.get() + 1);
        
        Object[] args = joinPoint.getArgs();
        if (args.length >= 2) {
            ActionResult matches = (ActionResult) args[0];
            ObjectCollection[] collections = (ObjectCollection[]) args[1];
            
            log.info("{}[DEBUG_FIND] Arguments:", indent());
            log.info("{}  - ActionResult: {}", indent(), matches);
            log.info("{}  - Collections: {}", indent(), collections.length);
            
            for (int i = 0; i < collections.length; i++) {
                ObjectCollection col = collections[i];
                log.info("{}  - Collection[{}]: {} StateImages", indent(), i, col.getStateImages().size());
                for (StateImage img : col.getStateImages()) {
                    log.info("{}    - StateImage: name={}, ownerState={}, instance={}", 
                            indent(), img.getName(), img.getOwnerStateName(), System.identityHashCode(img));
                    log.info("{}      lastMatchesFound.size() BEFORE: {}", 
                            indent(), img.getLastMatchesFound().size());
                }
            }
        }
        
        Object result = joinPoint.proceed();
        
        if (args.length >= 2) {
            ActionResult matches = (ActionResult) args[0];
            ObjectCollection[] collections = (ObjectCollection[]) args[1];
            
            log.info("{}[DEBUG_FIND] After Find.perform():", indent());
            log.info("{}  - Matches found: {}", indent(), matches.size());
            
            // Check if matches have StateObjectData
            for (Match match : matches.getMatchList()) {
                if (match.getStateObjectData() != null) {
                    log.info("{}  - Match has StateObjectData: {}", 
                            indent(), match.getStateObjectData().getStateObjectName());
                } else {
                    log.warn("{}  - Match has NO StateObjectData!", indent());
                }
            }
            
            // Check if StateImages have updated lastMatchesFound
            for (ObjectCollection col : collections) {
                for (StateImage img : col.getStateImages()) {
                    log.info("{}    - StateImage '{}' lastMatchesFound.size() AFTER: {}", 
                            indent(), img.getName(), img.getLastMatchesFound().size());
                }
            }
        }
        
        depth.set(depth.get() - 1);
        log.info("{}[DEBUG_FIND] <<< Find.perform() COMPLETED <<<", indent());
        
        return result;
    }
    
    @Around("execution(* io.github.jspinak.brobot.action.basic.find.FindPipeline.saveMatchesToStateImages(..))")
    public Object debugSaveMatches(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("{}[DEBUG_SAVE] >>> saveMatchesToStateImages() INTERCEPTED <<<", indent());
        depth.set(depth.get() + 1);
        
        Object[] args = joinPoint.getArgs();
        if (args.length >= 2) {
            ActionResult matches = (ActionResult) args[0];
            ObjectCollection[] collections = (ObjectCollection[]) args[1];
            
            log.info("{}[DEBUG_SAVE] Arguments:", indent());
            log.info("{}  - Matches: {}", indent(), matches.size());
            log.info("{}  - Collections: {}", indent(), collections.length);
            
            // Log match details
            for (Match match : matches.getMatchList()) {
                log.info("{}  - Match:", indent());
                log.info("{}    Region: {}", indent(), match.getRegion());
                if (match.getStateObjectData() != null) {
                    log.info("{}    StateObjectData: name={}, ownerState={}", 
                            indent(), 
                            match.getStateObjectData().getStateObjectName(),
                            match.getStateObjectData().getOwnerStateName());
                } else {
                    log.warn("{}    NO StateObjectData!", indent());
                }
            }
            
            // Log collection details
            for (int i = 0; i < collections.length; i++) {
                ObjectCollection col = collections[i];
                log.info("{}  - Collection[{}]:", indent(), i);
                for (StateImage img : col.getStateImages()) {
                    log.info("{}    StateImage: name={}, instance={}", 
                            indent(), img.getName(), System.identityHashCode(img));
                }
            }
        }
        
        Object result = joinPoint.proceed();
        
        depth.set(depth.get() - 1);
        log.info("{}[DEBUG_SAVE] <<< saveMatchesToStateImages() COMPLETED <<<", indent());
        
        return result;
    }
    
    @Around("execution(* io.github.jspinak.brobot.action.basic.find.FindAll.perform(..))")
    public Object debugFindAll(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("{}[DEBUG_FINDALL] >>> FindAll.perform() <<<", indent());
        depth.set(depth.get() + 1);
        
        Object result = joinPoint.proceed();
        
        depth.set(depth.get() - 1);
        log.info("{}[DEBUG_FINDALL] <<< FindAll completed <<<", indent());
        
        return result;
    }
}