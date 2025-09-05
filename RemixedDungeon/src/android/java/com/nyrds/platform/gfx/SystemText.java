package com.nyrds.platform.gfx;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;

import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.platform.app.RemixedDungeonApp;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.util.TrackedRuntimeException;
import com.nyrds.util.LRUCache;
import com.nyrds.util.ModdingMode;
import com.watabou.glwrap.Matrix;
import com.watabou.noosa.Group;
import com.watabou.noosa.SystemTextBase;
import com.watabou.noosa.SystemTextLine;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Synchronized;

public class SystemText extends SystemTextBase {

    static private final Map<Float, TextPaint> textPaints = new ConcurrentHashMap<>();
    private final TextPaint textPaint;

    static private final Map<Float, TextPaint> contourPaints = new ConcurrentHashMap<>();
    private final TextPaint contourPaint;

    private final ArrayList<SystemTextLine> lineImage = new ArrayList<>();

    private static final Map<SystemText, Boolean> texts = new ConcurrentHashMap<>();

    private static Typeface tf;
    private static float oversample;

    private final boolean needWidth;

    private static final LRUCache<String, BitmapData> bitmapCache = new LRUCache<>(256);

    private static int cacheHits = 0;
    private static int cacheMiss = 0;

    private static final Pattern STRIPPER = Pattern.compile("[ \\n]");
    
    // Color mapping for different text segments
    private int[] colorMap;
    private int defaultColor = Color.WHITE;
    // highlightColor is now inherited from BaseText
    // hasMarkup is now inherited from BaseText
    private int highlightColor = 0xFFCC33FF; // Same as Window.TITLE_COLOR

    public SystemText(float baseLine) {
        this(Utils.EMPTY_STRING, baseLine, false);
    }

    public SystemText(final String text, float size, boolean multiline) {
        super(0, 0, 0, 0);

        //noinspection ExpressionComparedToItself
        if (fontScale != fontScale) {
            updateFontScale();
        }

        if (tf == null) {

            if (GamePreferences.classicFont()) {
                oversample = 4;
                var fontFile = ModdingMode.getFile("fonts/pixel_font.ttf");
                if (fontFile != null && fontFile.exists()) {
                    tf = Typeface.createFromFile(fontFile);
                } else {
                    tf = Typeface.createFromAsset(RemixedDungeonApp.getContext().getAssets(), "fonts/pixel_font.ttf");
                }
            } else {
                if (Game.smallResScreen()) {
                    tf = Typeface.create((String) null, Typeface.BOLD);
                    oversample = 1;
                } else {
                    tf = Typeface.create((String) null, Typeface.NORMAL);
                    oversample = 4;
                }
            }
        }

        size *= fontScale;

        needWidth = multiline;

        if (size == 0) {
            throw new TrackedRuntimeException("zero sized font!!!");
        }

        float textSize = size * oversample;
        if (!textPaints.containsKey(textSize)) {
            TextPaint tx = new TextPaint();

            tx.setTextSize(textSize);
            tx.setStyle(Paint.Style.FILL);
            tx.setHinting(Paint.HINTING_ON);
            tx.setAntiAlias(true);

            tx.setColor(Color.WHITE);

            tx.setTypeface(tf);

            TextPaint cp = new TextPaint();
            cp.set(tx);
            cp.setStyle(Paint.Style.FILL_AND_STROKE);
            cp.setStrokeWidth(textSize * 0.2f);
            cp.setColor(Color.BLACK);
            cp.setAntiAlias(true);

            textPaints.put(textSize, tx);
            contourPaints.put(textSize, cp);
        }

        textPaint = textPaints.get(textSize);
        contourPaint = contourPaints.get(textSize);

        // Parse markup in the text
        if (text != null && !text.isEmpty()) {
            parseMarkup(text);
        } else {
            this.text = Utils.EMPTY_STRING;
        }
        
        texts.put(this, true);
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

        if (Game.smallResScreen()) {
            scale *= 1.5;
        }

        fontScale = scale;
    }

    private void destroyLines() {
        for (SystemTextLine img : lineImage) {
            if (getParent() != null) {
                getParent().remove(img);
            }
            img.destroy();
        }
    }

    @Synchronized
    @Override
    public void destroy() {
        destroyLines();

        text = Utils.EMPTY_STRING;
        super.destroy();
        texts.remove(this);
    }

    @Synchronized
    @Override
    public void kill() {
        destroyLines();

        text = Utils.EMPTY_STRING;
        super.kill();
        texts.remove(this);
    }

    private final ArrayList<Float> xCharPos = new ArrayList<>();
    private final ArrayList<Integer> codePoints = new ArrayList<>();
    private String currentLine;

    private float fontHeight;

    private float lineWidth;

