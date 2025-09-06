package io.github.jspinak.claude.tools;

import io.github.jspinak.brobot.capture.UnifiedCaptureService;
import io.github.jspinak.brobot.capture.JavaCVFFmpegCapture;
import org.sikuli.script.Screen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

@SpringBootApplication
@ComponentScan(basePackages = {"io.github.jspinak.brobot", "io.github.jspinak.claude"})
public class CaptureResolutionDiagnostic implements CommandLineRunner {

    @Autowired
    private UnifiedCaptureService captureService;

    public static void main(String[] args) {
        // Set DPI system properties before Spring starts
        System.setProperty("brobot.dpi.disable", "true");
        System.setProperty("sun.java2d.dpiaware", "false");
        System.setProperty("sun.java2d.uiScale", "1.0");
        System.setProperty("BROBOT_DISABLE_DPI", "true");
        
        SpringApplication.run(CaptureResolutionDiagnostic.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n=== CAPTURE RESOLUTION DIAGNOSTIC ===\n");
        
        // 1. Check screen dimensions
        Screen screen = new Screen();
        Rectangle bounds = screen.getBounds();
        System.out.println("SikuliX Screen bounds: " + bounds.width + "x" + bounds.height);
        
        // 2. Check Toolkit screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        System.out.println("Java Toolkit screen size: " + (int)screenSize.getWidth() + "x" + (int)screenSize.getHeight());
        
        // 3. Test UnifiedCaptureService capture
        System.out.println("\n--- Testing UnifiedCaptureService ---");
        System.out.println("Active provider: " + captureService.getActiveProviderName());
        BufferedImage captureImage = captureService.captureScreen();
        if (captureImage != null) {
            System.out.println("Capture resolution: " + captureImage.getWidth() + "x" + captureImage.getHeight());
            ImageIO.write(captureImage, "png", new File("unified-capture.png"));
            System.out.println("Saved: unified-capture.png");
        } else {
            System.out.println("Capture failed!");
        }
        
        // 4. Test JavaCV FFmpeg directly
        System.out.println("\n--- Testing JavaCV FFmpeg directly ---");
        try {
            BufferedImage ffmpegImage = JavaCVFFmpegCapture.capture();
            if (ffmpegImage != null) {
                System.out.println("FFmpeg capture resolution: " + ffmpegImage.getWidth() + "x" + ffmpegImage.getHeight());
                ImageIO.write(ffmpegImage, "png", new File("ffmpeg-direct.png"));
                System.out.println("Saved: ffmpeg-direct.png");
            }
        } catch (Exception e) {
            System.out.println("FFmpeg capture failed: " + e.getMessage());
        }
        
        // 5. Test Robot capture
        System.out.println("\n--- Testing Robot capture ---");
        try {
            Robot robot = new Robot();
            BufferedImage robotImage = robot.createScreenCapture(new Rectangle(screenSize));
            System.out.println("Robot capture resolution: " + robotImage.getWidth() + "x" + robotImage.getHeight());
            ImageIO.write(robotImage, "png", new File("robot-capture.png"));
            System.out.println("Saved: robot-capture.png");
        } catch (Exception e) {
            System.out.println("Robot capture failed: " + e.getMessage());
        }
        
        // 6. Test SikuliX capture
        System.out.println("\n--- Testing SikuliX capture ---");
        BufferedImage sikuliImage = screen.capture().getImage();
        System.out.println("SikuliX capture resolution: " + sikuliImage.getWidth() + "x" + sikuliImage.getHeight());
        ImageIO.write(sikuliImage, "png", new File("sikuli-capture.png"));
        System.out.println("Saved: sikuli-capture.png");
        
        // 7. Pattern resolution check
        System.out.println("\n--- Pattern Resolutions ---");
        checkPatternResolution("images/prompt/windows.png");
        checkPatternResolution("images/prompt/ffmpeg.png");
        
        System.out.println("\n=== DIAGNOSTIC COMPLETE ===\n");
    }
    
    private void checkPatternResolution(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                BufferedImage img = ImageIO.read(file);
                System.out.println(path + ": " + img.getWidth() + "x" + img.getHeight());
            } else {
                System.out.println(path + ": File not found");
            }
        } catch (Exception e) {
            System.out.println(path + ": Error reading - " + e.getMessage());
        }
    }
}