# OpenAPI Specs Consolidation

## Problem Fixed

The Genesis project had **MULTIPLE CONFLICTING OPENAPI SOURCES** scattered across:

1. `api-spec/` → 7 specs (.yml format)
2. `openapi/specs/` → 10 specs (.yml + .yaml formats)
3. `openapi.backup/` → 3 backup specs
4. `openapi.backup.app/` → 1 backup spec
5. `app/src/main/openapi/` → 2 specs + backup

## Solution: Consolidated Directory

All OpenAPI specifications have been consolidated into:

```
openapi-specs-consolidated/
├── genesis-api.yml          # Main Genesis Protocol API (OpenAPI 3.1.0)
├── ai-api.yml              # AI Content & Agents API  
├── oracle-drive-api.yml    # Oracle Drive API
├── customization-api.yml   # UI Theme Management API
├── romtools-api.yml        # ROM Tools & DataveinConstructor API
├── sandbox-api.yml         # UI Component Testing API
├── system-api.yml          # System Customization & Tasks API
├── api-spec.yaml           # Generic AuraFrameFX API
├── aura-api.yaml           # AuraOS Backend Services API
└── auraframefx_ai_api.yaml # AuraFrameFx AI Content Generation API
```

## Key Changes Made

1. **Standardized to OpenAPI 3.1.0** (latest version)
2. **Eliminated duplicates** and version conflicts
3. **Consolidated all variants** into single source of truth
4. **Updated build configurations** to point to consolidated directory
5. **Preserved all functionality** while removing conflicts

## Usage

- **Primary API Spec**: `genesis-api.yml` for main Genesis Protocol
- **All specs are now OpenAPI 3.1.0 compatible**
- **Single directory** for all API specifications
- **No more conflicts** between different versions

## Next Steps

1. Update your build configurations to point to `openapi-specs-consolidated/`
2. Remove old duplicate directories after testing
3. Use this as the single source of truth for all OpenAPI specs

## Build Configuration

The build system should now reference:

```
openApiGeneratorConfig.inputSpec = "$projectDir/openapi-specs-consolidated/genesis-api.yml"
```

All conflicts resolved! 🎉
