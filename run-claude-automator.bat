@echo off
echo Starting Claude Automator with OpenCV 4.9.0...
echo.

REM Set environment variables for library loading
set SIKULIXLIBS=%CD%\lib
set PATH=%CD%\lib;%PATH%

REM Set Java options for native library loading and DPI settings
set JAVA_OPTS=-Djava.library.path=%CD%\lib;%CD%\lib\natives\windows-x86_64 ^
              -Djna.library.path=%CD%\lib ^
              -Dsikulixlibs=%CD%\lib ^
              -Dbrobot.dpi.disable=true ^
              -Dsun.java2d.dpiaware=false ^
              -Dsun.java2d.uiScale=1.0 ^
              -Dfile.encoding=UTF-8

REM Run with Gradle
echo Running with Gradle...
gradlew bootRun

REM Alternative: Run the JAR directly if it exists
REM if exist "build\libs\claude-automator-1.0.0.jar" (
REM     java %JAVA_OPTS% -jar build\libs\claude-automator-1.0.0.jar
REM )

pause