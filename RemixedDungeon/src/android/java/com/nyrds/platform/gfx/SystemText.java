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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Synchronized;
import lombok.val;

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

    

    private float measureLine(List<ColoredSegment> line) {
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
            
            // Use the base class wrapText method with appropriate max width
            float maxWidthForWrapping = needWidth ? (maxWidth / scale.x) : Float.MAX_VALUE;
            val wrappedLines = wrapText(parseMarkupToSegments(originalText), maxWidthForWrapping);
            textLines.clear();
            textLines.addAll(wrappedLines);

            for (val line : textLines) {
                setHeight(height + fontHeight);
                
                float lineWidth = measureLine(line);  // Convert to ArrayList for measureLine method
                
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

    private void drawTextLine(List<ColoredSegment> line, Canvas canvas, TextPaint paint) {
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

    @Override
    protected float measureTextWidth(String text) {
        return contourPaint.measureText(text) / oversample;
    }

    private float symbolWidth(String symbol) {
        return contourPaint.measureText(symbol) / oversample;
    }

    @Override
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

        // Respect minimum height
        if (height < minHeight) {
            setHeight(minHeight);
        }
    }

    @Override
    public float baseLine() {
        return height();
    }

    @Override
    public int lines() {
        return this.textLines.size();
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