# Complete OpenCV 4.9.0 Fix for Windows

## Problem Summary
The application is failing with `org.sikuli.script.SikuliXception: loadlib: opencv_java490.dll not in any libs folder`

This occurs because:
1. SikuliX 2.0.5 expects OpenCV 4.9.0 (`opencv_java490.dll`)
2. There's a version mismatch between different dependencies
3. The native Windows DLL is not found in the java.library.path

## Solution Steps

### Step 1: Run the Setup Script
First, run the automated setup script to download and install OpenCV 4.9.0:

```batch
cd claude-automator
setup-opencv-490.bat
```

This script will:
- Download OpenCV 4.9.0 native libraries for Windows
- Extract and copy `opencv_java490.dll` to the correct locations
- Download and install OpenBLAS dependencies
- Set up the `lib` directory structure

### Step 2: Clean and Rebuild
After running the setup script:

```batch
gradlew clean
gradlew build
```

### Step 3: Run the Application
Use the provided run script:

```batch
run-claude-automator.bat
```

Or run directly with Gradle:

```batch
gradlew bootRun
```

## What Has Been Fixed

### 1. Build.gradle Updates
- Excluded transitive JavaCV dependencies from Brobot to avoid conflicts
- Added explicit OpenCV 4.9.0 and OpenBLAS dependencies for Windows x64
- Configured java.library.path to include `lib` and `lib/natives/windows-x86_64`
- Added environment variables for SIKULIXLIBS

### 2. Native Library Setup
- Created `setup-opencv-490.bat` to automatically download OpenCV 4.9.0
- Downloads the exact version SikuliX expects: `opencv_java490.dll`
- Includes OpenBLAS dependencies required by OpenCV

### 3. Runtime Configuration
- Created `run-claude-automator.bat` with proper environment setup
- Configured multiple library paths for native DLL loading
- Set DPI-related system properties for proper scaling

## Manual Alternative

If the automated setup doesn't work, manually download and place the DLLs:

1. Download: https://repo1.maven.org/maven2/org/bytedeco/opencv/4.9.0-1.5.10/opencv-4.9.0-1.5.10-windows-x86_64.jar
2. Extract the JAR (it's a ZIP file)
3. Copy all DLLs from `org/bytedeco/opencv/windows-x86_64/` to:
   - `claude-automator/lib/`
   - `C:\Users\jspin\.jdks\openjdk-21.0.1\bin\`

## Troubleshooting

### If you still get the error:

1. **Check if running as 64-bit Java:**
   ```batch
   java -version
   ```
   Should show "64-Bit" in the output

2. **Set SIKULIXLIBS environment variable:**
   ```batch
   set SIKULIXLIBS=C:\Users\jspin\Documents\brobot_parent\claude-automator\lib
   ```

3. **Add to PATH:**
   ```batch
   set PATH=C:\Users\jspin\Documents\brobot_parent\claude-automator\lib;%PATH%
   ```

4. **Run as Administrator:**
   Some system directories require admin privileges to copy DLLs

5. **Check for conflicting OpenCV versions:**
   ```batch
   where opencv*.dll
   ```
   Remove any conflicting versions from PATH

## Verification

To verify the fix worked:
1. The application should start without UnsatisfiedLinkError
2. You should see SikuliX initialization messages
3. The Spring Boot application should fully start

## Dependencies Added

```gradle
implementation 'org.bytedeco:javacv:1.5.10'
implementation 'org.bytedeco:opencv:4.9.0-1.5.10'
implementation 'org.bytedeco:opencv:4.9.0-1.5.10:windows-x86_64'
implementation 'org.bytedeco:openblas:0.3.26-1.5.10'
implementation 'org.bytedeco:openblas:0.3.26-1.5.10:windows-x86_64'
```

These ensure the correct OpenCV version is available at runtime.