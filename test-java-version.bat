@echo off
echo ========================================================
echo    JAVA VERSION IMAGE COMPARISON TEST
echo ========================================================
echo.
echo Testing if Java version differences affect image loading...
echo.

echo Current Java version:
java -version
echo.

gradlew.bat test --tests "com.claude.automator.debug.JavaVersionImageComparisonTest"

echo.
echo ========================================================
echo To test with Java 8 (if available):
echo   1. Set JAVA_HOME to Java 8 installation
echo   2. Run this test again
echo ========================================================
pause