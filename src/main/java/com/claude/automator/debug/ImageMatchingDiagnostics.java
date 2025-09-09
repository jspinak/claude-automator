package com.claude.automator.debug;

import io.github.jspinak.brobot.util.image.ImageNormalizer;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.util.image.core.BufferedImageUtilities;
import lombok.extern.slf4j.Slf4j;
import org.sikuli.script.Finder;
import org.sikuli.script.Match;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Diagnostic tool for troubleshooting image matching issues.
 * Helps identify bit depth mismatches, scaling issues, and other problems
 * that prevent pattern matching from working correctly.
 */
@Slf4j
@Component
public class ImageMatchingDiagnostics {
    
    private final BufferedImageUtilities bufferedImageUtilities = new BufferedImageUtilities();
    
    private static final DateTimeFormatter TIMESTAMP_FORMAT = 
        DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    
    /**
     * Runs comprehensive diagnostics on a pattern matching attempt.
     * 
     * @param pattern The pattern being searched for
     * @param scene The scene being searched in
     * @return A diagnostic report
     */
    public DiagnosticReport runDiagnostics(Pattern pattern, Scene scene) {
        DiagnosticReport report = new DiagnosticReport();
        report.setTimestamp(LocalDateTime.now());
        report.setPatternName(pattern.getName());
        
        BufferedImage patternImage = pattern.getBImage();
        BufferedImage sceneImage = scene.getPattern().getBImage();
        
        // Check for null images
        if (patternImage == null) {
            report.addIssue("Pattern image is null");
            return report;
        }
        if (sceneImage == null) {
            report.addIssue("Scene image is null");
            return report;
        }
        
        // Diagnose bit depth
        diagnoseBitDepth(patternImage, sceneImage, report);
        
        // Check format compatibility
        boolean compatible = ImageNormalizer.areFormatsCompatible(patternImage, sceneImage);
        report.setFormatsCompatible(compatible);
        if (!compatible) {
            report.addIssue("Image formats are incompatible for matching");
        }
        
        // Try matching with original images
        double originalScore = tryMatch(patternImage, sceneImage);
        report.setOriginalMatchScore(originalScore);
        
        // Try matching with normalized images
        BufferedImage normalizedPattern = ImageNormalizer.normalizeToRGB(patternImage);
        BufferedImage normalizedScene = ImageNormalizer.normalizeToRGB(sceneImage);
        double normalizedScore = tryMatch(normalizedPattern, normalizedScene);
        report.setNormalizedMatchScore(normalizedScore);
        
        // Compare scores
        if (normalizedScore > originalScore + 0.1) {
            report.addIssue("Normalization significantly improves matching (+" + 
                String.format("%.2f", normalizedScore - originalScore) + ")");
            report.addSuggestion("Use ImageNormalizer to normalize images before matching");
        }
        
        // Check for size issues
        if (patternImage.getWidth() > sceneImage.getWidth() || 
            patternImage.getHeight() > sceneImage.getHeight()) {
            report.addIssue("Pattern is larger than scene");
        }
        
        // Generate suggestions
        generateSuggestions(report);
        
        return report;
    }
    
    /**
     * Diagnoses bit depth issues between pattern and scene.
     */
    private void diagnoseBitDepth(BufferedImage pattern, BufferedImage scene, 
                                  DiagnosticReport report) {
        int patternBits = pattern.getColorModel().getPixelSize();
        int sceneBits = scene.getColorModel().getPixelSize();
        
        report.setPatternBitDepth(patternBits);
        report.setSceneBitDepth(sceneBits);
        
        if (patternBits != sceneBits) {
            report.addIssue(String.format("Bit depth mismatch: Pattern=%d-bit, Scene=%d-bit",
                patternBits, sceneBits));
            
            if (patternBits == 24 && sceneBits == 32) {
                report.addIssue("Pattern lacks alpha channel while scene has one");
            } else if (patternBits == 32 && sceneBits == 24) {
                report.addIssue("Pattern has alpha channel while scene lacks one");
            }
        }
    }
    
