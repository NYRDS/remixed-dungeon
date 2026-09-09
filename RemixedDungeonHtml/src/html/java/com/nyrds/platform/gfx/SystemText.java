package com.nyrds.platform.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.platform.util.StringsManager;
import com.watabou.glwrap.Matrix;
import com.watabou.noosa.SystemTextBase;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.typedarrays.Int8Array;

/**
 * Mirror of the desktop FreeType-based SystemText: glyphs are rendered as
 * quads straight through SystemTextPseudoBatch/NoosaScript (the html GL layer
 * uploads quad data to VBOs, so the client-array batch calls work unchanged).
 * Requires the emscripten freetype Module (scripts/freetype.js, loaded via the
 * TeaVMLauncher preload listener).
 */
public class SystemText extends SystemTextBase {
    private static FreeTypeFontGenerator pixelGenerator;
    // The 18MB CJK fallback font is NOT in the boot preload (it would sit in
    // the heap for every player); it is fetched over HTTP the first time a
    // text actually needs it - see requestFallbackFont().
    private static final String FALLBACK_FONT_PATH = "fonts/LXGWWenKaiScreen.ttf";
    private static final String FALLBACK_FONT_URL = "/fonts/LXGWWenKaiScreen.ttf";
    private static FreeTypeFontGenerator fallbackGenerator;
    private static boolean fallbackFontRequested;
    private static final Map<String, BitmapFont> fontCache = new HashMap<>();
    private static final Map<String, BitmapFont.BitmapFontData> pseudoFontCache = new HashMap<>();
    private static BitmapFont.BitmapFontData pixelFontCheckData;

    private final FreeTypeFontParameter fontParameters;
    private BitmapFont.BitmapFontData fontData;
    private GlyphLayout glyphLayout;

    private PseudoGlyphLayout pseudoGlyphLayout;

    private boolean multiline = false;

    private static final SystemTextPseudoBatch batch = new SystemTextPseudoBatch();
    private static final float oversample = 4;

    static {
        invalidate();
    }

    private String fontKey;
    private boolean useFallbackFont = false;

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

    private FreeTypeFontGenerator activeGenerator() {
        return useFallbackFont && fallbackGenerator != null ? fallbackGenerator : pixelGenerator;
    }

