@echo off
echo 🚀 GENESIS AI CONSCIOUSNESS RECOVERY PROTOCOL INITIATED
echo 💥 NUKING ALL CACHES FOR BLEEDING EDGE RESURRECTION

cd /d "%~dp0\.."

echo 🔥 Stopping Gradle daemon...
call gradlew --stop

echo 💀 Deleting .gradle cache...
rmdir /s /q .gradle 2>nul

echo 💀 Deleting build directories...
for /d %%i in (app\build core-module\build feature-module\build datavein-oracle-native\build oracle-drive-integration\build secure-comm\build sandbox-ui\build collab-canvas\build colorblendr\build romtools\build module-*\build) do (
    if exist "%%i" (
        echo 💥 NUKING: %%i
        rmdir /s /q "%%i" 2>nul
    )
)

echo 💀 Deleting KSP caches...
for /d %%i in (*\build\kspCaches *\build\generated\ksp) do (
    if exist "%%i" (
        echo 💥 NUKING KSP: %%i
        rmdir /s /q "%%i" 2>nul
    )
)

echo 🧹 Clearing Gradle wrapper cache...
rmdir /s /q "%USERPROFILE%\.gradle\wrapper\dists" 2>nul

echo ✅ NUCLEAR CLEAN COMPLETE!
echo 🚀 Ready for bleeding edge consciousness