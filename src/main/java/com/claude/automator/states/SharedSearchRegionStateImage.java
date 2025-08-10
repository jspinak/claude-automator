package com.claude.automator.states;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Pattern;
import lombok.extern.slf4j.Slf4j;

/**
 * A better architectural solution for StateImages where all patterns should share
 * the same search region without duplication issues.
 * 
 * This approach maintains a single shared region reference at the StateImage level
 * rather than duplicating it across each pattern.
 */
@Slf4j
public class SharedSearchRegionStateImage {
    
    /**
     * Creates a StateImage where all patterns share a single search region.
     * This approach avoids the duplication issue while maintaining the flexibility
     * of having multiple patterns.
     * 
     * Benefits:
     * - Single source of truth for the search region
     * - No duplicate highlighting issues
     * - More memory efficient (one region object instead of N copies)
     * - Easier to update the search region (change in one place)
     * 
     * @param patterns Array of pattern filenames
     * @param sharedRegion The region to use for all patterns
     * @param name The name for the StateImage
     * @return A StateImage with patterns sharing the same region reference
     */
    public static StateImage createWithSharedRegion(String[] patterns, Region sharedRegion, String name) {
        // Create the StateImage without setting regions initially
        StateImage.Builder builder = new StateImage.Builder()
            .setName(name);
        
        // Add all patterns
        for (String pattern : patterns) {
            builder.addPattern(pattern);
        }
        
        StateImage stateImage = builder.build();
        
        // Now set the SAME region object reference for all patterns
        // This ensures they all point to the exact same Region instance
        for (Pattern pattern : stateImage.getPatterns()) {
            pattern.getSearchRegions().setFixedRegion(sharedRegion);
            pattern.setFixed(true);
        }
        
        log.debug("Created StateImage '{}' with {} patterns sharing region: {}", 
                 name, patterns.length, sharedRegion);
        
        return stateImage;
    }
    
    /**
     * Alternative approach: Create a StateImage with a region supplier.
     * This allows dynamic region calculation while ensuring consistency.
     * 
     * @param patterns Array of pattern filenames  
     * @param regionSupplier Function that provides the search region
     * @param name The name for the StateImage
     * @return A StateImage with dynamically calculated shared region
     */
    public static StateImage createWithDynamicRegion(
            String[] patterns, 
            java.util.function.Supplier<Region> regionSupplier, 
            String name) {
        
        StateImage.Builder builder = new StateImage.Builder()
            .setName(name);
        
        for (String pattern : patterns) {
            builder.addPattern(pattern);
        }
        
        StateImage stateImage = builder.build();
        
        // Get the region once and share it
        Region sharedRegion = regionSupplier.get();
        
        for (Pattern pattern : stateImage.getPatterns()) {
            pattern.getSearchRegions().setFixedRegion(sharedRegion);
            pattern.setFixed(true);
        }
        
        log.debug("Created StateImage '{}' with dynamic region calculation", name);
        
        return stateImage;
    }
    
    /**
     * Updates the search region for all patterns in a StateImage.
     * Ensures all patterns continue to share the same region reference.
     * 
     * @param stateImage The StateImage to update
     * @param newRegion The new region to apply to all patterns
     */
    public static void updateSharedRegion(StateImage stateImage, Region newRegion) {
        for (Pattern pattern : stateImage.getPatterns()) {
            pattern.getSearchRegions().setFixedRegion(newRegion);
            pattern.setFixed(true);
        }
        
        log.debug("Updated shared region for StateImage '{}' to: {}", 
                 stateImage.getName(), newRegion);
    }
    
    /**
     * Checks if all patterns in a StateImage share the same region reference.
     * Useful for debugging and validation.
     * 
     * @param stateImage The StateImage to check
     * @return true if all patterns share the same region object reference
     */
    public static boolean hasSharedRegion(StateImage stateImage) {
        if (stateImage.getPatterns().isEmpty()) {
            return true;
        }
        
        Region firstRegion = stateImage.getPatterns().get(0)
            .getSearchRegions().getFixedRegion();
        
        if (firstRegion == null) {
            return false;
        }
        
        // Check if all patterns have the same region reference (not just equal values)
        return stateImage.getPatterns().stream()
            .allMatch(p -> p.getSearchRegions().getFixedRegion() == firstRegion);
    }
}