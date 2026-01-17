package com.nyrds.pixeldungeon.windows;

import com.watabou.noosa.Image;
import com.watabou.gltextures.SmartTexture;
import com.nyrds.platform.gfx.BitmapData;
import com.nyrds.platform.compatibility.RectF;

import java.util.Arrays;
import java.util.Random;

/**
 * High-fidelity procedural Transmutation Circle.
 * Features:
 * - 512x512 Resolution
 * - True Star Polygons (Pentagrams, Hexagrams) via Vertex Skipping
 * - Complex Grid-based Runes
 * - Additive Glowing Aesthetics
 */
public class TransmutationCircle extends Image {

    private String currentRecipeSeed = "";
    private boolean needsUpdate = true;

    // Resolution
    private static final int SIZE = 512;

    // Aesthetic constants
    private static final float GLOW_FALLOFF = 4.5f;
    private static final float MAIN_LINE_THICKNESS = 1.9f;
    private static final float RUNE_LINE_THICKNESS = 1.2f;

    public TransmutationCircle() {
        super();
        updateTexture();
        alpha(0.9f);
    }

    @Override
    public void update() {
        super.update();
        if (needsUpdate) {
            updateTexture();
            needsUpdate = false;
        }
    }

    public void setRecipeSeed(String recipeSeed) {
        if (!currentRecipeSeed.equals(recipeSeed)) {
            currentRecipeSeed = recipeSeed;
            needsUpdate = true;
        }
    }

    private void updateTexture() {
        SmartTexture tex = generateAlchemyArray(currentRecipeSeed);
        texture(tex);
        setWidth(SIZE);
        setHeight(SIZE);
        frame(new RectF(0, 0, 1, 1));
    }

