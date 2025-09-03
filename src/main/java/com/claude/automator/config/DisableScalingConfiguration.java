package com.claude.automator.config;

import lombok.extern.slf4j.Slf4j;
import org.sikuli.basics.Settings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Configuration to completely disable pattern scaling.
 * This can be activated by setting:
 * claude.automator.disable-scaling=true
 * 
 * This is a potential workaround if patterns match at 0.99 similarity in 
 * SikuliX IDE but not in Brobot despite correct DPI scaling.
 */
@Configuration
@ConditionalOnProperty(name = "claude.automator.disable-scaling", havingValue = "true")
@Slf4j
public class DisableScalingConfiguration {
    
    @PostConstruct
    public void disableScaling() {
        log.warn("╔════════════════════════════════════════════════════════════════════╗");
        log.warn("║                    PATTERN SCALING DISABLED                        ║");
        log.warn("║  Setting Settings.AlwaysResize = 0 to bypass all scaling          ║");
        log.warn("║  This may help if patterns were captured at current resolution    ║");
        log.warn("╚════════════════════════════════════════════════════════════════════╝");
        
        Settings.AlwaysResize = 0f;
        
        log.info("Pattern scaling disabled:");
        log.info("  Settings.AlwaysResize = {}", Settings.AlwaysResize);
        log.info("  Patterns will be matched at their original size");
        log.info("  No DPI compensation will be applied");
    }
}