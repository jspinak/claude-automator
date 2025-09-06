@echo off
echo ========================================
echo OpenCV 4.9.0 Setup for Claude Automator
echo ========================================
echo.

REM Check for JAVA_HOME
if "%JAVA_HOME%"=="" (
    echo Setting JAVA_HOME to detected JDK...
    set JAVA_HOME=C:\Users\jspin\.jdks\openjdk-21.0.1
)

echo Using JAVA_HOME: %JAVA_HOME%
echo.

REM Create lib directory if it doesn't exist
if not exist "lib" (
    echo Creating lib directory...
    mkdir lib
)

REM Create native directory structure
if not exist "lib\natives" mkdir lib\natives
if not exist "lib\natives\windows-x86_64" mkdir lib\natives\windows-x86_64

echo Downloading OpenCV 4.9.0 native library for Windows...
echo.

REM Download the specific OpenCV version that SikuliX is looking for
powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/bytedeco/opencv/4.9.0-1.5.10/opencv-4.9.0-1.5.10-windows-x86_64.jar' -OutFile 'opencv-windows.jar' -UseBasicParsing}"

if not exist "opencv-windows.jar" (
    echo Failed to download OpenCV 4.9.0!
    pause
    exit /b 1
)

echo Extracting OpenCV native libraries...
REM JAR files are ZIP files, but PowerShell Expand-Archive doesn't recognize .jar extension
REM Rename to .zip temporarily
copy opencv-windows.jar opencv-windows.zip >nul
powershell -Command "Expand-Archive -Path 'opencv-windows.zip' -DestinationPath 'opencv-extract' -Force"
del opencv-windows.zip

REM Copy all DLLs to appropriate locations
echo Copying native libraries...

REM Copy opencv_java490.dll specifically
if exist "opencv-extract\org\bytedeco\opencv\windows-x86_64\opencv_java490.dll" (
    copy "opencv-extract\org\bytedeco\opencv\windows-x86_64\*.dll" "lib\" /Y
    copy "opencv-extract\org\bytedeco\opencv\windows-x86_64\*.dll" "lib\natives\windows-x86_64\" /Y
    copy "opencv-extract\org\bytedeco\opencv\windows-x86_64\opencv_java490.dll" "%JAVA_HOME%\bin\" /Y
    
    REM Also copy to system directories (requires admin)
    echo.
    echo Attempting to copy to system directories (may require admin)...
    copy "opencv-extract\org\bytedeco\opencv\windows-x86_64\opencv_java490.dll" "C:\Windows\System32\" /Y 2>nul
    if errorlevel 1 (
        echo Note: Could not copy to System32 - not running as admin
    )
)

REM Also download OpenBLAS which OpenCV depends on
echo.
echo Downloading OpenBLAS dependencies...
powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/bytedeco/openblas/0.3.26-1.5.10/openblas-0.3.26-1.5.10-windows-x86_64.jar' -OutFile 'openblas-windows.jar' -UseBasicParsing}"

if exist "openblas-windows.jar" (
    echo Extracting OpenBLAS libraries...
    copy openblas-windows.jar openblas-windows.zip >nul
    powershell -Command "Expand-Archive -Path 'openblas-windows.zip' -DestinationPath 'openblas-extract' -Force"
    del openblas-windows.zip
    
    if exist "openblas-extract\org\bytedeco\openblas\windows-x86_64\*.dll" (
        copy "openblas-extract\org\bytedeco\openblas\windows-x86_64\*.dll" "lib\" /Y
        copy "openblas-extract\org\bytedeco\openblas\windows-x86_64\*.dll" "lib\natives\windows-x86_64\" /Y
    )
)

REM Clean up temporary files
echo.
echo Cleaning up temporary files...
if exist "opencv-extract" rmdir /s /q "opencv-extract"
if exist "openblas-extract" rmdir /s /q "openblas-extract"
if exist "opencv-windows.jar" del "opencv-windows.jar"
if exist "openblas-windows.jar" del "openblas-windows.jar"

echo.
echo ========================================
echo Setup Complete!
echo ========================================
echo.
echo OpenCV 4.9.0 native libraries have been installed to:
echo   - lib\ (project directory)
echo   - %JAVA_HOME%\bin\ (Java directory)
echo.
echo Please run the following commands to rebuild and run:
echo   gradlew clean
echo   gradlew build
echo   gradlew bootRun
echo.
echo If you still encounter issues, try:
echo   1. Run this script as Administrator
echo   2. Set SIKULIXLIBS environment variable to: %CD%\lib
echo   3. Add %CD%\lib to your PATH environment variable
echo.
pause