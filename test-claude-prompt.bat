@echo off
echo ========================================================
echo    CLAUDE-PROMPT-1.PNG SIMILARITY ANALYSIS
echo ========================================================
echo.
echo This test will analyze why SikuliX IDE gets 0.99 similarity
echo while Brobot gets ~0.69 similarity for claude-prompt-1.png
echo.
echo IMPORTANT: Position your screen EXACTLY as you do when
echo            testing with SikuliX IDE!
echo.
echo Starting in 3 seconds...
timeout /t 3 >nul

gradlew.bat test --tests "com.claude.automator.debug.ClaudePromptSimilarityTest"

echo.
echo ========================================================
echo Test complete!
echo.
echo Check the 'claude-prompt-analysis' folder for:
echo   - Screen capture
echo   - Matched region
echo   - Difference visualization
echo   - Side-by-side comparison
echo ========================================================
echo.
pause