package com.nyrds.pixeldungeon.windows;

import com.nyrds.platform.compatibility.RectF;
import com.nyrds.platform.gfx.BitmapData;
import com.watabou.gltextures.SmartTexture;
import com.watabou.noosa.Image;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * High-fidelity procedural Transmutation Circle.
 *
 * Contextual updates:
 * - Visuals now react to Recipe Complexity.
 * - Inputs determine outer node count.
 * - Total complexity determines central polygon sides.
 * - "Organic" or "Mob" ingredients shift color palette to "Forbidden/Blood" magic.
 */
public class TransmutationCircle extends Image {

    private String currentRecipeHash = "";
    private boolean needsUpdate = true;

    // Current Recipe Data
    private int inputCount = 0;
    private int outputCount = 0;
    private boolean isDarkMagic = false; // Triggered by Mob Corpses

    // Resolution
    private static final int SIZE = 512;

    // Aesthetic constants
    private static final float GLOW_FALLOFF = 4.5f;
    private static final float MAIN_LINE_THICKNESS = 1.9f;
    private static final float RUNE_LINE_THICKNESS = 1.2f;

    public TransmutationCircle() {
        super();
        // Default initialization
        setRecipe(new ArrayList<>(), new ArrayList<>());
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
        setOrigin(SIZE/2);
        angularSpeed = 100f; // Rotates slowly
        dirtyMatrix = true;
    }

    /**
     * Updates the circle based on the complexity and type of ingredients.
     */
    public void setRecipe(List<String> inputs, List<String> outputs) {
        // 1. Generate a stable hash for this specific combination
        List<String> combined = new ArrayList<>(inputs);
        combined.addAll(outputs);
        Collections.sort(combined); // Ensure order doesn't change seed
        String newHash = combined.toString();

        if (!currentRecipeHash.equals(newHash)) {
            currentRecipeHash = newHash;

            this.inputCount = inputs.size();
            this.outputCount = outputs.size();

            // 2. Analyze for "Dark Magic" (Corpses/Mobs)
            // You can adapt this logic to match your specific Item IDs or Class names
            this.isDarkMagic = false;
            for (String s : combined) {
                String lower = s.toLowerCase();
                if (lower.contains("corpse") || lower.contains("blood") || lower.contains("remains") || lower.contains("soul")) {
                    this.isDarkMagic = true;
                    break;
                }
            }

            needsUpdate = true;
        }
    }

    private void updateTexture() {
        SmartTexture tex = generateAlchemyArray();
        texture(tex);
        setWidth(SIZE);
        setHeight(SIZE);
        frame(new RectF(0, 0, 1, 1));
    }