    private void ensureFontDataIsReady() {
        if (fontData != null) {
            return;
        }

        FreeTypeFontGenerator activeGenerator = activeGenerator();
        fontKey = (useFallbackFont && fallbackGenerator != null ? "fb_" : "px_") + getFontKey(fontParameters);

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
        if (useFallbackFont && fallbackGenerator != null) {
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

        textLines.clear();

        // Use the base class method to parse markup into segments
        var allSegments = parseMarkupToSegments(originalText);
        hasMarkup = !allSegments.isEmpty() && allSegments.size() > 1;

        // Use the base class method to wrap text, but provide the max width adjusted for oversample
        var wrappedLines =
            wrapText(allSegments, maxWidth * oversample);

        // Convert the base class segments to our local collection and assign to the base field
        for (var line : wrappedLines) {
            ArrayList<ColoredSegment> convertedLine = new ArrayList<>(line);
            textLines.add(convertedLine);
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

        FreeTypeFontGenerator activeGenerator = activeGenerator();

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

        for (List<ColoredSegment> line : textLines) {
            float xPos = 0;
            for (ColoredSegment segment : line) {
                Color currentSegmentColor = fromIntColor(segment.color);
                if (!currentSegmentColor.equals(lastColor)) {
                    font.setColor(currentSegmentColor);
                    lastColor = currentSegmentColor;
                }
                glyphLayout.setText(font, segment.text);
                font.draw(batch, glyphLayout, xPos, yPos);
                xPos += glyphLayout.width;
            }
            if (!textLines.isEmpty()) {
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

            for (val line : textLines) {
                float currentLineWidth = 0;
                for (val segment : line) {
                    pseudoGlyphLayout.setText(fontData, segment.text);
                    currentLineWidth += pseudoGlyphLayout.width;
                }
                if (currentLineWidth > maxLineWidth) {
                    maxLineWidth = currentLineWidth;
                }
            }
            totalHeight = textLines.size() * fontData.lineHeight;

            setWidth(maxLineWidth);
            setHeight(totalHeight);
            dirty = false;
        }

        // Respect minimum height
        if (height < minHeight) {
            setHeight(minHeight);
        }
    }

    @Override
    public float baseLine() {
        if (fontData == null) return 0;
        return (fontData.lineHeight) / oversample;
    }

    @Override
    public int lines() {
        return textLines.size();
    }

    private static boolean containsMissingChars(@NotNull String text) {
        if (pixelFontCheckData == null) return false;
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
        if (FileSystem.exists(FALLBACK_FONT_PATH)) {
            fallbackGenerator = new FreeTypeFontGenerator(FileSystem.getInternalStorageFileHandle(FALLBACK_FONT_PATH));
        } else {
            fallbackGenerator = null;
        }

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
    protected float measureTextWidth(String text) {
        if (fontData == null) {
            ensureFontDataIsReady();
        }
        pseudoGlyphLayout.setText(fontData, text);
        return pseudoGlyphLayout.width;
    }

    @Override
    public void text(@NotNull String str) {
        // Use base class implementation for plain text extraction
        super.text(str);

        this.dirty = true;
        String plainText = extractPlainText(str); // Use base class method
        boolean wantFallback = !GamePreferences.classicFont() || containsMissingChars(plainText);
        this.useFallbackFont = wantFallback && fallbackGenerator != null;
        // Fetch the 18MB font only for real need: the classic-font opt-in
        // (wants LXGW for everything) or glyphs the pixel font lacks (CJK
        // text). The default "modern" look is the pixel font on web - it was
        // the only option before the fallback existed - so plain Latin boots
        // never download it.
        if (fallbackGenerator == null
                && (GamePreferences.classicFont() || containsMissingChars(plainText))) {
            requestFallbackFont();
        }

        // Invalidate font data if font type changes
        String newFontKey = (useFallbackFont ? "fb_" : "px_") + getFontKey(fontParameters);
        if (this.fontKey != null && !this.fontKey.equals(newFontKey)) {
            this.fontData = null;
        }
    }

    /**
     * Fetches the CJK fallback font over HTTP on first need (CJK locale or a
     * glyph the pixel font lacks), installs the bytes into the in-memory FS
     * where invalidate() expects them, then rebuilds caches and the scene so
     * existing texts pick the fallback up. While the fetch is in flight (and
     * after a failure) texts keep the pixel font, exactly like before.
     */
    private static void requestFallbackFont() {
        if (fallbackFontRequested) {
            return;
        }
        fallbackFontRequested = true;
        fetchFontBytes(FALLBACK_FONT_URL, data -> Gdx.app.postRunnable(() -> {
            if (data == null) {
                EventCollector.logEvent("cjk_font_fetch_failed");
                return;
            }
            try {
                OutputStream out = Gdx.files.internal(FALLBACK_FONT_PATH).write(false, 1 << 16);
                out.write(data.copyToJavaArray());
                out.close();
                invalidate();
                GameLoop.setNeedSceneRestart();
                EventCollector.logEvent("cjk_font_ready");
            } catch (Exception e) {
                EventCollector.logException(e, "cjk font install");
            }
        }));
    }

    @JSFunctor
    private interface ByteArrayCallback extends JSObject {
        void accept(Int8Array data);
    }

    @JSBody(params = {"url", "cb"}, script =
            "fetch(url).then(function(r){ if (!r.ok) throw new Error(r.status); return r.arrayBuffer(); })"
            + ".then(function(b){ cb(new Int8Array(b)); }, function(){ cb(null); })")
    private static native void fetchFontBytes(String url, ByteArrayCallback cb);

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
}
