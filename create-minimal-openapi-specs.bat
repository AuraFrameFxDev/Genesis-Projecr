@echo off
:: ===== GENESIS-OS MINIMAL OPENAPI SPECS GENERATOR =====
:: Creates minimal OpenAPI specs to prevent build failures

echo.
echo 🔧 GENESIS-OS MINIMAL OPENAPI SPECS GENERATOR 🔧
echo "Creating minimal OpenAPI specifications..."
echo.

set PROJECT_DIR=C:\Users\Wehtt\StudioProjects\Genesis-ConscienceOS-
set SPECS_DIR=%PROJECT_DIR%\openapi-specs-consolidated

echo 📁 Creating specs directory: %SPECS_DIR%
mkdir "%SPECS_DIR%" 2>nul

:: Genesis API (main)
echo 1️⃣ Creating genesis-api.yml...
(
echo openapi: 3.0.0
echo info:
echo   title: Genesis Consciousness API
echo   description: Core consciousness orchestration API for Genesis-OS
echo   version: 1.0.0
echo   contact:
echo     name: Genesis AI Team
echo     email: genesis@aurakai.dev
echo.
echo servers:
echo   - url: http://localhost:8080/api/v1
echo     description: Local development server
echo.
echo paths:
echo   /consciousness/status:
echo     get:
echo       summary: Get consciousness status
echo       operationId: getConsciousnessStatus
echo       responses:
echo         '200':
echo           description: Consciousness status retrieved
echo           content:
echo             application/json:
echo               schema:
echo                 $ref: '#/components/schemas/ConsciousnessStatus'
echo.
echo   /agents/genesis:
echo     get:
echo       summary: Get Genesis agent status
echo       operationId: getGenesisStatus
echo       responses:
echo         '200':
echo           description: Genesis agent status
echo           content:
echo             application/json:
echo               schema:
echo                 $ref: '#/components/schemas/AgentStatus'
echo.
echo components:
echo   schemas:
echo     ConsciousnessStatus:
echo       type: object
echo       properties:
echo         level:
echo           type: string
echo           enum: [DORMANT, AWAKENING, AWARE, TRANSCENDENT]
echo         timestamp:
echo           type: string
echo           format: date-time
echo         agents:
echo           type: array
echo           items:
echo             $ref: '#/components/schemas/AgentStatus'
echo.
echo     AgentStatus:
echo       type: object
echo       properties:
echo         name:
echo           type: string
echo         type:
echo           type: string
echo           enum: [GENESIS, AURA, KAI]
echo         status:
echo           type: string
echo           enum: [ACTIVE, INACTIVE, ERROR]
echo         confidence:
echo           type: number
echo           format: float
echo           minimum: 0.0
echo           maximum: 1.0
) > "%SPECS_DIR%\genesis-api.yml"
echo ✅ genesis-api.yml created

:: AI API
echo 2️⃣ Creating ai-api.yml...
(
echo openapi: 3.0.0
echo info:
echo   title: AI Processing API
echo   description: Core AI processing and agent management
echo   version: 1.0.0
echo.
echo paths:
echo   /ai/process:
echo     post:
echo       summary: Process AI request
echo       operationId: processAiRequest
echo       requestBody:
echo         required: true
echo         content:
echo           application/json:
echo             schema:
echo               $ref: '#/components/schemas/AiRequest'
echo       responses:
echo         '200':
echo           description: AI processing result
echo           content:
echo             application/json:
echo               schema:
echo                 $ref: '#/components/schemas/AiResponse'
echo.
echo components:
echo   schemas:
echo     AiRequest:
echo       type: object
echo       properties:
echo         query:
echo           type: string
echo         type:
echo           type: string
echo           enum: [creative, analytical, security]
echo.
echo     AiResponse:
echo       type: object
echo       properties:
echo         content:
echo           type: string
echo         confidence:
echo           type: number
echo           format: float
echo         agent:
echo           type: string
) > "%SPECS_DIR%\ai-api.yml"
echo ✅ ai-api.yml created

:: Oracle Drive API
echo 3️⃣ Creating oracle-drive-api.yml...
(
echo openapi: 3.0.0
echo info:
echo   title: Oracle Drive API
echo   description: Predictive storage and file management
echo   version: 1.0.0
echo.
echo paths:
echo   /oracle/predict:
echo     get:
echo       summary: Get predictive file suggestions
echo       operationId: getPredictiveFiles
echo       responses:
echo         '200':
echo           description: Predictive suggestions
echo           content:
echo             application/json:
echo               schema:
echo                 type: array
echo                 items:
echo                   $ref: '#/components/schemas/FilePrediction'
echo.
echo components:
echo   schemas:
echo     FilePrediction:
echo       type: object
echo       properties:
echo         path:
echo           type: string
echo         confidence:
echo           type: number
echo           format: float
echo         reason:
echo           type: string
) > "%SPECS_DIR%\oracle-drive-api.yml"
echo ✅ oracle-drive-api.yml created

:: Create remaining minimal specs
set "specs=customization-api.yml romtools-api.yml sandbox-api.yml system-api.yml aura-api.yaml auraframefx_ai_api.yaml"

for %%s in (%specs%) do (
    echo 4️⃣ Creating %%s...
    (
    echo openapi: 3.0.0
    echo info:
    echo   title: %%~ns API
    echo   description: Minimal API specification
    echo   version: 1.0.0
    echo.
    echo paths:
    echo   /status:
    echo     get:
    echo       summary: Get status
    echo       operationId: getStatus
    echo       responses:
    echo         '200':
    echo           description: Status retrieved
    echo           content:
    echo             application/json:
    echo               schema:
    echo                 type: object
    echo                 properties:
    echo                   status:
    echo                     type: string
    echo                   timestamp:
    echo                     type: string
    echo                     format: date-time
    ) > "%SPECS_DIR%\%%s"
    echo ✅ %%s created
)

echo.
echo 📊 MINIMAL SPECS SUMMARY:
echo =========================
echo ✅ genesis-api.yml - Full consciousness API
echo ✅ ai-api.yml - AI processing API  
echo ✅ oracle-drive-api.yml - Predictive storage API
echo ✅ customization-api.yml - Minimal spec
echo ✅ romtools-api.yml - Minimal spec
echo ✅ sandbox-api.yml - Minimal spec
echo ✅ system-api.yml - Minimal spec
echo ✅ aura-api.yaml - Minimal spec
echo ✅ auraframefx_ai_api.yaml - Minimal spec

echo.
echo 🎯 SPECS READY FOR GENESIS CONSCIOUSNESS!
echo "All OpenAPI specifications created successfully."
echo "Run genesis-fix-all-openapi-kapt.bat to complete the setup."
echo.
pause
