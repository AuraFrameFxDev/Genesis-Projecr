@echo off
:: ===== GENESIS-OS OPENAPI DEBUG HELPER =====
:: Diagnoses path and URI issues

echo.
echo 🔍 GENESIS-OS OPENAPI DIAGNOSTIC TOOL 🔍
echo "Analyzing OpenAPI configuration and paths..."
echo.

set PROJECT_DIR=C:\Users\Wehtt\StudioProjects\Genesis-ConscienceOS-
set SPECS_DIR=%PROJECT_DIR%\openapi-specs-consolidated
set OUTPUT_DIR=%PROJECT_DIR%\app\build\generated\openapi

echo 📋 DIAGNOSTIC REPORT:
echo =====================

:: Check project structure
echo.
echo 1️⃣ PROJECT STRUCTURE:
if exist "%PROJECT_DIR%" (
    echo ✅ Project directory exists: %PROJECT_DIR%
) else (
    echo ❌ Project directory missing: %PROJECT_DIR%
)

if exist "%PROJECT_DIR%\app\build.gradle.kts" (
    echo ✅ App build.gradle.kts exists
) else (
    echo ❌ App build.gradle.kts missing
)

if exist "%PROJECT_DIR%\gradlew.bat" (
    echo ✅ Gradle wrapper exists
) else (
    echo ❌ Gradle wrapper missing
)

:: Check OpenAPI specs
echo.
echo 2️⃣ OPENAPI SPECIFICATIONS:
if exist "%SPECS_DIR%" (
    echo ✅ Specs directory exists: %SPECS_DIR%
    
    set /a spec_count=0
    for %%f in ("%SPECS_DIR%\*.yml" "%SPECS_DIR%\*.yaml") do (
        if exist "%%f" (
            echo ✅ Found: %%~nxf
            set /a spec_count+=1
        )
    )
    
    if %spec_count% GTR 0 (
        echo ✅ Total specs found: %spec_count%
    ) else (
        echo ⚠️  No .yml/.yaml files found in specs directory
    )
) else (
    echo ❌ Specs directory missing: %SPECS_DIR%
)

:: Check output directory
echo.
echo 3️⃣ OUTPUT DIRECTORY:
if exist "%OUTPUT_DIR%" (
    echo ✅ Output directory exists: %OUTPUT_DIR%
    dir "%OUTPUT_DIR%" /a /s | find /c "File(s)" > temp_count.txt
    set /p file_count=<temp_count.txt
    del temp_count.txt 2>nul
    echo 📊 Generated files count: %file_count%
) else (
    echo ⚠️  Output directory missing: %OUTPUT_DIR%
)

:: Test URI conversion
echo.
echo 4️⃣ URI PATH TESTING:
set TEST_FILE=%SPECS_DIR%\genesis-api.yml
if exist "%TEST_FILE%" (
    echo ✅ Test file exists: %TEST_FILE%
    echo 🔗 Original path: %TEST_FILE%
    
    :: Simulate the URI conversion that Gradle will do
    set "FIXED_PATH=%TEST_FILE:\=/%"
    echo 🔗 Forward slash path: %FIXED_PATH%
    echo 🔗 Expected URI: file:///%FIXED_PATH%
) else (
    echo ❌ Test file missing: %TEST_FILE%
)

:: Check Java/Gradle processes
echo.
echo 5️⃣ PROCESS CHECK:
echo 🔍 Active Java/Gradle processes:
tasklist /fi "imagename eq java.exe" /fo table 2>nul | find "java.exe" >nul
if %errorlevel% equ 0 (
    echo ⚠️  Java processes running - may cause file locks
    tasklist /fi "imagename eq java.exe" /fo csv | find "java.exe"
) else (
    echo ✅ No Java processes detected
)

tasklist /fi "imagename eq kotlin-compiler-daemon.exe" /fo table 2>nul | find "kotlin-compiler-daemon.exe" >nul
if %errorlevel% equ 0 (
    echo ⚠️  Kotlin compiler daemon running - may cause file locks
) else (
    echo ✅ No Kotlin compiler daemon detected
)

:: Gradle project status
echo.
echo 6️⃣ GRADLE PROJECT STATUS:
pushd "%PROJECT_DIR%" >nul 2>&1
if exist "gradlew.bat" (
    echo 🧪 Testing Gradle configuration...
    call gradlew.bat tasks --group=openapi --quiet 2>temp_gradle_output.txt
    if %errorlevel% equ 0 (
        echo ✅ Gradle OpenAPI tasks available:
        type temp_gradle_output.txt 2>nul | find "openapi" 2>nul
    ) else (
        echo ❌ Gradle configuration issues detected:
        type temp_gradle_output.txt 2>nul
    )
    del temp_gradle_output.txt 2>nul
) else (
    echo ❌ Cannot test - gradlew.bat missing
)
popd >nul 2>&1

echo.
echo 🎯 RECOMMENDED ACTIONS:
echo =======================
if not exist "%SPECS_DIR%\genesis-api.yml" (
    echo ❗ Create missing OpenAPI spec files
)
if exist "%OUTPUT_DIR%" (
    echo ❗ Clear output directory: %OUTPUT_DIR%
)
echo ❗ Run: genesis-fix-all-openapi-kapt.bat
echo ❗ Restart Android Studio
echo ❗ Sync project with Gradle files

echo.
echo 📝 REPORT COMPLETE - GENESIS CONSCIOUSNESS AWAITS!
pause
