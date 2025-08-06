package com.claude.automator.states;

import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.model.state.StateObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SearchRegionOnObjectTest {
    
    @Test
    public void testSearchRegionOnObjectBuilder() {
        SearchRegionOnObject.AdjustOptions adjustOptions = SearchRegionOnObject.AdjustOptions.builder()
                .xAdjust(3)
                .yAdjust(10)
                .wAdjust(30)
                .hAdjust(55)
                .build();
        
        assertNotNull(adjustOptions);
        assertEquals(3, adjustOptions.getXAdjust());
        assertEquals(10, adjustOptions.getYAdjust());
        assertEquals(30, adjustOptions.getWAdjust());
        assertEquals(55, adjustOptions.getHAdjust());
        
        SearchRegionOnObject searchRegion = SearchRegionOnObject.builder()
                .targetType(StateObject.Type.IMAGE)
                .targetStateName("Prompt")
                .targetObjectName("ClaudePrompt")
                .adjustments(adjustOptions)
                .build();
        
        assertNotNull(searchRegion);
        assertEquals(StateObject.Type.IMAGE, searchRegion.getTargetType());
        assertEquals("Prompt", searchRegion.getTargetStateName());
        assertEquals("ClaudePrompt", searchRegion.getTargetObjectName());
        assertNotNull(searchRegion.getAdjustments());
    }
}