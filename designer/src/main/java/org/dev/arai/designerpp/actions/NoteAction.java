package org.dev.arai.designerpp.actions;

import org.dev.arai.designerpp.common.DesignerPlusPlusConstants;
import org.dev.arai.designerpp.designer.DesignerPlusPlusDesignerHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.*;

import java.awt.Dimension;
import com.inductiveautomation.ignition.client.util.action.BaseAction;
import com.inductiveautomation.ignition.common.gson.JsonObject;
import com.inductiveautomation.ignition.common.gson.JsonParser;
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

	private static final Logger logger = LoggerFactory.getLogger(DesignerPlusPlusConstants.MODULE_ID + ".ToolbarAction");
	private final DesignerContext context;
	private JFrame notePadFrame;
	private final File notePadFile = new File(DesignerPlusPlusConstants.NOTEPAD_FILE_LOCATION);
	private String currentProjectName = DesignerPlusPlusDesignerHook.getDesignerProject().getName();
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
			notePadFrame.toFront();
			notePadFrame.requestFocus();
			return;
		}
		logger.debug("Creating new NotePad frame");
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
				String content = loadNotePadContent(currentProjectName);
				textArea.setText(content);
			} catch (Exception ex) {
				logger.error("Error loading NotePad content from file: " + notePadFile.getAbsolutePath(), ex);
				textArea.setText("");
			}
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
		closeButton.addActionListener(e -> {
			try {
				saveNotePadContent(currentProjectName, notePadFrame);
			} catch (Exception ex) {
				logger.error("Error saving NotePad content on close: ", ex);
			}
		});
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

		// Save the content to the NotePad file when the frame is closed
		notePadFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					saveNotePadContent(currentProjectName, notePadFrame);
				} catch (Exception ex) {
					logger.error("Error saving NotePad content on close: ", ex);
				}
			}
		});
	}

	/**
	 * Loads the content of the NotePad from a file, if no JsonObject exists, it creates a new one and returns it.
	 * @param projectName The name of the project, used to identify the NotePad content.
	 * @return String containing the NotePad content filtered to the current project.
	 */
	public String loadNotePadContent(String projectName) {
		logger.debug("Loading NotePad content from file: " + notePadFile.getAbsolutePath());

		if (notePadFile.exists()) {
			try {
				JsonParser parser = new JsonParser();
				String fileContent = new String(java.nio.file.Files.readAllBytes(notePadFile.toPath()));
				if (fileContent.trim().isEmpty()) {
					return "";
				}

				JsonObject notePadContent = parser.parse(fileContent).getAsJsonObject();
				JsonObject projects = notePadContent.getAsJsonObject("projects");

				if (projects.has(projectName)) {
					JsonObject projectContent = projects.getAsJsonObject(projectName);
					logger.debug("NotePad content loaded for project: " + projectName);
					return projectContent.get("content").getAsString();
				} else {
					return "";
				}

			} catch (IOException e) {
				logger.error("Error reading NotePad file: " + notePadFile.getAbsolutePath(), e);
				return "";
			}
		} else {
			logger.warn("NotePad file does not exist at {}! This is unexpected! Creating new file. ", notePadFile.getAbsolutePath());
			try {
				notePadFile.createNewFile();
			} catch (IOException e) {
				logger.error("Error creating NotePad file: " + notePadFile.getAbsolutePath(), e);
				return "";
			}
		}
		return "";
	}

	/**
	 * Saves the content of the NotePad to a file in a JSONObject format that contains the project name and the content.
	 * @param projectName The name of the project.
	 * @param notePadFrame The NotePad frame containing the text area with content to save.
	 * @return void
	 */
	public void saveNotePadContent(String projectName, JFrame notePadFrame) {
		logger.debug("Saving NotePad content for project: " + projectName);
		String content;
		if (notePadFrame != null) {
			content = ((JTextArea) ((JScrollPane) notePadFrame.getContentPane().getComponent(0)).getViewport().getView()).getText();
		} else {
			return; // No NotePad frame, nothing to save
		}
		
		// Load existing content from file or create new if file doesn't exist
		JsonObject notePadContent = new JsonObject();
		if (notePadFile.exists()) {
			try {
				String fileContent = new String(java.nio.file.Files.readAllBytes(notePadFile.toPath()));
				if (!fileContent.trim().isEmpty()) {
					JsonParser parser = new JsonParser();
					notePadContent = parser.parse(fileContent).getAsJsonObject();
				}
			} catch (IOException e) {
				logger.error("Error reading existing NotePad file: " + notePadFile.getAbsolutePath(), e);
				// Continue with empty JsonObject if reading fails
			}
		}
		
		// Get or create the projects object
		JsonObject projects;
		if (notePadContent.has("projects")) {
			projects = notePadContent.getAsJsonObject("projects");
		} else {
			projects = new JsonObject();
			notePadContent.add("projects", projects);
		}
		
		// Add or update the current project's content
		if (!projects.has(projectName)) {
			projects.add(projectName, new JsonObject());
		}
		projects.getAsJsonObject(projectName).addProperty("content", content);

		// Write the JsonObject to the NotePad file
		try (FileWriter fileWriter = new FileWriter(notePadFile)) {
			fileWriter.write(notePadContent.toString());
			logger.debug("NotePad content saved successfully to file: " + notePadFile.getAbsolutePath());
		} catch (IOException e) {
			logger.error("Error writing NotePad content to file: " + notePadFile.getAbsolutePath(), e);
		}
	}
}
