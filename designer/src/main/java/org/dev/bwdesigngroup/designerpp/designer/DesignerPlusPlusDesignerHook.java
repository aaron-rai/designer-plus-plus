package org.dev.bwdesigngroup.designerpp.designer;

import org.dev.bwdesigngroup.designerpp.actions.CSSVariableViewerAction;
import org.dev.bwdesigngroup.designerpp.actions.NoteAction;
import org.dev.bwdesigngroup.designerpp.common.DesignerPlusPlusConstants;
import org.dev.bwdesigngroup.designerpp.utils.ProjectBrowserStateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import com.inductiveautomation.ignition.client.icons.VectorIcons;
import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.modules.ModuleInfo;
import com.inductiveautomation.ignition.designer.gui.DesignerToolbar;
import com.inductiveautomation.ignition.designer.model.AbstractDesignerModuleHook;
import com.inductiveautomation.ignition.designer.model.DesignerContext;
import com.inductiveautomation.ignition.designer.model.SaveContext;
import com.jidesoft.action.CommandBar;


/**
 * CSSVariableViewerDesignerHook is the main entry point for the CSS Variable Viewer module in the Ignition Designer.
 * It initializes the module, registers toolbars, and provides access to the designer context.
 * 
 * @author Aaron Rai
 */
public class DesignerPlusPlusDesignerHook extends AbstractDesignerModuleHook {

    private static final Logger logger = LoggerFactory.getLogger(DesignerPlusPlusConstants.MODULE_ID + ".designerHook");
    public static DesignerContext context;
    private ProjectBrowserStateManager browserStateManager;

    /**
     * Default constructor for the CSSVariableViewerDesignerHook.
     */
    @Override
    public void startup(DesignerContext context, LicenseState activationState) throws Exception {
        logger.info("Designer++ Designer Hook started");
        BundleUtil.get().addBundle("designerpp", this.getClass(), "designer");
        DesignerPlusPlusDesignerHook.context = context;
        browserStateManager = new ProjectBrowserStateManager(context);
    }

    /**
     * Get Module Toolbars
     * 
     * @return A list of CommandBar instances representing the toolbars for this module.
     */
    @Override
    public List<CommandBar> getModuleToolbars() {
        List<CommandBar> toolbars = new ArrayList<>();

        DesignerToolbar toolbar = new DesignerToolbar("DesignerPlusPlus", "Toolbar.Name");

        CSSVariableViewerAction cssAction = new CSSVariableViewerAction(
            context,
            VectorIcons.getInteractive("palette")
        );
        NoteAction noteAction = new NoteAction(
            context,
            VectorIcons.getInteractive("file-text")
        );

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
     * Checks if any Sepasoft modules are installed in the designer.
     * 
     * @return true if at least one Sepasoft module is detected, false otherwise
     */
    private boolean isSepasoftInstalled() {
        return getDesignerModules().stream()
            .anyMatch(module -> module.getName().toLowerCase().contains("sepasoft"));
    }

    @Override
    public void notifyProjectSaveStart(SaveContext save) {
        logger.debug("Project save started, checking if Sepasoft modules are present");
        
        if (isSepasoftInstalled()) {
            logger.info("Sepasoft module detected, capturing project browser state");
            if (browserStateManager != null) {
                browserStateManager.captureState();
            }
        } else {
            logger.info("No Sepasoft modules detected, skipping browser state capture");
        }
    }

    @Override
    public void notifyProjectSaveDone() {
        logger.debug("Project save completed, checking if we need to restore browser state");
        
        if (isSepasoftInstalled() && browserStateManager != null) {
            logger.info("Restoring project browser state");
            browserStateManager.restoreState();
        }
    }

    /**
     * Returns the designer context for this module.
     * 
     * @return The DesignerContext instance.
     */
    @Override
    public void shutdown() {
        logger.info("Designer++ Designer Hook shutting down");
        if (browserStateManager != null) {
            browserStateManager = null;
        }
    }
}
