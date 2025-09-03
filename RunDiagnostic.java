import java.awt.*;

/**
 * Standalone DPI diagnostic - no dependencies needed
 */
public class RunDiagnostic {
    public static void main(String[] args) {
        System.out.println("\n========== DPI AUTO-DETECTION DIAGNOSTIC ==========\n");
        
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
        System.out.println("   sun.java2d.win.uiScaleX: " + System.getProperty("sun.java2d.win.uiScaleX"));
        System.out.println("   sun.java2d.win.uiScaleY: " + System.getProperty("sun.java2d.win.uiScaleY"));
        
        // Check if running in WSL
        String wslDistro = System.getenv("WSL_DISTRO_NAME");
        if (wslDistro != null) {
            System.out.println("   ⚠ Running in WSL: " + wslDistro);
            System.out.println("   Note: WSL may not report Windows DPI correctly to Java");
        } else {
            System.out.println("   ✓ Running on native Windows");
        }
        
        // 3. Analysis
        System.out.println("\n3. ANALYSIS:");
        
        // Check if transform detects scaling
        if (Math.abs(transform.getScaleX() - 1.0) < 0.01) {
            System.out.println("   ❌ Java's transform.getScaleX() returns 1.0");
            System.out.println("      This means Java isn't detecting Windows DPI scaling");
            System.out.println("      Possible causes:");
            System.out.println("      - Java needs -Dsun.java2d.dpiaware=true flag");
            System.out.println("      - Windows compatibility settings override DPI");
            System.out.println("      - Java version doesn't support DPI detection");
        } else {
            System.out.println("   ✓ Java detected scaling: " + transform.getScaleX() + "x");
            double windowsScaling = transform.getScaleX() * 100;
            System.out.println("   Windows DPI scaling: " + (int)windowsScaling + "%");
            double patternScale = 1.0 / transform.getScaleX();
            System.out.println("   Recommended pattern scale factor: " + String.format("%.3f", patternScale));
        }
        
        // Check resolution-based detection
        if (screenSize.width == 1536 && screenSize.height == 864) {
            System.out.println("\n   ✓ Screen size 1536x864 suggests 125% scaling of 1920x1080");
            System.out.println("   Recommended resize factor: 0.8");
        } else if (screenSize.width == 1280 && screenSize.height == 720) {
            System.out.println("\n   ✓ Screen size 1280x720 suggests 150% scaling of 1920x1080");
            System.out.println("   Recommended resize factor: 0.667");
        } else if (screenSize.width == 1920 && screenSize.height == 1080) {
            System.out.println("\n   Screen size 1920x1080 - could be:");
            System.out.println("   - Native 1080p (no scaling) - resize factor: 1.0");
            System.out.println("   - 200% scaled 4K - resize factor: 0.5");
        } else {
            System.out.println("\n   Screen size: " + screenSize.width + "x" + screenSize.height);
            System.out.println("   Unable to determine scaling from resolution alone");
        }
        
        // 4. Recommendations
        System.out.println("\n4. RECOMMENDATIONS:");
        if (Math.abs(transform.getScaleX() - 1.0) < 0.01) {
            System.out.println("   To enable DPI detection, try running with:");
            System.out.println("   java -Dsun.java2d.dpiaware=true -Dsun.java2d.uiScale=1.25 YourApp");
            System.out.println("\n   Or set in application.properties:");
            System.out.println("   brobot.dpi.resize-factor=0.8  # For 125% scaling");
        }
        
        System.out.println("\n==============================================\n");
    }
}