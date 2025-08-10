package com.claude.automator.debug;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.sikuli.script.Screen;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class to ensure only unique regions are highlighted when a StateImage
 * has multiple patterns sharing the same search region.
 * 
 * This solves the issue where setSearchRegionForAllPatterns and setFixedSearchRegion
 * both add the same region to each pattern, causing duplicate highlights.
 */
@Slf4j
public class SingleRegionHighlighter {
    
    /**
     * Gets unique search regions from a StateImage to avoid duplicate highlights.
     * When multiple patterns share the same search region, only one instance is returned.
     * 
     * @param stateImage The StateImage to extract unique regions from
     * @return Set of unique regions (typically just one when using fixed regions)
     */
    public static Set<Region> getUniqueSearchRegions(StateImage stateImage) {
        Set<Region> uniqueRegions = new HashSet<>();
        
        if (stateImage == null || stateImage.getPatterns().isEmpty()) {
            log.debug("No patterns found in StateImage");
            return uniqueRegions;
        }
        
        // Check if all patterns share the same fixed region
        Region firstRegion = null;
        boolean allSameRegion = true;
        
        for (Pattern pattern : stateImage.getPatterns()) {
            Region fixedRegion = pattern.getSearchRegions().getFixedRegion();
            if (fixedRegion != null) {
                if (firstRegion == null) {
                    firstRegion = fixedRegion;
                } else if (!regionsEqual(firstRegion, fixedRegion)) {
                    allSameRegion = false;
                }
                uniqueRegions.add(fixedRegion);
            }
        }
        
        if (allSameRegion && firstRegion != null) {
            log.debug("All {} patterns share the same search region: {}", 
                     stateImage.getPatterns().size(), firstRegion);
            uniqueRegions.clear();
            uniqueRegions.add(firstRegion);
        } else {
            log.debug("Found {} unique search regions across {} patterns", 
                     uniqueRegions.size(), stateImage.getPatterns().size());
        }
        
        return uniqueRegions;
    }
    
    /**
     * Highlights unique search regions for a StateImage, avoiding duplicates.
     * 
     * @param stateImage The StateImage whose regions to highlight
     * @param duration How long to show the highlight in seconds
     */
    public static void highlightUniqueRegions(StateImage stateImage, double duration) {
        Set<Region> uniqueRegions = getUniqueSearchRegions(stateImage);
        
        for (Region region : uniqueRegions) {
            try {
                log.info("Highlighting unique region: {}", region);
                org.sikuli.script.Region sikuliRegion = new org.sikuli.script.Region(
                    region.getX(), 
                    region.getY(), 
                    region.getW(), 
                    region.getH()
                );
                sikuliRegion.highlight(duration);
            } catch (Exception e) {
                log.error("Failed to highlight region: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Compares two regions for equality based on their coordinates and dimensions.
     */
    private static boolean regionsEqual(Region r1, Region r2) {
        if (r1 == r2) return true;
        if (r1 == null || r2 == null) return false;
        
        return r1.getX() == r2.getX() && 
               r1.getY() == r2.getY() && 
               r1.getW() == r2.getW() && 
               r1.getH() == r2.getH();
    }
    
    /**
     * Debug method to print detailed information about search regions in a StateImage.
     */
    public static void debugSearchRegions(StateImage stateImage) {
        if (stateImage == null) {
            log.debug("StateImage is null");
            return;
        }
        
        log.debug("=== StateImage Search Region Debug ===");
        log.debug("StateImage name: {}", stateImage.getName());
        log.debug("Number of patterns: {}", stateImage.getPatterns().size());
        
        int patternIndex = 0;
        for (Pattern pattern : stateImage.getPatterns()) {
            log.debug("Pattern {}: {}", patternIndex++, pattern.getName());
            Region fixedRegion = pattern.getSearchRegions().getFixedRegion();
            if (fixedRegion != null) {
                log.debug("  Fixed region: {}", fixedRegion);
                log.debug("  Is fixed: {}", pattern.isFixed());
            } else {
                log.debug("  No fixed region set");
            }
        }
        
        Set<Region> uniqueRegions = getUniqueSearchRegions(stateImage);
        log.debug("Unique regions count: {}", uniqueRegions.size());
        for (Region region : uniqueRegions) {
            log.debug("  Unique region: {}", region);
        }
    }
}