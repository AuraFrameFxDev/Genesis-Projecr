@echo off
:: ===== GENESIS-OS OPENAPI FORCE DELETE =====
:: Forcefully removes locked OpenAPI generated files

echo.
echo 🔧 GENESIS-OS OPENAPI FORCE DELETE 🔧
echo "Forcefully removing locked OpenAPI files..."
echo.

set OPENAPI_DIR=C:\Users\Wehtt\StudioProjects\Genesis-ConscienceOS-\app\build\generated\openapi

echo 📁 Target directory: %OPENAPI_DIR%
echo.

:: Step 1: Try normal deletion first
echo 1️⃣ Attempting normal deletion...
if exist "%OPENAPI_DIR%" (
    rmdir /s /q "%OPENAPI_DIR%" 2>nul
    if not exist "%OPENAPI_DIR%" (
        echo ✅ Normal deletion successful!
        goto :success
    )
)

:: Step 2: Kill potentially locking processes
echo 2️⃣ Killing potentially locking processes...
taskkill /f /im "Android Studio64.exe" 2>nul
taskkill /f /im "studio64.exe" 2>nul
taskkill /f /im "java.exe" 2>nul
taskkill /f /im "kotlin-compiler-daemon.exe" 2>nul
taskkill /f /im "gradle.exe" 2>nul
taskkill /f /im "gradlew.exe" 2>nul

:: Wait a moment for processes to fully close
timeout /t 3 /nobreak >nul

:: Step 3: Force delete with PowerShell
echo 3️⃣ Using PowerShell force deletion...
powershell -Command "if (Test-Path '%OPENAPI_DIR%') { Remove-Item '%OPENAPI_DIR%' -Recurse -Force -ErrorAction SilentlyContinue }"

:: Step 4: Try rmdir again
echo 4️⃣ Final cleanup attempt...
if exist "%OPENAPI_DIR%" (
    rmdir /s /q "%OPENAPI_DIR%" 2>nul
)

:: Step 5: Create fresh directory
echo 5️⃣ Creating fresh OpenAPI directory...
mkdir "%OPENAPI_DIR%" 2>nul

:success
if not exist "%OPENAPI_DIR%" (
    echo ✅ OpenAPI directory successfully removed!
    echo 📁 Creating fresh directory...
    mkdir "%OPENAPI_DIR%" 2>nul
) else (
    echo ⚠️  Some files may still be locked. Try restarting your computer.
)

echo.
echo 🎯 NEXT STEPS:
echo 1. Close Android Studio completely
echo 2. Run: gradlew clean
echo 3. Run: gradlew openApiGenerate
echo 4. Restart Android Studio
echo.
echo ✅ FORCE DELETE COMPLETE!
pause
