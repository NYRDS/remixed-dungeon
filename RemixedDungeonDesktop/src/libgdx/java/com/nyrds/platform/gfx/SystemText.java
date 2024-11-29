package com.nyrds.platform.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.platform.util.PUtil;
import com.watabou.glwrap.Matrix;
import com.watabou.noosa.Text;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class SystemText extends Text {
    private static FreeTypeFontGenerator generator;
    private final FreeTypeFontParameter fontParameters;
    private final BitmapFont font;
    private final GlyphLayout glyphLayout;
    private boolean multiline = false;

    private final ArrayList<String> lines = new ArrayList<>();;

    private static float fontScale = Float.NaN;

    private static final SystemTextPseudoBatch batch = new SystemTextPseudoBatch();
    private static final float oversample = 4;

    static {
        generator = new FreeTypeFontGenerator(Gdx.files.internal("../assets/fonts/pixel_font.ttf"));
    }

    public SystemText(float baseLine) {
        super(0, 0, 0, 0);
        fontParameters = getFontParameters(baseLine);

        font = generator.generateFont(fontParameters);
        glyphLayout = new GlyphLayout();
    }

    private FreeTypeFontParameter getFontParameters(float baseLine) {
        if (fontScale != fontScale) {
            updateFontScale();
        }

        final FreeTypeFontParameter fontParameters;
        fontParameters = new FreeTypeFontParameter();
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

        // Perform wrapping based on maxWidth
        String[] paragraphs = text.split("\n");
        for (String paragraph : paragraphs) {
            if (paragraph.isEmpty()) {
                lines.add("");
                continue;
            }
            String[] words = paragraph.split("\\s+");
            String line = "";
            for (String word : words) {
                if (line.isEmpty()) {
                    line = word;
                } else {
                    String candidate = line + " " + word;
                    glyphLayout.setText(font, candidate);
                    if (glyphLayout.width / oversample <= maxWidth) {
                        PUtil.slog("text", "line: " + line + " width: " + glyphLayout.width + " max: " + maxWidth);
                        line = candidate;
                    } else {
                        PUtil.slog("text", "line: " + line + " width: " + glyphLayout.width + " max: " + maxWidth);
                        lines.add(line);
                        line = word;
                    }
                }
            }
            if (!line.isEmpty()) {
                lines.add(line);
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
        destroy();
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
        updateMatrix();
        SystemTextPseudoBatch.textBeingRendered = this;

        float y = 0;
        for (String line : lines) {
            if (!line.isEmpty()) {
                glyphLayout.setText(font, line);
                font.draw(batch, glyphLayout, 0, y);
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
        for (String line : lines) {
            if (!line.isEmpty()) {
                glyphLayout.setText(font, line);
                totalHeight += glyphLayout.height + 7;
                if (glyphLayout.width > maxWidth) {
                    maxWidth = glyphLayout.width;
                }
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
            generator = new FreeTypeFontGenerator(Gdx.files.internal("../assets/fonts/pixel_font.ttf"));
        }
    }

    @Override
    public void text(@NotNull String str) {
        lines.clear();
        super.text(str);
    }
}