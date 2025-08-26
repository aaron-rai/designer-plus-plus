package org.dev.arai.designerpp.actions;

import org.dev.arai.designerpp.common.DesignerPlusPlusConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inductiveautomation.ignition.client.gateway_interface.ModuleRPCFactory;
import com.inductiveautomation.ignition.client.util.action.BaseAction;
import com.inductiveautomation.ignition.designer.model.DesignerContext;

import static com.inductiveautomation.ignition.common.BundleUtil.i18n;

import javax.swing.Icon;

public class CleanParamsAction extends BaseAction {
	private static final Logger logger = LoggerFactory.getLogger(DesignerPlusPlusConstants.MODULE_ID + ".CleanParamsAction");
	private final DesignerContext context;


	public CleanParamsAction(DesignerContext context, Icon icon) {
		super(i18n("designerpp.Action.ClearParams.Name"), icon);
		this.context = context;
		putValue(SHORT_DESCRIPTION, i18n("designerpp.Action.ClearParams.Description"));
		logger.debug("Clean Parameters Action initialized");
	}

	@Override
	public void actionPerformed(java.awt.event.ActionEvent e) {
		logger.debug("Clean Parameters Action performed");
	}
}