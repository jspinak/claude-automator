package io.github.jspinak.claude.tools;

import org.sikuli.basics.Settings;
import org.sikuli.script.*;
import io.github.jspinak.brobot.capture.JavaCVFFmpegCapture;
import io.github.jspinak.brobot.capture.CrossPlatformPhysicalCapture;
import io.github.jspinak.brobot.capture.PhysicalScreenCapture;
import io.github.jspinak.brobot.capture.WindowsPhysicalCapture;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests pattern matching across different capture methods and resolutions
 * specifically for Claude Automator patterns.
 * 
 * This test:
 * 1. Captures screens at different resolutions
 * 2. Tests Claude-specific patterns with various scaling factors
 * 3. Creates visualizations showing matches with overlays
 * 4. Saves comparison images for debugging
 */
public class ComprehensiveCaptureTest {
    
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    // Use local images directory relative to claude-automator
    private static final String CLAUDE_PATH = "images/prompt/";
    
    // Test result storage
    static class TestResult {
        String patternName;
        String captureType;
        int captureWidth;
        int captureHeight;
        float dpiSetting;
        double similarity;
        boolean found;
        
        TestResult(String pattern, String capture, int w, int h, float dpi, double sim, boolean found) {
            this.patternName = pattern;
            this.captureType = capture;
            this.captureWidth = w;
            this.captureHeight = h;
            this.dpiSetting = dpi;
            this.similarity = sim;
            this.found = found;
        }
    }
    
    private static List<TestResult> results = new ArrayList<>();
    
