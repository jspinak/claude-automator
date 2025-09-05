package io.github.jspinak.claude.tools;

import org.sikuli.script.*;
import io.github.jspinak.brobot.capture.JavaCVFFmpegCapture;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Compares screenshots from different capture methods to determine the best tool.
 * 
 * This test:
 * 1. Takes screenshots using all three Brobot capture methods (SikuliX, Robot, FFmpeg)
 * 2. Compares them with Windows and SikuliX IDE captures
 * 3. Analyzes differences in quality, compression, and color accuracy
 * 4. Finds the most similar pair of capture methods
 * 5. Recommends the best capture tool for pattern matching
 */
public class ScreenshotComparisonTest {
    
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    
    // Screenshot info holder
    static class ScreenshotInfo {
        String name;
        File file;
        BufferedImage image;
        long fileSize;
        int width;
        int height;
        int bitDepth;
        String format;
        Map<String, Object> metadata;
        double avgBrightness;
        double avgSaturation;
        int uniqueColors;
        double compressionRatio;
        
        ScreenshotInfo(String name, File file) {
            this.name = name;
            this.file = file;
            this.metadata = new HashMap<>();
        }
    }
    
    public static void main(String[] args) throws Exception {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SCREENSHOT COMPARISON TEST");
        System.out.println("Comparing Brobot vs Windows vs SikuliX IDE capture methods");
        System.out.println("Time: " + dateFormat.format(new Date()));
        System.out.println("=".repeat(80));
        System.out.println("\nNOTE: Screenshots at different resolutions will be scaled to match");
        System.out.println("      (1920x1080 physical vs 1536x864 logical will be normalized)");
        
        // 1. Create/verify screenshots directory
        File screenshotsDir = new File("screenshots");
        if (!screenshotsDir.exists()) {
            screenshotsDir.mkdirs();
            System.out.println("\n✓ Created screenshots directory");
        } else {
            System.out.println("\n✓ Using existing screenshots directory");
        }
        
        // 2. Take screenshots with all three Brobot methods
        System.out.println("\n1. CAPTURING WITH BROBOT METHODS:");
        System.out.println("-".repeat(70));
        
        // Capture with SikuliX
        File sikulixFile = new File(screenshotsDir, "sikulix.png");
        captureSikuliXScreenshot(sikulixFile);
        
        // Capture with Robot
        File robotFile = new File(screenshotsDir, "robot.png");
        captureRobotScreenshot(robotFile);
        
        // Capture with FFmpeg
        File ffmpegFile = new File(screenshotsDir, "ffmpeg.png");
        captureFFmpegScreenshot(ffmpegFile);
        
        // 3. Load all screenshots
        System.out.println("\n2. LOADING SCREENSHOTS:");
        System.out.println("-".repeat(70));
        
        List<ScreenshotInfo> screenshots = new ArrayList<>();
        
        // Load Brobot method screenshots
        if (sikulixFile.exists()) {
            screenshots.add(loadScreenshot("SikuliX", sikulixFile));
        } else {
            System.err.println("   ✗ SikuliX screenshot not found!");
        }
        
        if (robotFile.exists()) {
            screenshots.add(loadScreenshot("Robot", robotFile));
        } else {
            System.err.println("   ✗ Robot screenshot not found!");
        }
        
        if (ffmpegFile.exists()) {
            screenshots.add(loadScreenshot("FFmpeg", ffmpegFile));
        } else {
            System.err.println("   ✗ FFmpeg screenshot not found!");
        }
        
        // Load Windows screenshot
        File windowsFile = new File(screenshotsDir, "windows.png");
        if (windowsFile.exists()) {
            screenshots.add(loadScreenshot("Windows", windowsFile));
        } else {
            System.out.println("   ⚠ Windows screenshot not found - please add windows.png to screenshots folder");
        }
        
        // Load SikuliX IDE screenshot
        File sikulixIdeFile = new File(screenshotsDir, "sikulix-ide.png");
        if (sikulixIdeFile.exists()) {
            screenshots.add(loadScreenshot("SikuliX IDE", sikulixIdeFile));
        } else {
            System.out.println("   ⚠ SikuliX IDE screenshot not found - please add sikulix-ide.png to screenshots folder");
        }
        
        if (screenshots.size() < 2) {
            System.err.println("\n✗ Need at least 2 screenshots to compare!");
            System.out.println("Please add windows.png and/or sikulix-ide.png to the screenshots folder");
            return;
        }
        
        // 4. Analyze each screenshot
        System.out.println("\n3. ANALYZING SCREENSHOTS:");
        System.out.println("=" + "=".repeat(79));
        
        for (ScreenshotInfo info : screenshots) {
            analyzeScreenshot(info);
            printScreenshotInfo(info);
        }
        
        // 5. Compare screenshots and find most similar pair
        System.out.println("\n4. COMPARING SCREENSHOTS:");
        System.out.println("=" + "=".repeat(79));
        
        double bestSimilarity = 0;
        ScreenshotInfo bestPair1 = null;
        ScreenshotInfo bestPair2 = null;
        
        // Store all similarities for later use
        class PairSimilarity {
            ScreenshotInfo first;
            ScreenshotInfo second;
            double similarity;
            
            PairSimilarity(ScreenshotInfo first, ScreenshotInfo second, double similarity) {
                this.first = first;
                this.second = second;
                this.similarity = similarity;
            }
        }
        
        List<PairSimilarity> allPairs = new ArrayList<>();
        
        if (screenshots.size() >= 2) {
            for (int i = 0; i < screenshots.size(); i++) {
                for (int j = i + 1; j < screenshots.size(); j++) {
                    double similarity = compareScreenshots(screenshots.get(i), screenshots.get(j));
                    allPairs.add(new PairSimilarity(screenshots.get(i), screenshots.get(j), similarity));
                    
                    if (similarity > bestSimilarity) {
                        bestSimilarity = similarity;
                        bestPair1 = screenshots.get(i);
                        bestPair2 = screenshots.get(j);
                    }
                }
            }
            
            // Find best pairing between external tools and Brobot methods
            System.out.println("\n🏆 BEST PAIRING FOR PATTERN MATCHING:");
            System.out.println("=" + "=".repeat(79));
            
            // Find the best external-Brobot pairing
            double bestExternalPairingSimilarity = 0;
            ScreenshotInfo bestExternalTool = null;
            ScreenshotInfo bestBrobotMethod = null;
            
            // Check all pairs for external-Brobot combinations
            for (PairSimilarity pair : allPairs) {
                boolean isFirstExternal = pair.first.name.equals("Windows") || pair.first.name.equals("SikuliX IDE");
                boolean isSecondExternal = pair.second.name.equals("Windows") || pair.second.name.equals("SikuliX IDE");
                boolean isFirstBrobot = pair.first.name.equals("SikuliX") || pair.first.name.equals("Robot") || pair.first.name.equals("FFmpeg");
                boolean isSecondBrobot = pair.second.name.equals("SikuliX") || pair.second.name.equals("Robot") || pair.second.name.equals("FFmpeg");
                
                // Check if this is an external-Brobot pairing
                if ((isFirstExternal && isSecondBrobot) || (isFirstBrobot && isSecondExternal)) {
                    if (pair.similarity > bestExternalPairingSimilarity) {
                        bestExternalPairingSimilarity = pair.similarity;
                        
                        if (isFirstExternal) {
                            bestExternalTool = pair.first;
                            bestBrobotMethod = pair.second;
                        } else {
                            bestExternalTool = pair.second;
                            bestBrobotMethod = pair.first;
                        }
                    }
                }
            }
            
            if (bestExternalTool != null && bestBrobotMethod != null) {
                System.out.println("\n   🎯 OPTIMAL PAIRING:");
                System.out.println("   Pattern Capture Tool: " + bestExternalTool.name);
                System.out.println("   Brobot Method: " + bestBrobotMethod.name);
                System.out.println("   Similarity: " + String.format("%.1f%%", bestExternalPairingSimilarity * 100));
                
                System.out.println("\n   📋 RECOMMENDATION:");
                System.out.println("   1. Capture patterns using: " + bestExternalTool.name);
                if (bestExternalTool.name.equals("Windows")) {
                    System.out.println("      (Windows Snipping Tool or Print Screen)");
                } else {
                    System.out.println("      (SikuliX IDE screenshot tool)");
                }
                
                System.out.println("\n   2. Configure Brobot to use: " + bestBrobotMethod.name);
                if (bestBrobotMethod.name.equals("SikuliX")) {
                    System.out.println("      (Default - uses Screen.capture())");
                } else if (bestBrobotMethod.name.equals("Robot")) {
                    System.out.println("      (Java Robot - Robot.createScreenCapture())");
                } else if (bestBrobotMethod.name.equals("FFmpeg")) {
                    System.out.println("      (JavaCV FFmpeg - physical resolution capture)");
                }
                
                System.out.println("\n   ✅ This pairing will give you the best pattern matching results!");
                
                // Additional info about resolution handling
                if (bestExternalTool.width != bestBrobotMethod.width) {
                    System.out.println("\n   ⚠️ Note: Resolution difference detected");
                    System.out.println("      " + bestExternalTool.name + ": " + bestExternalTool.width + "x" + bestExternalTool.height);
                    System.out.println("      " + bestBrobotMethod.name + ": " + bestBrobotMethod.width + "x" + bestBrobotMethod.height);
                    System.out.println("      Pattern scaling may be needed (use Settings.AlwaysResize)");
                }
            }
            
            // Still show the overall most similar pair for reference
            System.out.println("\n\n📊 OVERALL MOST SIMILAR PAIR (for reference):");
            System.out.println("-".repeat(70));
            if (bestPair1 != null && bestPair2 != null) {
                System.out.println("   " + bestPair1.name + " and " + bestPair2.name);
                System.out.println("   Similarity: " + String.format("%.1f%%", bestSimilarity * 100));
            }
        }
        
        // 6. Pattern matching simulation
        System.out.println("\n5. PATTERN MATCHING SIMULATION:");
        System.out.println("=" + "=".repeat(79));
        
        if (screenshots.size() >= 2) {
            simulatePatternMatching(screenshots);
        }
        
        // 7. Recommendations
        System.out.println("\n6. RECOMMENDATIONS:");
        System.out.println("=" + "=".repeat(79));
        
        provideRecommendations(screenshots);
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TEST COMPLETE");
        System.out.println("=".repeat(80));
    }
    
