package org.dev.bwdesigngroup.designerpp.actions;

import org.dev.bwdesigngroup.designerpp.common.DesignerPlusPlusConstants;
import org.dev.bwdesigngroup.designerpp.designer.DesignerPlusPlusDesignerHook;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

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
	private final File notePadFile = new File(DesignerPlusPlusConstants.NOTEPAD_FILE_LOCATION);
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
		String currentProject = DesignerPlusPlusDesignerHook.getDesignerProject().getName();
		logger.info("Current project: " + currentProject);
		createNotePadFrame();
	}

	/**
	 * Creates the NotePad frame and sets it up with necessary properties.
	 * 
	 * @return void
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
		
		// Add components to the NotePad frame
		JTextArea textArea = new JTextArea();
		if (notePadFile.exists()) {
			try {
				textArea.read(new java.io.FileReader(notePadFile), null);
				logger.debug("Loaded existing content from NotePad file: " + notePadFile.getAbsolutePath());
			} catch (Exception ex) {
				logger.error("Error reading NotePad file: " + notePadFile.getAbsolutePath(), ex);
			}
		} else {
			logger.warn("NotePad file does not exist, this should have been created on startup, please open an issue if this persists.");
		}
		
		textArea.setTabSize(4);
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

		// Add a clear button to the left of the close button to clear the text area
		JButton clearButton = new JButton("Clear");
		clearButton.setBackground(Color.GRAY);
		clearButton.setForeground(Color.WHITE);
		clearButton.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		clearButton.setToolTipText("Clears the NotePad text area");
		clearButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
		clearButton.setPreferredSize(new Dimension(80, 30));
		clearButton.addActionListener(e -> {
			textArea.setText("");	
		});

		buttonPanel.add(clearButton);
		buttonPanel.add(closeButton);
		bottomPanel.add(buttonPanel, BorderLayout.EAST);
		notePadFrame.add(bottomPanel, BorderLayout.SOUTH);
		notePadFrame.setVisible(true);
	}
}