package io.github.jspinak.claude.tools;

import org.sikuli.script.*;
import org.sikuli.basics.Settings;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Test to determine if SikuliX is using logical or physical resolution
 */
public class SikuliXResolutionTest {
    
    public static void main(String[] args) throws Exception {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SIKULIX RESOLUTION DETECTION TEST");
        System.out.println("=".repeat(80));
        
        // 1. Check Java AWT resolution
        System.out.println("\n1. JAVA AWT REPORTS:");
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        System.out.println("   Toolkit.getScreenSize(): " + screenSize.width + "x" + screenSize.height);
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        DisplayMode dm = gd.getDisplayMode();
        System.out.println("   DisplayMode: " + dm.getWidth() + "x" + dm.getHeight());
        
        // 2. Check SikuliX Screen
        System.out.println("\n2. SIKULIX SCREEN OBJECT:");
        Screen screen = new Screen();
        System.out.println("   Screen.w x Screen.h: " + screen.w + "x" + screen.h);
        System.out.println("   Screen.getBounds(): " + screen.getBounds());
        
        // 3. Take actual capture
        System.out.println("\n3. ACTUAL CAPTURE TEST:");
        ScreenImage screenImage = screen.capture();
        BufferedImage captured = screenImage.getImage();
        System.out.println("   Captured image size: " + captured.getWidth() + "x" + captured.getHeight());
        
        // Save the capture
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File outFile = new File("sikuli_resolution_test_" + timestamp + ".png");
        ImageIO.write(captured, "png", outFile);
        System.out.println("   Saved to: " + outFile.getName());
        
        // 4. Check Settings
        System.out.println("\n4. SIKULIX SETTINGS:");
        System.out.println("   Settings.AlwaysResize: " + Settings.AlwaysResize);
        System.out.println("   Settings.CheckLastSeen: " + Settings.CheckLastSeen);
        
        // 5. Robot test (what SikuliX uses internally)
        System.out.println("\n5. JAVA ROBOT TEST:");
        Robot robot = new Robot();
        BufferedImage robotCapture = robot.createScreenCapture(new Rectangle(screenSize));
        System.out.println("   Robot capture size: " + robotCapture.getWidth() + "x" + robotCapture.getHeight());
        
        // 6. DPI Detection
        System.out.println("\n6. DPI ANALYSIS:");
        int logicalWidth = screenSize.width;
        int physicalWidth = 1920; // Assuming standard physical
        
        if (logicalWidth == 1536) {
            System.out.println("   Detected: 125% DPI scaling (1536x864 logical, 1920x1080 physical)");
            System.out.println("   SikuliX is using: LOGICAL resolution");
        } else if (logicalWidth == 1920) {
            System.out.println("   Detected: No DPI scaling OR DPI-unaware");
            System.out.println("   SikuliX is using: PHYSICAL resolution");
        } else if (logicalWidth == 1280) {
            System.out.println("   Detected: 150% DPI scaling");
            System.out.println("   SikuliX is using: LOGICAL resolution");
        } else {
            System.out.println("   Unknown resolution: " + logicalWidth);
        }
        
        // 7. Pattern matching test
        System.out.println("\n7. PATTERN MATCHING IMPLICATIONS:");
        if (captured.getWidth() == 1536) {
            System.out.println("   ✓ Patterns captured in SikuliX IDE will be at 1536x864");
            System.out.println("   ✓ These patterns will match on 1536x864 screens");
            System.out.println("   ✗ These patterns need 0.8x scaling for 1920x1080 screens");
        } else if (captured.getWidth() == 1920) {
            System.out.println("   ✓ Patterns captured in SikuliX IDE will be at 1920x1080");
            System.out.println("   ✗ These patterns need 0.8x scaling for 1536x864 screens");
            System.out.println("   ✓ These patterns will match on 1920x1080 screens");
        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("CONCLUSION:");
        System.out.println("SikuliX is capturing at: " + captured.getWidth() + "x" + captured.getHeight());
        System.out.println("=".repeat(80));
    }
}