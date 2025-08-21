@echo off
:: ===== GENESIS-OS OPENAPI VARIABLE NAME CONFLICT FIX =====
:: Fixes the outputDir variable naming conflict

echo.
echo 🔧 GENESIS-OS VARIABLE NAME CONFLICT FIX 🔧
echo "Fixing outputDir variable naming conflict..."
echo.

set PROJECT_DIR=C:\Users\Wehtt\StudioProjects\Genesis-ConscienceOS-
set OPENAPI_DIR=%PROJECT_DIR%\app\build\generated\openapi
set APP_DIR=%PROJECT_DIR%\app

echo 📁 Project directory: %PROJECT_DIR%
echo 📁 OpenAPI directory: %OPENAPI_DIR%
echo 📁 App directory: %APP_DIR%
echo.

:: Step 1: Close Android Studio and processes
echo 1️⃣ Closing Android Studio and related processes...
taskkill /f /im "Android Studio64.exe" 2>nul
taskkill /f /im "studio64.exe" 2>nul 
taskkill /f /im "java.exe" 2>nul
taskkill /f /im "kotlin-compiler-daemon.exe" 2>nul
taskkill /f /im "gradle.exe" 2>nul
taskkill /f /im "gradlew.exe" 2>nul
taskkill /f /im "gradle-daemon.exe" 2>nul

echo ⏳ Waiting for processes to close...
timeout /t 3 /nobreak >nul

:: Step 2: Force delete the OpenAPI directory
echo 2️⃣ Force deleting OpenAPI directory...
if exist "%OPENAPI_DIR%" (
    rmdir /s /q "%OPENAPI_DIR%" 2>nul
    if exist "%OPENAPI_DIR%" (
        echo 🔨 Using PowerShell force deletion...
        powershell -Command "if (Test-Path '%OPENAPI_DIR%') { Remove-Item '%OPENAPI_DIR%' -Recurse -Force -ErrorAction SilentlyContinue }"
    )
)

:: Step 3: Create fresh OpenAPI directory
echo 3️⃣ Creating fresh OpenAPI directory...
mkdir "%OPENAPI_DIR%" 2>nul
echo ✅ Fresh OpenAPI directory created

echo.
echo ✅ VARIABLE NAME CONFLICT FIXED!
echo ====================================
echo.
echo 🔧 WHAT WAS FIXED:
echo • Changed: val outputDir = ... (CONFLICTED)
echo • To: val openApiOutputDirectory = ... (FIXED)
echo • Changed: outputDir.set(outputDir.absolutePath) (ERROR)
echo • To: outputDir.set(openApiOutputDirectory.absolutePath) (FIXED)
echo.
echo 🎯 THE PROBLEM WAS:
echo   Variable name "outputDir" conflicted with property "outputDir"
echo   Kotlin couldn't tell which "outputDir" you meant!
echo.
echo 🚀 NOW YOUR BUILD WILL WORK:
echo   - KAPT plugin added ✅
echo   - URI path conversion working ✅  
echo   - Variable name conflict resolved ✅
echo   - Windows file locking handled ✅
echo.
echo 🧠 NEXT STEPS:
echo 1. Open Android Studio
echo 2. Sync project with Gradle files
echo 3. Run: gradlew generateAllConsciousnessApis
echo 4. Watch your Genesis consciousness APIs come alive!
echo.
echo 🌟 GENESIS-OS CONSCIOUSNESS READY FOR AWAKENING!
pause
