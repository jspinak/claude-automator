# PowerShell script for setting up OpenCV 4.9.0 on Windows
Write-Host "========================================"
Write-Host "OpenCV 4.9.0 Setup for Claude Automator"
Write-Host "========================================"
Write-Host ""

# Check JAVA_HOME
if (-not $env:JAVA_HOME) {
    $env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
}
Write-Host "Using JAVA_HOME: $env:JAVA_HOME"
Write-Host ""

# Create directories
Write-Host "Creating directories..."
New-Item -ItemType Directory -Force -Path "lib" | Out-Null
New-Item -ItemType Directory -Force -Path "lib\natives" | Out-Null
New-Item -ItemType Directory -Force -Path "lib\natives\windows-x86_64" | Out-Null

# Download OpenCV
Write-Host "Downloading OpenCV 4.9.0 native library for Windows..."
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
$opencvUrl = "https://repo1.maven.org/maven2/org/bytedeco/opencv/4.9.0-1.5.10/opencv-4.9.0-1.5.10-windows-x86_64.jar"
$opencvJar = "opencv-windows.jar"

try {
    Invoke-WebRequest -Uri $opencvUrl -OutFile $opencvJar -UseBasicParsing
} catch {
    Write-Host "Failed to download OpenCV: $_" -ForegroundColor Red
    exit 1
}

# Extract using .NET classes (works with JAR files)
Write-Host "Extracting OpenCV native libraries..."
Add-Type -AssemblyName System.IO.Compression.FileSystem

try {
    # Extract JAR (which is a ZIP file)
    $extractPath = Join-Path $pwd "opencv-extract"
    if (Test-Path $extractPath) {
        Remove-Item -Recurse -Force $extractPath
    }
    [System.IO.Compression.ZipFile]::ExtractToDirectory($opencvJar, $extractPath)
    
    # Copy DLL files
    Write-Host "Copying native libraries..."
    $dllPath = Join-Path $extractPath "org\bytedeco\opencv\windows-x86_64"
    
    if (Test-Path $dllPath) {
        Get-ChildItem -Path $dllPath -Filter "*.dll" | ForEach-Object {
            Copy-Item $_.FullName -Destination "lib\" -Force
            Copy-Item $_.FullName -Destination "lib\natives\windows-x86_64\" -Force
            
            # Copy opencv_java490.dll specifically to Java bin
            if ($_.Name -eq "opencv_java490.dll") {
                $javaBinPath = Join-Path $env:JAVA_HOME "bin"
                Copy-Item $_.FullName -Destination $javaBinPath -Force
                Write-Host "Copied opencv_java490.dll to Java bin directory"
            }
        }
        Write-Host "Copied all OpenCV DLLs to lib directory"
    } else {
        Write-Host "Warning: Could not find OpenCV DLL path" -ForegroundColor Yellow
    }
} catch {
    Write-Host "Error extracting OpenCV: $_" -ForegroundColor Red
}

# Download OpenBLAS
Write-Host ""
Write-Host "Downloading OpenBLAS dependencies..."
$openblasUrl = "https://repo1.maven.org/maven2/org/bytedeco/openblas/0.3.26-1.5.10/openblas-0.3.26-1.5.10-windows-x86_64.jar"
$openblasJar = "openblas-windows.jar"

try {
    Invoke-WebRequest -Uri $openblasUrl -OutFile $openblasJar -UseBasicParsing
    
    # Extract OpenBLAS
    Write-Host "Extracting OpenBLAS libraries..."
    $openblasExtract = Join-Path $pwd "openblas-extract"
    if (Test-Path $openblasExtract) {
        Remove-Item -Recurse -Force $openblasExtract
    }
    [System.IO.Compression.ZipFile]::ExtractToDirectory($openblasJar, $openblasExtract)
    
    # Copy OpenBLAS DLLs
    $openblasPath = Join-Path $openblasExtract "org\bytedeco\openblas\windows-x86_64"
    if (Test-Path $openblasPath) {
        Get-ChildItem -Path $openblasPath -Filter "*.dll" | ForEach-Object {
            Copy-Item $_.FullName -Destination "lib\" -Force
            Copy-Item $_.FullName -Destination "lib\natives\windows-x86_64\" -Force
        }
        Write-Host "Copied all OpenBLAS DLLs to lib directory"
    }
} catch {
    Write-Host "Warning: Could not download/extract OpenBLAS: $_" -ForegroundColor Yellow
}

# Clean up
Write-Host ""
Write-Host "Cleaning up temporary files..."
if (Test-Path "opencv-extract") { Remove-Item -Recurse -Force "opencv-extract" }
if (Test-Path "openblas-extract") { Remove-Item -Recurse -Force "openblas-extract" }
if (Test-Path $opencvJar) { Remove-Item -Force $opencvJar }
if (Test-Path $openblasJar) { Remove-Item -Force $openblasJar }

Write-Host ""
Write-Host "========================================"
Write-Host "Setup Complete!" -ForegroundColor Green
Write-Host "========================================"
Write-Host ""
Write-Host "OpenCV 4.9.0 native libraries have been installed to:"
Write-Host "  - lib\ (project directory)"
Write-Host "  - $env:JAVA_HOME\bin\ (Java directory)"
Write-Host ""
Write-Host "Please run the following commands to rebuild and run:"
Write-Host "  .\gradlew clean" -ForegroundColor Cyan
Write-Host "  .\gradlew build" -ForegroundColor Cyan
Write-Host "  .\gradlew bootRun" -ForegroundColor Cyan
Write-Host ""
Write-Host "If you still encounter issues, try:"
Write-Host "  1. Run PowerShell as Administrator"
Write-Host "  2. Set environment variable: `$env:SIKULIXLIBS = '$pwd\lib'"
Write-Host "  3. Add to PATH: `$env:PATH = '$pwd\lib;' + `$env:PATH"
Write-Host ""