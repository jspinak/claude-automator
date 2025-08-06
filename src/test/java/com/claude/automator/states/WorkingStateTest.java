package com.claude.automator.states;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class WorkingStateTest {
    
    @Test
    public void testWorkingStateCreation() {
        WorkingState workingState = new WorkingState();
        assertNotNull(workingState);
        assertNotNull(workingState.getClaudeIcon());
        assertNotNull(workingState.getClaudeIcon().getSearchRegionOnObject());
    }
}