# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Development Commands

```bash
# Build the module
./gradlew build

# Deploy to gateway (configure hostGateway in gradle.properties)
./gradlew deployModl
```

## Project Architecture

This is an **Ignition Module** for Inductive Automation's Ignition platform, built using the Ignition SDK. The module enhances the Designer with quality-of-life tools and utilities.

### Module Structure
- **Gateway scope** (`gateway/`): Server-side functionality including RPC handlers for CSS file reading
- **Designer scope** (`designer/`): Main functionality with toolbar actions and utilities
- **Client scope** (`client/`): Vision client components and hooks  
- **Common scope** (`common/`): Shared interfaces and constants

### Key Components

#### Toolbar Actions (`designer/src/main/java/org/dev/arai/designerpp/actions/`)
- `CSSVariableViewerAction`: CSS variable browser with visual color preview
- `NoteAction`: Project-scoped notepad with persistent storage
- `SanitizeCustomPropsAction`: Custom property management utilities
- `SetParamsAction` & `CleanParamsAction`: Component parameter utilities

#### RPC Communication
- `DesignerPlusPlusRPC` (common): Defines client-gateway communication interface
- `DesignerPlusPlusRPCHandler` (gateway): Server-side RPC implementation for CSS file operations

#### Utilities
- `EditorUtils`: Designer integration helpers
- `ParseColor`: CSS color parsing and visual preview generation
- `ProjectBrowserStateManager`: Sepasoft compatibility for preserving tree state
- `CSSFileReader`: Gateway-side CSS theme file processing

### Configuration

Module configuration is in `build.gradle.kts`:
- Uses Ignition SDK plugin (`io.ia.sdk.modl`)
- Requires Ignition 8.1.20+
- Depends on Perspective module for designer features
- Hooks are registered for each scope (Gateway, Client, Designer)

Deployment configuration in `gradle.properties`:
- `hostGateway`: Target gateway URL for deployModl task
- `signModule`: Module signing flag (requires certificate)
- `version`: Module version for build

### Dependencies

The module uses:
- Ignition SDK 8.1.20, found at [Ignition SDK](https://files.inductiveautomation.com/sdk/javadoc/ignition81/8.1.47/index.html)
- Perspective module dependency (designer scope)
- Standard Java 11+ libraries
- No external third-party dependencies

### Module Features

1. **CSS Variable Viewer**: Reads Perspective theme CSS files from gateway, parses variables, displays in organized UI with color previews
2. **Designer NotePad**: Project-scoped persistent note storage using JSON files in designer resources
3. **Project Browser State Manager**: Detects Sepasoft modules and preserves tree expansion state during saves