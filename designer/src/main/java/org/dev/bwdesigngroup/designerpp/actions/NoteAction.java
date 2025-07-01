package org.dev.bwdesigngroup.designerpp.actions;

import org.dev.bwdesigngroup.designerpp.common.DesignerPlusPlusConstants;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

import java.awt.Dimension;
import com.inductiveautomation.ignition.client.util.action.BaseAction;
import com.inductiveautomation.ignition.common.util.LoggerEx;
import com.inductiveautomation.ignition.designer.model.DesignerContext;

import static com.inductiveautomation.ignition.common.BundleUtil.i18n;


/**
 * NoteAction is an action that represents a button in the Ignition Designer toolbar.
 * It is intended to open a NotePad or similar functionality for the designer.
 * 
 * @author Aaron Rai
 */
public class NoteAction extends BaseAction {

	private static final LoggerEx logger = LoggerEx.newBuilder().build(DesignerPlusPlusConstants.MODULE_ID + ".toolbarAction");
	private final DesignerContext context;
	private JFrame notePadFrame;

	/**
	 * Constructor for the NoteAction.
	 *
	 * @param context The designer context, used to interact with the Ignition Designer.
	 * @param icon The icon to be displayed for this action.
	 */
	public NoteAction(DesignerContext context, Icon icon) {
		super(i18n("designerpp.Action.NotePad.Name"), icon);
		this.context = context;
		putValue(SHORT_DESCRIPTION, i18n("designerpp.Action.NotePad.Description"));
		logger.debug("Toolbar Action initialized");
	}

	/**
	 * Invoked when the action is performed. This method opens a NotePad or similar functionality.
	 *
	 * @param e The action event that triggered this method.
	 */
	@Override
	public void actionPerformed(java.awt.event.ActionEvent e) {
		logger.debug("Toolbar Action button clicked, opening NotePad dropdown");

		if (notePadFrame != null && notePadFrame.isDisplayable()) {
			logger.info("NotePad already open, bringing it to the front");
			notePadFrame.toFront();
			notePadFrame.requestFocus();
			return;
		}
		logger.debug("Creating new NotePad frame");
		createNotePadFrame();
	}

	/**
	 * Creates the NotePad frame and sets it up with necessary properties.
	 */
	private void createNotePadFrame() {
		logger.debug("Creating NotePad frame");
		notePadFrame = new JFrame("Designer NodePad");
		notePadFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		notePadFrame.setSize(new Dimension(600, 400));
		
		notePadFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				notePadFrame = null;
				logger.debug("NotePad frame closed and reference cleared");
			}
		});
		
		Frame parent = context.getFrame();
		if (parent != null) {
			notePadFrame.setLocationRelativeTo(parent);
			logger.debug("NotePad frame positioned relative to parent frame");
		} else {
			logger.warn("Parent frame is null, positioning NotePad frame at default location");
		}
		// Add components to the NotePad frame (e.g., text area, buttons)
		JTextArea textArea = new JTextArea();
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		notePadFrame.add(new JScrollPane(textArea), BorderLayout.CENTER);

		// Create a panel for the bottom with right-aligned close button
		JPanel bottomPanel = new JPanel(new BorderLayout());
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		// Add a small close button
		JButton closeButton = new JButton("Close");
		closeButton.setBackground(Color.RED);
		closeButton.setForeground(Color.WHITE);
		closeButton.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
		closeButton.setToolTipText("Closes the NotePad");
		closeButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
		closeButton.setPreferredSize(new Dimension(80, 30));
		closeButton.addActionListener(e -> notePadFrame.dispose());
		
		buttonPanel.add(closeButton);
		bottomPanel.add(buttonPanel, BorderLayout.EAST);
		notePadFrame.add(bottomPanel, BorderLayout.SOUTH);
		notePadFrame.setVisible(true);
	}
}