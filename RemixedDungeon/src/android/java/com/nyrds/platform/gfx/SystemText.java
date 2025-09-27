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

    private static final Pattern MARKUP_PATTERN = Pattern.compile("_(.*?)_|\"(.*?)\"");

    // A line is composed of segments, each with its own text and color.
    private static class ColoredSegment {
        String text;
        int color;

        ColoredSegment(String text, int color) {
            this.text = text;
            this.color = color;
        }
    }

    private final ArrayList<ArrayList<ColoredSegment>> linesOfSegments = new ArrayList<>();

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
            scale *= 1.5f;
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

    private float fontHeight;

    private void wrapText() {
        linesOfSegments.clear();
        ArrayList<ColoredSegment> allSegments = parseMarkupToSegments(originalText);
        hasMarkup = !allSegments.isEmpty() && allSegments.size() > 1;

        ArrayList<ColoredSegment> currentLine = new ArrayList<>();
        float currentLineWidth = 0;

        for (ColoredSegment segment : allSegments) {
            String[] paragraphs = segment.text.split("\n", -1);

            for (int p = 0; p < paragraphs.length; p++) {
                String paragraph = paragraphs[p];
                String[] words = paragraph.split("(?<=\\s)|(?=\\s)"); // Split while keeping spaces

                for (String word : words) {
                    if (word.isEmpty()) continue;

                    float wordWidth = symbolWidth(word);

                    if (needWidth && !currentLine.isEmpty() && currentLineWidth + wordWidth > (maxWidth / scale.x)) {
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
    }
    private ArrayList<ColoredSegment> parseMarkupToSegments(String input) {
        if (input == null) input = "";

        ArrayList<ColoredSegment> segments = new ArrayList<>();
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

    private float measureLine(ArrayList<ColoredSegment> line) {
        float lineWidth = 0;
        for(ColoredSegment seg : line) {
            lineWidth += symbolWidth(seg.text);
        }
        return lineWidth;
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
            
            wrapText();

            for (ArrayList<ColoredSegment> line : linesOfSegments) {
                setHeight(height + fontHeight);
                
                float lineWidth = measureLine(line);
                
                if (lineWidth > 0) {
                    lineWidth += 1; // Add padding
                    setWidth(Math.max(lineWidth, width));

                    StringBuilder lineTextBuilder = new StringBuilder();
                    for (ColoredSegment seg : line) lineTextBuilder.append(seg.text);
                    String currentLine = lineTextBuilder.toString();

                    String key = Utils.format("%fx%f_%s", lineWidth, fontHeight, currentLine);

                    if (!bitmapCache.containsKey(key)) {
                        BitmapData bitmap = BitmapData.createBitmap(
                                (int) (lineWidth * oversample),
                                (int) (fontHeight * oversample + textPaint.descent()));
                        bitmapCache.put(key, bitmap);

                        Canvas canvas = new Canvas(bitmap.bmp);
                        drawTextLine(line, canvas, contourPaint);
                        drawTextLine(line, canvas, textPaint);

                        cacheMiss++;
                    } else {
                        cacheHits++;
                    }
                    SystemTextLine text_line = new SystemTextLine(bitmapCache.get(key));
                    text_line.setVisible(getVisible());
                    lineImage.add(text_line);
                } else {
                    lineImage.add(SystemTextLine.emptyLine);
                }
            }
        }
    }

    private void drawTextLine(ArrayList<ColoredSegment> line, Canvas canvas, TextPaint paint) {
        float y = (fontHeight) * oversample - paint.descent();
        float x = 0.5f * oversample;

        // Check if this is the contour paint (should always be black)
        boolean isContour = (paint.getColor() == Color.BLACK && 
                            paint.getStyle() == Paint.Style.FILL_AND_STROKE);
            
        // Save original paint color
        int originalColor = paint.getColor();

        for (ColoredSegment segment : line) {
            if (!isContour) {
                paint.setColor(segment.color);
            }

            canvas.drawText(segment.text, x, y, paint);
            x += paint.measureText(segment.text);
        }

        if (!isContour) {
            paint.setColor(originalColor);
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
