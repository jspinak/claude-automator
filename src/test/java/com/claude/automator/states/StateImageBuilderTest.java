package com.claude.automator.states;

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
                .adjustments(SearchRegionOnObject.AdjustOptions.builder()
                        .xAdjust(3)
                        .yAdjust(10)
                        .wAdjust(30)
                        .hAdjust(55)
                        .build())
                .build();
        
        assertNotNull(searchRegion);
        
        // Now test with StateImage.Builder
        try {
            StateImage stateImage = new StateImage.Builder()
                    .addPatterns("test-pattern")
                    .setName("TestImage")
                    .setSearchRegionOnObject(searchRegion)
                    .build();
            
            assertNotNull(stateImage);
            assertNotNull(stateImage.getSearchRegionOnObject());
            assertEquals("TestImage", stateImage.getName());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}