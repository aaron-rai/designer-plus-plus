package org.dev.arai.designerpp.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.dev.arai.designerpp.common.DesignerPlusPlusConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inductiveautomation.ignition.common.gson.JsonArray;
import com.inductiveautomation.ignition.common.gson.JsonObject;
import com.inductiveautomation.ignition.common.gson.JsonPrimitive;
import com.inductiveautomation.ignition.designer.model.DesignerContext;
import com.inductiveautomation.ignition.designer.tabbedworkspace.ResourceEditor;
import com.inductiveautomation.ignition.designer.tabbedworkspace.TabbedResourceWorkspace;
import com.inductiveautomation.perspective.common.api.PropertyKey;
import com.inductiveautomation.perspective.common.config.PropertyConfigCollection;
import com.inductiveautomation.perspective.common.config.ViewConfig;
import com.inductiveautomation.perspective.designer.DesignerHook;
import com.inductiveautomation.perspective.designer.workspace.ViewResourceEditor;
import com.teamdev.jxbrowser.js.Json;
/**
 * EditorUtils provides utility methods for interacting with the editor workspace in the Ignition Designer.
 * It allows retrieval of the workspace, editors, and selected editor using reflection.
 */
public class EditorUtils {
	private final static Logger logger = LoggerFactory.getLogger(DesignerPlusPlusConstants.MODULE_ID + ".EditorUtils");
	private final DesignerContext context;
	private final DesignerHook perspective;

	public EditorUtils(DesignerContext context) {
		this.context = context;
		this.perspective = (DesignerHook) context.getModule(DesignerPlusPlusConstants.PERSPECTIVE_MODULE_ID);
	}

	/**
	 * Retrieves the workspace object from the perspective module.
	 * Falls back to reflection if direct casting fails.
	 * 
	 * @return The workspace object, or null if it could not be retrieved.
	 */
	public TabbedResourceWorkspace getWorkspace() {
		return perspective.getWorkspace();
	}

	/**
	 * Retrieves the editors from the workspace.
	 * 
	 * @param workspace The workspace object from which to retrieve editors.
	 * @return The editors object, or null if it could not be retrieved.
	 */
	public Collection<ResourceEditor> getEditors(TabbedResourceWorkspace workspace) {
		if (workspace != null) {
			return workspace.getEditors();
		}
		return null;
	}

	/**
	 * Retrieves the selected editor from the workspace.
	 * 
	 * @param workspace The workspace object from which to retrieve the selected editor.
	 * @return The selected editor object, or null if it could not be retrieved.
	 */
	public ResourceEditor<?> getSelectedEditor(TabbedResourceWorkspace workspace) {
		if (workspace != null) {
			return workspace.getSelectedEditor();
		}
		return null;
	}

	/**
	 * Convenience method to get the current selected editor directly.
	 * 
	 * @return The currently selected editor, or null if not available.
	 */
	public ResourceEditor<?> getCurrentSelectedEditor(TabbedResourceWorkspace workspace) {
		if (workspace != null) {
			return getSelectedEditor(workspace);
		}
		return null;
	}

	/**
	 * Convenience method to get all editors directly.
	 * 
	 * @return The editors object, or null if not available.
	 */
	public Collection<ResourceEditor> getAllEditors() {
		TabbedResourceWorkspace workspace = getWorkspace();
		if (workspace != null) {
			return getEditors(workspace);
		}
		return null;
	}

	/**
	 * Convenience method to get ViewResourceEditor from the current selected editor
	 * @return The ViewResourceEditor object, or null if not available.
	 */
	public ViewResourceEditor getCurrentSelectedViewResourceEditor(TabbedResourceWorkspace currentWorkspace) {
		ResourceEditor<?> selectedEditor = getCurrentSelectedEditor(currentWorkspace);
		if (selectedEditor instanceof ViewResourceEditor) {
			return (ViewResourceEditor) selectedEditor;
		}
		return null;
	}

	/**
	 * Convenience method to get the ViewConfig from the current selected editor.
	 * 
	 * @return The ViewConfig object, or null if not available.
	 */
	public ViewConfig getCurrentSelectedViewConfig(ViewResourceEditor currentViewResourceEditor) {
		if (currentViewResourceEditor != null) {
			return currentViewResourceEditor.getViewConfig();
		}
		return null;
	}

	/**
	 * Convenience method to get the PropertyConfigCollection from the current selected ViewConfig.
	 * 
	 * @return The PropertyConfigCollection object, or null if not available.
	 */
	public PropertyConfigCollection getCurrentSelectedPropertyConfigCollection(ViewConfig currentViewConfig) {
		if (currentViewConfig != null) {
			return currentViewConfig.propConfig;
		}
		return null;
	}

	/**
	 * Convenience method to get the current custom properties JSON object from the current selected ViewConfig.
	 * 
	 * @return The JSON object, or null if not available.
	 */
	public JsonObject getCurrentSelectedCustomProperties(ViewConfig currentViewConfig) {
		if (currentViewConfig != null) {
			JsonObject customProperties = currentViewConfig.custom;
			if (customProperties != null) {
				return customProperties;
			}
		}
		return null;
	}

	/**
	 * Convenience method to get a list of custom property names from the current
	 * selected ViewConfig that have enabled bindings.
	 *
	 * @param currentViewConfig The current ViewConfig.
	 * @return A list of custom property names with bindings, or an empty list if none.
	 */
	public List<String> getCustomPropNamesWithBindings(PropertyConfigCollection propConfigs) {
		if (propConfigs != null) {
			return propConfigs.stream()
					.filter(entry -> entry.getValue().hasEnabledBinding())
					.filter(entry -> !propConfigs.isSystem(entry.getKey()))
					.map(entry -> entry.getKey().toString())
					.collect(Collectors.toList());
		}
		return List.of();
	}

	/**
	 * Sets the properties as private and also sets the persistence flag to false.
	 * @param customProps The JSON object containing custom properties.
	 * @param propertyConfigCollection The PropertyConfigCollection to set the properties on.
	 */
	public void setPrivate(List<String> customProps, PropertyConfigCollection propertyConfigCollection) {
		if (customProps != null && propertyConfigCollection != null) {
			for (String key : customProps) {
				propertyConfigCollection.setPrivate(key);
			}
		}
	}

}