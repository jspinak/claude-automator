package com.claude.automator.debug;

import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.config.environment.ExecutionEnvironment;
import io.github.jspinak.brobot.util.image.core.BufferedImageUtilities;
import io.github.jspinak.brobot.model.element.Region;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Debug component to check ExecutionEnvironment state.
 */
@Component
@Profile("debug-env")
public class ExecutionEnvironmentDebug implements CommandLineRunner {

    @Autowired
    private BrobotProperties brobotProperties;

    @Autowired
    private BufferedImageUtilities bufferedImageUtilities;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n=== EXECUTION ENVIRONMENT DEBUG ===");

        ExecutionEnvironment env = ExecutionEnvironment.getInstance();

        System.out.println("\n1. ExecutionEnvironment State:");
        System.out.println("   hasDisplay(): " + env.hasDisplay());
        System.out.println("   canCaptureScreen(): " + env.canCaptureScreen());
        System.out.println("   isMockMode(): " + env.isMockMode());
        System.out.println("   useRealFiles(): " + env.useRealFiles());
        System.out.println("   shouldSkipSikuliX(): " + env.shouldSkipSikuliX());
        System.out.println("   Full info: " + env.getEnvironmentInfo());

        System.out.println("\n2. System Properties:");
        System.out.println("   java.awt.headless: " + System.getProperty("java.awt.headless"));
        System.out.println("   brobot.force.headless: " + System.getProperty("brobot.force.headless"));
        System.out.println("   GraphicsEnvironment.isHeadless: " + GraphicsEnvironment.isHeadless());

        System.out.println("\n3. BrobotProperties:");
        System.out.println("   core.mock: " + brobotProperties.getCore().isMock());
        System.out.println("   core.headless: " + brobotProperties.getCore().isHeadless());
        System.out.println("   screenshot.save-history: " + brobotProperties.getScreenshot().isSaveHistory());

        // Test 1: Direct Robot (should work)
        System.out.println("\n4. Direct Robot Test:");
        try {
            Robot robot = new Robot();
            BufferedImage directCapture = robot.createScreenCapture(new Rectangle(0, 0, 200, 200));
            File directFile = new File("debug-direct-robot.png");
            ImageIO.write(directCapture, "png", directFile);
            System.out.println("   Direct capture saved: " + directFile.getAbsolutePath());
            System.out.println("   Direct capture black pixels: " + analyzeBlackPixels(directCapture) + "%");
        } catch (Exception e) {
            System.err.println("   Direct Robot failed: " + e.getMessage());
        }

        // Test 2: Brobot utilities (might return black)
        System.out.println("\n5. Brobot BufferedImageUtilities Test:");
        try {
            // Force ExecutionEnvironment to allow capture
            System.out.println("   Before override - canCaptureScreen: " + env.canCaptureScreen());

            // Try to override the environment
            ExecutionEnvironment overrideEnv = ExecutionEnvironment.builder()
                    .mockMode(false)
                    .forceHeadless(false)
                    .allowScreenCapture(true)
                    .build();
            ExecutionEnvironment.setInstance(overrideEnv);

            System.out.println(
                    "   After override - canCaptureScreen: " + ExecutionEnvironment.getInstance().canCaptureScreen());

            // Try capture
            Region region = new Region(0, 0, 200, 200);
            BufferedImage brobotCapture = BufferedImageUtilities.getBufferedImageFromScreen(region);

            if (brobotCapture != null) {
                File brobotFile = new File("debug-brobot-capture.png");
                ImageIO.write(brobotCapture, "png", brobotFile);
                System.out.println("   Brobot capture saved: " + brobotFile.getAbsolutePath());
                System.out.println("   Brobot capture black pixels: " + analyzeBlackPixels(brobotCapture) + "%");
            } else {
                System.out.println("   Brobot capture returned null!");
            }
        } catch (Exception e) {
            System.err.println("   Brobot capture failed: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n=== DEBUG COMPLETE ===");
        System.out.println(
                "If Brobot capture is black but direct Robot works, the issue is ExecutionEnvironment.canCaptureScreen()");

        // Exit to prevent normal application startup
        System.exit(0);
    }

    private double analyzeBlackPixels(BufferedImage img) {
        int blackCount = 0;
        int samples = 100;
        for (int i = 0; i < samples; i++) {
            int x = (int) (Math.random() * img.getWidth());
            int y = (int) (Math.random() * img.getHeight());
            if (img.getRGB(x, y) == 0xFF000000)
                blackCount++;
        }
        return (blackCount * 100.0) / samples;
    }
}