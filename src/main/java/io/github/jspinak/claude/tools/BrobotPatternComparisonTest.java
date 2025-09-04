package io.github.jspinak.claude.tools;

import org.sikuli.basics.Settings;
import org.sikuli.script.*;
import io.github.jspinak.brobot.capture.PhysicalScreenCapture;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Test that compares claude-prompt-3 and claude-prompt-win patterns
 * with Brobot screenshots (now at physical resolution with DPI scaling disabled).
 * 
 * This test:
 * 1. Captures the screen using Brobot (physical resolution)
 * 2. Tests both patterns for matches
 * 3. Reports similarity scores
 * 4. Creates comparison PNG showing the match, claude-prompt-3, and claude-prompt-win
 */
public class BrobotPatternComparisonTest {
    
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    private static final SimpleDateFormat fileTimestamp = new SimpleDateFormat("yyyyMMdd_HHmmss");
    private static final String CLAUDE_PATH = "images/prompt/";
    
    public static void main(String[] args) throws Exception {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("BROBOT PATTERN COMPARISON TEST");
        System.out.println("Comparing claude-prompt-3 and claude-prompt-win patterns");
        System.out.println("with Brobot screenshots");
        System.out.println("Time: " + dateFormat.format(new Date()));
        System.out.println("=".repeat(80));
        
        // Check DPI and display settings
        System.out.println("\n0. CHECKING DPI AWARENESS AND SETTINGS:");
        System.out.println("-".repeat(70));
        
        // Get actual screen dimensions from Java AWT
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int actualWidth = gd.getDisplayMode().getWidth();
        int actualHeight = gd.getDisplayMode().getHeight();
        System.out.println("   Java AWT Screen Resolution: " + actualWidth + "x" + actualHeight);
        
        // Check SikuliX Settings
        System.out.println("   Settings.AlwaysResize (before): " + Settings.AlwaysResize);
        
        // Get screen from SikuliX
        Screen screen = new Screen();
        Rectangle bounds = screen.getBounds();
        System.out.println("   SikuliX Screen Bounds: " + bounds.width + "x" + bounds.height);
        
        // Try to force physical resolution
        System.out.println("\n   Testing different capture methods:");
        
        // Method 1: Default capture
        Settings.AlwaysResize = 1.0f;
        BufferedImage capture1 = captureFullScreen(screen);
        System.out.println("   With AlwaysResize=1.0: " + capture1.getWidth() + "x" + capture1.getHeight());
        
        // Method 2: Try to get physical resolution
        Settings.AlwaysResize = 1.25f; // 1.25 = 1/0.8 to compensate for 80% scaling
        BufferedImage capture2 = captureFullScreen(screen);
        System.out.println("   With AlwaysResize=1.25: " + capture2.getWidth() + "x" + capture2.getHeight());
        
        // Method 3: Direct Robot capture
        try {
            Robot robot = new Robot();
            Rectangle screenRect = new Rectangle(0, 0, actualWidth, actualHeight);
            BufferedImage robotCapture = robot.createScreenCapture(screenRect);
            System.out.println("   Direct Robot capture: " + robotCapture.getWidth() + "x" + robotCapture.getHeight());
            
            // Save this for comparison
            File robotFile = saveImage(robotCapture, "robot_direct_capture");
        } catch (Exception e) {
            System.out.println("   Direct Robot capture failed: " + e.getMessage());
        }
        
        // Method 4: Try to capture at 1920x1080 explicitly
        try {
            Robot robot = new Robot();
            Rectangle fullRect = new Rectangle(0, 0, 1920, 1080);
            BufferedImage fullCapture = robot.createScreenCapture(fullRect);
            System.out.println("   Robot 1920x1080 explicit: " + fullCapture.getWidth() + "x" + fullCapture.getHeight());
        } catch (Exception e) {
            System.out.println("   Robot 1920x1080 failed: " + e.getMessage());
        }
        
        // Method 5: Try Brobot's PhysicalScreenCapture
        System.out.println("\n   Testing Brobot's PhysicalScreenCapture:");
        try {
            BufferedImage physicalCapture = PhysicalScreenCapture.capturePhysicalScreen();
            System.out.println("   PhysicalScreenCapture: " + physicalCapture.getWidth() + "x" + physicalCapture.getHeight());
            File physFile = saveImage(physicalCapture, "brobot_physical_capture");
            System.out.println("   Saved as: " + (physFile != null ? physFile.getName() : "Failed"));
        } catch (Exception e) {
            System.out.println("   PhysicalScreenCapture failed: " + e.getMessage());
        }
        
        // Decide which capture method to use based on results
        Settings.AlwaysResize = 1.0f; // Start with default
        
        // Capture the current screen
        System.out.println("\n1. CAPTURING SCREEN WITH BROBOT:");
        System.out.println("-".repeat(70));
        
        // First attempt with default settings
        BufferedImage screenCapture = captureFullScreen(screen);
        System.out.println("   Default capture: " + screenCapture.getWidth() + "x" + screenCapture.getHeight());
        
        // If we got scaled resolution, try to get physical resolution
        if (screenCapture.getWidth() == 1536 && screenCapture.getHeight() == 864) {
            System.out.println("   Detected scaled capture, attempting physical resolution...");
            
            // Try with 1.25x scaling to compensate
            Settings.AlwaysResize = 1.25f;
            BufferedImage physicalCapture = captureFullScreen(screen);
            System.out.println("   With compensation (1.25x): " + physicalCapture.getWidth() + "x" + physicalCapture.getHeight());
            
            // Use the larger capture if it worked
            if (physicalCapture.getWidth() > screenCapture.getWidth()) {
                screenCapture = physicalCapture;
                System.out.println("   ✅ Using compensated capture for better resolution");
            } else {
                Settings.AlwaysResize = 1.0f; // Reset if it didn't help
            }
        }
        
        System.out.println("   Final capture resolution: " + screenCapture.getWidth() + "x" + screenCapture.getHeight());
        
        // Determine if we have physical resolution
        if (screenCapture.getWidth() == 1920 && screenCapture.getHeight() == 1080) {
            System.out.println("   ✅ Captured at PHYSICAL RESOLUTION (1920x1080)");
        } else if (screenCapture.getWidth() == 1536 && screenCapture.getHeight() == 864) {
            System.out.println("   ⚠️ Captured at SCALED RESOLUTION (1536x864 = 80% of 1920x1080)");
            System.out.println("   DPI scaling appears to be ACTIVE (125% Windows scaling)");
            System.out.println("   To get physical resolution, you may need to:");
            System.out.println("   - Set Windows DPI awareness for Java process");
            System.out.println("   - Or use Settings.AlwaysResize = 1.25");
        } else {
            System.out.println("   Resolution: " + screenCapture.getWidth() + "x" + screenCapture.getHeight());
        }
        
        // Save the screen capture for reference
        File screenFile = saveImage(screenCapture, "brobot_screen_capture");
        System.out.println("   Saved as: " + (screenFile != null ? screenFile.getName() : "Failed to save"));
        
        // Test patterns
        String[] patterns = {
            "claude-prompt-3.png",
            "claude-prompt-win.png"  // Windows-captured pattern
        };
        
        // Store results for comparison
        Match[] matches = new Match[2];
        BufferedImage[] patternImages = new BufferedImage[2];
        double[] similarities = new double[2];
        
        System.out.println("\n2. TESTING PATTERNS:");
        System.out.println("=" + "=".repeat(79));
        
        for (int i = 0; i < patterns.length; i++) {
            String patternName = patterns[i];
            String patternPath = CLAUDE_PATH + patternName;
            File patternFile = new File(patternPath);
            
            System.out.println("\n📁 Testing: " + patternName);
            System.out.println("-".repeat(70));
            
            if (!patternFile.exists()) {
                System.out.println("   ❌ Pattern file not found: " + patternFile.getAbsolutePath());
                continue;
            }
            
            try {
                // Load pattern image
                patternImages[i] = ImageIO.read(patternFile);
                System.out.println("   Pattern dimensions: " + patternImages[i].getWidth() + "x" + patternImages[i].getHeight());
                
                // Search for pattern in captured screen
                System.out.println("\n   Searching in Brobot capture...");
                
                // Create a Finder to search within the captured image
                if (screenFile != null) {
                    Finder finder = new Finder(screenFile.getAbsolutePath());
                    Pattern pattern = new Pattern(patternPath).similar(0.3); // Low threshold to find any match
                    finder.find(pattern);
                    
                    if (finder.hasNext()) {
                        matches[i] = finder.next();
                        similarities[i] = matches[i].getScore();
                        
                        System.out.printf("   ✅ MATCH FOUND!\n");
                        System.out.printf("   Similarity: %.1f%%", similarities[i] * 100);
                        
                        if (similarities[i] > 0.95) System.out.println(" [PERFECT]");
                        else if (similarities[i] > 0.90) System.out.println(" [EXCELLENT]");
                        else if (similarities[i] > 0.80) System.out.println(" [VERY GOOD]");
                        else if (similarities[i] > 0.70) System.out.println(" [GOOD]");
                        else if (similarities[i] > 0.60) System.out.println(" [MODERATE]");
                        else System.out.println(" [WEAK]");
                        
                        System.out.println("   Location: x=" + matches[i].x + ", y=" + matches[i].y);
                        System.out.println("   Match size: " + matches[i].w + "x" + matches[i].h);
                        
                    } else {
                        System.out.println("   ❌ No match found");
                        System.out.println("   (Pattern may need scaling or the content is not on screen)");
                    }
                    
                    finder.destroy();
                }
                
                // Also try different scaling factors to see what works
                System.out.println("\n   Testing with different scale factors:");
                testWithScaling(screen, screenFile, patternPath, patternImages[i]);
                
            } catch (Exception e) {
                System.err.println("   Error testing pattern: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Create comparison visualization
        System.out.println("\n3. CREATING COMPARISON VISUALIZATION:");
        System.out.println("=" + "=".repeat(79));
        
        createComparisonVisualization(screenCapture, patternImages, matches, similarities, patterns);
        
        // Print summary
        System.out.println("\n4. SUMMARY:");
        System.out.println("=" + "=".repeat(79));
        printSummary(patterns, similarities, matches, screenCapture);
    }
    
    private static void testWithScaling(Screen screen, File screenFile, String patternPath, BufferedImage patternImage) {
        if (screenFile == null) return;
        
        float[] scaleFactors = {0.8f, 1.25f, 0.67f, 1.5f};
        System.out.println("   Scale Factor | Similarity | Result");
        System.out.println("   " + "-".repeat(40));
        
        for (float scale : scaleFactors) {
            try {
                // Create scaled pattern
                int newWidth = (int)(patternImage.getWidth() * scale);
                int newHeight = (int)(patternImage.getHeight() * scale);
                BufferedImage scaledPattern = resizeImage(patternImage, newWidth, newHeight);
                
                // Save scaled pattern temporarily
                File scaledFile = saveImage(scaledPattern, "temp_scaled_" + scale);
                if (scaledFile == null) continue;
                
                // Search with scaled pattern
                Finder finder = new Finder(screenFile.getAbsolutePath());
                Pattern pattern = new Pattern(scaledFile.getAbsolutePath()).similar(0.3);
                finder.find(pattern);
                
                if (finder.hasNext()) {
                    Match match = finder.next();
                    System.out.printf("   %6.2fx     | %6.1f%%   | ", scale, match.getScore() * 100);
                    
                    if (match.getScore() > 0.80) System.out.println("✅ Good match!");
                    else if (match.getScore() > 0.60) System.out.println("⚠️  Moderate");
                    else System.out.println("❌ Poor");
                } else {
                    System.out.printf("   %6.2fx     |    --     | No match\n", scale);
                }
                
                finder.destroy();
                scaledFile.delete(); // Clean up temp file
                
            } catch (Exception e) {
                System.out.printf("   %6.2fx     |    --     | Error\n", scale);
            }
        }
    }
    
    private static void createComparisonVisualization(BufferedImage screenCapture, 
                                                     BufferedImage[] patternImages,
                                                     Match[] matches, 
                                                     double[] similarities,
                                                     String[] patternNames) {
        try {
            System.out.println("\nCreating comprehensive comparison image...");
            
            // Calculate layout
            int padding = 20;
            int labelHeight = 30;
            
            // We'll show: screen with matches highlighted, then the two patterns side by side
            int screenHeight = 600; // Scale screen down for display
            double screenScale = (double)screenHeight / screenCapture.getHeight();
            int screenWidth = (int)(screenCapture.getWidth() * screenScale);
            
            // Pattern display sizes (keep original aspect ratio, max height 200)
            int maxPatternHeight = 200;
            int[] patternWidths = new int[2];
            int[] patternHeights = new int[2];
            
            for (int i = 0; i < 2; i++) {
                if (patternImages[i] != null) {
                    double scale = Math.min(1.0, (double)maxPatternHeight / patternImages[i].getHeight());
                    patternWidths[i] = (int)(patternImages[i].getWidth() * scale);
                    patternHeights[i] = (int)(patternImages[i].getHeight() * scale);
                } else {
                    patternWidths[i] = 150; // Default for missing pattern
                    patternHeights[i] = 100;
                }
            }
            
            // Calculate total dimensions
            int totalWidth = Math.max(screenWidth + padding * 2, 
                                     patternWidths[0] + patternWidths[1] + padding * 3);
            int totalHeight = screenHeight + maxPatternHeight + labelHeight * 3 + padding * 4;
            
            // Create the visualization
            BufferedImage visualization = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = visualization.createGraphics();
            
            // Set rendering hints for quality
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            // White background
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, visualization.getWidth(), visualization.getHeight());
            
            // Title
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("Brobot Pattern Comparison - Physical Resolution", padding, padding + 15);
            
            // Draw the screen capture with matches
            int screenY = padding * 2 + labelHeight;
            g.drawImage(screenCapture, padding, screenY, screenWidth, screenHeight, null);
            
            // Draw border around screen
            g.setColor(Color.GRAY);
            g.setStroke(new BasicStroke(1));
            g.drawRect(padding, screenY, screenWidth, screenHeight);
            
            // Draw match rectangles and labels on the screen
            for (int i = 0; i < 2; i++) {
                if (matches[i] != null) {
                    // Scale match coordinates to display size
                    int x = padding + (int)(matches[i].x * screenScale);
                    int y = screenY + (int)(matches[i].y * screenScale);
                    int w = (int)(matches[i].w * screenScale);
                    int h = (int)(matches[i].h * screenScale);
                    
                    // Use different colors for each pattern
                    g.setColor(i == 0 ? Color.RED : Color.BLUE);
                    g.setStroke(new BasicStroke(3));
                    g.drawRect(x, y, w, h);
                    
                    // Draw similarity score
                    g.setFont(new Font("Arial", Font.BOLD, 12));
                    String scoreText = String.format("%.1f%%", similarities[i] * 100);
                    g.drawString(scoreText, x, y - 5);
                }
            }
            
            // Label for screen
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString("Brobot Screen Capture (" + screenCapture.getWidth() + "x" + screenCapture.getHeight() + ")", 
                        padding, screenY - 5);
            
            // Draw the patterns below
            int patternY = screenY + screenHeight + padding * 2;
            
            // Pattern 1 (claude-prompt-3)
            int pattern1X = padding;
            drawPatternWithInfo(g, patternImages[0], pattern1X, patternY, patternWidths[0], patternHeights[0],
                              "claude-prompt-3.png", similarities[0], Color.RED);
            
            // Pattern 2 (claude-prompt-win)
            int pattern2X = pattern1X + patternWidths[0] + padding * 2;
            drawPatternWithInfo(g, patternImages[1], pattern2X, patternY, patternWidths[1], patternHeights[1],
                              "claude-prompt-win.png", similarities[1], Color.BLUE);
            
            // Add legend
            int legendY = patternY + maxPatternHeight + padding;
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.setColor(Color.BLACK);
            g.drawString("Red box = claude-prompt-3 match | Blue box = claude-prompt-win match", padding, legendY);
            
            g.dispose();
            
            // Save the visualization
            File vizFile = saveImage(visualization, "pattern_comparison");
            if (vizFile != null) {
                System.out.println("   ✅ Saved comparison visualization: " + vizFile.getName());
                System.out.println("   Location: " + vizFile.getAbsolutePath());
            }
            
        } catch (Exception e) {
            System.err.println("   Error creating visualization: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void drawPatternWithInfo(Graphics2D g, BufferedImage pattern, 
                                           int x, int y, int width, int height,
                                           String name, double similarity, Color borderColor) {
        // Draw pattern or placeholder
        if (pattern != null) {
            g.drawImage(pattern, x, y + 20, width, height, null);
        } else {
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(x, y + 20, width, height);
        }
        
        // Draw border
        g.setColor(borderColor);
        g.setStroke(new BasicStroke(2));
        g.drawRect(x, y + 20, width, height);
        
        // Draw label and info
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString(name, x, y + 10);
        
        if (pattern != null) {
            g.setFont(new Font("Arial", Font.PLAIN, 10));
            g.drawString(String.format("Size: %dx%d", pattern.getWidth(), pattern.getHeight()), 
                        x, y + height + 35);
            
            if (similarity > 0) {
                g.setColor(similarity > 0.8 ? new Color(0, 128, 0) : Color.BLACK);
                g.drawString(String.format("Match: %.1f%%", similarity * 100), 
                           x, y + height + 48);
            } else {
                g.setColor(Color.RED);
                g.drawString("No match", x, y + height + 48);
            }
        }
    }
    
    private static void printSummary(String[] patterns, double[] similarities, 
                                    Match[] matches, BufferedImage screen) {
        System.out.println("\n📊 RESULTS SUMMARY:");
        System.out.println("-".repeat(70));
        System.out.println("Screen Resolution: " + screen.getWidth() + "x" + screen.getHeight());
        System.out.println("(Physical resolution with DPI scaling disabled)\n");
        
        for (int i = 0; i < patterns.length; i++) {
            String displayName = patterns[i].replace(".png", "");
            
            System.out.println(displayName + ":");
            if (similarities[i] > 0) {
                System.out.printf("  ✅ Match found: %.1f%% similarity\n", similarities[i] * 100);
                if (matches[i] != null) {
                    System.out.println("  Location: (" + matches[i].x + ", " + matches[i].y + ")");
                    System.out.println("  Size: " + matches[i].w + "x" + matches[i].h);
                }
            } else {
                System.out.println("  ❌ No match found");
            }
            System.out.println();
        }
        
        // Comparison
        System.out.println("COMPARISON:");
        if (similarities[0] > 0 && similarities[1] > 0) {
            if (similarities[1] > similarities[0]) {
                System.out.println("✅ claude-prompt-win performs better (" + 
                                 String.format("%.1f%% vs %.1f%%", similarities[1] * 100, similarities[0] * 100) + ")");
            } else if (similarities[0] > similarities[1]) {
                System.out.println("✅ claude-prompt-3 performs better (" + 
                                 String.format("%.1f%% vs %.1f%%", similarities[0] * 100, similarities[1] * 100) + ")");
            } else {
                System.out.println("Both patterns perform equally well");
            }
        } else if (similarities[0] > 0) {
            System.out.println("Only claude-prompt-3 found a match");
        } else if (similarities[1] > 0) {
            System.out.println("Only claude-prompt-win found a match");
        } else {
            System.out.println("Neither pattern found a match - ensure the target is visible on screen");
        }
        
        System.out.println("\n" + "=".repeat(80));
    }
    
    private static BufferedImage captureFullScreen(Screen screen) {
        try {
            ScreenImage screenImage = screen.capture();
            return screenImage.getImage();
        } catch (Exception e) {
            System.err.println("Failed to capture screen: " + e.getMessage());
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        }
    }
    
    private static BufferedImage resizeImage(BufferedImage original, int targetWidth, int targetHeight) {
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return resized;
    }
    
    private static File saveImage(BufferedImage image, String prefix) {
        try {
            File debugDir = new File("debug_captures");
            if (!debugDir.exists()) debugDir.mkdirs();
            
            String timestamp = fileTimestamp.format(new Date());
            File file = new File(debugDir, prefix + "_" + timestamp + ".png");
            ImageIO.write(image, "png", file);
            return file;
        } catch (IOException e) {
            System.err.println("Failed to save image: " + e.getMessage());
            return null;
        }
    }
}