package com.claude.automator.debug;

import org.sikuli.basics.Settings;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import io.github.jspinak.brobot.config.dpi.DPIAutoDetector;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Diagnostic to understand why auto-detection isn't working.
 * Run this as a standalone Spring Boot app to see what values are detected.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"io.github.jspinak.brobot.config"})
public class AutoDetectionDiagnostic {
    
    public static void main(String[] args) {
        SpringApplication.run(AutoDetectionDiagnostic.class, args);
    }
    
    @Bean
    CommandLineRunner diagnose(DPIAutoDetector detector) {
        return args -> {
            System.out.println("\n========== AUTO-DETECTION DIAGNOSTIC ==========\n");
            
            // 1. Test Java's DPI detection
            System.out.println("1. JAVA AWT DETECTION:");
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Dimension screenSize = toolkit.getScreenSize();
            System.out.println("   Toolkit screen size: " + screenSize.width + "x" + screenSize.height);
            
            var transform = gc.getDefaultTransform();
            System.out.println("   Transform scaleX: " + transform.getScaleX());
            System.out.println("   Transform scaleY: " + transform.getScaleY());
            
            // Get DPI
            int dpi = toolkit.getScreenResolution();
            System.out.println("   Screen DPI: " + dpi);
            
            // Check system properties
            System.out.println("\n2. SYSTEM PROPERTIES:");
            System.out.println("   os.name: " + System.getProperty("os.name"));
            System.out.println("   java.version: " + System.getProperty("java.version"));
            System.out.println("   sun.java2d.uiScale: " + System.getProperty("sun.java2d.uiScale"));
            System.out.println("   sun.java2d.dpiaware: " + System.getProperty("sun.java2d.dpiaware"));
            
            // Check if running in WSL
            String wslDistro = System.getenv("WSL_DISTRO_NAME");
            if (wslDistro != null) {
                System.out.println("   ⚠ Running in WSL: " + wslDistro);
                System.out.println("   Note: WSL may not report Windows DPI correctly to Java");
            }
            
            // 3. Test SikuliX capture
            System.out.println("\n3. SIKULIX CAPTURE:");
            try {
                Screen screen = new Screen();
                ScreenImage capture = screen.capture();
                BufferedImage img = capture.getImage();
                System.out.println("   SikuliX capture size: " + img.getWidth() + "x" + img.getHeight());
            } catch (Exception e) {
                System.out.println("   Error capturing: " + e.getMessage());
            }
            
            // 4. Test Brobot auto-detector
            System.out.println("\n4. BROBOT AUTO-DETECTION:");
            float detectedFactor = detector.detectScalingFactor();
            System.out.println("   Detected resize factor: " + detectedFactor);
            System.out.println("   Description: " + detector.getScalingDescription());
            
            // 5. Analysis
            System.out.println("\n5. ANALYSIS:");
            if (Math.abs(transform.getScaleX() - 1.0) < 0.01) {
                System.out.println("   ❌ Java's transform.getScaleX() returns 1.0");
                System.out.println("      This means Java isn't detecting Windows DPI scaling");
                System.out.println("      Possible causes:");
                System.out.println("      - Running from WSL (DPI info not passed through)");
                System.out.println("      - Java needs -Dsun.java2d.dpiaware=true");
                System.out.println("      - Windows compatibility settings override DPI");
            }
            
            if (screenSize.width == 1536 && screenSize.height == 864) {
                System.out.println("   ✓ Screen size 1536x864 suggests 125% scaling");
                if (Math.abs(detectedFactor - 0.8f) < 0.01) {
                    System.out.println("   ✓ Fallback detection worked correctly!");
                } else {
                    System.out.println("   ❌ But detector returned: " + detectedFactor);
                }
            }
            
            // 6. Current Settings
            System.out.println("\n6. CURRENT SIKULIX SETTINGS:");
            System.out.println("   Settings.AlwaysResize: " + Settings.AlwaysResize);
            System.out.println("   Settings.MinSimilarity: " + Settings.MinSimilarity);
            
            System.out.println("\n==============================================\n");
        };
    }
}