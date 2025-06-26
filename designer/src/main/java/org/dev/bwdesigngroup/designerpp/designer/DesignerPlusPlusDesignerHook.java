package org.dev.bwdesigngroup.designerpp.designer;

import org.dev.bwdesigngroup.designerpp.actions.CSSVariableViewerAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import com.inductiveautomation.ignition.client.icons.VectorIcons;
import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.designer.gui.DesignerToolbar;
import com.inductiveautomation.ignition.designer.model.AbstractDesignerModuleHook;
import com.inductiveautomation.ignition.designer.model.DesignerContext;
import com.jidesoft.action.CommandBar;


/**
 * CSSVariableViewerDesignerHook is the main entry point for the CSS Variable Viewer module in the Ignition Designer.
 * It initializes the module, registers toolbars, and provides access to the designer context.
 * 
 * @author Aaron Rai
 */
public class DesignerPlusPlusDesignerHook extends AbstractDesignerModuleHook {

    private static final Logger logger = LoggerFactory.getLogger(DesignerPlusPlusDesignerHook.class);
    public static DesignerContext context;

    /**
     * Default constructor for the CSSVariableViewerDesignerHook.
     */
    @Override
    public void startup(DesignerContext context, LicenseState activationState) throws Exception {
        logger.info("Designer++ Designer Hook started");
        BundleUtil.get().addBundle("designerpp", this.getClass(), "designer");
        DesignerPlusPlusDesignerHook.context = context;
    }

    /**
     * Get Module Toolbars
     * 
     * @return A list of CommandBar instances representing the toolbars for this module.
     */
    @Override
    public List<CommandBar> getModuleToolbars() {
        List<CommandBar> toolbars = new ArrayList<>();

        DesignerToolbar toolbar = new DesignerToolbar("CSSVariableViewer", "cssvariableviewer.Toolbar.Name");
        CSSVariableViewerAction cssAction = new CSSVariableViewerAction(
            context,
            VectorIcons.getInteractive("palette")
        );

        toolbar.addButton(cssAction);

        toolbars.add(toolbar);
        return toolbars;
    }

    /**
     * Returns the designer context for this module.
     * 
     * @return The DesignerContext instance.
     */
    @Override
    public void shutdown() {
        logger.info("Designer++ Designer Hook shutting down");
    }
}
