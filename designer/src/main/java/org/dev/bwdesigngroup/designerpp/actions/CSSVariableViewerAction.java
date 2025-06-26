package org.dev.bwdesigngroup.designerpp.actions;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import org.dev.bwdesigngroup.designerpp.common.DesignerPlusPlusConstants;
import org.dev.bwdesigngroup.designerpp.common.DesignerPlusPlusRPC;

import com.inductiveautomation.ignition.client.gateway_interface.ModuleRPCFactory;
import com.inductiveautomation.ignition.client.util.action.BaseAction;
import com.inductiveautomation.ignition.common.gson.JsonObject;
import com.inductiveautomation.ignition.common.util.LoggerEx;
import com.inductiveautomation.ignition.designer.model.DesignerContext;

import static com.inductiveautomation.ignition.common.BundleUtil.i18n;

/**
 * CSSVariableViewerAction is an action that retrieves and displays CSS variables from the Ignition Designer.
 * It creates a GUI to visualize these variables, allowing users to see and copy color values.
 * 
 * @author Aaron Rai
 */
public class CSSVariableViewerAction extends BaseAction {
    private static final LoggerEx logger = LoggerEx.newBuilder().build(CSSVariableViewerAction.class);
    private final DesignerContext context;

    /**
     * Constructor for the CSSVariableViewerAction.
     * 
     * @param context The designer context, used to interact with the Ignition Designer.
     * @param icon The icon to be displayed for this action.
     */
    public CSSVariableViewerAction(DesignerContext context, Icon icon) {
        super(i18n("designerpp.Action.CSSVariableViewer.Name"), icon);
        this.context = context;
        putValue(SHORT_DESCRIPTION, i18n("designerpp.Action.CSSVariableViewer.Description"));
        logger.debug("CSS Variable Viewer Action initialized");
    }

    /**
     * Invoked when the action is performed. This method retrieves CSS data from the gateway
     * and creates the GUI to display the CSS variables.
     * 
     * @param e The action event that triggered this method.
     */
    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        logger.debug("CSS Variable Viewer button clicked, initiating view variables request");
		DesignerPlusPlusRPC rpc = ModuleRPCFactory.create(DesignerPlusPlusConstants.MODULE_ID, DesignerPlusPlusRPC.class);
		JsonObject cssData = rpc.getCSSData();
        if (cssData != null) {
            logger.debug("CSS Data retrieved successfully, creating GUI");
            createAndShowGUI(cssData);
        } else {
            logger.error("Failed to retrieve CSS Data from the gateway");
        }
	}

    /**
     * Creates and displays the CSS Variable Viewer GUI.
     * 
     * @param cssData The JSON object containing CSS variable data.
     */
    private static void createAndShowGUI(JsonObject cssData) {
        logger.debug("Creating and showing CSS Variable Viewer GUI");
        JFrame frame = new JFrame("CSS Variable Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 600);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JsonObject themes = cssData.getAsJsonObject("themes");
        for (String themeName : themes.keySet()) {
            JsonObject theme = themes.getAsJsonObject(themeName);
            JsonObject variables;

            if (theme.has("variables.css")) {
                variables = theme.getAsJsonObject("variables.css");
            }
            else if (theme.has("styles.css")) {
                variables = theme.getAsJsonObject("styles.css");
            }
            else {
                continue;  // Skip themes without variable definitions
            }

            // Convert JSONObject to Map<String, String> for easy access
            Map<String, String> resolved = new HashMap<>();
            for (String key : variables.keySet()) {
                resolved.put(key, variables.get(key).getAsString());
            }

            // Container for collapsible content
            JPanel sectionPanel = new JPanel();
            sectionPanel.setLayout(new BoxLayout(sectionPanel, BoxLayout.Y_AXIS));
            sectionPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

            for (Map.Entry<String, String> entry : resolved.entrySet()) {
                String varName = entry.getKey();
                String rawValue = entry.getValue();

                String finalColor = resolveColorValue(rawValue, resolved, 0);

                if (!isColor(finalColor)) continue;  // Skip non-colors

                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
                row.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel colorBox = new JLabel();
                try {
                    colorBox.setBackground(Color.decode(finalColor));
                } catch (Exception e) {
                    logger.errorEvent(themeName, e);
                }
                colorBox.setOpaque(true);
                colorBox.setPreferredSize(new Dimension(20, 20));

                JLabel label = new JLabel(varName + ": " + finalColor);

                row.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        Toolkit.getDefaultToolkit().getSystemClipboard()
                            .setContents(new StringSelection(varName), null);
                        System.out.println("Copied to clipboard: " + varName);
                    }
                });

                row.add(colorBox);
                row.add(Box.createHorizontalStrut(10));
                row.add(label);
                sectionPanel.add(row);
            }

            // Collapsible wrapper
            JPanel wrapper = new JPanel(new BorderLayout());

            JButton toggleButton = new JButton("▼ " + themeName);
            toggleButton.setFocusPainted(false);
            toggleButton.setContentAreaFilled(false);
            toggleButton.setBorderPainted(false);
            toggleButton.setHorizontalAlignment(SwingConstants.LEFT);

            toggleButton.addActionListener(new ActionListener() {
                boolean expanded = true;

                @Override
                public void actionPerformed(ActionEvent e) {
                    expanded = !expanded;
                    sectionPanel.setVisible(expanded);
                    toggleButton.setText((expanded ? "▼ " : "► ") + themeName);
                    mainPanel.revalidate();
                }
            });

            wrapper.add(toggleButton, BorderLayout.NORTH);
            wrapper.add(sectionPanel, BorderLayout.CENTER);
            mainPanel.add(wrapper);
        }

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        frame.add(scrollPane);
        frame.setVisible(true);
    }

    /**
     * Resolves a color value, handling variable references recursively.
     * 
     * @param value The color value to resolve.
     * @param map The map of variable names to values.
     * @param depth The current recursion depth (to prevent infinite loops).
     * @return The resolved color value.
     */
    private static String resolveColorValue(String value, Map<String, String> map, int depth) {
        if (depth > 10) return value;  // Prevent infinite loops

        if (value.startsWith("var(") && value.endsWith(")")) {
            String key = value.substring(4, value.length() - 1);
            String referenced = map.getOrDefault(key, "");
            return resolveColorValue(referenced, map, depth + 1);
        }
        return value;
    }

    /**
     * Checks if a string is a valid CSS color value.
     * 
     * @param value The string to check.
     * @return True if the string is a valid color, false otherwise.
     */
    private static boolean isColor(String value) {
        if (value == null) return false;
        value = value.trim().toLowerCase();

        return value.matches("^#([0-9a-f]{3}|[0-9a-f]{6})$")
                || value.matches("rgba?\\(([^)]+)\\)");
    }
}