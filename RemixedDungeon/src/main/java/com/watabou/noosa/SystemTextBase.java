package com.watabou.noosa;

import com.nyrds.pixeldungeon.game.GamePreferences;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public abstract class SystemTextBase extends Text {
    // Markup pattern for highlighting (_text_)
    protected static final Pattern HIGHLIGHTER = Pattern.compile("_(.*?)_");
    // Markup pattern for bronze text ("text")
    protected static final Pattern BRONZE_HIGHLIGHTER = Pattern.compile("\"(.*?)\"");
    
    protected static float fontScale = Float.NaN;
    
    // Color properties
    static protected int highlightColor = 0xFFCC33FF; // Default violet highlight color (ARGB)
    static protected int defaultColor = 0xFFFFFFFF; // White color (ARGB)
    static protected int bronzeColor = 0xFFCD7F32; // Bronze color for quoted text (ARGB)
    protected boolean hasMarkup = false;
    protected String originalText = "";
    
    protected SystemTextBase(float x, float y, float width, float height) {
        super(x, y, width, height);
    }
    
    public static void updateFontScale() {
        float scale = 0.5f + 0.01f * GamePreferences.fontScale();
        scale *= 1.2f;
        
        if (scale < 0.1f) {
            fontScale = 0.1f;
            return;
        }
        if (scale > 4) {
            fontScale = 4f;
            return;
        }
        
        // Note: Platform-specific screen size checks should be handled by subclasses
        fontScale = scale;
    }
    
    /**
     * Parse markup in the text and create color mapping
     * This is a common implementation for handling both _text_ (violet) and "text" (bronze) markup
     * Subclasses can override if needed
     */
    protected void parseMarkup(String input) {
        if (input != null && (HIGHLIGHTER.matcher(input).find() || BRONZE_HIGHLIGHTER.matcher(input).find())) {
            hasMarkup = true;
        }
    }
    
    /**
     * Extract plain text by removing markup
     * @param str input string with markup
     * @return plain text with markup removed
     */
    protected String extractPlainText(String str) {
        if (str == null) return "";
        
        // Remove both underscore markup (_text_) and quote markup ("text")
        return str.replaceAll("_(.*?)_", "$1").replaceAll("\"(.*?)\"", "$1");
    }
    
    /**
     * Set the highlight color for marked text
     */
    public void highlightColor(int color) {
        this.highlightColor = color;
        if (hasMarkup) {
            dirty = true;
        }
    }
    
    /**
     * Set the default color for unmarked text
     */
    public void defaultColor(int color) {
        defaultColor = color;
        dirty = true;
    }
    
    /**
     * Set the bronze color for quoted text
     */
    public void bronzeColor(int color) {
        bronzeColor = color;
        if (hasMarkup) {
            dirty = true;
        }
    }
    
    @Override
    public void text(@NotNull String str) {
        if (text.equals(str)) {
            return;
        }
        
        dirty = true;
        
        // Build the plain text representation for superclass and measurement
        String plainText = extractPlainText(str);
        text = plainText != null ? plainText : "";
        originalText = str; // Keep the original text with markup
        
        // Parse markup in the original text
        parseMarkup(str);
    }
    
    /**
     * Invalidate caches - to be implemented by subclasses
     */
    public static void invalidate() {
        // Platform-specific implementation required
    }
}