    private SmartTexture generateAlchemyArray(String seed) {
        int[] pixels = new int[SIZE * SIZE];
        Arrays.fill(pixels, 0x00000000);

        long seedNum = seed.hashCode();
        Random rng = new Random(seedNum);

        // --- Color Palette ---
        float hue = rng.nextFloat();
        // Shift hues towards Red/Purple/Cyan (Alchemy colors)
        if (hue > 0.15f && hue < 0.45f) hue += 0.5f;

        int baseColor = getColorFromHSB(hue, 0.7f, 1.0f);

        float centerX = SIZE / 2.0f;
        float centerY = SIZE / 2.0f;
        float maxRadius = (SIZE / 2.0f) * 0.96f;

        // --- Layer 1: Outer Container & Rune Band ---
        // Typical FMA: Double circle on outside, text inside
        float outerRingR = maxRadius;
        float innerRingR = maxRadius * 0.82f;

        drawRing(pixels, centerX, centerY, outerRingR, baseColor, 1.0f, MAIN_LINE_THICKNESS);
        drawRing(pixels, centerX, centerY, outerRingR * 0.985f, baseColor, 0.6f, MAIN_LINE_THICKNESS * 0.5f);
        drawRing(pixels, centerX, centerY, innerRingR, baseColor, 0.9f, MAIN_LINE_THICKNESS);

        // Runes
        float runeCenterR = (outerRingR + innerRingR) / 2.0f;
        float runeHeight = (outerRingR - innerRingR) * 0.65f;
        drawComplexRunes(pixels, centerX, centerY, runeCenterR, runeHeight, baseColor, seedNum, 0);

        // --- Layer 2: Main Geometry (Pentagrams/Hexagrams) ---
        float geoRadius = innerRingR * 0.95f;

        // Weighted Random for Sides: Favor 5 (Pentagram) and 6 (Hexagram)
        // 3=Tri, 4=Square, 5=Pent, 6=Hex, 8=Oct
        int[] sideOptions = {3, 4, 5, 5, 5, 6, 6, 8};
        int sides = sideOptions[rng.nextInt(sideOptions.length)];

        float angleOffset = rng.nextFloat() * (float)Math.PI;

        boolean isStar = rng.nextBoolean() || sides == 5; // Always prioritize stars for pentagons

        if (isStar && sides >= 5) {
            // Draw a proper Star Polygon (connect vertices with step 2 or 3)
            int step = (sides >= 7) ? 3 : 2; // For 8 sides, step 3 looks sharper
            drawStarPolygon(pixels, centerX, centerY, sides, geoRadius, angleOffset, step, baseColor, 1.0f);

            // Draw a Circle Enclosing the star points (very common in FMA)
            drawRing(pixels, centerX, centerY, geoRadius, baseColor, 0.7f, MAIN_LINE_THICKNESS);

            // Recursive: Draw smaller shape inside the central polygon of the star
            // Calculate inner radius roughly based on step
            float innerStarR = geoRadius * (float)(Math.cos(Math.PI * step / sides) / Math.cos(Math.PI * (step - 1) / sides)) * 0.5f;
            // Simplified inner radius for visuals
            if (sides == 5) innerStarR = geoRadius * 0.382f;
            if (sides == 6) innerStarR = geoRadius * 0.577f;

            drawRing(pixels, centerX, centerY, innerStarR, baseColor, 0.8f, MAIN_LINE_THICKNESS);

            // Inner Text
            drawComplexRunes(pixels, centerX, centerY, innerStarR * 0.85f, innerStarR * 0.15f, baseColor, seedNum + 1, 1);

        } else {
            // Regular Polygon (Triangle, Square, or non-star Pent/Hex)
            float[] mainVerts = getPolygonVerts(sides, geoRadius, angleOffset);
            drawPolygon(pixels, centerX, centerY, mainVerts, baseColor, 1.0f, MAIN_LINE_THICKNESS);

            // Overlapping rotated copy? (e.g. Square + Rotated Square = Octagram)
            if (rng.nextBoolean()) {
                float[] secVerts = getPolygonVerts(sides, geoRadius, angleOffset + (float)Math.PI/sides);
                drawPolygon(pixels, centerX, centerY, secVerts, baseColor, 0.8f, MAIN_LINE_THICKNESS);
            }

            // Inner details
            if (sides == 3) {
                // Eye in triangle or circle in triangle
                drawRing(pixels, centerX, centerY, geoRadius * 0.5f, baseColor, 1.0f, MAIN_LINE_THICKNESS);
            }
        }

        // --- Layer 4: Center Details ---
        if (rng.nextBoolean()) {
            // Center Flux Lines
            int lines = 2 + rng.nextInt(3);
            for(int k=0; k<lines; k++) {
                float a = k * (float)Math.PI / lines + rng.nextFloat();
                float x1 = centerX + (float)Math.cos(a) * geoRadius * 0.9f;
                float y1 = centerY + (float)Math.sin(a) * geoRadius * 0.9f;
                float x2 = centerX - (float)Math.cos(a) * geoRadius * 0.9f;
                float y2 = centerY - (float)Math.sin(a) * geoRadius * 0.9f;
                drawLine(pixels, x1, y1, x2, y2, baseColor, 0.4f, MAIN_LINE_THICKNESS);
            }
        }

        // Center Dot
        drawRing(pixels, centerX, centerY, 4.0f, baseColor, 1.0f, 3.0f);

        BitmapData bitmap = BitmapData.createBitmap(SIZE, SIZE);
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                bitmap.setPixel(x, y, pixels[y * SIZE + x]);
            }
        }
        return new SmartTexture(bitmap);
    }

    /**
     * Draws a Star Polygon {n/k}.
     * Handles both unicursal stars (Pentagram) and compound stars (Hexagram/Star of David).
     */
    private void drawStarPolygon(int[] pixels, float cx, float cy, int sides, float r, float angleOffset, int step, int color, float intensity) {
        float[] verts = getPolygonVerts(sides, r, angleOffset);

        // Calculate GCD to determine number of disjoint loops
        // e.g. Sides=6, Step=2 -> GCD=2. We need 2 loops (0-2-4 and 1-3-5).
        // e.g. Sides=5, Step=2 -> GCD=1. We need 1 loop (0-2-4-1-3).
        int loops = gcd(sides, step);
        int pointsPerLoop = sides / loops;

        for (int l = 0; l < loops; l++) {
            int currentIdx = l;
            for (int i = 0; i < pointsPerLoop; i++) {
                int nextIdx = (currentIdx + step) % sides;

                // Draw line from current vertex to next vertex
                float x1 = verts[currentIdx * 2] + cx;
                float y1 = verts[currentIdx * 2 + 1] + cy;
                float x2 = verts[nextIdx * 2] + cx;
                float y2 = verts[nextIdx * 2 + 1] + cy;

                drawLine(pixels, x1, y1, x2, y2, color, intensity, MAIN_LINE_THICKNESS);

                currentIdx = nextIdx;
            }
        }
    }

    private int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    private void drawComplexRunes(int[] pixels, float cx, float cy, float radiusCenter, float runeHalfHeight, int color, long seed, int type) {
        Random runeRng = new Random(seed + type * 999);
        float runeWidth = runeHalfHeight * 1.2f;
        float circumference = 2 * (float)Math.PI * radiusCenter;
        int numRunes = (int)(circumference / (runeWidth * 1.4f));
        float angleStep = (float)(Math.PI * 2) / numRunes;

        for (int i = 0; i < numRunes; i++) {
            float angle = i * angleStep;
            runeRng.setSeed(seed + (i * 31) + (type * 7));

            float[] gridX = new float[] {-0.5f, 0.0f, 0.5f};
            float[] gridY = new float[] {-0.7f, 0.0f, 0.7f};

            int numStrokes = 3 + runeRng.nextInt(3);
            float cos = (float)Math.cos(angle);
            float sin = (float)Math.sin(angle);

            for (int s = 0; s < numStrokes; s++) {
                int p1 = runeRng.nextInt(9);
                int p2 = runeRng.nextInt(9);
                while (p1 == p2) p2 = runeRng.nextInt(9);

                float lx1 = gridX[p1 % 3] * runeWidth;
                float ly1 = gridY[p1 / 3] * runeHalfHeight;
                float lx2 = gridX[p2 % 3] * runeWidth;
                float ly2 = gridY[p2 / 3] * runeHalfHeight;

                float rx1 = lx1 * sin + ly1 * cos;
                float ry1 = lx1 * -cos + ly1 * sin;
                float rx2 = lx2 * sin + ly2 * cos;
                float ry2 = lx2 * -cos + ly2 * sin;

                float globalX1 = cx + (cos * radiusCenter) + rx1;
                float globalY1 = cy + (sin * radiusCenter) + ry1;
                float globalX2 = cx + (cos * radiusCenter) + rx2;
                float globalY2 = cy + (sin * radiusCenter) + ry2;

                drawLine(pixels, globalX1, globalY1, globalX2, globalY2, color, 0.9f, RUNE_LINE_THICKNESS);
            }

            // Dot detail
            if (runeRng.nextFloat() > 0.7f) {
                float lx = gridX[1] * runeWidth; // Center column is index 1
                float ly = gridY[runeRng.nextInt(3)] * runeHalfHeight;

                float rx = lx * sin + ly * cos;
                float ry = lx * -cos + ly * sin;
                drawRing(pixels, cx + (cos * radiusCenter) + rx, cy + (sin * radiusCenter) + ry, 2.5f, color, 1.0f, 0.9f);
            }
        }
    }

    // --- Drawing Primitives ---

    private void drawRing(int[] pixels, float cx, float cy, float r, int color, float intensity, float thickness) {
        int minX = Math.max(0, (int)(cx - r - GLOW_FALLOFF - thickness));
        int maxX = Math.min(SIZE, (int)(cx + r + GLOW_FALLOFF + thickness));
        int minY = Math.max(0, (int)(cy - r - GLOW_FALLOFF - thickness));
        int maxY = Math.min(SIZE, (int)(cy + r + GLOW_FALLOFF + thickness));

        for (int y = minY; y < maxY; y++) {
            for (int x = minX; x < maxX; x++) {
                float dx = x - cx;
                float dy = y - cy;
                float dist = (float)Math.sqrt(dx*dx + dy*dy);
                float distFromRing = Math.abs(dist - r);

                float alpha = getGlowIntensity(distFromRing, thickness) * intensity;
                if (alpha > 0) blendPixel(pixels, x, y, color, alpha);
            }
        }
    }

    private void drawPolygon(int[] pixels, float cx, float cy, float[] verts, int color, float intensity, float thickness) {
        int sides = verts.length / 2;
        for (int i = 0; i < sides; i++) {
            float x1 = verts[i*2] + cx;
            float y1 = verts[i*2+1] + cy;
            float x2 = verts[((i+1)%sides)*2] + cx;
            float y2 = verts[((i+1)%sides)*2+1] + cy;
            drawLine(pixels, x1, y1, x2, y2, color, intensity, thickness);
        }
    }

    private void drawLine(int[] pixels, float x1, float y1, float x2, float y2, int color, float intensity, float thickness) {
        int range = (int)(GLOW_FALLOFF + thickness + 1);
        int minX = Math.max(0, (int)(Math.min(x1, x2) - range));
        int maxX = Math.min(SIZE, (int)(Math.max(x1, x2) + range));
        int minY = Math.max(0, (int)(Math.min(y1, y2) - range));
        int maxY = Math.min(SIZE, (int)(Math.max(y1, y2) + range));

        for (int y = minY; y < maxY; y++) {
            for (int x = minX; x < maxX; x++) {
                float d = distToSegment(x, y, x1, y1, x2, y2);
                float alpha = getGlowIntensity(d, thickness) * intensity;
                if (alpha > 0) blendPixel(pixels, x, y, color, alpha);
            }
        }
    }

    // --- Math Helpers ---

    private float[] getPolygonVerts(int sides, float r, float angleOffset) {
        float[] verts = new float[sides * 2];
        for (int i = 0; i < sides; i++) {
            double theta = angleOffset + (i * 2 * Math.PI / sides);
            verts[i*2] = (float)(Math.cos(theta) * r);
            verts[i*2+1] = (float)(Math.sin(theta) * r);
        }
        return verts;
    }

    private float distToSegment(float px, float py, float vx, float vy, float wx, float wy) {
        float l2 = (vx - wx) * (vx - wx) + (vy - wy) * (vy - wy);
        if (l2 == 0) return (float)Math.sqrt((px-vx)*(px-vx) + (py-vy)*(py-vy));
        float t = ((px - vx) * (wx - vx) + (py - vy) * (wy - vy)) / l2;
        t = Math.max(0, Math.min(1, t));
        float projX = vx + t * (wx - vx);
        float projY = vy + t * (wy - vy);
        return (float)Math.sqrt((px-projX)*(px-projX) + (py-projY)*(py-projY));
    }

    private float getGlowIntensity(float distance, float thickness) {
        if (distance <= thickness * 0.5f) return 1.0f;
        if (distance >= thickness * 0.5f + GLOW_FALLOFF) return 0.0f;
        float t = (distance - thickness * 0.5f) / GLOW_FALLOFF;
        return (1.0f - t) * (1.0f - t);
    }

    private void blendPixel(int[] pixels, int x, int y, int color, float alpha) {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) return;

        int idx = y * SIZE + x;
        int current = pixels[idx];

        int r1 = (current >> 16) & 0xFF;
        int g1 = (current >> 8) & 0xFF;
        int b1 = current & 0xFF;
        int a1 = (current >> 24) & 0xFF;

        int r2 = (color >> 16) & 0xFF;
        int g2 = (color >> 8) & 0xFF;
        int b2 = color & 0xFF;
        int a2 = (int)(255 * Math.min(1.0f, alpha));

        int rOut = Math.min(255, r1 + (int)(r2 * alpha));
        int gOut = Math.min(255, g1 + (int)(g2 * alpha));
        int bOut = Math.min(255, b1 + (int)(b2 * alpha));
        int aOut = Math.min(255, Math.max(a1, a2));

        pixels[idx] = (aOut << 24) | (rOut << 16) | (gOut << 8) | bOut;
    }

    private int getColorFromHSB(float h, float s, float b) {
        float c = b * s;
        float hp = h * 6;
        float x = c * (1 - Math.abs((hp % 2) - 1));
        float r = 0, g = 0, bl = 0;

        if (hp < 1) { r = c; g = x; bl = 0; }
        else if (hp < 2) { r = x; g = c; bl = 0; }
        else if (hp < 3) { r = 0; g = c; bl = x; }
        else if (hp < 4) { r = 0; g = x; bl = c; }
        else if (hp < 5) { r = x; g = 0; bl = c; }
        else { r = c; g = 0; bl = x; }

        float m = b - c;
        int red = (int) ((r + m) * 255);
        int green = (int) ((g + m) * 255);
        int blue = (int) ((bl + m) * 255);

        return (red << 16) | (green << 8) | blue;
    }
}