package org.dev.bwdesigngroup.designerpp.gateway;

import org.dev.bwdesigngroup.designerpp.common.DesignerPlusPlusConstants;
import org.dev.bwdesigngroup.designerpp.common.DesignerPlusPlusRPC;
import org.dev.bwdesigngroup.designerpp.gateway.utils.CSSFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inductiveautomation.ignition.common.gson.JsonObject;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;


/**
 * CSSVariableViewerRPCHandler is the server-side implementation of the CSSVariableViewerRPC interface.
 * It handles requests to retrieve CSS variable data from the themes directory and processes CSS files.
 * 
 * @author Aaron Rai
 */
public class DesignerPlusPlusRPCHandler implements DesignerPlusPlusRPC {
	private final Logger logger = LoggerFactory.getLogger(DesignerPlusPlusConstants.MODULE_ID + ".rpcHandler");
	private final GatewayContext context;

	/**
	 * Constructor for the CSSVariableViewerRPCHandler.
	 * 
	 * @param context The gateway context, used to interact with the Ignition Gateway.
	 */
	public DesignerPlusPlusRPCHandler(GatewayContext context) {
		this.context = context;
		logger.debug("Designer++ RPC Handler initialized");
	}

	/**
	 * Retrieves CSS data from the themes directory and processes it.
	 * 
	 * @return A JsonObject containing the CSS variables organized by theme.
	 */
	@Override
	public JsonObject getCSSData() {
		logger.debug("getCSSData called");
		CSSFileReader cssFileReader = new CSSFileReader();
		return cssFileReader.readCSSFiles(DesignerPlusPlusConstants.THEMES_DIRECTORY);
	}
}