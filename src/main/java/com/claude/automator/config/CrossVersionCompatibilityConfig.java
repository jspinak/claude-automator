package com.claude.automator.config;

import org.sikuli.basics.Settings;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

/**
 * Configure SikuliX for cross-version compatibility
 * Handles differences between Java 8 (IDE) and Java 21 (Brobot)
 */
@Configuration
public class CrossVersionCompatibilityConfig {
    
    @PostConstruct
    public void configureCrossVersionCompatibility() {
        System.out.println("=== CROSS-VERSION COMPATIBILITY CONFIGURATION ===");
        
        // Check Java version
        String javaVersion = System.getProperty("java.version");
        System.out.println("Running on Java: " + javaVersion);
        
        if (javaVersion.startsWith("21") || javaVersion.startsWith("17") || 
            javaVersion.startsWith("11") || javaVersion.startsWith("9")) {
            
            System.out.println("Detected Java 9+ - Applying compatibility settings");
            
            // PRIMARY FIX: Scale patterns to 0.8x to match IDE's patterns
            // This accounts for the 25% DPI scaling difference
            Settings.AlwaysResize = 0.8f;
            
            // SECONDARY FIX: Lower similarity threshold slightly to account for
            // Java version rendering differences (antialiasing, color management)
            Settings.MinSimilarity = 0.68;  // Slightly lower than default 0.7
            
            // Keep optimization
            Settings.CheckLastSeen = true;
            
            // Disable some Java 9+ specific image optimizations that might
            // interfere with pattern matching
            System.setProperty("sun.java2d.opengl", "false");
            System.setProperty("sun.java2d.d3d", "false");
            
            // Use consistent color rendering
            System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
            
        } else {
            System.out.println("Running on Java 8 - Using default settings");
            Settings.MinSimilarity = 0.7;
            Settings.CheckLastSeen = true;
        }
        
        System.out.println("\nFinal Settings:");
        System.out.println("  AlwaysResize: " + Settings.AlwaysResize);
        System.out.println("  MinSimilarity: " + Settings.MinSimilarity);
        System.out.println("  CheckLastSeen: " + Settings.CheckLastSeen);
        System.out.println("==========================================\n");
    }
}