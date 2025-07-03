package org.dev.bwdesigngroup.designerpp.actions;

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.StringSelection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import org.dev.bwdesigngroup.designerpp.common.DesignerPlusPlusConstants;
import org.dev.bwdesigngroup.designerpp.common.DesignerPlusPlusRPC;
import org.dev.bwdesigngroup.designerpp.utils.ParseColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inductiveautomation.ignition.client.gateway_interface.ModuleRPCFactory;
import com.inductiveautomation.ignition.client.util.action.BaseAction;
import com.inductiveautomation.ignition.common.gson.JsonElement;
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
    private final List<String> defaultThemes = DesignerPlusPlusConstants.IGNITION_DEFAULT_THEMES;

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
         * Creates and displays the CSS Variable Viewer GUI with variables in order.
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

        for (Map.Entry<String, JsonElement> themeEntry : themes.entrySet()) {
            String themeName = themeEntry.getKey();
            JsonObject theme = themeEntry.getValue().getAsJsonObject();
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

            Map<String, String> resolved = new LinkedHashMap<>();
            for (Map.Entry<String, JsonElement> varEntry : variables.entrySet()) {
                resolved.put(varEntry.getKey(), varEntry.getValue().getAsString());
            }

            // Container for collapsible content
            JPanel sectionPanel = new JPanel();
            sectionPanel.setLayout(new BoxLayout(sectionPanel, BoxLayout.Y_AXIS));
            sectionPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

            for (Map.Entry<String, String> entry : resolved.entrySet()) {
                String varName = "var(--" + entry.getKey() + ")";
                String rawValue = entry.getValue();

                // Resolve the value only for color detection and parsing
                String finalValue = resolveColorValue(rawValue, resolved, 0);

                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
                row.setAlignmentX(Component.LEFT_ALIGNMENT);

                // Check if this is a color value that we can parse
                boolean isColorValue = ParseColor.isColor(finalValue);
                Color parsedColor = null;
                
                if (isColorValue) {
                    parsedColor = ParseColor.parseColor(finalValue);
                }

                if (parsedColor != null) {
                    JLabel colorBox = new JLabel();
                    colorBox.setBackground(parsedColor);
                    colorBox.setOpaque(true);
                    colorBox.setPreferredSize(new Dimension(20, 20));
                    row.add(colorBox);
                    row.add(Box.createHorizontalStrut(10));
                } else {
                    JLabel placeholderIcon = new JLabel("🔗");
                    row.add(placeholderIcon);
                }

                JLabel label = new JLabel("<html>" + varName + ": <b>" + rawValue + "</b></html>");
                row.add(label);

                row.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        Toolkit.getDefaultToolkit().getSystemClipboard()
                            .setContents(new StringSelection(varName), null);
                        System.out.println("Copied: " + varName);
                    }
                });

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
        if (depth > 10) return value;

        if (value.startsWith("var(") && value.endsWith(")")) {
            String referenced = map.getOrDefault(value, "");
            return resolveColorValue(referenced, map, depth + 1);
        }
        return value;
    }
}