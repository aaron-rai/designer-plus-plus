package org.dev.arai.designerpp.actions;

import org.dev.arai.designerpp.common.DesignerPlusPlusConstants;
import org.dev.arai.designerpp.common.DesignerPlusPlusRPC;
import org.dev.arai.designerpp.designer.DesignerPlusPlusDesignerHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inductiveautomation.ignition.client.util.action.BaseAction;
import com.inductiveautomation.ignition.designer.gui.tools.CodeEditor;
import com.inductiveautomation.ignition.designer.model.DesignerContext;
import java.awt.Color;
import java.awt.Font;

import static com.inductiveautomation.ignition.common.BundleUtil.i18n;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

public class GatewayScriptConsole extends BaseAction {
	private static final Logger logger = LoggerFactory.getLogger(DesignerPlusPlusConstants.MODULE_ID + ".GatewayScriptConsole");
	private final DesignerContext context;


	public GatewayScriptConsole(DesignerContext context, Icon icon) {
		super(i18n("designerpp.Action.GatewayScriptConsole.Name"), icon);
		this.context = context;
		putValue(SHORT_DESCRIPTION, i18n("designerpp.Action.GatewayScriptConsole.Description"));
		logger.debug("Gateway Script Console Action initialized");
	}

	@Override
	public void actionPerformed(java.awt.event.ActionEvent e) {
		logger.debug("Opening Designer++ Gateway Script Console...");

		JFrame frame = new JFrame("Gateway Script Console ++");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(1200, 800);

		// ===== Editor Panel (Left) =====
		JPanel editorPanel = new JPanel(new BorderLayout(10, 10));
		editorPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JLabel editorTitle = new JLabel("Script Editor");
		editorTitle.setFont(editorTitle.getFont().deriveFont(Font.BOLD, 14f));
		editorPanel.add(editorTitle, BorderLayout.NORTH);

		// Code editor
		CodeEditor codeEditor = new CodeEditor(frame, context);
		codeEditor.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true));
		JScrollPane editorScroll = new JScrollPane(codeEditor);
		editorPanel.add(editorScroll, BorderLayout.CENTER);

		// Run button under editor
		JButton runButton = new JButton("▶ Run in Gateway");
		runButton.setFocusPainted(false);
		runButton.setBackground(new Color(60, 120, 200));
		runButton.setForeground(Color.WHITE);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(runButton);
		editorPanel.add(buttonPanel, BorderLayout.SOUTH);

		// ===== Output Panel (Right) =====
		JPanel outputPanel = new JPanel(new BorderLayout(10, 10));
		outputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JLabel outputTitle = new JLabel("Execution Output");
		outputTitle.setFont(outputTitle.getFont().deriveFont(Font.BOLD, 14f));
		outputPanel.add(outputTitle, BorderLayout.NORTH);

		JTextArea outputArea = new JTextArea();
		outputArea.setEditable(false);
		outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		outputArea.setBackground(new Color(245, 245, 245));
		outputArea.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true));
		JScrollPane outputScroll = new JScrollPane(outputArea);
		outputPanel.add(outputScroll, BorderLayout.CENTER);

		// ===== Split Pane =====
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editorPanel, outputPanel);
		splitPane.setDividerLocation(500);
		splitPane.setResizeWeight(0.5);
		splitPane.setBorder(BorderFactory.createEmptyBorder());

		frame.getContentPane().add(splitPane, BorderLayout.CENTER);

		// ===== Run Action =====
		Runnable runAction = () -> {
			String code = codeEditor.getText();
			System.out.println(code);
			DesignerPlusPlusRPC rpc = DesignerPlusPlusDesignerHook.getRPCHandler();
			rpc.runCode(code);
			outputArea.append("Result: [pending gateway response]\n\n");
		};

		runButton.addActionListener(ev -> runAction.run());

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

}