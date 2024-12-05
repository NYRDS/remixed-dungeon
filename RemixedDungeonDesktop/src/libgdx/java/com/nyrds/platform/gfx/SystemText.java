package com.nyrds.platform.gfx;

import static com.nyrds.pixeldungeon.ml.BuildConfig.ASSETS_PATH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.platform.util.PUtil;
import com.nyrds.platform.util.StringsManager;
import com.watabou.glwrap.Matrix;
import com.watabou.noosa.Text;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class SystemText extends Text {
    private static FreeTypeFontGenerator generator;
    private final FreeTypeFontParameter fontParameters;
    private final BitmapFont font;
    private final GlyphLayout glyphLayout;
    private final GlyphLayout spaceLayout; // New instance to measure space width
    private boolean multiline = false;

    private final ArrayList<ArrayList<String>> lines = new ArrayList<>();

    private static float fontScale = Float.NaN;

    private static final SystemTextPseudoBatch batch = new SystemTextPseudoBatch();
    private static final float oversample = 4;
    private ArrayList<Boolean> wordMask = null;
    static {
        generator = new FreeTypeFontGenerator(FileSystem.getInternalStorageFileHandle("fonts/pixel_font.ttf"));
    }

    public SystemText(float baseLine) {
        super(0, 0, 0, 0);
        fontParameters = getFontParameters(baseLine);

        font = generator.generateFont(fontParameters);
        glyphLayout = new GlyphLayout();
        spaceLayout = new GlyphLayout(); // Initialize spaceLayout
        spaceLayout.setText(font, " "); // Measure the width of a space
    }

    private FreeTypeFontParameter getFontParameters(float baseLine) {
        if (fontScale != fontScale) {
            updateFontScale();
        }

        final FreeTypeFontParameter fontParameters;
        fontParameters = new FreeTypeFontParameter();
        fontParameters.characters = FreeTypeFontGenerator.DEFAULT_CHARS + StringsManager.getAllCharsAsString();
        fontParameters.size = (int) (baseLine * oversample * fontScale);
        fontParameters.borderColor = Color.BLACK;
        fontParameters.borderWidth = oversample * fontScale;
        fontParameters.flip = true;
        fontParameters.genMipMaps = true;
        fontParameters.magFilter = Texture.TextureFilter.Linear;
        fontParameters.minFilter = Texture.TextureFilter.MipMapLinearLinear;
        fontParameters.spaceX = 0;
        fontParameters.spaceY = 0;
        return fontParameters;
    }

    public SystemText(final String text, float size, boolean multiline) {
        super(0, 0, 0, 0);
        fontParameters = getFontParameters(size);

        font = generator.generateFont(fontParameters);
        glyphLayout = new GlyphLayout();
        spaceLayout = new GlyphLayout(); // Initialize spaceLayout
        spaceLayout.setText(font, " "); // Measure the width of a space
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

    private void wrapText() {
        if (multiline && maxWidth == Integer.MAX_VALUE) {
            return;
        }

        lines.clear();
        if(mask != null) {
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
                glyphLayout.setText(font, word);
                if(mask != null) {
                    wordMask.add(mask[index]);
                }
                if ((line_width + glyphLayout.width + spaceLayout.width)/oversample <= maxWidth) {
                    currentLine.add(word);
                    line_width += glyphLayout.width + spaceLayout.width;
                } else {
                    lines.add(currentLine);
                    currentLine = new ArrayList<>();
                    currentLine.add(word);
                    line_width = glyphLayout.width + spaceLayout.width;
                }
                index+=word.length();
            }
            if (!currentLine.isEmpty()) {
                lines.add(currentLine);
            }
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        font.dispose();
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
        if(dirty) {
            lines.clear();
            measure();
        }

        updateMatrix();
        SystemTextPseudoBatch.textBeingRendered = this;
        float y = 0;
        int wi = 0;
        for (ArrayList<String> line : lines) {
            float x = 0;
            for (String word : line) {
                glyphLayout.setText(font, word);
                if(wordMask == null || wordMask.get(wi)) {
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
        if (lines.isEmpty()) {
            wrapText();
        }

        // Calculate total height and maximum width for wrapped lines
        float totalHeight = 0;
        float maxWidth = 0;
        for (ArrayList<String> line : lines) {
            if (!line.isEmpty()) {
                float line_width = 0;
                for (String word : line) {
                    glyphLayout.setText(font, word);
                    line_width += glyphLayout.width + spaceLayout.width; // Use spaceLayout.width
                }
                if (line_width > maxWidth) {
                    maxWidth = line_width;
                }
                totalHeight += glyphLayout.height + 5;
            }
        }
        setWidth(maxWidth);
        setHeight(totalHeight);
    }

    @Override
    public float baseLine() {
        return (font.getLineHeight() + 7) / oversample;
    }

    @Override
    public int lines() {
        return lines.size();
    }

    public static void invalidate() {
        if (generator != null) {
            generator.dispose();
            generator = new FreeTypeFontGenerator(FileSystem.getInternalStorageFileHandle("fonts/pixel_font.ttf"));
        }
    }

    @Override
    public void text(@NotNull String str) {
        lines.clear();
        super.text(str);
    }
}