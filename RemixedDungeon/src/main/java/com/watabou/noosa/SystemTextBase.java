package com.watabou.noosa;

import com.nyrds.pixeldungeon.game.GamePreferences;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public abstract class SystemTextBase extends Text {
    // Markup pattern for highlighting (_text_)
    protected static final Pattern HIGHLIGHTER = Pattern.compile("_(.*?)_");
    
    protected static float fontScale = Float.NaN;
    
    // Color properties
    protected int highlightColor;
    protected int defaultColor = 0xFFFFFFFF; // White color (ARGB)
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
     * This is a basic implementation that subclasses can override
     */
    protected void parseMarkup(String input) {
        // Basic implementation - subclasses should provide their own
        if (input != null && HIGHLIGHTER.matcher(input).find()) {
            hasMarkup = true;
        }
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
        this.defaultColor = color;
        dirty = true;
    }
    
    @Override
    public void text(@NotNull String str) {
        if (text.equals(str)) {
            return;
        }
        
        dirty = true;
        text = str != null ? str : "";
        originalText = text;
        
        // Parse markup in the text
        parseMarkup(text);
    }
    
    /**
     * Invalidate caches - to be implemented by subclasses
     */
    public static void invalidate() {
        // Platform-specific implementation required
    }
}