    private int fillLine(int startFrom) {
        int offset = startFrom;

        float xPos = 0;
        lineWidth = 0;
        xCharPos.clear();
        codePoints.clear();

        final int length = text.length();
        int lastWordOffset = offset;

        int lastWordStart = 0;

        float symbolWidth = 0;

        while (offset < length) {

            int codepoint = text.codePointAt(offset);
            int codepointCharCount = Character.charCount(codepoint);

            offset += codepointCharCount;

            boolean isWhiteSpace = Character.isWhitespace(codepoint);

            if (isWhiteSpace) {
                lastWordOffset = offset;
                lastWordStart = xCharPos.size();
            }

            if (!isWhiteSpace) {
                xCharPos.add(xPos);
                codePoints.add(codepoint);
            }

            if (codepoint == 0x000A || codepoint == 0x000D) {
                lineWidth += symbolWidth;
                return offset;
            }

            symbolWidth = symbolWidth(Character.toString((char) (codepoint)));
            xPos += symbolWidth;
            lineWidth = xPos;

            if (maxWidth != Integer.MAX_VALUE
                    && xPos + symbolWidth > maxWidth / scale.x) {
                if (lastWordOffset != startFrom) {
                    xCharPos.subList(lastWordStart, xCharPos.size()).clear();
                    codePoints.subList(lastWordStart, codePoints.size()).clear();
                    return lastWordOffset;
                } else {
                    xCharPos.remove(xCharPos.size() - 1);
                    codePoints.remove(codePoints.size() - 1);
                    return offset - 1;
                }
            }
        }

        return offset;
    }

    @SuppressLint("NewApi")
    private void createText() {
        if (needWidth && maxWidth == Integer.MAX_VALUE) {
            return;
        }

        if (fontHeight > 0) {
            destroyLines();
            lineImage.clear();
            setWidth(0);

            setHeight(0);

            int startLine = 0;

            while (startLine < text.length()) {

                int nextLine = fillLine(startLine);
                if (nextLine == startLine) { // WTF???
                    return;
                }
                setHeight(height + fontHeight);

                if (lineWidth > 0) {

                    lineWidth += 1;
                    setWidth(Math.max(lineWidth, width));

                    currentLine = text.substring(startLine, nextLine);

                    String key = Utils.format("%fx%f_%s", lineWidth, fontHeight, currentLine);

                    if (!bitmapCache.containsKey(key)) {

                        BitmapData bitmap = BitmapData.createBitmap(
                                (int) (lineWidth * oversample),
                                (int) (fontHeight * oversample + textPaint.descent()));
                        bitmapCache.put(key, bitmap);

                        Canvas canvas = new Canvas(bitmap.bmp);

                        drawTextLine(startLine, canvas, contourPaint);
                        drawTextLine(startLine, canvas, textPaint);

                        cacheMiss++;
                    } else {
                        cacheHits++;
                    }
                    SystemTextLine line = new SystemTextLine(bitmapCache.get(key));
                    line.setVisible(getVisible());
                    lineImage.add(line);
                } else {
                    lineImage.add(SystemTextLine.emptyLine);
                }
                startLine = nextLine;
            }
        }
    }
    private void drawTextLine(int charIndex, Canvas canvas, TextPaint paint) {

        float y = (fontHeight) * oversample - paint.descent();

        // Handle empty lines
        if (codePoints.isEmpty()) {
            return;
        }

        if (colorMap == null) {
            if (!xCharPos.isEmpty()) {
                float x = (xCharPos.get(0) + 0.5f) * oversample;
                canvas.drawText(currentLine, x, y, paint);
            }
            return;
        }

        // Check if this is the contour paint (should always be black)
        boolean isContour = (paint.getColor() == Color.BLACK && 
                            paint.getStyle() == Paint.Style.FILL_AND_STROKE);

        // Create a proper mapping from codePoints indices to colorMap indices
        int[] codePointToColorMapIndex = new int[codePoints.size()];
        
        // Track position in currentLine
        int currentLineIndex = 0;
        // Track position in codePoints
        int codePointIndex = 0;
        // Track actual position in full text
        int textPosition = charIndex;
        
        // Process each character in currentLine to build the mapping
        while (currentLineIndex < currentLine.length() && codePointIndex < codePoints.size()) {
            char currentChar = currentLine.charAt(currentLineIndex);
            
            // If this is not whitespace and matches the current codePoint
            if (!Character.isWhitespace(currentChar) && ((int)currentChar) == codePoints.get(codePointIndex)) {
                codePointToColorMapIndex[codePointIndex] = textPosition;
                codePointIndex++;
            }
            
            currentLineIndex++;
            textPosition++;
        }

        // Draw each codePoint with the correct color
        for (int i = 0; i < codePoints.size(); ++i) {
            int codepoint = codePoints.get(i);
            float x = (xCharPos.get(i) + 0.5f) * oversample;
            
            if (!Character.isWhitespace(codepoint)) {
                if (isContour) {
                    // Always draw contour in black
                    canvas.drawText(Character.toString((char) codepoint), x, y, paint);
                } else {
                    // Get the correct index in the colorMap
                    int colorMapIndex = codePointToColorMapIndex[i];
                    
                    // Save original paint color
                    int originalColor = paint.getColor();

                    // Set color based on color map for text
                    if (colorMapIndex < colorMap.length) {
                        paint.setColor(colorMap[colorMapIndex]);
                    } else {
                        // Fallback to default color if index is out of bounds
                        paint.setColor(defaultColor);
                    }

                    // Draw character
                    canvas.drawText(Character.toString((char) codepoint), x, y, paint);

                    // Restore original paint color
                    paint.setColor(originalColor);
                }
            }
        }
    }

