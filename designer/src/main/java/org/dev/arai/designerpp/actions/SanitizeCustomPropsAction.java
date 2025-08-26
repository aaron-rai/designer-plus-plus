package org.dev.arai.designerpp.actions;

import org.dev.arai.designerpp.common.DesignerPlusPlusConstants;
import org.dev.arai.designerpp.utils.EditorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inductiveautomation.ignition.client.gateway_interface.ModuleRPCFactory;
import com.inductiveautomation.ignition.client.util.action.BaseAction;
import com.inductiveautomation.ignition.common.gson.JsonObject;
import com.inductiveautomation.ignition.designer.model.DesignerContext;
import com.inductiveautomation.ignition.designer.tabbedworkspace.TabbedResourceWorkspace;
import com.inductiveautomation.perspective.common.api.PropertyKey;
import com.inductiveautomation.perspective.common.config.BindingConfig;
import com.inductiveautomation.perspective.common.config.PropertyConfig;
import com.inductiveautomation.perspective.common.config.PropertyConfigCollection;
import com.inductiveautomation.perspective.common.config.ViewConfig;
import com.inductiveautomation.perspective.designer.workspace.ViewResourceEditor;

import static com.inductiveautomation.ignition.common.BundleUtil.i18n;

import java.util.List;

import javax.swing.Icon;

public class SanitizeCustomPropsAction extends BaseAction {
	private static final Logger logger = LoggerFactory.getLogger(DesignerPlusPlusConstants.MODULE_ID + ".SanitizeCustomPropsAction");
	private final DesignerContext context;
	private EditorUtils editorUtils;


	public SanitizeCustomPropsAction(DesignerContext context, Icon icon) {
		super(i18n("designerpp.Action.SanitizeCustomProps.Name"), icon);
		this.context = context;
		this.editorUtils = new EditorUtils(context);
		putValue(SHORT_DESCRIPTION, i18n("designerpp.Action.SanitizeCustomProps.Description"));
		logger.debug("Sanitize Custom Props Action initialized");
	}

	@Override
	public void actionPerformed(java.awt.event.ActionEvent e) {
		logger.debug("Sanitize Custom Props Action performed");
		TabbedResourceWorkspace workspace = editorUtils.getWorkspace();
		ViewResourceEditor currentViewResourceEditor = editorUtils.getCurrentSelectedViewResourceEditor(workspace);
		currentViewResourceEditor.getWorkspace().commitAll();
		ViewConfig currentViewConfig = editorUtils.getCurrentSelectedViewConfig(currentViewResourceEditor);
		PropertyConfigCollection customPropsCollection = editorUtils.getCurrentSelectedPropertyConfigCollection(currentViewConfig);
		List<String> customPropsWithBindings = editorUtils.getCustomPropNamesWithBindings(customPropsCollection);
		editorUtils.setPrivate(customPropsWithBindings, customPropsCollection);
		currentViewResourceEditor.getWorkspace().getPropertyEditor().commitEdit();
		currentViewResourceEditor.refresh();
		logger.info("Custom properties with bindings were set to private and non-persistent, be sure to save your designer before committing.");
	}
}