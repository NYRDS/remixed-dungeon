package com.nyrds.platform.gfx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.watabou.glwrap.Matrix;
import com.watabou.noosa.Text;

import java.util.HashMap;
import java.util.Map;

public class SystemText extends Text {
    private static final Map<String, BitmapFont> fontCache = new HashMap<>();
    private static BitmapFont defaultFont;
    
    private BitmapFont font;
    private GlyphLayout glyphLayout;
    
    public SystemText(String text, float size, boolean multiline) {
        super(text, 0, 0, 0);
        initFont();
        glyphLayout = new GlyphLayout();
        updateText();
    }
    
    public SystemText(String text, float x, float y, int align) {
        super(text, x, y, align);
        initFont();
        glyphLayout = new GlyphLayout();
        updateText();
    }
    
    public SystemText(String text, float x, float y, int maxWidth, int align) {
        super(text, x, y, maxWidth, align);
        initFont();
        glyphLayout = new GlyphLayout();
        updateText();
    }
    
    public SystemText(float baseLine) {
        super(0, 0, 0, 0);
        initFont();
        glyphLayout = new GlyphLayout();
    }
    
    private void initFont() {
        if (defaultFont == null) {
            // Create a simple default font for HTML version
            defaultFont = new BitmapFont();
        }
        font = defaultFont;
    }
    
    @Override
    public void text(String str) {
        super.text(str);
        updateText();
    }
    
    private void updateText() {
        if (font != null && text != null) {
            glyphLayout.setText(font, text);
            width = glyphLayout.width;
            height = glyphLayout.height;
        }
    }
    
    public static void invalidate() {
        // Clear font cache
        fontCache.clear();
        defaultFont = null;
    }
    
    public static boolean needFontReload() {
        return false;
    }
    
    public static void reloadFonts() {
        // Reload fonts if needed
    }
    
    @Override
    public float baseLine() {
        return 12f; // Simple implementation for HTML version
    }
}