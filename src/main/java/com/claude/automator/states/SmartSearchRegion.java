package com.claude.automator.states;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Pattern;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Intelligent search region management that handles variable-sized patterns
 * within StateImages while preventing the issues with duplicate highlighting.
 * 
 * This addresses the critical constraint that patterns must be smaller than
 * their search regions in both dimensions for matching to work.
 */
@Slf4j
public class SmartSearchRegion {
    
    /**
     * Validation result for search region compatibility checks.
     */
    @Data
    public static class ValidationResult {
        private boolean valid = true;
        private List<String> issues = new ArrayList<>();
        
        public void addIssue(String issue) {
            this.valid = false;
            this.issues.add(issue);
        }
    }
    
    /**
     * Calculates the minimum search region that can accommodate all patterns
     * in a StateImage. This ensures all patterns can be found while minimizing
     * the search area for performance.
     * 
     * @param stateImage The StateImage containing patterns to accommodate
     * @param baseRegion The desired base region (will be expanded if needed)
     * @return A region large enough for all patterns, centered on the base region
     */
    public static Region calculateMinimumSharedRegion(StateImage stateImage, Region baseRegion) {
        int maxWidth = 0;
        int maxHeight = 0;
        
        // Find the largest dimensions needed across all patterns
        for (Pattern pattern : stateImage.getPatterns()) {
            if (pattern.getImage() != null) {
                maxWidth = Math.max(maxWidth, pattern.getImage().w());
                maxHeight = Math.max(maxHeight, pattern.getImage().h());
                log.debug("Pattern '{}' size: {}x{}", 
                         pattern.getName(), 
                         pattern.getImage().w(), 
                         pattern.getImage().h());
            }
        }
        
        // Add a small buffer to ensure patterns fit comfortably
        int buffer = 10;  // pixels
        maxWidth += buffer;
        maxHeight += buffer;
        
        // Ensure the region is at least as large as the base region
        int finalWidth = Math.max(baseRegion.getW(), maxWidth);
        int finalHeight = Math.max(baseRegion.getH(), maxHeight);
        
        // If expansion is needed, try to keep the region centered
        if (finalWidth > baseRegion.getW() || finalHeight > baseRegion.getH()) {
            int xAdjust = (finalWidth - baseRegion.getW()) / 2;
            int yAdjust = (finalHeight - baseRegion.getH()) / 2;
            
            // Ensure we don't go off-screen (assuming 1920x1080 default)
            int newX = Math.max(0, baseRegion.getX() - xAdjust);
            int newY = Math.max(0, baseRegion.getY() - yAdjust);
            
            // Adjust width/height if we hit screen boundaries
            if (newX + finalWidth > 1920) {
                finalWidth = 1920 - newX;
            }
            if (newY + finalHeight > 1080) {
                finalHeight = 1080 - newY;
            }
            
            Region expandedRegion = new Region(newX, newY, finalWidth, finalHeight);
            log.info("Expanded search region from {} to {} to accommodate all patterns", 
                    baseRegion, expandedRegion);
            return expandedRegion;
        }
        
        return baseRegion;
    }
    
    /**
     * Validates whether a search region can accommodate all patterns in a StateImage.
     * This is critical because Brobot will return empty results if a pattern is
     * larger than its search region.
     * 
     * @param stateImage The StateImage containing patterns to validate
     * @param region The search region to validate against
     * @return ValidationResult indicating whether all patterns fit
     */
    public static ValidationResult validateSearchRegion(StateImage stateImage, Region region) {
        ValidationResult result = new ValidationResult();
        
        if (region == null) {
            result.addIssue("Search region is null");
            return result;
        }
        
        for (Pattern pattern : stateImage.getPatterns()) {
            if (pattern.getImage() != null) {
                int patternWidth = pattern.getImage().w();
                int patternHeight = pattern.getImage().h();
                
                // Check width constraint
                if (patternWidth > region.getW()) {
                    result.addIssue(String.format(
                        "Pattern '%s' width (%dpx) exceeds search region width (%dpx)",
                        pattern.getName(), patternWidth, region.getW()
                    ));
                }
                
                // Check height constraint
                if (patternHeight > region.getH()) {
                    result.addIssue(String.format(
                        "Pattern '%s' height (%dpx) exceeds search region height (%dpx)",
                        pattern.getName(), patternHeight, region.getH()
                    ));
                }
            }
        }
        
        if (result.isValid()) {
            log.debug("All {} patterns fit within search region {}", 
                     stateImage.getPatterns().size(), region);
        } else {
            log.warn("Search region validation failed: {}", result.getIssues());
        }
        
        return result;
    }
    