    /**
     * Attempts to match a pattern in a scene and returns the best score.
     */
    private double tryMatch(BufferedImage pattern, BufferedImage scene) {
        try {
            // Save original threshold
            double originalSimilarity = org.sikuli.basics.Settings.MinSimilarity;
            
            try {
                // Set very low threshold to find any match
                org.sikuli.basics.Settings.MinSimilarity = 0.01;
                
                Finder finder = new Finder(scene);
                org.sikuli.script.Pattern sikuliPattern = 
                    new org.sikuli.script.Pattern(pattern).similar(0.01);
                finder.findAll(sikuliPattern);
                
                double bestScore = 0;
                while (finder.hasNext()) {
                    Match match = finder.next();
                    if (match.getScore() > bestScore) {
                        bestScore = match.getScore();
                    }
                }
                
                finder.destroy();
                return bestScore;
                
            } finally {
                // Restore original threshold
                org.sikuli.basics.Settings.MinSimilarity = originalSimilarity;
            }
        } catch (Exception e) {
            log.error("Error during match attempt: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * Generates suggestions based on diagnostic findings.
     */
    private void generateSuggestions(DiagnosticReport report) {
        if (report.getPatternBitDepth() != report.getSceneBitDepth()) {
            report.addSuggestion("Normalize both images to the same bit depth using ImageNormalizer");
        }
        
        if (report.getOriginalMatchScore() < 0.5 && report.getNormalizedMatchScore() < 0.5) {
            report.addSuggestion("Pattern may not exist in the scene or may be significantly different");
            report.addSuggestion("Check if the UI has changed or if scaling/DPI settings are different");
        }
        
        if (!report.isFormatsCompatible()) {
            report.addSuggestion("Convert both images to RGB format for consistent matching");
        }
    }
    
    /**
     * Saves diagnostic images for manual inspection.
     */
    public void saveDiagnosticImages(Pattern pattern, Scene scene, String outputDir) {
        try {
            Path dir = Paths.get(outputDir);
            Files.createDirectories(dir);
            
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String safeName = pattern.getName().replaceAll("[^a-zA-Z0-9.-]", "_");
            
            BufferedImage patternImg = pattern.getBImage();
            BufferedImage sceneImg = scene.getPattern().getBImage();
            
            // Save original images
            if (patternImg != null) {
                File patternFile = dir.resolve(timestamp + "_" + safeName + "_pattern_original.png").toFile();
                ImageIO.write(patternImg, "png", patternFile);
                log.info("Saved original pattern to: {}", patternFile.getPath());
                
                // Save normalized version
                BufferedImage normalized = ImageNormalizer.normalizeToRGB(patternImg);
                File normalizedFile = dir.resolve(timestamp + "_" + safeName + "_pattern_normalized.png").toFile();
                ImageIO.write(normalized, "png", normalizedFile);
                log.info("Saved normalized pattern to: {}", normalizedFile.getPath());
            }
            
            // Save scene region where pattern should be
            if (sceneImg != null) {
                // Save a cropped region for easier inspection
                int cropSize = 200; // pixels around expected location
                File sceneFile = dir.resolve(timestamp + "_" + safeName + "_scene.png").toFile();
                ImageIO.write(sceneImg, "png", sceneFile);
                log.info("Saved scene to: {}", sceneFile.getPath());
            }
            
        } catch (IOException e) {
            log.error("Failed to save diagnostic images: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Diagnostic report containing findings and suggestions.
     */
    public static class DiagnosticReport {
        private LocalDateTime timestamp;
        private String patternName;
        private int patternBitDepth;
        private int sceneBitDepth;
        private boolean formatsCompatible;
        private double originalMatchScore;
        private double normalizedMatchScore;
        private List<String> issues = new ArrayList<>();
        private List<String> suggestions = new ArrayList<>();
        
        // Getters and setters
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public String getPatternName() { return patternName; }
        public void setPatternName(String patternName) { this.patternName = patternName; }
        
        public int getPatternBitDepth() { return patternBitDepth; }
        public void setPatternBitDepth(int depth) { this.patternBitDepth = depth; }
        
        public int getSceneBitDepth() { return sceneBitDepth; }
        public void setSceneBitDepth(int depth) { this.sceneBitDepth = depth; }
        
        public boolean isFormatsCompatible() { return formatsCompatible; }
        public void setFormatsCompatible(boolean compatible) { this.formatsCompatible = compatible; }
        
        public double getOriginalMatchScore() { return originalMatchScore; }
        public void setOriginalMatchScore(double score) { this.originalMatchScore = score; }
        
        public double getNormalizedMatchScore() { return normalizedMatchScore; }
        public void setNormalizedMatchScore(double score) { this.normalizedMatchScore = score; }
        
        public List<String> getIssues() { return issues; }
        public void addIssue(String issue) { this.issues.add(issue); }
        
        public List<String> getSuggestions() { return suggestions; }
        public void addSuggestion(String suggestion) { this.suggestions.add(suggestion); }
        
        /**
         * Prints the report to console.
         */
        public void print() {
            System.out.println("\n=== Image Matching Diagnostic Report ===");
            System.out.println("Timestamp: " + timestamp);
            System.out.println("Pattern: " + patternName);
            System.out.println("\nBit Depth:");
            System.out.println("  Pattern: " + patternBitDepth + "-bit");
            System.out.println("  Scene: " + sceneBitDepth + "-bit");
            System.out.println("\nMatch Scores:");
            System.out.println("  Original: " + String.format("%.3f", originalMatchScore));
            System.out.println("  Normalized: " + String.format("%.3f", normalizedMatchScore));
            System.out.println("  Formats Compatible: " + formatsCompatible);
            
            if (!issues.isEmpty()) {
                System.out.println("\nIssues Found:");
                for (String issue : issues) {
                    System.out.println("  ⚠ " + issue);
                }
            }
            
            if (!suggestions.isEmpty()) {
                System.out.println("\nSuggestions:");
                for (String suggestion : suggestions) {
                    System.out.println("  → " + suggestion);
                }
            }
            System.out.println("=====================================\n");
        }
    }
}