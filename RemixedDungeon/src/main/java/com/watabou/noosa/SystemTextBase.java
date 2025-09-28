package com.watabou.noosa;

import com.nyrds.pixeldungeon.game.GamePreferences;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class SystemTextBase extends Text {
    // Combined markup pattern for violet highlighting (_text_) and bronze ("text")
    protected static final Pattern MARKUP_PATTERN = Pattern.compile("_(.*?)_|\"(.*?)\"");
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
    
    /**
     * A segment of text with its associated color.
     * Platform-specific implementations will handle color representation differently.
     */
    protected static class ColoredSegment {
        public String text;
        public int color; // Store as int, subclasses can convert to their platform-specific color type
        
        public ColoredSegment(String text, int color) {
            this.text = text;
            this.color = color;
        }
    }
    
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
     * Parse markup in the input string and convert to a list of colored segments.
     * @param input The original text with markup
     * @return List of ColoredSegment objects
     */
    protected List<ColoredSegment> parseMarkupToSegments(String input) {
        if (input == null) input = "";

        List<ColoredSegment> segments = new ArrayList<>();
        Matcher m = MARKUP_PATTERN.matcher(input);
        int lastEnd = 0;

        while (m.find()) {
            if (m.start() > lastEnd) {
                segments.add(new ColoredSegment(input.substring(lastEnd, m.start()), defaultColor));
            }
            if (m.group(1) != null) {
                segments.add(new ColoredSegment(m.group(1), highlightColor));
            } else if (m.group(2) != null) {
                segments.add(new ColoredSegment(m.group(2), bronzeColor));
            }
            lastEnd = m.end();
        }
        if (lastEnd < input.length()) {
            segments.add(new ColoredSegment(input.substring(lastEnd), defaultColor));
        }
        return segments;
    }

    /**
     * Wrap lines based on available width.
     * @param allSegments The list of colored segments to wrap into lines
     * @param maxWidth The maximum width for a line
     * @return List of lines, where each line is a list of ColoredSegment
     */
    protected List<List<ColoredSegment>> wrapText(List<ColoredSegment> allSegments, float maxWidth) {
        List<List<ColoredSegment>> linesOfSegments = new ArrayList<>();
        List<ColoredSegment> currentLine = new ArrayList<>();
        float currentLineWidth = 0;

        for (ColoredSegment segment : allSegments) {
            String[] paragraphs = segment.text.split("\n", -1);

            for (int p = 0; p < paragraphs.length; p++) {
                String paragraph = paragraphs[p];
                String[] words = paragraph.split("(?<=\\s)|(?=\\s)"); // Split while keeping spaces

                for (String word : words) {
                    if (word.isEmpty()) continue;

                    float wordWidth = measureTextWidth(word);

                    if (!currentLine.isEmpty() && currentLineWidth + wordWidth > maxWidth) {
                        linesOfSegments.add(currentLine);
                        currentLine = new ArrayList<>();
                        currentLineWidth = 0;
                    }

                    currentLine.add(new ColoredSegment(word, segment.color));
                    currentLineWidth += wordWidth;
                }

                if (p < paragraphs.length - 1) { // This was a newline character
                    linesOfSegments.add(currentLine);
                    currentLine = new ArrayList<>();
                    currentLineWidth = 0;
                }
            }
        }

        if (!currentLine.isEmpty()) {
            linesOfSegments.add(currentLine);
        }

        return linesOfSegments;
    }

    /**
     * Measure the width of a text string. This must be implemented by platform-specific subclasses.
     * @param text The text to measure
     * @return The width of the text
     */
    protected abstract float measureTextWidth(String text);
    
    /**
     * Store the text lines after parsing and wrapping
     */
    protected final ArrayList<ArrayList<ColoredSegment>> textLines = new ArrayList<>();
    
    /**
     * Invalidate caches - to be implemented by subclasses
     */
    public static void invalidate() {
        // Platform-specific implementation required
    }
}