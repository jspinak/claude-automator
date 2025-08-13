@echo off
echo Running Scaling Analysis Test...
echo ================================
echo.
echo This test will analyze if DPI/scaling is causing the pattern mismatch.
echo Make sure your target application is visible!
echo.

gradlew.bat test --tests "com.claude.automator.debug.ScalingAnalysisTest"

echo.
echo Test complete. Check the scaling-analysis folder for:
echo   - Original pattern
echo   - Matched region
echo   - Upscaled comparison
echo   - Side-by-side comparison
echo.
pause