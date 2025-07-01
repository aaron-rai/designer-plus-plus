package org.dev.bwdesigngroup.designerpp.utils;

import org.dev.bwdesigngroup.designerpp.common.DesignerPlusPlusConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inductiveautomation.ignition.designer.model.DesignerContext;
import com.inductiveautomation.ignition.designer.navtree.model.AbstractNavTreeNode;
import com.inductiveautomation.ignition.designer.navtree.model.ProjectBrowserRoot;

import java.util.*;


/**
 * ProjectBrowserStateManager is responsible for capturing and restoring the state of the project browser in the Ignition Designer.
 * It tracks the expanded and selected states of nodes in the project browser tree.
 * 
 * @author Keith Gamble
 */
public class ProjectBrowserStateManager {
    private final Logger logger = LoggerFactory.getLogger(DesignerPlusPlusConstants.MODULE_ID + ".browserState");
    private final DesignerContext context;
    private Map<String, TreeNodeState> lastTreeState;

    public ProjectBrowserStateManager(DesignerContext context) {
        this.context = context;
    }

    /**
     * Captures the current state of the project browser.
     */
    public void captureState() {
        ProjectBrowserRoot root = context.getProjectBrowserRoot();
        if (root != null) {
            lastTreeState = new HashMap<>();
            for (int i = 0; i < root.getChildCount(); i++) {
                captureNodeState(root.getChildAt(i), lastTreeState);
            }
            logger.debug("Captured project browser state");
        }
    }

    /**
     * Restores the previously captured state of the project browser.
     * This will expand and select nodes based on the last captured state.
     */
    public void restoreState() {
        if (lastTreeState != null) {
            ProjectBrowserRoot root = context.getProjectBrowserRoot();
            if (root != null) {
                for (int i = 0; i < root.getChildCount(); i++) {
                    restoreNodeState(root.getChildAt(i));
                }
                logger.debug("Restored project browser state");
            }
        }
    }

    /**
     * Captures the state of a single node and its children.
     * This includes whether the node is expanded and selected.
     * @param node
     * @param stateMap
     */
    private void captureNodeState(AbstractNavTreeNode node, Map<String, TreeNodeState> stateMap) {
        String pathString = getNodePath(node);
        boolean expanded = node.getModel().isExpanded(node);
        boolean selected = node.isSelected();
        
        TreeNodeState state = new TreeNodeState(
            pathString,
            node.getName(),
            expanded,
            selected
        );
        stateMap.put(pathString, state);

        if (!node.isLeaf()) {
            for (int i = 0; i < node.getChildCount(); i++) {
                captureNodeState(node.getChildAt(i), stateMap);
            }
        }
    }

    /**
     * Constructs a path string for the given node, representing its position in the tree.
     * The path is built from the node's name and its parent's names.
     * @param node The node for which to construct the path.
     * @return A string representing the path of the node in the tree.
     */
    private String getNodePath(AbstractNavTreeNode node) {
        List<String> pathParts = new ArrayList<>();
        AbstractNavTreeNode current = node;
        while (current != null && current.getParent() != null) {
            pathParts.add(0, current.getName());
            current = current.getParent();
        }
        return String.join("/", pathParts);
    }

    /**
     * Restores the state of a single node based on the last captured state.
     * This will expand the node if it was previously expanded and select it if it was selected.
     * @param node The node to restore.
     */
    private void restoreNodeState(AbstractNavTreeNode node) {
        String pathString = getNodePath(node);
        TreeNodeState savedState = lastTreeState.get(pathString);
        
        if (savedState != null) {
            if (savedState.expanded) {
                node.getModel().expandNode(node);
            }
            if (savedState.selected) {
                node.select(true);
            }
        }

        if (!node.isLeaf()) {
            for (int i = 0; i < node.getChildCount(); i++) {
                restoreNodeState(node.getChildAt(i));
            }
        }
    }

    /**
     * Represents the state of a tree node, including its path, name, expanded state, and selected state.
     */
    private static class TreeNodeState {
        final String path;
        final String name; 
        final boolean expanded;
        final boolean selected;

        TreeNodeState(String path, String name, boolean expanded, boolean selected) {
            this.path = path;
            this.name = name;
            this.expanded = expanded;
            this.selected = selected;
        }
    }
}
