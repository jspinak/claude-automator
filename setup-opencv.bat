@echo off
echo ========================================
echo OpenCV Setup for Claude Automator
echo ========================================
echo.

REM Check for JAVA_HOME
if "%JAVA_HOME%"=="" (
    echo ERROR: JAVA_HOME is not set!
    echo Please set JAVA_HOME to your JDK installation directory
    echo Example: set JAVA_HOME=C:\Users\jspin\.jdks\openjdk-21.0.1
    pause
    exit /b 1
)

echo Using JAVA_HOME: %JAVA_HOME%
echo.

REM Detect architecture
if "%PROCESSOR_ARCHITECTURE%"=="AMD64" (
    set ARCH=x64
    echo Detected 64-bit Windows
) else (
    set ARCH=x86
    echo Detected 32-bit Windows
)
echo.

REM Create lib directory if it doesn't exist
if not exist "lib" (
    echo Creating lib directory...
    mkdir lib
)

REM Option 1: Try to copy from existing SikuliX installation
echo Checking for existing SikuliX installation...
if exist "%APPDATA%\Sikulix\SikulixLibs\opencv_java430.dll" (
    echo Found OpenCV in SikuliX directory!
    copy "%APPDATA%\Sikulix\SikulixLibs\opencv_java430.dll" "lib\" /Y
    copy "%APPDATA%\Sikulix\SikulixLibs\opencv_java430.dll" "%JAVA_HOME%\bin\" /Y
    echo OpenCV library copied successfully!
    goto :success
)

REM Option 2: Download pre-built OpenCV Java bindings
echo SikuliX not found. Downloading OpenCV Java bindings...
echo.

REM Create temp directory
if not exist "temp" mkdir temp
cd temp

REM Download using PowerShell
echo Downloading OpenCV 4.3.0 Java bindings...
powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://github.com/bytedeco/javacpp-presets/releases/download/1.5.3/opencv-4.3.0-1.5.3-windows-%ARCH%.jar' -OutFile 'opencv-java.jar' -UseBasicParsing}"

if not exist "opencv-java.jar" (
    echo Failed to download OpenCV Java bindings!
    echo.
    echo Please try one of these alternatives:
    echo 1. Install SikuliX 2.0.5 from https://github.com/RaiMan/SikuliX1/releases
    echo 2. Download OpenCV 4.3.0 manually from https://opencv.org/releases/
    echo 3. Use the Gradle dependency solution in OPENCV-WINDOWS-FIX.md
    cd ..
    pause
    exit /b 1
)

REM Extract DLL from JAR
echo Extracting native libraries...
powershell -Command "Expand-Archive -Path 'opencv-java.jar' -DestinationPath 'extracted' -Force"

REM Find and copy the DLL
if exist "extracted\org\bytedeco\opencv\windows-%ARCH%\opencv_java430.dll" (
    copy "extracted\org\bytedeco\opencv\windows-%ARCH%\opencv_java430.dll" "..\lib\" /Y
    copy "extracted\org\bytedeco\opencv\windows-%ARCH%\opencv_java430.dll" "%JAVA_HOME%\bin\" /Y
) else (
    REM Try alternative structure
    for /r "extracted" %%f in (opencv_java*.dll) do (
        echo Found: %%f
        copy "%%f" "..\lib\" /Y
        copy "%%f" "%JAVA_HOME%\bin\" /Y
        goto :cleanup
    )
    echo Could not find opencv_java DLL in extracted files!
    cd ..
    pause
    exit /b 1
)

:cleanup
cd ..
rmdir /s /q temp

:success
echo.
echo ========================================
echo Setup Complete!
echo ========================================
echo.
echo OpenCV native library has been installed to:
echo   - lib\opencv_java430.dll (project directory)
echo   - %JAVA_HOME%\bin\opencv_java430.dll (Java directory)
echo.
echo You can now run claude-automator with:
echo   gradlew bootRun
echo.
echo If you still encounter issues, try running with:
echo   gradlew bootRun -Djava.library.path=lib
echo.
pause