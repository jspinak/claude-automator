package com.claude.automator.states;

import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StateImageBuilderTest {
    
    @Test
    public void testStateImageWithSearchRegionOnObject() {
        SearchRegionOnObject searchRegion = SearchRegionOnObject.builder()
                .targetType(StateObject.Type.IMAGE)
                .targetStateName("Prompt")
                .targetObjectName("ClaudePrompt")
                .adjustments(MatchAdjustmentOptions.builder()
                        .addX(3)
                        .addY(10)
                        .addW(30)
                        .addH(55)
                        .build())
                .build();
        
        assertNotNull(searchRegion);
        
        // Now test with StateImage.Builder
        // Create a Pattern object directly without loading an image file
        Pattern testPattern = new Pattern();
        
        StateImage stateImage = new StateImage.Builder()
                .addPattern(testPattern)
                .setName("TestImage")
                .setSearchRegionOnObject(searchRegion)
                .build();
        
        assertNotNull(stateImage);
        assertNotNull(stateImage.getSearchRegionOnObject());
        assertEquals("TestImage", stateImage.getName());
        assertEquals(searchRegion, stateImage.getSearchRegionOnObject());
    }
}