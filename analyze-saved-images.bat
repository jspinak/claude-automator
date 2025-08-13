@echo off
echo Analyzing saved comparison images...
echo =====================================
echo.

gradlew.bat test --tests "com.claude.automator.debug.AnalyzeSavedComparisonTest"

echo.
echo Analysis complete!
pause