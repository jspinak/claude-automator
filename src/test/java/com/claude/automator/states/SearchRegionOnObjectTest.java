package com.claude.automator.states;

import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.model.state.StateObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SearchRegionOnObjectTest {
    
    @Test
    public void testSearchRegionOnObjectBuilder() {
        MatchAdjustmentOptions adjustOptions = MatchAdjustmentOptions.builder()
                .addX(3)
                .addY(10)
                .addW(30)
                .addH(55)
                .build();
        
        assertNotNull(adjustOptions);
        assertEquals(3, adjustOptions.getAddX());
        assertEquals(10, adjustOptions.getAddY());
        assertEquals(30, adjustOptions.getAddW());
        assertEquals(55, adjustOptions.getAddH());
        
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