    private SmartTexture generateAlchemyArray() {
        int[] pixels = new int[SIZE * SIZE];
        Arrays.fill(pixels, 0x00000000);

        long seedNum = currentRecipeHash.hashCode();
        Random rng = new Random(seedNum);

        // --- 1. Determine Color Palette ---
        int baseColor;
        if (isDarkMagic) {
            // Blood Red / Purple for Necromancy/Mob transmutation
            float hue = 0.95f + (rng.nextFloat() * 0.1f); // Red to Purple range
            if (hue > 1.0f) hue -= 1.0f;
            baseColor = getColorFromHSB(hue, 0.85f, 1.0f);
        } else {
            // Cyan / Gold for Standard transmutation
            float hue = rng.nextBoolean() ?
                    0.5f + (rng.nextFloat() * 0.15f) : // Cyan/Blue
                    0.1f + (rng.nextFloat() * 0.1f);   // Gold
            baseColor = getColorFromHSB(hue, 0.65f, 1.0f);
        }

        float centerX = SIZE / 2.0f;
        float centerY = SIZE / 2.0f;
        float maxRadius = (SIZE / 2.0f) * 0.96f;

        // --- 2. Layer 1: Container Rings ---
        float outerRingR = maxRadius;
        float innerRingR = maxRadius * 0.82f;

        drawRing(pixels, centerX, centerY, outerRingR, baseColor, 1.0f, MAIN_LINE_THICKNESS);
        drawRing(pixels, centerX, centerY, innerRingR, baseColor, 0.9f, MAIN_LINE_THICKNESS);

        // --- 3. Input Nodes (Representation of Ingredients) ---
        // Draw small circles on the outer rim representing the number of input items
        if (inputCount > 0) {
            int nodesToDraw = Math.min(inputCount, 12); // Cap visuals at 12 to prevent overcrowding
            float angleStep = (float)(2 * Math.PI) / nodesToDraw;
            float nodeRadius = (outerRingR - innerRingR) * 0.35f;

            for (int i = 0; i < nodesToDraw; i++) {
                float angle = i * angleStep - (float)Math.PI / 2;
                float nx = centerX + (float)Math.cos(angle) * ((outerRingR + innerRingR) / 2);
                float ny = centerY + (float)Math.sin(angle) * ((outerRingR + innerRingR) / 2);

                drawRing(pixels, nx, ny, nodeRadius, baseColor, 1.0f, MAIN_LINE_THICKNESS);

                // Connect node to center if it's a dark recipe (channeling energy)
                if (isDarkMagic && rng.nextBoolean()) {
                    float ex = centerX + (float)Math.cos(angle) * innerRingR;
                    float ey = centerY + (float)Math.sin(angle) * innerRingR;
                    drawLine(pixels, ex, ey, nx, ny, baseColor, 0.5f, 1.0f);
                }
            }
        } else {
            // Fallback Text Runes if no specific inputs defined yet
            float runeCenterR = (outerRingR + innerRingR) / 2.0f;
            float runeHeight = (outerRingR - innerRingR) * 0.65f;
            drawComplexRunes(pixels, centerX, centerY, runeCenterR, runeHeight, baseColor, seedNum, 0);
        }

        // --- 4. Main Geometry (Complexity based on Total Mass) ---
        float geoRadius = innerRingR * 0.95f;

        // Calculate sides based on complexity (Input + Output)
        // Min 3 (Triangle), Max 9 (Nonagon).
        // 1 item -> Triangle. 2 items -> Square. 3 items -> Pentagram, etc.
        int totalComplexity = Math.max(1, inputCount + outputCount);
        int sides = Math.max(3, Math.min(9, totalComplexity + 2));

        // If it's a standard transmutation (e.g. 1 seed -> 1 potion), use 6 (Hexagram) as it's the "Standard" alchemy shape
        if (inputCount == 1 && outputCount == 1) sides = 6;

        float angleOffset = rng.nextFloat() * (float)Math.PI;

        // Determine if Star or Polygon
        boolean isStar = sides >= 5;

        if (isStar) {
            // Step 2 for Pentagram (5/2), Step 3 for Heptagram/Octagram (7/3, 8/3)
            int step = (sides >= 7) ? 3 : 2;

            drawStarPolygon(pixels, centerX, centerY, sides, geoRadius, angleOffset, step, baseColor, 1.0f);
            drawRing(pixels, centerX, centerY, geoRadius, baseColor, 0.7f, MAIN_LINE_THICKNESS);

            // Inner circle proportional to the hole in the star
            float innerStarR = geoRadius * 0.5f;
            drawRing(pixels, centerX, centerY, innerStarR, baseColor, 0.8f, MAIN_LINE_THICKNESS);

            // Draw Inner Runes representing the Result
            if (outputCount > 0) {
                drawComplexRunes(pixels, centerX, centerY, innerStarR * 0.85f, innerStarR * 0.15f, baseColor, seedNum + 1, 1);
            }

        } else {
            // Simple geometry for simple recipes
            float[] mainVerts = getPolygonVerts(sides, geoRadius, angleOffset);
            drawPolygon(pixels, centerX, centerY, mainVerts, baseColor, 1.0f, MAIN_LINE_THICKNESS);

            // If Dark Magic and simple geometry, double it up (rotated)
            if (isDarkMagic) {
                float[] secVerts = getPolygonVerts(sides, geoRadius, angleOffset + (float)Math.PI/sides);
                drawPolygon(pixels, centerX, centerY, secVerts, baseColor, 0.8f, MAIN_LINE_THICKNESS);
            }

            // Eye/Core
            drawRing(pixels, centerX, centerY, geoRadius * 0.5f, baseColor, 1.0f, MAIN_LINE_THICKNESS);
        }

        // --- 5. Center Flux (The Reaction) ---
        // More output items = More chaotic lines in center
        int fluxLines = outputCount * 2 + (isDarkMagic ? 3 : 0);
        fluxLines = Math.min(fluxLines, 10);

        for(int k=0; k<fluxLines; k++) {
            float a = k * (float)Math.PI / (fluxLines/2f) + rng.nextFloat();
            float len = geoRadius * 0.9f;
            float x1 = centerX + (float)Math.cos(a) * len;
            float y1 = centerY + (float)Math.sin(a) * len;
            float x2 = centerX - (float)Math.cos(a) * len;
            float y2 = centerY - (float)Math.sin(a) * len;
            drawLine(pixels, x1, y1, x2, y2, baseColor, 0.3f, MAIN_LINE_THICKNESS);
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
     */
    private void drawStarPolygon(int[] pixels, float cx, float cy, int sides, float r, float angleOffset, int step, int color, float intensity) {
        float[] verts = getPolygonVerts(sides, r, angleOffset);
        int loops = gcd(sides, step);
        int pointsPerLoop = sides / loops;

        for (int l = 0; l < loops; l++) {
            int currentIdx = l;
            for (int i = 0; i < pointsPerLoop; i++) {
                int nextIdx = (currentIdx + step) % sides;
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

                drawLine(pixels, cx + (cos * radiusCenter) + rx1, cy + (sin * radiusCenter) + ry1,
                        cx + (cos * radiusCenter) + rx2, cy + (sin * radiusCenter) + ry2,
                        color, 0.9f, RUNE_LINE_THICKNESS);
            }
        }
    }

    // --- Drawing Primitives (Glow Logic) ---

    private void drawRing(int[] pixels, float cx, float cy, float r, int color, float intensity, float thickness) {
        int range = (int)(GLOW_FALLOFF + thickness + 1);
        int minX = Math.max(0, (int)(cx - r - range));
        int maxX = Math.min(SIZE, (int)(cx + r + range));
        int minY = Math.max(0, (int)(cy - r - range));
        int maxY = Math.min(SIZE, (int)(cy + r + range));

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
        int a2 = (int)(255 * Math.min(1.0f, alpha)); // Glow strength

        // Additive blending for glow effect
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
        return ((int)((r + m) * 255) << 16) | ((int)((g + m) * 255) << 8) | (int)((bl + m) * 255);
    }
}