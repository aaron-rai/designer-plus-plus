package org.dev.arai.designerpp.common;

import java.util.List;

/**
 * CSSVariableViewerConstants contains constants used throughout the CSS Variable Viewer module.
 * It includes the module ID and the path to the themes directory where CSS files are stored.
 * 
 * @author Aaron Rai
 */
public class DesignerPlusPlusConstants {
    public static final String MODULE_ID = "DesignerPlusPlus"; // Must match build.gradle.kts module ID
    public static final String THEMES_DIRECTORY = "/usr/local/bin/ignition/data/modules/com.inductiveautomation.perspective/themes";
    public static final String NOTEPAD_FILE_LOCATION = "/Applications/Designer Launcher.app/Contents/Resources/notePad.json";
    public static final List<String> IGNITION_DEFAULT_THEMES = List.of(
        "dark", "dark-cool", "dark-warm", "light", "light-cool", "light-warm", "sepasoft-light", "sepasoft-dark"
    );
    public static final String PERSPECTIVE_MODULE_ID = "com.inductiveautomation.perspective";
}
