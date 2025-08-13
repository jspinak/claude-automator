@echo off
echo Running Pattern Matching Diagnostic Test on Windows...
echo.
echo Make sure you have the target application visible on screen!
echo.

REM Run the diagnostic test
gradlew.bat test --tests "com.claude.automator.debug.PatternMatchingDiagnosticTest"

echo.
echo Test complete. Check the pattern-diagnostic folder for results.
pause