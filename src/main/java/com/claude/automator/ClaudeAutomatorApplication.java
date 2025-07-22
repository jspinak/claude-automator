package com.claude.automator;

import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Slf4j
@SpringBootApplication
@EnableAspectJAutoProxy
@ComponentScan(basePackages = {"com.claude.automator", "io.github.jspinak.brobot"})
public class ClaudeAutomatorApplication {

    private static BrobotLogger brobotLogger;

    public static void main(String[] args) {
        log.info("Starting Claude Automator Application...");
        
        // Set headless mode based on environment before AWT initializes
        configureHeadlessMode();
        
        try {
            // Start Spring Boot application
            ConfigurableApplicationContext context = SpringApplication.run(ClaudeAutomatorApplication.class, args);
            
            // Get BrobotLogger from context
            brobotLogger = context.getBean(BrobotLogger.class);
            
            // Create a session for the entire application run
            try (var session = brobotLogger.session("claude-automator-" + System.currentTimeMillis())) {
                brobotLogger.observation("Claude Automator started successfully");
                brobotLogger.log()
                    .observation("Application context initialized")
                    .metadata("beanCount", context.getBeanDefinitionCount())
                    .metadata("activeProfiles", context.getEnvironment().getActiveProfiles())
                    .metadata("javaVersion", System.getProperty("java.version"))
                    .metadata("osName", System.getProperty("os.name"))
                    .log();
                
                // Log component scan results
                logComponentScanResults(context);
                
                // Keep application running
                log.info("Claude Automator is running. Press Ctrl+C to stop.");
                brobotLogger.observation("Application ready and monitoring started");
                
                // Add shutdown hook for cleanup logging
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    brobotLogger.observation("Shutdown initiated");
                    log.info("Shutting down Claude Automator...");
                }));
                
                // Run diagnostics after startup
                runStartupDiagnostics(context);
                
