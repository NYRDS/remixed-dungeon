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
import com.watabou.noosa.Text;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SystemText extends Text {
    private static FreeTypeFontGenerator generator;
    private static final Map<String, BitmapFont> fontCache = new HashMap<>();
    private static final Map<String, BitmapFont.BitmapFontData> pseudoFontCache = new HashMap<>();

    private final FreeTypeFontParameter fontParameters;
    private final BitmapFont.BitmapFontData fontData;
    private final PseudoGlyphLayout pseudoGlyphLayout;
    private GlyphLayout glyphLayout;

    private final PseudoGlyphLayout spaceLayout; // New instance to measure space width
    private boolean multiline = false;

    private final ArrayList<ArrayList<String>> lines = new ArrayList<>();

    private static float fontScale = Float.NaN;

    private static final SystemTextPseudoBatch batch = new SystemTextPseudoBatch();
    private static final float oversample = 4;
    private ArrayList<Boolean> wordMask = null;

    static {
        invalidate();
    }

    final private String fontKey;

    public SystemText(float baseLine) {
        super(0, 0, 0, 0);
        fontParameters = getPseudoFontParameters(baseLine);

        fontKey = getFontKey(fontParameters);
        synchronized (pseudoFontCache) {
            if (!pseudoFontCache.containsKey(fontKey)) {
                BitmapFont.BitmapFontData fontData = generator.generateData(fontParameters);
                pseudoFontCache.put(fontKey, fontData);
            }
            fontData = pseudoFontCache.get(fontKey);
        }

        pseudoGlyphLayout = new PseudoGlyphLayout();
        spaceLayout = new PseudoGlyphLayout(); // Initialize spaceLayout
        spaceLayout.setText(fontData, " "); // Measure the width of a space
    }

    public SystemText(final String text, float size, boolean multiline) {
        this(size);
        this.text(text);
        this.multiline = multiline;
        wrapText();
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

        fontScale = scale;
    }

    private FreeTypeFontParameter getFontParameters(float baseLine) {
        if (fontScale != fontScale) {
            updateFontScale();
        }

        final FreeTypeFontParameter fontParameters;
        fontParameters = new FreeTypeFontParameter();
        fontParameters.characters = FreeTypeFontGenerator.DEFAULT_CHARS + StringsManager.getAllCharsAsString();
        //PUtil.slog("font", "characters " + fontParameters.characters);
        fontParameters.size = (int) (baseLine * oversample * fontScale);
        fontParameters.borderColor = Color.BLACK;
        fontParameters.borderWidth = oversample * fontScale;
        fontParameters.flip = true;
        fontParameters.genMipMaps = true;
        fontParameters.magFilter = Texture.TextureFilter.Linear;
        fontParameters.minFilter = Texture.TextureFilter.MipMapLinearLinear;
        fontParameters.spaceX = -2;
        fontParameters.spaceY = 0;
        return fontParameters;
    }

    private FreeTypeFontParameter getPseudoFontParameters(float baseLine) {
        FreeTypeFontParameter fontParameter = getFontParameters(baseLine);
        fontParameter.packer = new PseudoPixmapPacker();
        return fontParameter;
    }

    private String getFontKey(FreeTypeFontParameter params) {
        return params.size + "_" + params.characters + "_" + params.borderColor + "_" + params.borderWidth + "_" + params.flip + "_" + params.genMipMaps + "_" + params.magFilter + "_" + params.minFilter + "_" + params.spaceX + "_" + params.spaceY;
    }

    private void wrapText() {
        if (multiline && maxWidth == Integer.MAX_VALUE) {
            return;
        }

        lines.clear();
        if (mask != null) {
            wordMask = new ArrayList<>();
        }

        int index = 0;
        // Perform wrapping based on maxWidth
        String[] paragraphs = text.split("\n");
        for (String paragraph : paragraphs) {
            if (paragraph.isEmpty()) {
                lines.add(new ArrayList<>());
                continue;
            }
            String[] words = paragraph.split("\\s+");
            ArrayList<String> currentLine = new ArrayList<>();
            float line_width = 0;
            for (String word : words) {
                pseudoGlyphLayout.setText(fontData, word);
                if (mask != null) {
                    wordMask.add(mask[index]);
                }
                if ((line_width + pseudoGlyphLayout.width + spaceLayout.width) / oversample <= maxWidth) {
                    currentLine.add(word);
                    line_width += pseudoGlyphLayout.width + spaceLayout.width;
                } else {
                    lines.add(currentLine);
                    currentLine = new ArrayList<>();
                    currentLine.add(word);
                    line_width = pseudoGlyphLayout.width + spaceLayout.width;
                }
                index += word.length();
            }
            if (!currentLine.isEmpty()) {
                lines.add(currentLine);
            }
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        // No need to dispose font here as it's managed by the cache
    }

    @Override
    public void kill() {
        super.kill();
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
        if (dirty) {
            lines.clear();
            measure();
        }

        BitmapFont font;
        synchronized (fontCache) {
            if (!fontCache.containsKey(fontKey)) {
                fontParameters.packer = null;
                fontCache.put(fontKey, generator.generateFont(fontParameters));
            }
            font = fontCache.get(fontKey);
        }

        if(glyphLayout == null) {
            glyphLayout = new GlyphLayout();
        }

        updateMatrix();
        SystemTextPseudoBatch.textBeingRendered = this;
        float y = 0;
        int wi = 0;
        for (ArrayList<String> line : lines) {
            float x = 0;
            for (String word : line) {
                glyphLayout.setText(font, word);
                if (wordMask == null || wordMask.get(wi)) {
                    font.draw(batch, glyphLayout, x, y);
                }
                x += glyphLayout.width + spaceLayout.width; // Use spaceLayout.width
                wi++;
            }

            y += glyphLayout.height + 5;
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
        if (lines.isEmpty() || dirty) {
            wrapText();
        }

        // Calculate total height and maximum width for wrapped lines
        float totalHeight = 0;
        float maxWidth = 0;
        for (ArrayList<String> line : lines) {
            if (!line.isEmpty()) {
                float line_width = 0;
                for (String word : line) {
                    pseudoGlyphLayout.setText(fontData, word);
                    line_width += pseudoGlyphLayout.width + spaceLayout.width; // Use spaceLayout.width
                }
                if (line_width > maxWidth) {
                    maxWidth = line_width;
                }
                totalHeight += pseudoGlyphLayout.height + 5;
            }
        }
        setWidth(maxWidth);
        setHeight(totalHeight);
    }

    @Override
    public float baseLine() {
        return (fontData.lineHeight + 7) / oversample;
    }

    @Override
    public int lines() {
        return lines.size();
    }

    public static void invalidate() {
        if (generator != null) {
            generator.dispose();
        }

        //if (GamePreferences.classicFont()) {
        //    generator = new FreeTypeFontGenerator(FileSystem.getInternalStorageFileHandle("fonts/pixel_font.ttf"));
        //} else {
        generator = new FreeTypeFontGenerator(FileSystem.getInternalStorageFileHandle("fonts/LXGWWenKaiScreen.ttf"));
        //}

        synchronized (fontCache) {
            for (BitmapFont font : fontCache.values()) {
                font.dispose();
            }
            fontCache.clear();
        }
    }

    @Override
    public void text(@NotNull String str) {
        dirty = true;
        lines.clear();
        super.text(str);
    }
}