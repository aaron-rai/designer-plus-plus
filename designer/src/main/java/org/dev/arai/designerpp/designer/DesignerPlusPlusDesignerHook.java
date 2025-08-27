package org.dev.arai.designerpp.designer;

import org.dev.arai.designerpp.actions.CSSVariableViewerAction;
import org.dev.arai.designerpp.actions.CleanParamsAction;
import org.dev.arai.designerpp.actions.NoteAction;
import org.dev.arai.designerpp.actions.SanitizeCustomPropsAction;
import org.dev.arai.designerpp.actions.SetParamsAction;
import org.dev.arai.designerpp.common.DesignerPlusPlusConstants;
import org.dev.arai.designerpp.utils.ProjectBrowserStateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.inductiveautomation.ignition.client.icons.VectorIcons;
import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.modules.ModuleInfo;
import com.inductiveautomation.ignition.designer.gui.DesignerToolbar;
import com.inductiveautomation.ignition.designer.model.AbstractDesignerModuleHook;
import com.inductiveautomation.ignition.designer.model.DesignerContext;
import com.inductiveautomation.ignition.designer.model.SaveContext;
import com.inductiveautomation.ignition.designer.project.DesignableProject;
import com.jidesoft.action.CommandBar;


/**
 * CSSVariableViewerDesignerHook is the main entry point for the CSS Variable Viewer module in the Ignition Designer.
 * It initializes the module, registers toolbars, and provides access to the designer context.
 * 
 * @author Aaron Rai
 */
public class DesignerPlusPlusDesignerHook extends AbstractDesignerModuleHook {

    private static final Logger logger = LoggerFactory.getLogger(DesignerPlusPlusConstants.MODULE_ID + ".DesignerHook");
    public static DesignerContext context;
    private ProjectBrowserStateManager browserStateManager;

    /**
     * Default constructor for the CSSVariableViewerDesignerHook.
     * Initializes the designer context and perspective module.
     * @param context The DesignerContext instance for this module.
     * @param activationState The LicenseState for the module.
     */
    @Override
    public void startup(DesignerContext context, LicenseState activationState) throws Exception {
        logger.debug("Designer++ Designer Hook started");
        
        DesignerPlusPlusDesignerHook.context = context;
        browserStateManager = new ProjectBrowserStateManager(context);

        BundleUtil.get().addBundle("designerpp", this.getClass(), "designer");

        String userHome = System.getProperty("user.home");
        File noteFile = new File(userHome, "notePad.json");
        
        if (!noteFile.exists()) {
            try {
                noteFile.createNewFile();
                logger.info("Created notePad.json file at: " + noteFile.getAbsolutePath());
            } catch (Exception e) {
                logger.error("Error creating notePad.json file: " + e.getMessage());
            }
        }
    }

    /**
     * Get Module Toolbars
     * 
     * @return A list of CommandBar instances representing the toolbars for this module.
     */
    @Override
    public List<CommandBar> getModuleToolbars() {
        List<CommandBar> toolbars = new ArrayList<>();

        DesignerToolbar toolbar = new DesignerToolbar("Designer++", "designerpp.Toolbar.Name");

        CSSVariableViewerAction cssAction = new CSSVariableViewerAction(
            context,
            VectorIcons.getInteractive("palette")
        );
        NoteAction noteAction = new NoteAction(
            context,
            VectorIcons.getInteractive("file-text")
        );
        CleanParamsAction cleanParams = new CleanParamsAction(
            context,
            VectorIcons.getInteractive("clear-erase")
        );
        SetParamsAction setParams = new SetParamsAction(
            context,
            VectorIcons.getInteractive("set-prop")
        );
        SanitizeCustomPropsAction sanitizeCustomProps = new SanitizeCustomPropsAction(
            context,
            VectorIcons.getInteractive("property-transient")
        );

        // toolbar.addButton(setParams);
        // toolbar.addButton(cleanParams);
        toolbar.addButton(sanitizeCustomProps);
        toolbar.addSeparator();
        toolbar.addButton(cssAction);
        toolbar.addButton(noteAction);

        toolbars.add(toolbar);
        return toolbars;
    }
    
    /**
     * Gets Module Information from the designer.
     * 
     * @return A list of ModuleInfo instances representing the modules for this designer.
     */
    public List <ModuleInfo> getDesignerModules() {
        return context.getModules();
    }


    /**
     * Gets the current project in the designer.
     * 
     * @return The DesignableProject instance representing the current project.
     */
    public static DesignableProject getDesignerProject() {
        return context.getProject();
    }
    
    /**
     * Checks if any Sepasoft modules are installed in the designer.
     * 
     * @return true if at least one Sepasoft module is detected, false otherwise
     */
    private boolean isSepasoftInstalled() {
        List<String> sepasoftIndicators = Arrays.asList(
            "sepasoft", "mes", "track", "spc", "oee", "downtime", "scheduler", "production", "batch"
        );
        
        return getDesignerModules().stream()
            .anyMatch(module -> {
                String moduleName = module.getName().toLowerCase();
                
                return sepasoftIndicators.stream()
                    .anyMatch(indicator ->
                        moduleName.contains(indicator)
                    );
            });
    }

    @Override
    public void notifyProjectSaveStart(SaveContext save) {
        logger.debug("Project save started, checking if Sepasoft modules are present");
        
        if (isSepasoftInstalled()) {
            logger.trace("Sepasoft module detected, capturing project browser state");
            if (browserStateManager != null) {
                try {
                    browserStateManager.captureState();
                } catch (Exception ex) {
                    logger.error("Error capturing browser state", ex);
                }
            }
        }
    }

    @Override
    public void notifyProjectSaveDone() {
        logger.debug("Project save completed, checking if we need to restore browser state");
        
        if (isSepasoftInstalled() && browserStateManager != null) {
            logger.trace("Restoring project browser state");
            try {
                browserStateManager.restoreState();
            } catch (Exception ex) {
                logger.error("Error restoring browser state", ex);
            }
        }
    }

    /**
     * Returns the designer context for this module.
     * 
     * @return The DesignerContext instance.
     */
    @Override
    public void shutdown() {
        logger.debug("Designer++ Designer Hook shutting down");
        if (browserStateManager != null) {
            browserStateManager = null;
            logger.debug("BrowserStateManager reference cleared");
        }
    }
}
