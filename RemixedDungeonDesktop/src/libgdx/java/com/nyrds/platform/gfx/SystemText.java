package com.nyrds.platform.gfx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.platform.util.StringsManager;
import com.watabou.glwrap.Matrix;
import com.watabou.noosa.SystemTextBase;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SystemText extends SystemTextBase {
    private static FreeTypeFontGenerator pixelGenerator;
    private static FreeTypeFontGenerator fallbackGenerator;
    // Combined markup pattern for violet highlighting (_text_) and bronze ("text")
    private static final Pattern MARKUP_PATTERN = Pattern.compile("_(.*?)_|\"(.*?)\"");

    private static final Map<String, BitmapFont> fontCache = new HashMap<>();
    private static final Map<String, BitmapFont.BitmapFontData> pseudoFontCache = new HashMap<>();
    private static BitmapFont.BitmapFontData pixelFontCheckData;

    private final FreeTypeFontParameter fontParameters;
    private BitmapFont.BitmapFontData fontData;
    private GlyphLayout glyphLayout;

    private PseudoGlyphLayout pseudoGlyphLayout;

    private boolean multiline = false;

    // A line is composed of segments, each with its own text and color.
    private static class ColoredSegment {
        String text;
        Color color;

        ColoredSegment(String text, Color color) {
            this.text = text;
            this.color = color;
        }
    }

    private final ArrayList<ArrayList<ColoredSegment>> lines = new ArrayList<>();

    private static final SystemTextPseudoBatch batch = new SystemTextPseudoBatch();
    private static final float oversample = 4;

    static {
        invalidate();
    }

    private String fontKey;
    private boolean useFallbackFont = false;

    private Color defaultColor = Color.WHITE;
    private Color highlightColor = new Color(0xcc / 255f, 0x33 / 255f, 0xff / 255f, 1);
    private Color bronzeColor = new Color(0xCD / 255f, 0x7F / 255f, 0x32 / 255f, 1);

    public SystemText(float baseLine) {
        super(0, 0, 0, 0);
        FreeTypeFontParameter fontParameter = getFontParameters(baseLine);
        fontParameter.packer = new PseudoPixmapPacker();
        fontParameters = fontParameter;
        this.originalText = "";
    }

    public SystemText(final String text, float size, boolean multiline) {
        this(size);
        this.multiline = multiline;
        text(text); // Sets originalText and triggers wrapping
    }

    public static void updateFontScale() {
        float scale = 0.5f + 0.01f * (GamePreferences.fontScale() + 9);
        scale *= 1.2f;
        fontScale = Math.max(0.1f, Math.min(4f, scale));
    }

    private FreeTypeFontParameter getFontParameters(float baseLine) {
        if (Float.isNaN(fontScale)) {
            updateFontScale();
        }

        final FreeTypeFontParameter fontParameters = new FreeTypeFontParameter();
        fontParameters.characters = FreeTypeFontGenerator.DEFAULT_CHARS + StringsManager.getAllCharsAsString();
        fontParameters.size = (int) (baseLine * oversample * fontScale);
        fontParameters.borderColor = Color.BLACK;
        fontParameters.borderWidth = oversample * fontScale;
        fontParameters.flip = true;
        fontParameters.genMipMaps = false;
        fontParameters.magFilter = Texture.TextureFilter.Linear;
        fontParameters.minFilter = Texture.TextureFilter.Linear;
        return fontParameters;
    }

    private String getFontKey(FreeTypeFontParameter params) {
        return params.size + "_" + params.characters + "_" + params.borderColor + "_" + params.borderWidth + "_" + params.flip + "_" + params.genMipMaps + "_" + params.magFilter + "_" + params.minFilter + "_" + params.spaceX;
    }

    private void ensureFontDataIsReady() {
        if (fontData != null) {
            return;
        }

        FreeTypeFontGenerator activeGenerator = useFallbackFont ? fallbackGenerator : pixelGenerator;
        fontKey = (useFallbackFont ? "fb_" : "px_") + getFontKey(fontParameters);

        synchronized (pseudoFontCache) {
            if (!pseudoFontCache.containsKey(fontKey)) {
                adjustFontParams();
                BitmapFont.BitmapFontData generatedData = activeGenerator.generateData(fontParameters);
                pseudoFontCache.put(fontKey, generatedData);
            }
            fontData = pseudoFontCache.get(fontKey);
        }

        pseudoGlyphLayout = new PseudoGlyphLayout();
    }

    private void adjustFontParams() {
        if (useFallbackFont) {
            fontParameters.spaceX = -1;
            fontParameters.spaceY = -2;
        } else {
            fontParameters.spaceX = 0;
            fontParameters.spaceY = 0;
        }
    }

    private void parseMarkupAndWrap() {
        ensureFontDataIsReady();

        if (multiline && maxWidth == Integer.MAX_VALUE) {
            return;
        }

        lines.clear();
        String textToParse = (this.originalText == null) ? "" : this.originalText;

        // Create a flat list of colored segments from the original text with markup
        ArrayList<ColoredSegment> allSegments = new ArrayList<>();
        Matcher m = MARKUP_PATTERN.matcher(textToParse);
        int lastEnd = 0;

        while (m.find()) {
            if (m.start() > lastEnd) {
                allSegments.add(new ColoredSegment(textToParse.substring(lastEnd, m.start()), defaultColor));
            }
            if (m.group(1) != null) {
                // Violet highlight for _text_
                allSegments.add(new ColoredSegment(m.group(1), highlightColor));
            } else if (m.group(2) != null) {
                // Bronze color for "text"
                allSegments.add(new ColoredSegment(m.group(2), bronzeColor));
            }
            lastEnd = m.end();
        }
        if (lastEnd < textToParse.length()) {
            allSegments.add(new ColoredSegment(textToParse.substring(lastEnd), defaultColor));
        }

        hasMarkup = !allSegments.isEmpty() && allSegments.size() > 1;

        // Now, wrap the flat list of segments into lines
        ArrayList<ColoredSegment> currentLine = new ArrayList<>();
        float currentLineWidth = 0;

        for (ColoredSegment segment : allSegments) {
            String[] paragraphs = segment.text.split("\n", -1);

            for (int p = 0; p < paragraphs.length; p++) {
                String paragraph = paragraphs[p];
                String[] words = paragraph.split("(?<=\\s)|(?=\\s)"); // Split while keeping spaces

                for (String word : words) {
                    if (word.isEmpty()) continue;

                    pseudoGlyphLayout.setText(fontData, word);
                    float wordWidth = pseudoGlyphLayout.width;

                    if (multiline && !currentLine.isEmpty() && currentLineWidth + wordWidth > (maxWidth * oversample)) {
                        lines.add(currentLine);
                        currentLine = new ArrayList<>();
                        currentLineWidth = 0;
                    }

                    currentLine.add(new ColoredSegment(word, segment.color));
                    currentLineWidth += wordWidth;
                }

                if (p < paragraphs.length - 1) { // This was a newline character
                    lines.add(currentLine);
                    currentLine = new ArrayList<>();
                    currentLineWidth = 0;
                }
            }
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine);
        }
    }


    @Override
    protected void updateMatrix() {
        if (dirtyMatrix) {
            Matrix.setIdentity(matrix);
            Matrix.translate(matrix, x, y);
            if (angle != 0) {
                Matrix.rotate(matrix, angle);
            }
            Matrix.scale(matrix, scale.x / oversample, scale.y / oversample);
            dirtyMatrix = false;
        }
    }

    @Override
    public void draw() {
        measure();

        FreeTypeFontGenerator activeGenerator = useFallbackFont ? fallbackGenerator : pixelGenerator;

        BitmapFont font;
        synchronized (fontCache) {
            if (!fontCache.containsKey(fontKey)) {
                fontParameters.packer = null; // Use default packer for real font generation
                adjustFontParams();
                fontCache.put(fontKey, activeGenerator.generateFont(fontParameters));
                fontParameters.packer = new PseudoPixmapPacker(); // Restore for future pseudo-generations
            }
            font = fontCache.get(fontKey);
        }

        if (glyphLayout == null) {
            glyphLayout = new GlyphLayout();
        }

        updateMatrix();
        SystemTextPseudoBatch.textBeingRendered = this;

        float yPos = 0;
        Color lastColor = null;

        for (ArrayList<ColoredSegment> line : lines) {
            float xPos = 0;
            for (ColoredSegment segment : line) {
                if (!segment.color.equals(lastColor)) {
                    font.setColor(segment.color);
                    lastColor = segment.color;
                }
                glyphLayout.setText(font, segment.text);
                font.draw(batch, glyphLayout, xPos, yPos);
                xPos += glyphLayout.width;
            }
            if (!lines.isEmpty()) {
                yPos += fontData.lineHeight;
            }
        }
    }

    @Override
    public void setWidth(float width) {
        super.setWidth(width / oversample);
    }

    @Override
    public void setHeight(float height) {
        super.setHeight(height / oversample);
    }

    @Override
    protected void measure() {
        if (dirty) {
            parseMarkupAndWrap();

            float totalHeight = 0;
            float maxLineWidth = 0;

            if (!lines.isEmpty()){
                for (ArrayList<ColoredSegment> line : lines) {
                    float currentLineWidth = 0;
                    for (ColoredSegment segment : line) {
                        pseudoGlyphLayout.setText(fontData, segment.text);
                        currentLineWidth += pseudoGlyphLayout.width;
                    }
                    if (currentLineWidth > maxLineWidth) {
                        maxLineWidth = currentLineWidth;
                    }
                }
                totalHeight = lines.size() * fontData.lineHeight;
            }

            setWidth(maxLineWidth);
            setHeight(totalHeight);
            dirty = false;
        }
    }

    @Override
    public float baseLine() {
        if (fontData == null) return 0;
        return (fontData.lineHeight) / oversample;
    }

    @Override
    public int lines() {
        return lines.size();
    }

    private static boolean containsMissingChars(@NotNull String text) {
        if (pixelFontCheckData == null) return true;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (pixelFontCheckData.isWhitespace(ch)) {
                continue;
            }

            if (pixelFontCheckData.getGlyph(ch) == null) {
                return true;
            }
        }
        return false;
    }

    public static void invalidate() {
        if (pixelGenerator != null) pixelGenerator.dispose();
        if (fallbackGenerator != null) fallbackGenerator.dispose();

        pixelGenerator = new FreeTypeFontGenerator(FileSystem.getInternalStorageFileHandle("fonts/pixel_font.ttf"));
        fallbackGenerator = new FreeTypeFontGenerator(FileSystem.getInternalStorageFileHandle("fonts/LXGWWenKaiScreen.ttf"));

        synchronized (fontCache) {
            for (BitmapFont font : fontCache.values()) {
                font.dispose();
            }
            fontCache.clear();
        }

        synchronized (pseudoFontCache){
            pseudoFontCache.clear();
        }

        FreeTypeFontParameter checkParams = new FreeTypeFontParameter();
        checkParams.characters = FreeTypeFontGenerator.DEFAULT_CHARS + StringsManager.getAllCharsAsString();
        checkParams.packer = new PseudoPixmapPacker();
        pixelFontCheckData = pixelGenerator.generateData(checkParams);
    }

    @Override
    public void text(@NotNull String str) {
        // Build the plain text representation for superclass and measurement
        String plainText = str.replaceAll("_(.*?)_", "$1").replaceAll("\"(.*?)\"", "$1");
        super.text(plainText); // Superclass holds the plain text

        this.originalText = str; // Keep the original text with markup
        this.dirty = true;
        this.useFallbackFont = !GamePreferences.classicFont() || containsMissingChars(plainText);

        // Invalidate font data if font type changes
        String newFontKey = (useFallbackFont ? "fb_" : "px_") + getFontKey(fontParameters);
        if (this.fontKey != null && !this.fontKey.equals(newFontKey)) {
            this.fontData = null;
        }
    }

    /**
     * Converts an Android-style ARGB integer color to a LibGDX Color object.
     */
    private Color fromIntColor(int color) {
        float a = ((color >> 24) & 0xFF) / 255.0f;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        return new Color(r, g, b, a);
    }

    /**
     * Set the highlight color for marked text.
     * @param color An ARGB integer color, same as used in the Android version.
     */
    public void highlightColor(int color) {
        this.highlightColor = fromIntColor(color);
        if (hasMarkup) {
            dirty = true;
        }
    }

    /**
     * Set the default color for unmarked text.
     * @param color An ARGB integer color, same as used in the Android version.
     */
    public void defaultColor(int color) {
        this.defaultColor = fromIntColor(color);
        dirty = true;
    }
}