    public static void main(String[] args) throws Exception {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("CLAUDE AUTOMATOR - COMPREHENSIVE CAPTURE TEST");
        System.out.println("Testing Claude-specific patterns across different capture methods");
        System.out.println("Time: " + dateFormat.format(new Date()));
        System.out.println("=".repeat(80));
        
        Screen screen = new Screen();
        
        // Test patterns - using only the two specified Claude Automator images
        String[] patterns = {
            "claude-prompt-3.png",     // SikuliX capture pattern
            "claude-prompt-win.png"    // Windows capture pattern
        };
        
        // First, capture the screen normally and at adjusted resolution
        System.out.println("\n1. CAPTURING SCREENS:");
        System.out.println("-".repeat(70));
        
        // Normal SikuliX capture (expected 1536x864 with DPI scaling)
        Settings.AlwaysResize = 1.0f;
        BufferedImage normalCapture = captureFullScreen(screen, "normal_sikuli");
        System.out.println("   SikuliX capture: " + normalCapture.getWidth() + "x" + normalCapture.getHeight());
        
        // On Windows, try Windows-specific capture methods first
        BufferedImage physicalCapture = null;
        String os = System.getProperty("os.name").toLowerCase();
        
        if (os.contains("win")) {
            // Try Windows-specific physical capture (combines multiple methods)
            try {
                System.out.println("\n   Attempting WindowsPhysicalCapture (multi-strategy):");
                physicalCapture = WindowsPhysicalCapture.capture();
                System.out.println("   Windows Physical capture: " + physicalCapture.getWidth() + "x" + physicalCapture.getHeight());
                saveCapture(physicalCapture, "windows_physical");
            } catch (Exception e) {
                System.out.println("   Windows Physical failed: " + e.getMessage());
            }
            
            // Also try PowerShell directly
            if (physicalCapture == null || physicalCapture.getWidth() < 1920) {
                try {
                    System.out.println("\n   Attempting PowerShell capture:");
                    BufferedImage psCapture = WindowsPhysicalCapture.captureWithPowerShell();
                    System.out.println("   PowerShell capture: " + psCapture.getWidth() + "x" + psCapture.getHeight());
                    saveCapture(psCapture, "powershell");
                    if (psCapture.getWidth() >= 1920) {
                        physicalCapture = psCapture;
                    }
                } catch (Exception e) {
                    System.out.println("   PowerShell failed: " + e.getMessage());
                }
            }
        }
        
        // Try JavaCV FFmpeg capture (should get physical resolution)
        if (physicalCapture == null || physicalCapture.getWidth() < 1920) {
            try {
                System.out.println("\n   Attempting JavaCV FFmpeg capture (bundled):");
                BufferedImage jcvCapture = JavaCVFFmpegCapture.capture();
                System.out.println("   JavaCV FFmpeg capture: " + jcvCapture.getWidth() + "x" + jcvCapture.getHeight());
                saveCapture(jcvCapture, "javacv_physical");
                if (jcvCapture.getWidth() >= 1920) {
                    physicalCapture = jcvCapture;
                }
            } catch (Exception e) {
                System.out.println("   JavaCV FFmpeg failed: " + e.getMessage());
            }
        }
        
        // Try CrossPlatformPhysicalCapture (will use best available method)
        try {
            System.out.println("\n   Attempting CrossPlatformPhysicalCapture:");
            BufferedImage crossCapture = CrossPlatformPhysicalCapture.capture();
            System.out.println("   CrossPlatform capture: " + crossCapture.getWidth() + "x" + crossCapture.getHeight());
            saveCapture(crossCapture, "cross_platform");
            
            // Use this as physical capture if JavaCV failed
            if (physicalCapture == null) {
                physicalCapture = crossCapture;
            }
        } catch (Exception e) {
            System.out.println("   CrossPlatform capture failed: " + e.getMessage());
        }
        
        // Try PhysicalScreenCapture
        try {
            System.out.println("\n   Attempting PhysicalScreenCapture:");
            BufferedImage physScreenCapture = PhysicalScreenCapture.capture();
            System.out.println("   PhysicalScreen capture: " + physScreenCapture.getWidth() + "x" + physScreenCapture.getHeight());
            saveCapture(physScreenCapture, "physical_screen");
            
            // Use this if we still don't have physical capture
            if (physicalCapture == null) {
                physicalCapture = physScreenCapture;
            }
        } catch (Exception e) {
            System.out.println("   PhysicalScreen capture failed: " + e.getMessage());
        }
        
        // Decide which capture to use for testing
        BufferedImage adjustedCapture;
        if (physicalCapture != null && physicalCapture.getWidth() == 1920 && physicalCapture.getHeight() == 1080) {
            System.out.println("\n   ✅ Using PHYSICAL RESOLUTION capture (1920x1080)");
            adjustedCapture = physicalCapture;
        } else if (physicalCapture != null && physicalCapture.getWidth() > normalCapture.getWidth()) {
            System.out.println("\n   ✅ Using higher resolution capture (" + physicalCapture.getWidth() + "x" + physicalCapture.getHeight() + ")");
            adjustedCapture = physicalCapture;
        } else {
            // Fall back to resizing
            System.out.println("\n   ⚠️ Physical capture methods didn't achieve 1920x1080");
            System.out.println("   Falling back to resized capture");
            adjustedCapture = resizeImage(normalCapture, 1920, 1080);
        }
        
        System.out.println("\n   Final captures for testing:");
        System.out.println("   - Normal (SikuliX): " + normalCapture.getWidth() + "x" + normalCapture.getHeight());
        System.out.println("   - Adjusted/Physical: " + adjustedCapture.getWidth() + "x" + adjustedCapture.getHeight());
        
        // Save both captures for visual inspection
        File normalFile = saveCapture(normalCapture, "final_normal_capture");
        File physicalFile = saveCapture(adjustedCapture, "final_physical_capture");
        System.out.println("\n   📸 Saved captures for inspection:");
        if (normalFile != null) System.out.println("   - Normal: " + normalFile.getName());
        if (physicalFile != null) System.out.println("   - Physical: " + physicalFile.getName());
        
        // Check if physical capture is actually different or just black/empty
        System.out.println("\n   🔍 Analyzing capture content:");
        analyzeCapture(normalCapture, "Normal");
        analyzeCapture(adjustedCapture, "Physical");
        
        // Now test each pattern
        System.out.println("\n2. TESTING PATTERNS:");
        System.out.println("=" + "=".repeat(79));
        
        for (String patternName : patterns) {
            String patternPath = CLAUDE_PATH + patternName;
            File patternFile = new File(patternPath);
            
            if (!patternFile.exists()) {
                System.out.println("\n❌ Pattern not found: " + patternName);
                System.out.println("   Looking in: " + patternFile.getAbsolutePath());
                continue;
            }
            
            System.out.println("\n📁 TESTING: " + patternName);
            System.out.println("-".repeat(70));
            
            try {
                BufferedImage patternImage = ImageIO.read(patternFile);
                System.out.println("   Pattern dimensions: " + patternImage.getWidth() + "x" + patternImage.getHeight());
                
                // For 1536x864 capture, scale patterns down by 0.8
                // For 1920x1080 capture, use original size
                
                // Test 1: Pattern on normal capture - scale if needed
                System.out.println("\n   TEST 1: On normal capture:");
                if (normalCapture.getWidth() < 1920) {
                    System.out.println("   Strategy: Scale patterns down by 0.8 for smaller capture");
                    BufferedImage scaledForNormal = resizeImage(patternImage, 
                        (int)(patternImage.getWidth() * 0.8), 
                        (int)(patternImage.getHeight() * 0.8));
                    File scaledForNormalFile = saveCapture(scaledForNormal, patternName.replace(".png", "_scaled_for_normal"));
                    if (scaledForNormalFile != null) {
                        testPatternOnCapture(screen, scaledForNormalFile.getAbsolutePath(), normalCapture, "Normal", patternName + " (scaled 0.8)");
                    }
                }
                // Also test original size for comparison
                testPatternOnCapture(screen, patternPath, normalCapture, "Normal", patternName + " (original)");
                
                // Test 2: Pattern on adjusted 1920x1080 capture - use original size
                System.out.println("\n   TEST 2: On adjusted 1920x1080 capture:");
                System.out.println("   Strategy: Use original pattern size for 1920x1080 capture");
                testPatternOnCapture(screen, patternPath, adjustedCapture, "Adjusted", patternName);
                
                // Test 2B: Try scaled pattern on physical capture to verify theory
                if (adjustedCapture.getWidth() == 1920) {
                    System.out.println("\n   TEST 2B: Scaled pattern (1.25x) on 1920x1080 capture:");
                    BufferedImage scaledForPhysical = resizeImage(patternImage, 
                        (int)(patternImage.getWidth() * 1.25), 
                        (int)(patternImage.getHeight() * 1.25));
                    File scaledForPhysicalFile = saveCapture(scaledForPhysical, patternName.replace(".png", "_scaled_for_physical"));
                    if (scaledForPhysicalFile != null) {
                        testPatternOnCapture(screen, scaledForPhysicalFile.getAbsolutePath(), adjustedCapture, "Physical125", patternName + " (scaled 1.25)");
                    }
                }
                
                // Test 3: Pattern on physical capture (if different from adjusted)
                if (physicalCapture != null && physicalCapture != adjustedCapture) {
                    System.out.println("\n   TEST 3: On physical capture:");
                    testPatternOnCapture(screen, patternPath, physicalCapture, "Physical", patternName);
                }
                
                // Test 4: Direct screen search (for comparison)
                System.out.println("\n   TEST 4: Direct screen search:");
                testDirectScreenSearch(screen, patternPath, patternName);
                
            } catch (IOException e) {
                System.err.println("   Error reading pattern: " + e.getMessage());
            }
        }
        
        // Test with resized patterns
        System.out.println("\n3. TESTING WITH RESIZED PATTERNS:");
        System.out.println("=" + "=".repeat(79));
        
        testResizedPatterns(screen, normalCapture, adjustedCapture);
        
        // Create visual comparison
        System.out.println("\n4. CREATING VISUAL COMPARISON:");
        System.out.println("=" + "=".repeat(79));
        
        createVisualComparison(screen);
        
        // Print summary
        printSummary();
    }
    
