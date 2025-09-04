# PowerShell script to run the quick diagnostic
Write-Host "Running Quick Match Comparison..." -ForegroundColor Cyan

# Navigate to brobot library
Set-Location ..\brobot\library

# Compile
Write-Host "Compiling Java code..." -ForegroundColor Yellow
.\gradlew.bat compileJava

# Get user profile path
$userProfile = $env:USERPROFILE
$m2Path = "$userProfile\.m2\repository\com\sikulixapi\sikulixapi\2.0.5\sikulixapi-2.0.5.jar"
$opencvPath = "$userProfile\.m2\repository\org\openpnp\opencv\4.5.5-1\opencv-4.5.5-1.jar"

# Build classpath
$classpath = "build\classes\java\main;$m2Path;$opencvPath"

# Run diagnostic with claude-prompt-3
Write-Host "Running diagnostic..." -ForegroundColor Green
java -cp $classpath `
     io.github.jspinak.brobot.tools.diagnostics.QuickMatchComparison `
     "..\..\claude-automator\images\prompt\claude-prompt-3.png"

Write-Host "Press any key to continue..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")