    /**
     * Captures screenshot using SikuliX Screen class
     */
    private static void captureSikuliXScreenshot(File outputFile) {
        try {
            // Use SikuliX Screen class (Brobot's default)
            Screen screen = new Screen();
            
            System.out.println("   Capturing with SikuliX Screen...");
            
            // Capture full screen
            ScreenImage screenImage = screen.capture();
            BufferedImage image = screenImage.getImage();
            
            System.out.println("   Captured: " + image.getWidth() + "x" + image.getHeight());
            
            // Save to file
            ImageIO.write(image, "png", outputFile);
            
            System.out.println("   ✓ Saved as: " + outputFile.getName());
            System.out.println("   File size: " + (outputFile.length() / 1024) + " KB");
            
        } catch (Exception e) {
            System.err.println("   ✗ Failed to capture with SikuliX: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Captures screenshot using Java Robot
     */
    private static void captureRobotScreenshot(File outputFile) {
        try {
            System.out.println("\n   Capturing with Java Robot...");
            
            // Use Java Robot
            Robot robot = new Robot();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Rectangle screenRect = new Rectangle(screenSize);
            
            BufferedImage image = robot.createScreenCapture(screenRect);
            
            System.out.println("   Captured: " + image.getWidth() + "x" + image.getHeight());
            
            // Save to file
            ImageIO.write(image, "png", outputFile);
            
            System.out.println("   ✓ Saved as: " + outputFile.getName());
            System.out.println("   File size: " + (outputFile.length() / 1024) + " KB");
            
        } catch (Exception e) {
            System.err.println("   ✗ Failed to capture with Robot: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Captures screenshot using FFmpeg via JavaCV
     */
    private static void captureFFmpegScreenshot(File outputFile) {
        try {
            System.out.println("\n   Capturing with FFmpeg (JavaCV)...");
            
            // Use JavaCV FFmpeg capture
            BufferedImage image = JavaCVFFmpegCapture.capture();
            
            System.out.println("   Captured: " + image.getWidth() + "x" + image.getHeight());
            
            // Save to file
            ImageIO.write(image, "png", outputFile);
            
            System.out.println("   ✓ Saved as: " + outputFile.getName());
            System.out.println("   File size: " + (outputFile.length() / 1024) + " KB");
            
        } catch (Exception e) {
            System.err.println("   ✗ Failed to capture with FFmpeg: " + e.getMessage());
            System.err.println("   Note: FFmpeg capture may not work in WSL/headless environments");
        }
    }
    
    /**
     * Loads a screenshot and extracts basic info
     */
    private static ScreenshotInfo loadScreenshot(String name, File file) throws IOException {
        System.out.println("   Loading " + name + "...");
        
        ScreenshotInfo info = new ScreenshotInfo(name, file);
        info.image = ImageIO.read(file);
        info.fileSize = file.length();
        info.width = info.image.getWidth();
        info.height = info.image.getHeight();
        
        // Get format and metadata
        try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                reader.setInput(iis);
                info.format = reader.getFormatName();
                
                // Try to get bit depth
                try {
                    info.bitDepth = info.image.getColorModel().getPixelSize();
                } catch (Exception e) {
                    info.bitDepth = 24; // Default
                }
            }
        }
        
        System.out.println("   ✓ Loaded: " + info.width + "x" + info.height + ", " + 
                         (info.fileSize / 1024) + " KB");
        
        return info;
    }
    
    /**
     * Analyzes a screenshot for quality metrics
     */
    private static void analyzeScreenshot(ScreenshotInfo info) {
        BufferedImage img = info.image;
        
        // Calculate average brightness and saturation
        long totalBrightness = 0;
        long totalSaturation = 0;
        Set<Integer> uniqueColors = new HashSet<>();
        
        int sampleRate = 10; // Sample every 10th pixel for performance
        int samples = 0;
        
        for (int x = 0; x < img.getWidth(); x += sampleRate) {
            for (int y = 0; y < img.getHeight(); y += sampleRate) {
                int rgb = img.getRGB(x, y);
                uniqueColors.add(rgb);
                
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                // Brightness
                totalBrightness += (r + g + b) / 3;
                
                // Saturation (simplified)
                int max = Math.max(r, Math.max(g, b));
                int min = Math.min(r, Math.min(g, b));
                if (max > 0) {
                    totalSaturation += ((max - min) * 100) / max;
                }
                
                samples++;
            }
        }
        
        info.avgBrightness = (double) totalBrightness / samples;
        info.avgSaturation = (double) totalSaturation / samples;
        info.uniqueColors = uniqueColors.size();
        
        // Calculate compression ratio (theoretical uncompressed size vs actual)
        long uncompressedSize = (long) info.width * info.height * (info.bitDepth / 8);
        info.compressionRatio = (double) uncompressedSize / info.fileSize;
    }
    
    /**
     * Prints detailed info about a screenshot
     */
    private static void printScreenshotInfo(ScreenshotInfo info) {
        System.out.println("\n📸 " + info.name.toUpperCase() + ":");
        System.out.println("-".repeat(70));
        System.out.println("   Resolution:        " + info.width + "x" + info.height);
        System.out.println("   File size:         " + (info.fileSize / 1024) + " KB");
        System.out.println("   Bit depth:         " + info.bitDepth + " bits");
        System.out.println("   Format:            " + info.format);
        System.out.println("   Compression ratio: " + String.format("%.1fx", info.compressionRatio));
        System.out.println("   Avg brightness:    " + String.format("%.1f", info.avgBrightness) + "/255");
        System.out.println("   Avg saturation:    " + String.format("%.1f%%", info.avgSaturation));
        System.out.println("   Unique colors:     " + info.uniqueColors + " (sampled)");
    }
    
    /**
     * Compares two screenshots and returns similarity score
     */
    private static double compareScreenshots(ScreenshotInfo info1, ScreenshotInfo info2) {
        System.out.println("\n🔍 COMPARING: " + info1.name + " vs " + info2.name);
        System.out.println("-".repeat(70));
        
        // Handle different resolutions by scaling
        BufferedImage img1 = info1.image;
        BufferedImage img2 = info2.image;
        
        if (info1.width != info2.width || info1.height != info2.height) {
            System.out.println("   ⚠ Different resolutions - normalizing for comparison");
            System.out.println("     " + info1.name + ": " + info1.width + "x" + info1.height);
            System.out.println("     " + info2.name + ": " + info2.width + "x" + info2.height);
            
            // Scale to the smaller resolution for fair comparison
            int targetWidth = Math.min(info1.width, info2.width);
            int targetHeight = Math.min(info1.height, info2.height);
            
            // Scale images if needed
            if (info1.width > targetWidth || info1.height > targetHeight) {
                img1 = scaleImage(info1.image, targetWidth, targetHeight);
                System.out.println("     Scaled " + info1.name + " to " + targetWidth + "x" + targetHeight);
            }
            if (info2.width > targetWidth || info2.height > targetHeight) {
                img2 = scaleImage(info2.image, targetWidth, targetHeight);
                System.out.println("     Scaled " + info2.name + " to " + targetWidth + "x" + targetHeight);
            }
        }
        
        // File size difference
        long sizeDiff = Math.abs(info1.fileSize - info2.fileSize);
        double sizeDiffPercent = (sizeDiff * 100.0) / Math.max(info1.fileSize, info2.fileSize);
        System.out.println("   File size difference: " + (sizeDiff / 1024) + " KB (" + 
                         String.format("%.1f%%", sizeDiffPercent) + ")");
        
        // Color accuracy difference
        System.out.println("   Brightness difference: " + 
                         String.format("%.1f", Math.abs(info1.avgBrightness - info2.avgBrightness)));
        System.out.println("   Saturation difference: " + 
                         String.format("%.1f%%", Math.abs(info1.avgSaturation - info2.avgSaturation)));
        System.out.println("   Unique colors diff:    " + 
                         Math.abs(info1.uniqueColors - info2.uniqueColors));
        
        // Pixel-level comparison using normalized images
        double similarity = comparePixelsNormalized(img1, img2, info1.name, info2.name);
        
        return similarity;
    }
    
    /**
     * Scales an image to target dimensions
     */
    private static BufferedImage scaleImage(BufferedImage source, int targetWidth, int targetHeight) {
        BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaled.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        return scaled;
    }
    
    /**
     * Pixel-level comparison with normalized images - returns similarity score (0-1)
     */
    private static double comparePixelsNormalized(BufferedImage img1, BufferedImage img2, 
                                                  String name1, String name2) {
        // Images should already be the same size from scaling
        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
            System.out.println("   ERROR: Images still different sizes after normalization!");
            return 0;
        }
        
        long totalDiff = 0;
        int maxDiff = 0;
        int identicalPixels = 0;
        int samples = 0;
        
        // Sample comparison
        int sampleRate = 100;
        for (int x = 0; x < img1.getWidth(); x += sampleRate) {
            for (int y = 0; y < img1.getHeight(); y += sampleRate) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);
                
                if (rgb1 == rgb2) {
                    identicalPixels++;
                }
                
                int r1 = (rgb1 >> 16) & 0xFF;
                int g1 = (rgb1 >> 8) & 0xFF;
                int b1 = rgb1 & 0xFF;
                
                int r2 = (rgb2 >> 16) & 0xFF;
                int g2 = (rgb2 >> 8) & 0xFF;
                int b2 = rgb2 & 0xFF;
                
                int diff = Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
                totalDiff += diff;
                maxDiff = Math.max(maxDiff, diff);
                samples++;
            }
        }
        
        double avgDiff = (double) totalDiff / samples;
        double identicalPercent = (identicalPixels * 100.0) / samples;
        
        System.out.println("   Pixel comparison (after normalization):");
        System.out.println("     Comparing at: " + img1.getWidth() + "x" + img1.getHeight());
        System.out.println("     Identical pixels:  " + String.format("%.1f%%", identicalPercent));
        System.out.println("     Avg pixel diff:    " + String.format("%.1f", avgDiff) + "/765");
        System.out.println("     Max pixel diff:    " + maxDiff + "/765");
        
        // Judge similarity
        if (identicalPercent > 99) {
            System.out.println("     ✅ Nearly identical");
        } else if (identicalPercent > 95) {
            System.out.println("     ✓ Very similar");
        } else if (identicalPercent > 90) {
            System.out.println("     ⚠ Similar with differences");
        } else {
            System.out.println("     ✗ Significantly different");
        }
        
        // Return similarity as a value between 0 and 1
        return identicalPercent / 100.0;
    }
    
    /**
     * Pixel-level comparison - returns similarity score (0-1)
     * (Original method kept for backwards compatibility)
     */
    private static double comparePixels(ScreenshotInfo info1, ScreenshotInfo info2) {
        BufferedImage img1 = info1.image;
        BufferedImage img2 = info2.image;
        
        long totalDiff = 0;
        int maxDiff = 0;
        int identicalPixels = 0;
        int samples = 0;
        
        // Sample comparison
        int sampleRate = 100;
        for (int x = 0; x < img1.getWidth(); x += sampleRate) {
            for (int y = 0; y < img1.getHeight(); y += sampleRate) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);
                
                if (rgb1 == rgb2) {
                    identicalPixels++;
                }
                
                int r1 = (rgb1 >> 16) & 0xFF;
                int g1 = (rgb1 >> 8) & 0xFF;
                int b1 = rgb1 & 0xFF;
                
                int r2 = (rgb2 >> 16) & 0xFF;
                int g2 = (rgb2 >> 8) & 0xFF;
                int b2 = rgb2 & 0xFF;
                
                int diff = Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
                totalDiff += diff;
                maxDiff = Math.max(maxDiff, diff);
                samples++;
            }
        }
        