    private static void testPatternOnCapture(Screen screen, String patternPath, 
                                            BufferedImage capture, String captureType,
                                            String patternName) {
        // Save the capture as a temporary file to search within it
        File captureFile = saveCapture(capture, "test_capture_" + captureType);
        
        if (captureFile == null) {
            System.out.println("     ❌ Failed to save capture");
            return;
        }
        
        System.out.println("     Searching in " + capture.getWidth() + "x" + capture.getHeight() + " capture:");
        
        // Test with different DPI settings
        float[] dpiSettings = {1.0f};  // Simplified - pattern is already scaled appropriately
        
        for (float dpi : dpiSettings) {
            Settings.AlwaysResize = dpi;
            
            try {
                // Load pattern to check its size
                File patternFile = new File(patternPath);
                if (patternFile.exists()) {
                    BufferedImage patternImg = ImageIO.read(patternFile);
                    System.out.println("     Pattern size: " + patternImg.getWidth() + "x" + patternImg.getHeight());
                }
                
                // Create a Finder to search within the captured image
                Finder finder = new Finder(captureFile.getAbsolutePath());
                Pattern pattern = new Pattern(patternPath).similar(0.3);
                finder.find(pattern);
                
                boolean foundAny = false;
                Match bestMatch = null;
                double bestScore = 0;
                
                while (finder.hasNext()) {
                    Match match = finder.next();
                    double score = match.getScore();
                    
                    if (score > bestScore) {
                        bestMatch = match;
                        bestScore = score;
                    }
                    
                    if (!foundAny) {  // Only report the best match
                        System.out.printf("     Similarity: %.1f%%", score * 100);
                        
                        if (score > 0.90) System.out.println(" ✅ EXCELLENT");
                        else if (score > 0.80) System.out.println(" ✓ Very Good");
                        else if (score > 0.70) System.out.println(" ⚠ Good");
                        else if (score > 0.60) System.out.println(" ⚠ Moderate");
                        else System.out.println(" ❌ Poor");
                        
                        System.out.println("     Match location: " + match.x + "," + match.y);
                        
                        results.add(new TestResult(patternName, captureType, capture.getWidth(), 
                                                 capture.getHeight(), dpi, score, true));
                        foundAny = true;
                    }
                }
                
                // Create visualization if we found a match
                if (bestMatch != null && patternFile.exists()) {
                    BufferedImage patternImg = ImageIO.read(patternFile);
                    createMatchVisualization(capture, patternImg, bestMatch, 
                                           patternName.replace(".png", "") + "_" + captureType);
                }
                
                if (!foundAny) {
                    System.out.println("     No match found");
                    results.add(new TestResult(patternName, captureType, capture.getWidth(), 
                                             capture.getHeight(), dpi, 0.0, false));
                }
                
                finder.destroy();
                
            } catch (Exception e) {
                System.err.println("     Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private static void testDirectScreenSearch(Screen screen, String patternPath, String patternName) {
        float[] dpiSettings = {1.0f, 0.8f, 1.25f, 0.67f};
        
        for (float dpi : dpiSettings) {
            Settings.AlwaysResize = dpi;
            
            try {
                Pattern pattern = new Pattern(patternPath).similar(0.3);
                Match match = screen.exists(pattern, 0.1);
                
                if (match != null) {
                    double score = match.getScore();
                    System.out.printf("     DPI %.2f: %.1f%%", dpi, score * 100);
                    
                    if (score > 0.90) System.out.println(" ✅");
                    else if (score > 0.80) System.out.println(" ✓");
                    else if (score > 0.70) System.out.println(" ⚠");
                    else System.out.println("");
                    
                    results.add(new TestResult(patternName, "DirectScreen", 0, 0, dpi, score, true));
                } else {
                    System.out.printf("     DPI %.2f: No match\n", dpi);
                    results.add(new TestResult(patternName, "DirectScreen", 0, 0, dpi, 0.0, false));
                }
            } catch (Exception e) {
                System.err.printf("     DPI %.2f: Error\n", dpi);
            }
        }
    }
    
    private static void testResizedPatterns(Screen screen, BufferedImage normalCapture, 
                                           BufferedImage adjustedCapture) {
        System.out.println("\nTesting with dynamically resized patterns:");
        
        String testPattern = CLAUDE_PATH + "claude-prompt-3.png";
        File patternFile = new File(testPattern);
        
        if (!patternFile.exists()) {
            System.out.println("   Original pattern not found for resizing");
            return;
        }
        
        try {
            BufferedImage original = ImageIO.read(patternFile);
            
            // Create resized versions
            BufferedImage pattern80 = resizeImage(original, 
                                                 (int)(original.getWidth() * 0.8), 
                                                 (int)(original.getHeight() * 0.8));
            BufferedImage pattern125 = resizeImage(original, 
                                                  (int)(original.getWidth() * 1.25), 
                                                  (int)(original.getHeight() * 1.25));
            
            // Save resized patterns
            File pattern80File = saveCapture(pattern80, "pattern_80_percent");
            File pattern125File = saveCapture(pattern125, "pattern_125_percent");
            
            System.out.println("\n   80% resized pattern (" + pattern80.getWidth() + "x" + pattern80.getHeight() + "):");
            if (pattern80File != null) {
                testPatternOnCapture(screen, pattern80File.getAbsolutePath(), normalCapture, 
                                   "Normal", "Resized80");
            }
            
            System.out.println("\n   125% resized pattern (" + pattern125.getWidth() + "x" + pattern125.getHeight() + "):");
            if (pattern125File != null) {
                testPatternOnCapture(screen, pattern125File.getAbsolutePath(), adjustedCapture, 
                                   "Adjusted", "Resized125");
            }
            
        } catch (IOException e) {
            System.err.println("   Error resizing patterns: " + e.getMessage());
        }
    }
    
    private static void createMatchVisualization(BufferedImage screenshot, BufferedImage pattern, 
                                                Match match, String outputName) {
        System.out.println("   Creating match visualization for " + outputName);
        
        try {
            // Create a new image that shows the screenshot with the match highlighted
            // and the pattern placed vertically next to it
            int totalWidth = screenshot.getWidth() + pattern.getWidth() + 60; // 60px for spacing and labels
            int totalHeight = Math.max(screenshot.getHeight(), pattern.getHeight() + 100);
            
            BufferedImage visualization = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = visualization.createGraphics();
            
            // Set rendering hints for quality
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // White background
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, visualization.getWidth(), visualization.getHeight());
            
            // Draw the screenshot
            g.drawImage(screenshot, 10, 30, null);
            
            // Draw rectangle around match location if match found
            if (match != null) {
                g.setColor(Color.RED);
                g.setStroke(new java.awt.BasicStroke(3));
                g.drawRect(10 + match.x, 30 + match.y, match.w, match.h);
                
                // Draw similarity score
                g.setColor(Color.RED);
                g.setFont(new Font("Arial", Font.BOLD, 14));
                String scoreText = String.format("Match: %.1f%%", match.getScore() * 100);
                g.drawString(scoreText, 10 + match.x, 30 + match.y - 5);
            }
            
            // Draw the pattern on the right
            int patternX = screenshot.getWidth() + 30;
            int patternY = 60;
            g.drawImage(pattern, patternX, patternY, null);
            
            // Draw border around pattern
            g.setColor(Color.BLUE);
            g.setStroke(new java.awt.BasicStroke(2));
            g.drawRect(patternX, patternY, pattern.getWidth(), pattern.getHeight());
            
            // Add labels
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString("Screenshot (" + screenshot.getWidth() + "x" + screenshot.getHeight() + ")", 10, 20);
            g.drawString("Pattern (" + pattern.getWidth() + "x" + pattern.getHeight() + ")", patternX, patternY - 10);
            
            g.dispose();
            
            // Save the visualization
            File file = saveCapture(visualization, "match_viz_" + outputName);
            if (file != null) {
                System.out.println("     ✅ Saved: " + file.getName());
            }
            
        } catch (Exception e) {
            System.err.println("     Error creating match visualization: " + e.getMessage());
        }
    }
    
    private static void createVisualComparison(Screen screen) {
        System.out.println("\nCreating side-by-side visual comparison:");
        
        try {
            // Load the two test patterns
            File sikuliFile = new File(CLAUDE_PATH + "claude-prompt-3.png");
            File windowsFile = new File(CLAUDE_PATH + "claude-prompt-win.png");
            File scaledFile = null; // Not using pre-scaled file anymore
            
            BufferedImage sikuliImg = sikuliFile.exists() ? ImageIO.read(sikuliFile) : null;
            BufferedImage windowsImg = windowsFile.exists() ? ImageIO.read(windowsFile) : null;
            BufferedImage scaledImg = null; // Not using pre-scaled file
            
            // Try to capture a match from screen
            BufferedImage capturedMatch = null;
            String capturedInfo = "Not found";
            
            // Try to find and capture any pattern
            for (String patternName : new String[]{"claude-prompt-3.png", "claude-prompt-win.png"}) {
                String patternPath = CLAUDE_PATH + patternName;
                File f = new File(patternPath);
                if (!f.exists()) continue;
                
                Settings.AlwaysResize = 0.8f; // Use common successful setting
                Pattern pattern = new Pattern(patternPath).similar(0.3);
                Match match = screen.exists(pattern, 0.5);
                
                if (match != null) {
                    Region matchRegion = new Region(match);
                    capturedMatch = screen.capture(matchRegion).getImage();
                    capturedInfo = String.format("Captured %dx%d (%.1f%%)", 
                                                capturedMatch.getWidth(), 
                                                capturedMatch.getHeight(),
                                                match.getScore() * 100);
                    break;
                }
            }
            
            // Create comparison image
            int maxHeight = 200; // Scale all to max 200px height for comparison
            int padding = 20;
            int labelHeight = 30;
            
            // Calculate scaled dimensions - only showing the two patterns and live capture
            BufferedImage[] images = {sikuliImg, windowsImg, capturedMatch};
            String[] labels = {
                sikuliImg != null ? String.format("claude-prompt-3\n%dx%d", sikuliImg.getWidth(), sikuliImg.getHeight()) : "Not found",
                windowsImg != null ? String.format("claude-prompt-win\n%dx%d", windowsImg.getWidth(), windowsImg.getHeight()) : "Not found",
                capturedMatch != null ? "Live Capture\n" + capturedInfo : "Not found"
            };
            
            // Calculate total width
            int totalWidth = padding;
            int[] widths = new int[3];
            for (int i = 0; i < images.length; i++) {
                if (images[i] != null) {
                    double scale = (double)maxHeight / images[i].getHeight();
                    widths[i] = (int)(images[i].getWidth() * scale);
                    totalWidth += widths[i] + padding;
                } else {
                    widths[i] = 100; // Placeholder width
                    totalWidth += widths[i] + padding;
                }
            }
            
            // Create comparison image
            BufferedImage comparison = new BufferedImage(totalWidth, maxHeight + labelHeight + padding * 2, 
                                                        BufferedImage.TYPE_INT_RGB);
            Graphics2D g = comparison.createGraphics();
            
            // White background
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, comparison.getWidth(), comparison.getHeight());
            
            // Draw each image
            int xOffset = padding;
            for (int i = 0; i < images.length; i++) {
                // Draw label
                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.BOLD, 12));
                
                String[] lines = labels[i].split("\n");
                for (int j = 0; j < lines.length; j++) {
                    g.drawString(lines[j], xOffset, padding + j * 15);
                }
                
                // Draw image or placeholder
                if (images[i] != null) {
                    double scale = (double)maxHeight / images[i].getHeight();
                    int scaledWidth = (int)(images[i].getWidth() * scale);
                    int scaledHeight = maxHeight;
                    
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                                      RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g.drawImage(images[i], xOffset, labelHeight + padding, 
                               scaledWidth, scaledHeight, null);
                    
                    // Draw border
                    g.setColor(Color.GRAY);
                    g.drawRect(xOffset, labelHeight + padding, scaledWidth, scaledHeight);
                } else {
                    // Draw placeholder
                    g.setColor(Color.LIGHT_GRAY);
                    g.fillRect(xOffset, labelHeight + padding, widths[i], maxHeight);
                    g.setColor(Color.GRAY);
                    g.drawRect(xOffset, labelHeight + padding, widths[i], maxHeight);
                    g.setColor(Color.DARK_GRAY);
                    g.drawString("Not Available", xOffset + 10, labelHeight + padding + maxHeight/2);
                }
                
                xOffset += widths[i] + padding;
            }
            
            g.dispose();
            
            // Save comparison
            File comparisonFile = saveCapture(comparison, "visual_comparison");
            if (comparisonFile != null) {
                System.out.println("   ✅ Saved visual comparison: " + comparisonFile.getName());
                System.out.println("   Shows side-by-side: claude-prompt-3 | claude-prompt-win | Live Capture");
            }
            
            // Print analysis
            System.out.println("\n   VISUAL ANALYSIS:");
            if (sikuliImg != null) {
                System.out.println("   claude-prompt-3: " + sikuliImg.getWidth() + "x" + sikuliImg.getHeight());
            }
            if (windowsImg != null) {
                System.out.println("   claude-prompt-win: " + windowsImg.getWidth() + "x" + windowsImg.getHeight());
                if (sikuliImg != null) {
                    double ratio = (double)windowsImg.getWidth() / sikuliImg.getWidth();
                    System.out.println("   Win/SikuliX size ratio: " + String.format("%.3f", ratio));
                }
            }
            if (capturedMatch != null) {
                System.out.println("   Live capture: " + capturedInfo);
            }
            
        } catch (Exception e) {
            System.err.println("   Error creating visual comparison: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void printSummary() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SUMMARY OF RESULTS:");
        System.out.println("=".repeat(80));
        
        // Find best combinations
        System.out.println("\n🏆 BEST MATCHES (>80% similarity):");
        results.stream()
               .filter(r -> r.similarity > 0.80)
               .sorted((a, b) -> Double.compare(b.similarity, a.similarity))
               .limit(10)
               .forEach(r -> {
                   System.out.printf("   %.1f%% - %s on %s capture (%dx%d) with DPI %.2f\n",
                                   r.similarity * 100, r.patternName, r.captureType,
                                   r.captureWidth, r.captureHeight, r.dpiSetting);
               });
        
        // Analyze by pattern type
        System.out.println("\n📊 ANALYSIS BY PATTERN:");
        
        for (String pattern : new String[]{"claude-prompt-3.png", "claude-prompt-win.png"}) {
            System.out.println("\n   " + pattern + ":");
            
            results.stream()
                   .filter(r -> r.patternName.contains(pattern))
                   .sorted((a, b) -> Double.compare(b.similarity, a.similarity))
                   .limit(3)
                   .forEach(r -> {
                       System.out.printf("     %.1f%% on %s with DPI %.2f\n",
                                       r.similarity * 100, r.captureType, r.dpiSetting);
                   });
        }
        
        System.out.println("\n💡 CONCLUSIONS:");
        
        // Determine which capture method works best
        double normalAvg = results.stream()
            .filter(r -> r.captureType.equals("Normal"))
            .mapToDouble(r -> r.similarity)
            .average().orElse(0);
            
        double adjustedAvg = results.stream()
            .filter(r -> r.captureType.equals("Adjusted"))
            .mapToDouble(r -> r.similarity)
            .average().orElse(0);
            
        System.out.println("   Average on normal capture: " + String.format("%.1f%%", normalAvg * 100));
        System.out.println("   Average on adjusted capture (1920x1080): " + String.format("%.1f%%", adjustedAvg * 100));
        
        if (normalAvg > adjustedAvg) {
            System.out.println("\n   ✅ Normal capture works better");
            System.out.println("   → Use 80% pre-scaled patterns or Settings.AlwaysResize=1.25");
        } else {
            System.out.println("\n   ✅ Adjusted 1920x1080 capture works better");
            System.out.println("   → Consider resizing captures to 1920x1080 before matching");
        }
        
        System.out.println("\n" + "=".repeat(80));
    }
    