                // Keep the main thread alive
                Thread.currentThread().join();
            }
        } catch (Exception e) {
            log.error("Failed to start Claude Automator", e);
            if (brobotLogger != null) {
                brobotLogger.error("Application startup failed", e);
            }
            System.exit(1);
        }
    }
    
    private static void logComponentScanResults(ConfigurableApplicationContext context) {
        brobotLogger.log()
            .observation("Component scan completed")
            .metadata("automationBeans", context.getBeansOfType(com.claude.automator.automation.ClaudeMonitoringAutomation.class).size())
            .metadata("stateBeans", context.getBeansOfType(io.github.jspinak.brobot.model.state.State.class).size())
            .metadata("springBeans", context.getBeanDefinitionNames().length)
            .log();
    }
    
    private static void configureHeadlessMode() {
        String os = System.getProperty("os.name", "").toLowerCase();
        boolean isWindows = os.contains("windows");
        boolean isMac = os.contains("mac");
        boolean isLinux = os.contains("linux");
        boolean isWSL = System.getenv("WSL_DISTRO_NAME") != null || 
                       System.getenv("WSL_INTEROP") != null;
        
        // Check for explicit profile override
        String profile = System.getProperty("spring.profiles.active");
        boolean hasWindowsProfile = profile != null && profile.contains("windows");
        boolean hasLinuxProfile = profile != null && profile.contains("linux");
        boolean hasHeadlessProfile = profile != null && profile.contains("headless");
        
        // Check if DISPLAY variable is set (important for Linux)
        String display = System.getenv("DISPLAY");
        boolean hasDisplayEnv = display != null && !display.isEmpty();
        
        // Check if we can actually use the display
        boolean canUseDisplay = checkDisplayCapability();
        
        // Decision logic
        if (hasHeadlessProfile) {
            // Explicit headless profile always wins
            System.setProperty("java.awt.headless", "true");
            log.info("Headless profile detected - Setting java.awt.headless=true");
        } else if (hasWindowsProfile || (isWindows && !isWSL)) {
            // Windows (non-WSL) typically has display
            System.setProperty("java.awt.headless", "false");
            log.info("Windows environment detected - Setting java.awt.headless=false");
        } else if (isWSL) {
            // WSL usually needs headless unless WSLg is properly configured
            if (canUseDisplay && hasDisplayEnv) {
                System.setProperty("java.awt.headless", "false");
                log.info("WSL with display access (WSLg) - Setting java.awt.headless=false");
            } else {
                System.setProperty("java.awt.headless", "true");
                log.info("WSL without display access - Setting java.awt.headless=true");
            }
        } else if (isMac || isLinux) {
            // Mac and Linux - check actual display capability
            if (canUseDisplay) {
                System.setProperty("java.awt.headless", "false");
                log.info("{} with display access - Setting java.awt.headless=false", 
                        isMac ? "macOS" : "Linux");
            } else {
                System.setProperty("java.awt.headless", "true");
                String message = isMac 
                    ? "macOS without display access - Setting java.awt.headless=true (grant Screen Recording permission)"
                    : "Linux without display access - Setting java.awt.headless=true (check DISPLAY variable or X11/Wayland)";
                log.info(message);
            }
        } else {
            // Unknown system - be conservative and check display
            if (canUseDisplay) {
                System.setProperty("java.awt.headless", "false");
                log.info("Unknown OS with display access - Setting java.awt.headless=false");
            } else {
                System.setProperty("java.awt.headless", "true");
                log.info("Unknown OS without display access - Setting java.awt.headless=true");
            }
        }
        
        // Log the final decision
        log.info("Headless mode configuration: os.name={}, WSL={}, DISPLAY={}, profile={}, canUseDisplay={}, java.awt.headless={}",
                os, isWSL, display, profile, canUseDisplay, System.getProperty("java.awt.headless"));
    }
    
    /**
     * Check if we can actually use the display (especially important for macOS).
     * This attempts to create a Robot instance which will fail if we don't have
     * the necessary permissions.
     */
    private static boolean checkDisplayCapability() {
        try {
            // First check if AWT thinks we're headless
            if (java.awt.GraphicsEnvironment.isHeadless()) {
                return false;
            }
            
            // Try to create a Robot - this will fail on macOS without permissions
            java.awt.Robot robot = new java.awt.Robot();
            
            // Try to capture a small region - this specifically tests screen recording permission
            robot.createScreenCapture(new java.awt.Rectangle(1, 1));
            
            log.info("Display capability check: SUCCESS - Screen access is available");
            return true;
        } catch (java.awt.AWTException e) {
            log.warn("Display capability check: FAILED - AWTException: {}", e.getMessage());
            return false;
        } catch (SecurityException e) {
            log.warn("Display capability check: FAILED - SecurityException (likely missing permissions): {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.warn("Display capability check: FAILED - {}: {}", e.getClass().getSimpleName(), e.getMessage());
            return false;
        }
    }
    
    private static void runStartupDiagnostics(ConfigurableApplicationContext context) {
        log.info("Running startup diagnostics...");
        try {
            // Wait a moment for everything to initialize
            Thread.sleep(1000);
            
            // The ImageLoadingDiagnosticsRunner runs automatically if enabled via property
            // brobot.diagnostics.image-loading.enabled=true
            
            // Log diagnostic status with BrobotLogger
            if (brobotLogger != null) {
                boolean diagnosticsEnabled = context.getEnvironment()
                    .getProperty("brobot.diagnostics.image-loading.enabled", Boolean.class, false);
                
                brobotLogger.log()
                    .observation("Startup diagnostics status")
                    .metadata("imageLoadingDiagnosticsEnabled", diagnosticsEnabled)
                    .metadata("hint", "Set brobot.diagnostics.image-loading.enabled=true to enable image loading diagnostics")
                    .log();
            }
        } catch (Exception e) {
            log.error("Error running startup diagnostics", e);
        }
    }
}