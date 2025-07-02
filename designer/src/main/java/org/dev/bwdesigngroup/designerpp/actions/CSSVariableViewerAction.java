package org.dev.bwdesigngroup.designerpp.actions;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import org.dev.bwdesigngroup.designerpp.common.DesignerPlusPlusConstants;
import org.dev.bwdesigngroup.designerpp.common.DesignerPlusPlusRPC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inductiveautomation.ignition.client.gateway_interface.ModuleRPCFactory;
import com.inductiveautomation.ignition.client.util.action.BaseAction;
import com.inductiveautomation.ignition.common.gson.JsonObject;
import com.inductiveautomation.ignition.designer.model.DesignerContext;

import static com.inductiveautomation.ignition.common.BundleUtil.i18n;

/**
 * CSSVariableViewerAction is an action that retrieves and displays CSS variables from the Ignition Designer.
 * It creates a GUI to visualize these variables, allowing users to see and copy color values.
 * 
 * @author Aaron Rai
 */
public class CSSVariableViewerAction extends BaseAction {
    private static final Logger logger = LoggerFactory.getLogger(DesignerPlusPlusConstants.MODULE_ID + ".cssVariableViewer");
    private final DesignerContext context;
    private JFrame cssViewerFrame;

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
        if (cssViewerFrame != null && cssViewerFrame.isDisplayable()) {
            logger.debug("CSS Variable Viewer frame already exists, bringing to front");
            cssViewerFrame.toFront();
            cssViewerFrame.requestFocus();
            return;
        }
        
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
    private void createAndShowGUI(JsonObject cssData) {
        logger.debug("Creating and showing CSS Variable Viewer GUI");
        cssViewerFrame = new JFrame("CSS Variable Viewer");
        cssViewerFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        cssViewerFrame.setSize(450, 500);

        cssViewerFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                cssViewerFrame = null;
                logger.debug("CSS Variable Viewer frame closed and reference cleared");
            }
        });

        Frame parent = context.getFrame();
        if (parent != null) {
            cssViewerFrame.setLocationRelativeTo(parent);
        } else {
            logger.warn("No parent frame found, centering on screen");
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            cssViewerFrame.setLocation((screenSize.width - cssViewerFrame.getWidth()) / 2, (screenSize.height - cssViewerFrame.getHeight()) / 2);
        }
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
                continue;
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
                String varName = "var(--" + entry.getKey() + ")";
                String rawValue = entry.getValue();

                String finalColor = resolveColorValue(rawValue, resolved, 0);

                if (!isColor(finalColor)) continue;  // Skip non-colors

                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
                row.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel colorBox = new JLabel();
                Color color = parseColor(finalColor);
                if (color != null) {
                    colorBox.setBackground(color);
                } else {
                    logger.warn("Could not parse color value for " + varName + ": " + finalColor);
                    // Use a default gray color for unparseable colors
                    colorBox.setBackground(Color.LIGHT_GRAY);
                }
                colorBox.setOpaque(true);
                colorBox.setPreferredSize(new Dimension(20, 20));

                JLabel label = new JLabel(varName + ": " + finalColor);

                row.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        Toolkit.getDefaultToolkit().getSystemClipboard()
                            .setContents(new StringSelection(varName), null);
                        System.out.println("Copied: " + varName);
                    }
                });

                row.add(colorBox);
                row.add(Box.createHorizontalStrut(10));
                row.add(label);
                row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                row.setToolTipText("Click to copy variable name: " + varName);
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
        cssViewerFrame.add(scrollPane);
        cssViewerFrame.setVisible(true);
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
            String referenced = map.getOrDefault(value, "");
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

    /**
     * Parses a CSS color string and returns a Java Color object.
     * Supports hex colors (#RGB, #RRGGBB) and rgba/rgb colors.
     * 
     * @param colorString The CSS color string to parse
     * @return A Color object, or null if parsing fails
     */
    private static Color parseColor(String colorString) {
        if (colorString == null) return null;
        
        colorString = colorString.trim().toLowerCase();
        
        // Handle hex colors
        if (colorString.matches("^#([0-9a-f]{3}|[0-9a-f]{6})$")) {
            try {
                return Color.decode(colorString);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        // Handle rgba colors
        if (colorString.startsWith("rgba(") && colorString.endsWith(")")) {
            String values = colorString.substring(5, colorString.length() - 1);
            String[] parts = values.split(",");
            
            if (parts.length == 4) {
                try {
                    int r = Integer.parseInt(parts[0].trim());
                    int g = Integer.parseInt(parts[1].trim());
                    int b = Integer.parseInt(parts[2].trim());
                    float a = Float.parseFloat(parts[3].trim());
                    
                    // Clamp values to valid ranges
                    r = Math.max(0, Math.min(255, r));
                    g = Math.max(0, Math.min(255, g));
                    b = Math.max(0, Math.min(255, b));
                    a = Math.max(0.0f, Math.min(1.0f, a));
                    
                    return new Color(r, g, b, (int)(a * 255));
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        
        // Handle rgb colors
        if (colorString.startsWith("rgb(") && colorString.endsWith(")")) {
            String values = colorString.substring(4, colorString.length() - 1);
            String[] parts = values.split(",");
            
            if (parts.length == 3) {
                try {
                    int r = Integer.parseInt(parts[0].trim());
                    int g = Integer.parseInt(parts[1].trim());
                    int b = Integer.parseInt(parts[2].trim());
                    
                    // Clamp values to valid ranges
                    r = Math.max(0, Math.min(255, r));
                    g = Math.max(0, Math.min(255, g));
                    b = Math.max(0, Math.min(255, b));
                    
                    return new Color(r, g, b);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        
        return null;
    }
}