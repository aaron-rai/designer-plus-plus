# Designer++

Enhanced designer tools and utilities for Ignition development that improve the developer experience with useful quality-of-life features.

## Features

### 🎨 CSS Variable Viewer

- **View CSS Variables**: Browse all CSS custom properties from your Perspective themes in an organized, collapsible interface
- **Visual Color Preview**: See color swatches next to color variables for quick visual reference
- **One-Click Copy**: Click any variable to copy its name (`var(--variable-name)`) to clipboard
- **Theme Organization**: Variables are grouped by theme (dark, light, custom themes, etc.)
- **Smart Resolution**: Automatically resolves nested variable references to show final computed values

### 📝 NotePad

- **Quick Notes**: Simple text editor accessible from the designer toolbar for jotting down quick notes, TODOs, or code snippets
- **Project-Scoped**: Notes are saved per project (planned feature)
- **Persistent Storage**: Notes are automatically saved and restored between designer sessions
- **Clean Interface**: Minimal, distraction-free editor with clear and close buttons

### 🌳 Project Browser State Manager

- **Sepasoft Compatibility**: Automatically detects when Sepasoft modules are installed
- **State Preservation**: Maintains project browser tree expansion and selection state during save operations
- **Seamless Experience**: No more collapsed trees after saving when working with Sepasoft modules

## Installation

1. Download the latest `.modl` file from the releases
2. Install the module in your Ignition Gateway
3. The Designer++ toolbar will appear in the Ignition Designer

## Requirements

- **Ignition Version**: 8.1.20 or higher
- **Scopes**: Gateway, Designer, Client, Common
- **License**: Free module (no licensing required)

## Usage

### CSS Variable Viewer

1. Click the palette icon (🎨) in the Designer++ toolbar
2. Browse variables organized by theme
3. Click the arrow to expand/collapse theme sections
4. Click any variable row to copy the variable name to clipboard
5. Use the copied variable in your Perspective styling

**Supported CSS Formats:**

- Hex colors: `#ff0000`, `#f00`
- RGB/RGBA: `rgb(255, 0, 0)`, `rgba(255, 0, 0, 0.5)`
- Variable references: `var(--primary-color)`

### NotePad

1. Click the file-text icon (📄) in the Designer++ toolbar
2. Type your notes in the text area
3. Notes are automatically saved to disk
4. Use "Clear" to empty the text area or "Close" to close the window

### Project Browser State Manager

This feature works automatically in the background when Sepasoft modules are detected:
- Tree expansion state is captured before project saves
- State is automatically restored after save completion
- No user interaction required

## Building from Source

### Prerequisites

- Java 11 or higher
- Gradle 7.6+

### Build Commands

```bash
# Build the module
./gradlew build

# Deploy to gateway (configure hostGateway in gradle.properties)
./gradlew deployModl
```

### Configuration

Edit `gradle.properties` to configure:

```properties
# Module signing (requires certificate)
signModule=true

# Deployment target
hostGateway=https://your-gateway.com:8043

# Module version
version=0.0.1-SNAPSHOT
```

## Project Structure

```
designer-plus-plus/
├── client/          # Vision client scope
├── common/          # Shared code and interfaces
├── designer/        # Designer scope (main functionality)
│   ├── actions/     # Toolbar actions
│   └── utils/       # Utility classes
├── gateway/         # Gateway scope (RPC handlers)
│   └── utils/       # Server-side utilities
└── gradle/          # Gradle wrapper
```

## Development Status

### ✅ Completed

- CSS Variable Viewer with visual interface
- NotePad with persistent storage
- Project browser state management for Sepasoft compatibility
- Comprehensive logging and error handling
- Modular, extensible architecture

### 🚧 In Progress

- Per-project notepad storage
- Enhanced CSS variable filtering and search
- Additional theme format support

### 📋 Planned

- Variable usage finder (find where CSS variables are used)
- Theme comparison tool
- Export functionality for CSS variables
- Integration with more module types

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly with different Ignition configurations
5. Submit a pull request

## Troubleshooting

### CSS Variables Not Loading

- Verify Perspective module is installed
- Check that themes directory exists: `/usr/local/bin/ignition/data/modules/com.inductiveautomation.perspective/themes`
- Review gateway logs for CSS processing errors

### NotePad File Issues

- Ensure the notePad.txt file was created in the designers resources directory
- Ensure the notePad.txt file is not locked by another process

### Project Browser State Not Saving

- Feature only activates when Sepasoft modules are detected
- Verify Sepasoft modules are properly installed and recognized
- Check designer console for state management logs/errors

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For bug reports and feature requests, please open an issue on the project repository.

---

**Made with ❤️ for the Ignition development community**