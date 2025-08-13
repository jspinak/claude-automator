@echo off
echo ========================================================
echo    TESTING PATTERN MATCHING FIX
echo ========================================================
echo.
echo This test verifies that Brobot now achieves 0.99 
echo similarity scores like SikuliX IDE by using direct
echo file path loading instead of BufferedImage conversion.
echo.
echo THE FIX:
echo - SikuliX IDE: finder.find(patFilename) 
echo - Old Brobot: Convert to BufferedImage first
echo - New Brobot: Use file path directly when available
echo.
echo Make sure VS Code is visible with the pattern!
echo.
pause

gradlew.bat test --tests "com.claude.automator.debug.VerifyPatternMatchingFixTest"

echo.
echo ========================================================
echo If the test shows 0.99 similarity, the fix is working!
echo ========================================================
pause