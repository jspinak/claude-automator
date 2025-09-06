# OpenCV Library Loading Fix for Windows

## Problem
The error `java.lang.UnsatisfiedLinkError: no opencv_java430 in java.library.path` occurs because the OpenCV native library (opencv_java430.dll) is not found on Windows.

## Root Cause
- Brobot uses SikuliX 2.0.5 which depends on OpenCV 4.3.0
- The JavaCV/OpenCV platform dependencies include native libraries for multiple platforms
- On Windows, the native DLL needs to be accessible via java.library.path

## Solutions

### Solution 1: Use JavaCV's OpenCV (Recommended)
Update the claude-automator `build.gradle` to explicitly include OpenCV platform dependencies:

```gradle
dependencies {
    // Brobot library
    implementation 'io.github.jspinak:brobot'
    
    // Explicit OpenCV platform dependency for Windows
    implementation 'org.bytedeco:opencv-platform:4.9.0-1.5.10'
    implementation 'org.bytedeco:javacv-platform:1.5.10'
    
    // Rest of dependencies...
}
```

### Solution 2: Manual OpenCV Library Setup
1. Download OpenCV 4.3.0 for Windows from: https://github.com/opencv/opencv/releases/tag/4.3.0
2. Extract the `opencv_java430.dll` from `opencv/build/java/x64/` (or x86 for 32-bit)
3. Place the DLL in one of these locations:
   - `C:\Windows\System32\` (for 64-bit DLL on 64-bit Windows)
   - `C:\Windows\SysWOW64\` (for 32-bit DLL on 64-bit Windows)
   - Your Java installation's `bin` directory: `C:\Users\jspin\.jdks\openjdk-21.0.1\bin\`
   - Create a `lib` folder in your project and add to java.library.path

### Solution 3: Set Java Library Path
Add the OpenCV library path when running the application:

```batch
java -Djava.library.path="C:\path\to\opencv\build\java\x64" -jar claude-automator.jar
```

Or in `build.gradle`:

```gradle
bootRun {
    jvmArgs = [
        '-Djava.library.path=C:\\path\\to\\opencv\\build\\java\\x64',
        '-Dbrobot.dpi.disable=true',
        '-Dsun.java2d.dpiaware=false',
        '-Dsun.java2d.uiScale=1.0'
    ]
}
```

### Solution 4: Use SikuliX Setup (Automated)
1. Download SikuliX IDE 2.0.5 from: https://github.com/RaiMan/SikuliX1/releases
2. Run the setup which will automatically download and configure OpenCV libraries
3. The setup places libraries in `%APPDATA%\Sikulix\SikulixLibs\`
4. Add this to your java.library.path

## Quick Fix Script for Windows

Create `setup-opencv.bat`:

```batch
@echo off
echo Setting up OpenCV for Claude Automator...

REM Check if running as admin
net session >nul 2>&1
if %errorLevel% NEQ 0 (
    echo Please run this script as Administrator
    pause
    exit /b 1
)

REM Download OpenCV if not present
if not exist "opencv-4.3.0-vc14_vc15.exe" (
    echo Downloading OpenCV 4.3.0...
    powershell -Command "Invoke-WebRequest -Uri 'https://github.com/opencv/opencv/releases/download/4.3.0/opencv-4.3.0-vc14_vc15.exe' -OutFile 'opencv-4.3.0-vc14_vc15.exe'"
)

REM Extract OpenCV
echo Extracting OpenCV...
opencv-4.3.0-vc14_vc15.exe -o"C:\opencv" -y

REM Copy DLL to Java bin
echo Copying opencv_java430.dll to Java directory...
copy "C:\opencv\opencv\build\java\x64\opencv_java430.dll" "%JAVA_HOME%\bin\" /Y

echo Setup complete! You can now run claude-automator.
pause
```

## Verification
After applying the fix, verify it works:

```batch
cd claude-automator
gradlew bootRun
```

The application should start without the UnsatisfiedLinkError.

## Alternative: Docker Container
Consider running in a Docker container with all dependencies pre-installed:

```dockerfile
FROM openjdk:21-jdk-slim
RUN apt-get update && apt-get install -y \
    libopencv-dev \
    python3-opencv
COPY . /app
WORKDIR /app
CMD ["./gradlew", "bootRun"]
```

## Notes
- Ensure your Java version (32-bit vs 64-bit) matches the OpenCV DLL architecture
- The error path shows you're using OpenJDK 21, which is compatible
- Windows 11 may require additional permissions for DLL loading