        double avgDiff = (double) totalDiff / samples;
        double identicalPercent = (identicalPixels * 100.0) / samples;
        
        System.out.println("   Pixel comparison (sampled):");
        System.out.println("     Identical pixels:  " + String.format("%.1f%%", identicalPercent));
        System.out.println("     Avg pixel diff:    " + String.format("%.1f", avgDiff) + "/765");
        System.out.println("     Max pixel diff:    " + maxDiff + "/765");
        
        // Judge similarity
        if (identicalPercent > 99) {
            System.out.println("     ✅ Nearly identical");
        } else if (identicalPercent > 95) {
            System.out.println("     ✓ Very similar");
        } else if (identicalPercent > 90) {
            System.out.println("     ⚠ Similar with differences");
        } else {
            System.out.println("     ✗ Significantly different");
        }
        
        // Return similarity as a value between 0 and 1
        return identicalPercent / 100.0;
    }
    
    /**
     * Simulates pattern matching between screenshots
     */
    private static void simulatePatternMatching(List<ScreenshotInfo> screenshots) {
        System.out.println("\nSimulating pattern extraction and matching...\n");
        
        // Extract a small region from first screenshot as "pattern"
        ScreenshotInfo source = screenshots.get(0);
        int patternX = 100;
        int patternY = 100;
        int patternW = 200;
        int patternH = 100;
        
        if (source.width < patternX + patternW || source.height < patternY + patternH) {
            System.out.println("   Screenshots too small for pattern simulation");
            return;
        }
        
        BufferedImage pattern = source.image.getSubimage(patternX, patternY, patternW, patternH);
        System.out.println("   Extracted pattern from " + source.name + ": " + patternW + "x" + patternH);
        System.out.println("   Pattern location: (" + patternX + ", " + patternY + ")");
        
        // Try to match on each screenshot
        System.out.println("\n   Matching results:");
        for (ScreenshotInfo target : screenshots) {
            double similarity = calculateSimilarity(pattern, target.image, patternX, patternY);
            System.out.println("     " + target.name + ": " + 
                             String.format("%.1f%%", similarity * 100) + " similarity");
            
            if (target == source) {
                System.out.println("       (Expected 100% - same source)");
            } else if (similarity > 0.95) {
                System.out.println("       ✅ Excellent match - captures are compatible");
            } else if (similarity > 0.90) {
                System.out.println("       ✓ Good match - minor differences");
            } else if (similarity > 0.80) {
                System.out.println("       ⚠ Fair match - may cause issues");
            } else {
                System.out.println("       ✗ Poor match - incompatible capture methods");
            }
        }
    }
    
    /**
     * Calculates similarity between pattern and region
     */
    private static double calculateSimilarity(BufferedImage pattern, BufferedImage target, int x, int y) {
        if (x + pattern.getWidth() > target.getWidth() || 
            y + pattern.getHeight() > target.getHeight()) {
            return 0;
        }
        
        long totalDiff = 0;
        int pixels = pattern.getWidth() * pattern.getHeight();
        
        for (int px = 0; px < pattern.getWidth(); px++) {
            for (int py = 0; py < pattern.getHeight(); py++) {
                int patternRGB = pattern.getRGB(px, py);
                int targetRGB = target.getRGB(x + px, y + py);
                
                int pr = (patternRGB >> 16) & 0xFF;
                int pg = (patternRGB >> 8) & 0xFF;
                int pb = patternRGB & 0xFF;
                
                int tr = (targetRGB >> 16) & 0xFF;
                int tg = (targetRGB >> 8) & 0xFF;
                int tb = targetRGB & 0xFF;
                
                totalDiff += Math.abs(pr - tr) + Math.abs(pg - tg) + Math.abs(pb - tb);
            }
        }
        
        // Calculate similarity (0 = completely different, 1 = identical)
        double avgDiff = (double) totalDiff / pixels;
        double maxPossibleDiff = 765.0; // 255 * 3
        return 1.0 - (avgDiff / maxPossibleDiff);
    }
    
    /**
     * Provides recommendations based on analysis
     */
    private static void provideRecommendations(List<ScreenshotInfo> screenshots) {
        System.out.println("\nBased on the analysis:\n");
        
        ScreenshotInfo best = null;
        double bestScore = 0;
        
        for (ScreenshotInfo info : screenshots) {
            double score = 0;
            
            // Score based on file size (smaller is better for storage)
            score += 50.0 * (1.0 - (double)info.fileSize / (2 * 1024 * 1024)); // Normalize to 2MB
            
            // Score based on unique colors (more is better for accuracy)
            score += 30.0 * Math.min(1.0, info.uniqueColors / 10000.0);
            
            // Score based on compression ratio (moderate is better)
            double idealCompression = 10.0;
            score += 20.0 * (1.0 - Math.abs(info.compressionRatio - idealCompression) / idealCompression);
            
            if (score > bestScore) {
                bestScore = score;
                best = info;
            }
            
            System.out.println("   " + info.name + " score: " + String.format("%.1f", score));
        }
        
        if (best != null) {
            System.out.println("\n   🏆 RECOMMENDED: " + best.name);
            System.out.println("   Reasons:");
            System.out.println("   - Good balance of file size and quality");
            System.out.println("   - Sufficient color accuracy");
            System.out.println("   - Reasonable compression");
        }
        
        // Pattern matching compatibility
        System.out.println("\n   PATTERN MATCHING COMPATIBILITY:");
        boolean allSimilar = true;
        for (int i = 0; i < screenshots.size(); i++) {
            for (int j = i + 1; j < screenshots.size(); j++) {
                double brightDiff = Math.abs(screenshots.get(i).avgBrightness - screenshots.get(j).avgBrightness);
                if (brightDiff > 5) {
                    allSimilar = false;
                    System.out.println("   ⚠ " + screenshots.get(i).name + " and " + 
                                     screenshots.get(j).name + " have different brightness");
                }
            }
        }
        
        if (allSimilar) {
            System.out.println("   ✅ All capture methods are compatible for pattern matching");
        } else {
            System.out.println("   ⚠ Use consistent capture method for patterns and targets");
        }
    }
}