    /**
     * Applies a shared search region to all patterns in a StateImage, but only
     * if the region can accommodate all patterns. Falls back to individual
     * regions if needed.
     * 
     * @param stateImage The StateImage to configure
     * @param desiredRegion The desired shared search region
     * @return true if shared region was applied, false if fallback was needed
     */
    public static boolean applySharedRegionSafely(StateImage stateImage, Region desiredRegion) {
        // First, calculate the minimum region needed
        Region requiredRegion = calculateMinimumSharedRegion(stateImage, desiredRegion);
        
        // Validate that all patterns fit
        ValidationResult validation = validateSearchRegion(stateImage, requiredRegion);
        
        if (validation.isValid()) {
            // Apply the shared region to all patterns
            for (Pattern pattern : stateImage.getPatterns()) {
                pattern.getSearchRegions().setFixedRegion(requiredRegion);
                pattern.setFixed(true);
            }
            log.info("Applied shared search region {} to StateImage '{}'", 
                    requiredRegion, stateImage.getName());
            return true;
        } else {
            // Fall back to individual regions or expand as needed
            log.warn("Cannot use shared region for StateImage '{}': {}", 
                    stateImage.getName(), validation.getIssues());
            
            // Apply individual regions with size validation
            for (Pattern pattern : stateImage.getPatterns()) {
                if (pattern.getImage() != null) {
                    // Calculate minimum region for this specific pattern
                    int minWidth = pattern.getImage().w() + 20;  // buffer
                    int minHeight = pattern.getImage().h() + 20;
                    
                    Region patternRegion = new Region(
                        desiredRegion.getX(),
                        desiredRegion.getY(),
                        Math.max(desiredRegion.getW(), minWidth),
                        Math.max(desiredRegion.getH(), minHeight)
                    );
                    
                    pattern.getSearchRegions().setFixedRegion(patternRegion);
                    pattern.setFixed(true);
                    log.debug("Set individual region {} for pattern '{}'", 
                            patternRegion, pattern.getName());
                }
            }
            return false;
        }
    }
    
    /**
     * Finds the optimal shared search region for a StateImage by analyzing
     * all patterns and finding the smallest region that accommodates them all.
     * 
     * @param stateImage The StateImage to analyze
     * @param preferredLocation The preferred center point for the region
     * @return The optimal search region
     */
    public static Region findOptimalSharedRegion(StateImage stateImage, Region preferredLocation) {
        if (stateImage.getPatterns().isEmpty()) {
            return preferredLocation;
        }
        
        // Find maximum dimensions
        int maxWidth = 0;
        int maxHeight = 0;
        
        for (Pattern pattern : stateImage.getPatterns()) {
            if (pattern.getImage() != null) {
                maxWidth = Math.max(maxWidth, pattern.getImage().w());
                maxHeight = Math.max(maxHeight, pattern.getImage().h());
            }
        }
        
        // Add 20% buffer for safety
        maxWidth = (int)(maxWidth * 1.2);
        maxHeight = (int)(maxHeight * 1.2);
        
        // Center on preferred location
        int x = preferredLocation.getX() + (preferredLocation.getW() / 2) - (maxWidth / 2);
        int y = preferredLocation.getY() + (preferredLocation.getH() / 2) - (maxHeight / 2);
        
        // Ensure we stay on screen
        x = Math.max(0, x);
        y = Math.max(0, y);
        
        return new Region(x, y, maxWidth, maxHeight);
    }
}