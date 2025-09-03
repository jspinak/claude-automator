package com.claude.automator.mock;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Handles find operations in mock mode by using ActionHistory data.
 * This fixes the issue where mock finds fail despite having ActionHistory.
 */
@Component
@Slf4j
public class MockFindHandler {

    private final Random random = new Random();

    /**
     * Performs a mock find operation using ActionHistory.
     * 
     * @param objects the objects to find
     * @return ActionResult with matches from ActionHistory
     */
    public ActionResult performMockFind(ObjectCollection objects) {
        if (!FrameworkSettings.mock) {
            log.warn("MockFindHandler called but mock mode is disabled");
            return new ActionResult();
        }

        log.debug("Performing mock find for {} images", objects.getStateImages().size());

        ActionResult result = new ActionResult();
        List<Match> allMatches = new ArrayList<>();

        // Process each StateImage
        for (StateImage stateImage : objects.getStateImages()) {
            log.debug("Processing StateImage: {}", stateImage.getName());

            // Check each pattern for ActionHistory
            for (Pattern pattern : stateImage.getPatterns()) {
                ActionHistory history = pattern.getMatchHistory();

                if (history == null || history.getSnapshots().isEmpty()) {
                    log.debug("Pattern {} has no ActionHistory", pattern.getName());
                    continue;
                }

                // Get a random successful snapshot
                List<ActionRecord> successfulSnapshots = history.getSnapshots().stream()
                        .filter(ActionRecord::isActionSuccess)
                        .toList();

                if (successfulSnapshots.isEmpty()) {
                    log.debug("Pattern {} has no successful snapshots", pattern.getName());
                    continue;
                }

                // Select a random successful snapshot
                ActionRecord snapshot = successfulSnapshots.get(
                        random.nextInt(successfulSnapshots.size()));

                // Add matches from the snapshot
                if (!snapshot.getMatchList().isEmpty()) {
                    Match match = snapshot.getMatchList().get(0);

                    // Create a new match with the StateImage reference
                    Match mockMatch = new Match.Builder()
                            .setRegion(match.getRegion())
                            .setSimScore(match.getScore())
                            .setStateObjectData(stateImage)
                            .build();

                    allMatches.add(mockMatch);
                    log.info("✓ Mock found {} at {}", stateImage.getName(), match.getRegion());

                    // Usually find only returns the first match
                    break;
                }
            }
        }

        // Add all matches to result
        for (Match match : allMatches) {
            result.add(match);
        }

        // Set success if any matches found
        if (!allMatches.isEmpty()) {
            result.setSuccess(true);
        }

        log.info("Mock find complete: {} matches found", allMatches.size());
        return result;
    }

    /**
     * Checks if a StateImage can be found in mock mode.
     * 
     * @param stateImage the image to check
     * @return true if the image has ActionHistory with successful finds
     */
    public boolean canFindInMock(StateImage stateImage) {
        for (Pattern pattern : stateImage.getPatterns()) {
            ActionHistory history = pattern.getMatchHistory();
            if (history != null && !history.getSnapshots().isEmpty()) {
                // Check for at least one successful snapshot
                boolean hasSuccess = history.getSnapshots().stream()
                        .anyMatch(ActionRecord::isActionSuccess);
                if (hasSuccess) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the probability of finding a StateImage based on its ActionHistory.
     * 
     * @param stateImage the image to check
     * @return probability between 0.0 and 1.0
     */
    public double getFindProbability(StateImage stateImage) {
        int totalSnapshots = 0;
        int successfulSnapshots = 0;

        for (Pattern pattern : stateImage.getPatterns()) {
            ActionHistory history = pattern.getMatchHistory();
            if (history != null) {
                for (ActionRecord record : history.getSnapshots()) {
                    totalSnapshots++;
                    if (record.isActionSuccess()) {
                        successfulSnapshots++;
                    }
                }
            }
        }

        if (totalSnapshots == 0) {
            return 0.0;
        }

        return (double) successfulSnapshots / totalSnapshots;
    }
}