    private static BufferedImage captureFullScreen(Screen screen, String label) {
        try {
            ScreenImage screenImage = screen.capture();
            BufferedImage capture = screenImage.getImage();
            saveCapture(capture, "screen_" + label);
            return capture;
        } catch (Exception e) {
            System.err.println("Failed to capture screen: " + e.getMessage());
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        }
    }
    
    private static void analyzeCapture(BufferedImage image, String label) {
        if (image == null) {
            System.out.println("     " + label + ": NULL image!");
            return;
        }
        
        // Check if image is all black or all same color
        int firstPixel = image.getRGB(0, 0);
        boolean allSame = true;
        int sampleSize = 100; // Sample pixels for performance
        
        for (int x = 0; x < image.getWidth() && allSame; x += image.getWidth() / sampleSize) {
            for (int y = 0; y < image.getHeight() && allSame; y += image.getHeight() / sampleSize) {
                if (image.getRGB(x, y) != firstPixel) {
                    allSame = false;
                }
            }
        }
        
        if (allSame) {
            System.out.println("     " + label + ": WARNING - All pixels same color!");
        } else {
            // Calculate average brightness
            long totalBrightness = 0;
            int samples = 0;
            for (int x = 0; x < image.getWidth(); x += image.getWidth() / sampleSize) {
                for (int y = 0; y < image.getHeight(); y += image.getHeight() / sampleSize) {
                    int rgb = image.getRGB(x, y);
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;
                    totalBrightness += (r + g + b) / 3;
                    samples++;
                }
            }
            int avgBrightness = (int)(totalBrightness / samples);
            System.out.println("     " + label + ": Valid content (avg brightness: " + avgBrightness + "/255)");
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
    
    private static File saveCapture(BufferedImage image, String prefix) {
        try {
            File debugDir = new File("debug_captures");
            if (!debugDir.exists()) debugDir.mkdirs();
            
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File file = new File(debugDir, prefix + "_" + timestamp + ".png");
            ImageIO.write(image, "png", file);
            return file;
        } catch (IOException e) {
            System.err.println("Failed to save capture: " + e.getMessage());
            return null;
        }
    }
}