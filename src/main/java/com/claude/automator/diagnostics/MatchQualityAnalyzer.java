package com.claude.automator.diagnostics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.match.Match;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Analyzes pattern matching quality to diagnose why matches are failing.
 * This component helps identify issues with:
 * - Similarity thresholds
 * - Image quality
 * - Resolution mismatches
 * - False positives
 */
@Slf4j
@Component
public class MatchQualityAnalyzer {
    
    // Thresholds for analysis
    private static final double GOOD_MATCH_THRESHOLD = 0.8;
    private static final double ACCEPTABLE_MATCH_THRESHOLD = 0.7;
    private static final double SUSPICIOUS_MATCH_THRESHOLD = 0.5;
    private static final int MAX_EXPECTED_MATCHES = 5;
    
    @PostConstruct
    public void init() {
        log.info("=== MATCH QUALITY ANALYZER INITIALIZED ===");
        log.info("Good match threshold: {}", GOOD_MATCH_THRESHOLD);
        log.info("Acceptable match threshold: {}", ACCEPTABLE_MATCH_THRESHOLD);
        log.info("Suspicious match threshold: {}", SUSPICIOUS_MATCH_THRESHOLD);
    }
    
    /**
     * Analyzes the quality of pattern matches and provides diagnostic information.
     */
    public void analyzeMatches(String patternName, ActionResult result) {
        if (result == null || result.getMatchList() == null) {
            log.warn("[MATCH ANALYSIS] No results to analyze for pattern '{}'", patternName);
            return;
        }
        
        List<Match> matches = result.getMatchList();
        
        log.info("=== MATCH QUALITY ANALYSIS for '{}' ===", patternName);
        log.info("Total matches found: {}", matches.size());
        
        if (matches.isEmpty()) {
            log.warn("NO MATCHES FOUND - Possible causes:");
            log.warn("  1. Pattern image doesn't exist on screen");
            log.warn("  2. Similarity threshold too high");
            log.warn("  3. Screen capture is black/corrupted");
            log.warn("  4. Wrong screen/window being captured");
            log.warn("  5. Pattern captured at different resolution/DPI");
            return;
        }
        
        // Analyze match scores
        double bestScore = matches.stream()
            .mapToDouble(Match::getScore)
            .max()
            .orElse(0.0);
            
        double averageScore = matches.stream()
            .mapToDouble(Match::getScore)
            .average()
            .orElse(0.0);
            
        double worstScore = matches.stream()
            .mapToDouble(Match::getScore)
            .min()
            .orElse(0.0);
        
        log.info("Score Statistics:");
        log.info("  Best score: {}", String.format("%.3f", bestScore));
        log.info("  Average score: {}", String.format("%.3f", averageScore));
        log.info("  Worst score: {}", String.format("%.3f", worstScore));
        
        // Quality assessment
        if (bestScore >= GOOD_MATCH_THRESHOLD) {
            log.info("✓ GOOD MATCH QUALITY - Pattern matching is working well");
        } else if (bestScore >= ACCEPTABLE_MATCH_THRESHOLD) {
            log.info("⚠ ACCEPTABLE MATCH QUALITY - Consider adjusting similarity threshold");
        } else if (bestScore >= SUSPICIOUS_MATCH_THRESHOLD) {
            log.warn("⚠ SUSPICIOUS MATCH QUALITY - Likely issues:");
            log.warn("  - Different resolution/scaling between pattern and screen");
            log.warn("  - Color depth or format mismatch");
            log.warn("  - UI has changed since pattern was captured");
            log.warn("  - Anti-aliasing or rendering differences");
        } else {
            log.error("✗ POOR MATCH QUALITY - Major issues detected:");
            log.error("  Best match only {}% similar", String.format("%.1f", bestScore * 100));
            log.error("  Likely causes:");
            log.error("    1. Screen capture returning black/corrupted images");
            log.error("    2. Wrong pattern images being used");
            log.error("    3. Completely different UI than expected");
            log.error("    4. Major resolution/DPI mismatch");
        }
        
        // Check for too many matches (false positives)
        if (matches.size() > MAX_EXPECTED_MATCHES) {
            log.warn("⚠ TOO MANY MATCHES ({}) - Indicates false positives", matches.size());
            log.warn("  Similarity threshold is too low (current: 0.1)");
            log.warn("  Recommend increasing to at least 0.7");
            
            // Show distribution of scores
            long goodMatches = matches.stream()
                .filter(m -> m.getScore() >= ACCEPTABLE_MATCH_THRESHOLD)
                .count();
            long suspiciousMatches = matches.stream()
                .filter(m -> m.getScore() < SUSPICIOUS_MATCH_THRESHOLD)
                .count();
                
            log.info("Match Distribution:");
            log.info("  Good matches (>{}): {}", ACCEPTABLE_MATCH_THRESHOLD, goodMatches);
            log.info("  Suspicious matches (<{}): {}", SUSPICIOUS_MATCH_THRESHOLD, suspiciousMatches);
        }
        
        // Show top 5 matches with locations
        log.info("Top {} matches:", Math.min(5, matches.size()));
        matches.stream()
            .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
            .limit(5)
            .forEach(match -> {
                log.info("  Score {} at ({}, {})", 
                    String.format("%.3f", match.getScore()), 
                    match.x(), 
                    match.y());
            });
        
        // Recommendations
        log.info("=== RECOMMENDATIONS ===");
        if (bestScore < SUSPICIOUS_MATCH_THRESHOLD) {
            log.info("1. Run on Windows directly (not WSL2) to ensure proper screen capture");
            log.info("2. Verify pattern images match current UI");
            log.info("3. Check screen resolution and DPI settings");
            log.info("4. Recapture pattern images if UI has changed");
        } else if (matches.size() > MAX_EXPECTED_MATCHES) {
            log.info("1. Increase similarity threshold from 0.1 to 0.7-0.8");
            log.info("2. Use more distinctive patterns");
            log.info("3. Define smaller, more specific search regions");
        }
    }
    
    /**
     * Checks if the match quality indicates WSL2 black screen issue
     */
    public boolean isProbablyBlackScreen(ActionResult result) {
        if (result == null || result.getMatchList() == null || result.getMatchList().isEmpty()) {
            return false;
        }
        
        // If best match is below 50% and we have many matches, it's likely a black screen
        double bestScore = result.getMatchList().stream()
            .mapToDouble(Match::getScore)
            .max()
            .orElse(0.0);
            
        return bestScore < 0.5 && result.getMatchList().size() > 20;
    }
}