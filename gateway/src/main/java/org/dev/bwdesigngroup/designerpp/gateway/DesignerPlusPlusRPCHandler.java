package org.dev.bwdesigngroup.designerpp.gateway;

import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.io.IOException;

import org.dev.bwdesigngroup.designerpp.common.DesignerPlusPlusConstants;
import org.dev.bwdesigngroup.designerpp.common.DesignerPlusPlusRPC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inductiveautomation.ignition.common.gson.JsonObject;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;


/**
 * CSSVariableViewerRPCHandler is the server-side implementation of the CSSVariableViewerRPC interface.
 * It handles requests to retrieve CSS variable data from the themes directory and processes CSS files.
 * 
 * @author Aaron Rai
 */
public class DesignerPlusPlusRPCHandler implements DesignerPlusPlusRPC {
	private final Logger logger = LoggerFactory.getLogger(DesignerPlusPlusRPCHandler.class);
	private final GatewayContext context;

	/**
	 * Constructor for the CSSVariableViewerRPCHandler.
	 * 
	 * @param context The gateway context, used to interact with the Ignition Gateway.
	 */
	public DesignerPlusPlusRPCHandler(GatewayContext context) {
		this.context = context;
		logger.info("Designer++ RPC Handler initialized");
	}

	/**
	 * Retrieves CSS data from the themes directory and processes it.
	 * 
	 * @return A JsonObject containing the CSS variables organized by theme.
	 */
	@Override
	public JsonObject getCSSData() {
		logger.debug("getCSSData called");
		return readCSSFiles(DesignerPlusPlusConstants.THEMES_DIRECTORY);
	}

	/**
	 * Reads CSS files from the specified directory and extracts CSS variables.
	 * 
	 * @param directory The directory containing CSS files.
	 * @return A JsonObject containing the extracted CSS variables organized by theme.
	 */
	private JsonObject readCSSFiles(String directory) {
		logger.debug("Reading CSS files from directory: {}", directory);
		JsonObject result = new JsonObject();
		
		try {
			Path themesPath = Paths.get(directory);
			if (!Files.exists(themesPath)) {
				logger.warn("Themes directory does not exist: {}", directory);
				result.addProperty("success", false);
				result.addProperty("error", "Themes directory not found");
				return result;
			}
			
			JsonObject themes = new JsonObject();
			int totalFiles = 0;
			
			// Process root-level CSS files (like dark.css, light.css, etc.)
			try (Stream<Path> rootFiles = Files.list(themesPath)) {
				List<Path> rootCssFiles = rootFiles
					.filter(Files::isRegularFile)
					.filter(path -> path.toString().endsWith(".css"))
					.collect(Collectors.toList());
					
				for (Path cssFile : rootCssFiles) {
					String content = Files.readString(cssFile);
					JsonObject variables = extractRootVariables(content);
					
					if (variables.size() > 0) {
						String themeName = cssFile.getFileName().toString().replace(".css", "");
						themes.add(themeName, variables);
					}
					totalFiles++;
				}
			}
			
			// Process theme directories (like dark/, light/, sepasoft-light/, etc.)
			try (Stream<Path> directories = Files.list(themesPath)) {
				List<Path> themeDirs = directories
					.filter(Files::isDirectory)
					.collect(Collectors.toList());
					
				for (Path themeDir : themeDirs) {
					String themeName = themeDir.getFileName().toString();
					JsonObject themeVariables = processThemeDirectory(themeDir);
					
					if (themeVariables.size() > 0) {
						themes.add(themeName, themeVariables);
					}
					
					// Count files in this directory
					totalFiles += countCssFiles(themeDir);
				}
			}
			
			result.addProperty("success", true);
			result.add("themes", themes);
			result.addProperty("filesProcessed", totalFiles);
			logger.info("Successfully processed {} CSS files across all themes", totalFiles);
			
		} catch (IOException e) {
			logger.error("Error reading CSS files from directory: {}", directory, e);
			result.addProperty("success", false);
			result.addProperty("error", e.getMessage());
		}

		return result;
	}

	/**
	 * Processes a theme directory to extract CSS variables from its CSS files.
	 * 
	 * @param themeDir The path to the theme directory.
	 * @return A JsonObject containing the extracted CSS variables for this theme.
	 * @throws IOException If an I/O error occurs while reading files.
	 */
	private JsonObject processThemeDirectory(Path themeDir) throws IOException {
		JsonObject themeData = new JsonObject();
		
		// Process all CSS files in this theme directory recursively
		try (Stream<Path> files = Files.walk(themeDir)) {
			List<Path> cssFiles = files
				.filter(Files::isRegularFile)
				.filter(path -> path.toString().endsWith(".css"))
				.collect(Collectors.toList());
				
			for (Path cssFile : cssFiles) {
				String content = Files.readString(cssFile);
				JsonObject variables = extractRootVariables(content);
				
				if (variables.size() > 0) {
					// Create a relative path from theme directory
					String relativePath = themeDir.relativize(cssFile).toString();
					themeData.add(relativePath, variables);
				}
			}
		}
		
		return themeData;
	}
	
	/**
	 * Counts the number of CSS files in a directory and its subdirectories.
	 * 
	 * @param directory The directory to search for CSS files.
	 * @return The count of CSS files found.
	 * @throws IOException If an I/O error occurs while reading the directory.
	 */
	private int countCssFiles(Path directory) throws IOException {
		try (Stream<Path> files = Files.walk(directory)) {
			return (int) files
				.filter(Files::isRegularFile)
				.filter(path -> path.toString().endsWith(".css"))
				.count();
		}
	}

	/**
	 * Extracts CSS custom properties defined in :root blocks from the given CSS content.
	 * 
	 * @param cssContent The content of a CSS file as a string.
	 * @return A JsonObject containing the extracted CSS variables.
	 */
	private JsonObject extractRootVariables(String cssContent) {
		JsonObject variables = new JsonObject();
		
		// Regex to match :root blocks
		Pattern rootPattern = Pattern.compile(":root\\s*\\{([^}]+)\\}", Pattern.DOTALL);
		Matcher rootMatcher = rootPattern.matcher(cssContent);
		
		while (rootMatcher.find()) {
			String rootContent = rootMatcher.group(1);
			
			// Extract CSS custom properties (--variable-name: value;)
			Pattern varPattern = Pattern.compile("--([^:]+):\\s*([^;]+);");
			Matcher varMatcher = varPattern.matcher(rootContent);
			
			while (varMatcher.find()) {
				String varName = varMatcher.group(1).trim();
				String varValue = varMatcher.group(2).trim();
				variables.addProperty(varName, varValue);
			}
		}
		
		return variables;
	}
}