@echo off
echo ========================================================
echo    SIKULIX IDE VS BROBOT COMPARISON TEST
echo ========================================================
echo.
echo SETUP:
echo   - SikuliX IDE: Running on Windows
echo   - Brobot: Running on Windows  
echo   - Target: VS Code in WSL2/Debian
echo   - Pattern: Same image from images.sikuli
echo.
echo Make sure VS Code is positioned exactly as when
echo testing with SikuliX IDE!
echo.

gradlew.bat test --tests "com.claude.automator.debug.SikuliXIDEComparisonTest"

echo.
echo ========================================================
echo If IDE gets 0.99 but Brobot gets 0.70, the difference
echo must be in how the pattern is loaded or processed.
echo ========================================================
echo.
pause