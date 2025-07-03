package org.dev.bwdesigngroup.designerpp.utils;

import java.awt.Color;

import org.dev.bwdesigngroup.designerpp.common.DesignerPlusPlusConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParseColor {

	private final static Logger logger = LoggerFactory.getLogger(DesignerPlusPlusConstants.MODULE_ID + ".parseColor");

	/**
     * Checks if a string is a valid CSS color value.
     * 
     * @param value The string to check.
     * @return True if the string is a valid color, false otherwise.
     */
    public static boolean isColor(String value) {
        if (value == null) return false;
        value = value.trim().toLowerCase();

        boolean isColor = value.matches("^#([0-9a-f]{3}|[0-9a-f]{6})$")
                || value.matches("rgba?\\([^)]+\\)")
                || value.startsWith("hsl")
                || value.startsWith("oklch");
		logger.debug("isColor('{}') = {}", value, isColor);
		return isColor;
    }

	 /**
     * Parses a CSS color string and returns a Java Color object.
     * Supports hex colors (#RGB, #RRGGBB), rgba/rgb colors (including space-separated syntax),
     * and basic hsl colors.
     * 
     * @param colorString The CSS color string to parse
     * @return A Color object, or null if parsing fails
     */
    public static Color parseColor(String colorString) {
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
        
        // Handle rgba colors (both comma and space separated)
        if (colorString.startsWith("rgba(") && colorString.endsWith(")")) {
			logger.debug("Parsing RGBA color: {}", colorString);
            String values = colorString.substring(5, colorString.length() - 1);
            return parseRgbaValues(values);
        }
        
        // Handle rgb colors (both comma and space separated)
        if (colorString.startsWith("rgb(") && colorString.endsWith(")")) {
			logger.debug("Parsing RGB color: {}", colorString);
            String values = colorString.substring(4, colorString.length() - 1);
            return parseRgbValues(values);
        }
        
        // Handle basic hsl colors
        if (colorString.startsWith("hsl(") && colorString.endsWith(")")) {
			logger.debug("Parsing HSL color: {}", colorString);
            String values = colorString.substring(4, colorString.length() - 1);
            return parseHslValues(values);
        }
        
        return null;
    }

    /**
     * Parses RGBA values from a string, handling both comma-separated and space-separated syntax.
     * Examples: "255, 0, 0, 0.5" or "255 0 0 / 50%"
     */
    private static Color parseRgbaValues(String values) {
        try {
            // Handle space-separated with slash for alpha: "51 110 173 / 4%"
            if (values.contains("/")) {
                String[] parts = values.split("/");
                if (parts.length == 2) {
                    String[] rgbParts = parts[0].trim().split("\\s+");
                    String alphaPart = parts[1].trim();
                    
                    if (rgbParts.length == 3) {
                        int r = Integer.parseInt(rgbParts[0].trim());
                        int g = Integer.parseInt(rgbParts[1].trim());
                        int b = Integer.parseInt(rgbParts[2].trim());
                        
                        float a;
                        if (alphaPart.endsWith("%")) {
                            a = Float.parseFloat(alphaPart.substring(0, alphaPart.length() - 1)) / 100.0f;
                        } else {
                            a = Float.parseFloat(alphaPart);
                        }
                        
                        // Clamp values to valid ranges
                        r = Math.max(0, Math.min(255, r));
                        g = Math.max(0, Math.min(255, g));
                        b = Math.max(0, Math.min(255, b));
                        a = Math.max(0.0f, Math.min(1.0f, a));
                        
                        return new Color(r, g, b, (int)(a * 255));
                    }
                }
            }
            
            // Handle comma-separated: "255, 0, 0, 0.5"
            String[] parts = values.split(",");
            if (parts.length == 4) {
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
            }
        } catch (NumberFormatException e) {
            return null;
        }
        
        return null;
    }

    /**
     * Parses RGB values from a string, handling both comma-separated and space-separated syntax.
     * Examples: "255, 0, 0" or "255 0 0"
     */
    private static Color parseRgbValues(String values) {
        try {
            String[] parts;
            
            // Check if comma-separated or space-separated
            if (values.contains(",")) {
                parts = values.split(",");
            } else {
                parts = values.split("\\s+");
            }
            
            if (parts.length == 3) {
                int r = Integer.parseInt(parts[0].trim());
                int g = Integer.parseInt(parts[1].trim());
                int b = Integer.parseInt(parts[2].trim());
                
                // Clamp values to valid ranges
                r = Math.max(0, Math.min(255, r));
                g = Math.max(0, Math.min(255, g));
                b = Math.max(0, Math.min(255, b));
                
                return new Color(r, g, b);
            }
        } catch (NumberFormatException e) {
            return null;
        }
        
        return null;
    }

    /**
     * Basic HSL color parsing (simplified implementation).
     * Example: "hsl(200, 50%, 50%)"
     */
    private static Color parseHslValues(String values) {
        try {
            String[] parts = values.split(",");
            if (parts.length == 3) {
                float h = Float.parseFloat(parts[0].trim()) / 360.0f;
                float s = Float.parseFloat(parts[1].trim().replace("%", "")) / 100.0f;
                float l = Float.parseFloat(parts[2].trim().replace("%", "")) / 100.0f;
                
                return Color.getHSBColor(h, s, l);
            }
        } catch (NumberFormatException e) {
            return null;
        }
        
        return null;
    }
}
