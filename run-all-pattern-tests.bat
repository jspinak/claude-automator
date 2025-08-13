@echo off
echo Running All Pattern Matching Debug Tests...
echo ==========================================
echo.

echo 1. Pattern Integrity Check
echo --------------------------
gradlew.bat test --tests "com.claude.automator.debug.PatternIntegrityCheckTest"

echo.
echo 2. Extract Matched Regions
echo --------------------------
echo Make sure your target application is visible!
timeout /t 3
gradlew.bat test --tests "com.claude.automator.debug.ExtractMatchedRegionTest"

echo.
echo 3. SikuliX IDE Exact Replication
echo ---------------------------------
echo Make sure your screen looks EXACTLY as in SikuliX IDE!
timeout /t 3
gradlew.bat test --tests "com.claude.automator.debug.SikuliXIDEExactReplicationTest"

echo.
echo ==========================================
echo All tests complete!
echo.
echo Check these folders for results:
echo   - pattern-integrity-test/
echo   - matched-regions-comparison/
echo   - ide-replication-test/
echo.
pause