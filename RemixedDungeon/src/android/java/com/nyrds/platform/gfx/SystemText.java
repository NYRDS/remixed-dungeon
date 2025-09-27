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

    // Combined markup pattern for violet highlighting (_text_) and bronze ("text")
    private static final Pattern MARKUP_PATTERN = Pattern.compile("_(.*?)_|\"(.*?)\"");

    // Color mapping for different text segments
    private int[] colorMap;

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

        text(text);

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
        int lastWordEndIndexInList = 0; // Index in codePoints/xCharPos after the last complete word

        while (offset < length) {
            int codepoint = text.codePointAt(offset);
            int codepointCharCount = Character.charCount(codepoint);

            if (codepoint == '\n') {
                return offset + codepointCharCount;
            }

            // Correctly measure width for all characters, including supplementary ones
            float symbolWidth = symbolWidth(new String(Character.toChars(codepoint)));

            if (maxWidth != Integer.MAX_VALUE && xPos + symbolWidth > maxWidth / scale.x) {
                if (lastWordOffset != startFrom) {
                    // Trim the current line lists back to the end of the last word
                    if (lastWordEndIndexInList < codePoints.size()) {
                        codePoints.subList(lastWordEndIndexInList, codePoints.size()).clear();
                        xCharPos.subList(lastWordEndIndexInList, xCharPos.size()).clear();
                    }
                    // Recalculate width
                    if (xCharPos.isEmpty()) {
                        lineWidth = 0;
                    } else {
                        int lastCodePoint = codePoints.get(xCharPos.size() - 1);
                        float lastSymbolWidth = symbolWidth(new String(Character.toChars(lastCodePoint)));
                        lineWidth = xCharPos.get(xCharPos.size() - 1) + lastSymbolWidth;
                    }
                    return lastWordOffset;
                } else { // No whitespace in the line yet, must break mid-character
                    // The current line is just what we have so far, excluding the current character
                    return offset;
                }
            }

            xCharPos.add(xPos);
            codePoints.add(codepoint);

            if (Character.isWhitespace(codepoint)) {
                lastWordOffset = offset + codepointCharCount;
                lastWordEndIndexInList = codePoints.size();
            }

            xPos += symbolWidth;
            lineWidth = xPos;
            offset += codepointCharCount;
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
                int nextLineStart = fillLine(startLine);

                // Handle case where nothing is consumed to prevent infinite loop
                if (nextLineStart <= startLine && startLine < text.length()) {
                    if (startLine < text.length()) nextLineStart = startLine + 1;
                    else return;
                }

                // Determine the end of the line's visible content, trimming trailing newlines
                int endOfLineContent = nextLineStart;
                if (endOfLineContent > startLine) {
                    char lastChar = text.charAt(endOfLineContent - 1);
                    if (lastChar == '\n' || lastChar == '\r') {
                        endOfLineContent--;
                    }
                }

                setHeight(height + fontHeight);

                if (lineWidth > 0) {
                    lineWidth += 1; // Add padding
                    setWidth(Math.max(lineWidth, width));

                    currentLine = text.substring(startLine, endOfLineContent);
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
                startLine = nextLineStart;
            }
             // Handle case where text ends with a newline, creating a final empty line
            if (text.endsWith("\n")) {
                setHeight(height + fontHeight);
                lineImage.add(SystemTextLine.emptyLine);
            }
        }
    }

    private void drawTextLine(int charIndex, Canvas canvas, TextPaint paint) {
        float y = (fontHeight) * oversample - paint.descent();

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
            dirty=false;
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

    @Override
    public void text(@NotNull String str) {
        // This method now handles both stripping the markup for measurement
        // and building the color map for rendering.
        parseMarkupAndBuildColorMap(str);
        this.dirty = true;
    }

    /**
     * Parse markup and build the color mapping array
     * @param input the text with markup to parse
     */
    protected void parseMarkupAndBuildColorMap(String input) {
        if (input == null) {
            input = "";
        }
        this.originalText = input;

        StringBuilder finalText = new StringBuilder();
        ArrayList<Integer> colorList = new ArrayList<>();

        Matcher m = MARKUP_PATTERN.matcher(input);
        int lastEnd = 0;

        while (m.find()) {
            // Add text before the markup
            String before = input.substring(lastEnd, m.start());
            for (char c : before.toCharArray()) {
                finalText.append(c);
                colorList.add(defaultColor);
            }

            // Check which group matched and apply the appropriate color
            if (m.group(1) != null) {
                // Violet highlight for _text_
                String highlighted = m.group(1);
                for (char c : highlighted.toCharArray()) {
                    finalText.append(c);
                    colorList.add(highlightColor);
                }
            } else if (m.group(2) != null) {
                // Bronze color for "text"
                String quotedText = m.group(2);
                for (char c : quotedText.toCharArray()) {
                    finalText.append(c);
                    colorList.add(bronzeColor);
                }
            }

            lastEnd = m.end();
        }

        // Add remaining text after the last markup
        String remaining = input.substring(lastEnd);
        for (char c : remaining.toCharArray()) {
            finalText.append(c);
            colorList.add(defaultColor);
        }

        text = finalText.toString();

        hasMarkup = colorList.stream().anyMatch(c -> c != defaultColor);

        // Convert ArrayList to array for performance
        if (!colorList.isEmpty()) {
            colorMap = new int[colorList.size()];
            for (int i = 0; i < colorList.size(); i++) {
                colorMap[i] = colorList.get(i);
            }
        }
    }

    @Override
    public void highlightColor(int color) {
        super.highlightColor(color);
        if (hasMarkup) {
            this.text(this.originalText); // Reparse with new color
            dirty = true;
        }
    }

    @Override
    public void defaultColor(int color) {
        super.defaultColor(color);
        this.text(this.originalText); // Reparse with new color
        dirty = true;
    }

    @Override
    public void bronzeColor(int color) {
        super.bronzeColor(color);
        if (hasMarkup) {
            this.text(this.originalText); // Reparse with new color
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
