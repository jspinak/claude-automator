package io.github.jspinak.claude.tools;

import io.github.jspinak.brobot.capture.JavaCVFFmpegCapture;
import io.github.jspinak.brobot.capture.CaptureConfiguration;
import io.github.jspinak.brobot.capture.BrobotCaptureService;
import org.sikuli.basics.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Spring-aware test to verify FFmpeg capture configuration with proper DPI settings.
 * This loads the Spring context and applies configuration from application.properties.
 */
@SpringBootApplication
public class TestFFmpegConfigSpring implements CommandLineRunner {
    
    @Autowired
    private ApplicationContext context;
    
    @Autowired(required = false)
    private CaptureConfiguration captureConfig;
    
    @Autowired(required = false)
    private BrobotCaptureService captureService;
    
    public static void main(String[] args) {
        // Disable GUI components for testing
        System.setProperty("java.awt.headless", "false");
        SpringApplication.run(TestFFmpegConfigSpring.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("FFMPEG CONFIGURATION TEST (Spring Context)");
        System.out.println("=".repeat(80));
        
        // 1. Check if Spring context loaded properly
        System.out.println("\n1. SPRING CONTEXT:");
        System.out.println("-".repeat(60));
        System.out.println("   Context loaded: " + (context != null ? "✅ Yes" : "✗ No"));
        System.out.println("   Active profiles: " + String.join(", ", context.getEnvironment().getActiveProfiles()));
        
        // 2. Check Settings.AlwaysResize (should be set by DPIConfiguration)
        System.out.println("\n2. PATTERN SCALING CONFIGURATION:");
        System.out.println("-".repeat(60));
        System.out.println("   Settings.AlwaysResize: " + Settings.AlwaysResize);
        System.out.println("   Expected value: 1.0 (no scaling)");
        
        if (Math.abs(Settings.AlwaysResize - 1.0) < 0.001) {
            System.out.println("   ✅ Pattern scaling is DISABLED (1.0)");
            System.out.println("      Patterns will use original size");
        } else if (Settings.AlwaysResize == 0.0) {
            System.out.println("   ⚠️ Settings.AlwaysResize is 0.0 (not configured)");
            System.out.println("      This means DPIConfiguration hasn't run yet");
            System.out.println("      Check if @PostConstruct methods are executing");
        } else {
            System.out.println("   ⚠️ Pattern scaling is ENABLED: " + Settings.AlwaysResize);
            System.out.println("      Patterns will be scaled by factor: " + Settings.AlwaysResize);
        }
        
        // 3. Check capture configuration
        System.out.println("\n3. CAPTURE PROVIDER CONFIGURATION:");
        System.out.println("-".repeat(60));
        
        if (captureConfig != null) {
            System.out.println("   Active provider: " + captureConfig.getCurrentProvider());
            System.out.println("   Physical resolution: " + captureConfig.isCapturingPhysicalResolution());
            
            // Check if FFmpeg is configured
            String provider = captureConfig.getCurrentProvider();
            if ("FFMPEG".equalsIgnoreCase(provider)) {
                System.out.println("   ✅ FFmpeg is the active provider");
            } else {
                System.out.println("   ⚠️ Provider is " + provider + " (expected FFMPEG)");
            }
        } else {
            System.out.println("   ⚠️ CaptureConfiguration not available");
        }
        
        // 4. Test actual capture
        System.out.println("\n4. TESTING CAPTURE:");
        System.out.println("-".repeat(60));
        
        if (captureService != null) {
            try {
                System.out.println("   Using BrobotCaptureService...");
                BufferedImage capture = captureService.captureScreen();
                analyzeCapture(capture, "Brobot");
            } catch (Exception e) {
                System.out.println("   ✗ BrobotCaptureService failed: " + e.getMessage());
            }
        } else {
            System.out.println("   BrobotCaptureService not available, testing JavaCV directly...");
            try {
                BufferedImage capture = JavaCVFFmpegCapture.capture();
                analyzeCapture(capture, "JavaCV");
            } catch (Exception e) {
                System.out.println("   ✗ JavaCV FFmpeg failed: " + e.getMessage());
            }
        }
        
        // 5. Configuration from properties
        System.out.println("\n5. CONFIGURATION PROPERTIES:");
        System.out.println("-".repeat(60));
        String resizeFactor = context.getEnvironment().getProperty("brobot.dpi.resize-factor");
        String captureProvider = context.getEnvironment().getProperty("brobot.capture.provider");
        String preferPhysical = context.getEnvironment().getProperty("brobot.capture.prefer-physical");
        
        System.out.println("   brobot.dpi.resize-factor: " + resizeFactor);
        System.out.println("   brobot.capture.provider: " + captureProvider);
        System.out.println("   brobot.capture.prefer-physical: " + preferPhysical);
        
        // 6. Summary
        System.out.println("\n6. CONFIGURATION SUMMARY:");
        System.out.println("-".repeat(60));
        
        boolean ffmpegConfigured = "FFMPEG".equalsIgnoreCase(captureProvider);
        boolean scalingDisabled = "1.0".equals(resizeFactor) || Math.abs(Settings.AlwaysResize - 1.0) < 0.001;
        
        if (ffmpegConfigured && scalingDisabled) {
            System.out.println("   ✅ OPTIMAL CONFIGURATION");
            System.out.println("      - FFmpeg configured as provider");
            System.out.println("      - Pattern scaling disabled (1.0)");
            System.out.println("      - Ready for Windows/SikuliX IDE patterns");
        } else {
            System.out.println("   ⚠️ CONFIGURATION NEEDS ADJUSTMENT:");
            if (!ffmpegConfigured) {
                System.out.println("      - FFmpeg not configured (current: " + captureProvider + ")");
            }
            if (!scalingDisabled) {
                System.out.println("      - Pattern scaling not disabled");
                System.out.println("        Property value: " + resizeFactor);
                System.out.println("        Settings.AlwaysResize: " + Settings.AlwaysResize);
            }
        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TEST COMPLETE");
        System.out.println("=".repeat(80));
        
        // Exit after test
        System.exit(0);
    }
    
    private void analyzeCapture(BufferedImage capture, String source) throws Exception {
        if (capture != null) {
            System.out.println("   ✅ " + source + " capture successful!");
            System.out.println("      Resolution: " + capture.getWidth() + "x" + capture.getHeight());
            
            if (capture.getWidth() == 1920 && capture.getHeight() == 1080) {
                System.out.println("      ✅ Physical resolution (1920x1080)");
            } else if (capture.getWidth() == 1536 && capture.getHeight() == 864) {
                System.out.println("      ⚠️ Logical resolution (1536x864) - 125% DPI");
            } else {
                System.out.println("      ℹ️ Other resolution");
            }
            
            // Save capture
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File outputFile = new File("ffmpeg_spring_test_" + timestamp + ".png");
            ImageIO.write(capture, "png", outputFile);
            System.out.println("      Saved to: " + outputFile.getName());
            System.out.println("      File size: " + (outputFile.length() / 1024) + " KB");
        } else {
            System.out.println("   ✗ " + source + " capture returned null");
        }
    }
}