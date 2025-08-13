@echo off
echo Extracting Matched Regions for Comparison...
echo.
echo Make sure you have the target application visible on screen!
echo This will extract the actual matched regions to compare with patterns.
echo.

REM Run the extraction test
gradlew.bat test --tests "com.claude.automator.debug.ExtractMatchedRegionTest"

echo.
echo Test complete. Check the matched-regions-comparison folder for:
echo   - ORIGINAL images (your patterns)
echo   - MATCHED images (what was found on screen)
echo   - DIFF images (showing pixel differences)
echo.
pause