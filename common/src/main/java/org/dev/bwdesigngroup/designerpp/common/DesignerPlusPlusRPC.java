package org.dev.bwdesigngroup.designerpp.common;

import com.inductiveautomation.ignition.common.gson.JsonObject;

/**
 * CSSVariableViewerRPC is an interface that defines the methods for retrieving CSS variable data.
 * It is used by the Ignition Designer to fetch CSS variables and their values.
 * 
 * @author Aaron Rai
 */
public interface DesignerPlusPlusRPC {
	
	JsonObject getCSSData();

}