    @Override
    protected void updateMatrix() {
        // "origin" field is ignored
        if (dirtyMatrix) {
            Matrix.setIdentity(matrix);
            Matrix.translate(matrix, x, y);
            Matrix.scale(matrix, scale.x, scale.y);
            Matrix.rotate(matrix, angle);
            dirtyMatrix = false;
        }
    }

    private void updateParent() {
        Group parent = getParent();
        for (SystemTextLine img : lineImage) {
            Group imgParent = img.getParent();

            if (imgParent != parent) {
                if (imgParent != null) {
                    imgParent.remove(img);
                }

                if (parent != null) {
                    parent.add(img);
                }
            }
        }
    }

    @Override
    public void setParent(@NotNull Group parent) {
        super.setParent(parent);

        updateParent();
    }

    @Override
    public boolean setVisible(boolean visible) {
        if (lineImage != null) {
            for (SystemTextLine img : lineImage) {
                img.setVisible(visible);
            }
        }
        return super.setVisible(visible);
    }

    @Override
    public void draw() {
        clean();
        if (lineImage != null) {
            int line = 0;

            updateParent();

            for (SystemTextLine img : lineImage) {

                img.ra = ra;
                img.ga = ga;
                img.ba = ba;
                img.rm = rm;
                img.gm = gm;
                img.bm = bm;
                img.am = am;
                img.aa = aa;

                img.setPos(PixelScene.align(PixelScene.uiCamera, getX()), PixelScene.align(PixelScene.uiCamera, getY() + (line * fontHeight) * scale.y));
                img.setScaleXY(scale.x / oversample, scale.y / oversample);

                line++;
            }
        }
    }

    private float symbolWidth(String symbol) {
        return contourPaint.measureText(symbol) / oversample;
    }

    public void measure() {
        if (Math.abs(scale.x) < 0.001) {
            return;
        }

        if (dirty) {

            fontHeight = (contourPaint.descent() - contourPaint.ascent())
                    / oversample;
            createText();
        }
    }


    @Override
    public float baseLine() {
        return height();
    }

    @Override
    public int lines() {
        return this.lineImage.size();
    }

    /**
     * Parse markup in the text and create color mapping
     */
    protected void parseMarkup(String input) {
        StringBuilder finalText = new StringBuilder();
        ArrayList<Integer> colorList = new ArrayList<>();
        
        Matcher m = HIGHLIGHTER.matcher(input);
        int lastEnd = 0;
        
        while (m.find()) {
            // Add text before the highlight
            String before = input.substring(lastEnd, m.start());
            for (int i = 0; i < before.length(); i++) {
                char c = before.charAt(i);
                finalText.append(c);
                colorList.add(defaultColor);
            }
            
            // Add highlighted text
            String highlighted = m.group(1);
            for (int i = 0; i < highlighted.length(); i++) {
                char c = highlighted.charAt(i);
                finalText.append(c);
                colorList.add(highlightColor);
            }
            
            lastEnd = m.end();
        }
        
        // Add remaining text after the last highlight
        String remaining = input.substring(lastEnd);
        for (int i = 0; i < remaining.length(); i++) {
            char c = remaining.charAt(i);
            finalText.append(c);
            colorList.add(defaultColor);
        }
        
        text = finalText.toString();
        
        // Convert ArrayList to array for performance
        if (!colorList.isEmpty()) {
            colorMap = new int[colorList.size()];
            for (int i = 0; i < colorList.size(); i++) {
                colorMap[i] = colorList.get(i);
            }
        }
        
        hasMarkup = true;
    }

    /**
     * Set the highlight color for marked text
     */
    public void highlightColor(int color) {
        highlightColor = color;
        if (hasMarkup) {
            dirty = true;
        }
    }

    /**
     * Set the default color for unmarked text
     */
    public void defaultColor(int color) {
        defaultColor = color;
        if (hasMarkup) {
            dirty = true;
        }
    }

    @Synchronized
    public static void invalidate() {
        for (SystemText txt : texts.keySet().toArray(new SystemText[0])) {
            txt.dirty = true;
            txt.destroyLines();
        }
        tf = null;
        textPaints.clear();
        contourPaints.clear();
        texts.clear();
        bitmapCache.clear();
    }
}
