package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.alchemy.AlchemyRecipes;
import com.nyrds.pixeldungeon.items.Carcass;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.nyrds.platform.compatibility.RectF;
import com.nyrds.platform.gfx.BitmapData;
import com.watabou.gltextures.SmartTexture;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * High-Fidelity Transmutation Circle.
 *
 * NEW FEATURES:
 * - Dual Geometry: Generates a Primary (Outer) and Secondary (Inner) shape.
 * - Procedural Shapes: Sides (3-12) and Density (Star sharpness) are randomized based on Seed.
 * - Thematic Bias: Organic recipes bias towards Odd/Sharp stars; Stable recipes bias towards Even/Solid polygons.
 */
public class TransmutationCircle extends Image {

    private String currentRecipeHash = "";
    private boolean needsUpdate = true;
    private RecipeAnalysis analysis;

    // Constants
    private static final int SIZE = 512;
    private static final float CENTER = SIZE / 2.0f;
    private static final float GLOW_FALLOFF = 14.0f;
    private static final float CORE_THICKNESS = 4.0f;
    private static final float RUNE_SIZE = 34.0f;
    private static final float RUNE_THICKNESS = 2.8f;

    public TransmutationCircle() {
        super();
        this.analysis = new RecipeAnalysis();
        setRecipe(new ArrayList<>(), new ArrayList<>());
        updateTexture();
        alpha(1.0f);
    }

    @Override
    public void update() {
        super.update();
        if (needsUpdate) {
            updateTexture();
            needsUpdate = false;
        }
        setOrigin(CENTER, CENTER);
        // Rotation speed based on complexity
        float speed = 15f + (analysis.instability * 40f);
        angularSpeed = speed;
        dirtyMatrix = true;
    }

    public void setRecipe(List<String> inputs, List<String> outputs) {
        List<String> combined = new ArrayList<>(inputs);
        combined.addAll(outputs);
        Collections.sort(combined);
        String newHash = combined.toString();

        if (newHash.equals(currentRecipeHash)) return;

        currentRecipeHash = newHash;
        analysis = new RecipeAnalysis();
        analysis.outputCount = outputs.size();
        analysis.seed = newHash.hashCode();

        // --- Analyze Inputs ---
        for(String input: inputs ) {
            AlchemyRecipes.EntityType type = AlchemyRecipes.determineEntityType(input);
            float weight = 10f;
            boolean organic = false;

            if (type == AlchemyRecipes.EntityType.ITEM) {
                Item item = ItemFactory.itemByName(input);
                if (item != null) {
                    weight += item.price();
                    if (input.contains("Potion") || input.contains("Seed") || input.contains("Meat") || input.contains("Blood")) {
                        organic = true;
                    }
                }
            } else if (type == AlchemyRecipes.EntityType.CARCASS) {
                Item item = ItemFactory.itemByName(input);
                if (item instanceof Carcass) {
                    Carcass body = (Carcass) item;
                    Mob m = (Mob) body.src;
                    if (m != null) weight += (m.ht() * 3);
                    organic = true;
                }
            }
            analysis.addIngredient(weight, organic, true, 0);
        }

        // --- Analyze Outputs ---
        for(String output: outputs ) {
            AlchemyRecipes.EntityType type = AlchemyRecipes.determineEntityType(output);
            float weight = 0;
            float combatPower = 0;
            boolean organic = false;

            if (type == AlchemyRecipes.EntityType.ITEM) {
                Item item = ItemFactory.itemByName(output);
                if (item != null) {
                    weight = item.price();
                    combatPower = item.level() * 15;
                }
            } else if (type == AlchemyRecipes.EntityType.MOB) {
                Mob mob = MobFactory.mobByName(output);
                if (mob != null) {
                    weight = mob.ht() * 3;
                    combatPower = mob.attackSkill(CharsList.DUMMY) + mob.defenseSkill(CharsList.DUMMY);
                    organic = true;
                }
            }
            analysis.addIngredient(weight, organic, false, combatPower);
        }

        analysis.calculateVisuals();
        needsUpdate = true;
    }

    private static class ShapeProfile {
        int sides;
        int step; // 1 = Polygon, 2+ = Star
        float radiusScale;
        float rotation;
        boolean drawCircle; // Enclose in a ring?
    }

    private static class RecipeAnalysis {
        List<Float> inputWeights = new ArrayList<>();
        int outputCount = 0;
        long seed;

        float totalMass = 0;
        float organicMass = 0;
        float totalCombat = 0;

        // Visual Props
        int primaryColor, secondaryColor;
        float instability;

        // Geometry Profiles
        ShapeProfile primaryShape = new ShapeProfile();
        ShapeProfile secondaryShape = new ShapeProfile();

        void addIngredient(float weight, boolean organic, boolean isInput, float combat) {
            totalMass += weight;
            if (organic) organicMass += weight;
            totalCombat += combat;
            if (isInput) inputWeights.add(Math.max(1f, weight));
        }

        void calculateVisuals() {
            Random rng = new Random(seed);
            float organicRatio = totalMass > 0 ? organicMass / totalMass : 0;
            boolean isDark = totalCombat > 50 || organicRatio > 0.7f;

            // --- Color ---
            float hue;
            if (isDark) hue = (rng.nextFloat() * 0.15f) + 0.85f; // Purple-Red
            else if (organicRatio > 0.4f) hue = rng.nextFloat() * 0.1f; // Orange-Gold
            else hue = 0.5f + (rng.nextFloat() * 0.15f); // Cyan-Blue

            primaryColor = HSBtoRGB(hue, 0.85f, 1.0f);
            secondaryColor = HSBtoRGB((hue + 0.1f) % 1.0f, 0.9f, 1.0f);
            instability = organicRatio * 0.5f;

            // --- Primary Shape Generation ---
            // Organic/Dark prefers ODD numbers (3, 5, 7, 9)
            // Inorganic/Stable prefers EVEN numbers (4, 6, 8, 12)
            int[] oddOptions = {3, 5, 5, 7, 7, 9};
            int[] evenOptions = {4, 4, 6, 6, 6, 8, 8, 12};

            if (isDark) {
                primaryShape.sides = oddOptions[rng.nextInt(oddOptions.length)];
                // High combat = High "step" (Sharp stars)
                int maxStep = (primaryShape.sides - 1) / 2;
                primaryShape.step = Math.max(1, rng.nextInt(maxStep) + 1);
                // Prefer sharp stars
                if (maxStep > 1 && rng.nextBoolean()) primaryShape.step = maxStep;
            } else {
                primaryShape.sides = evenOptions[rng.nextInt(evenOptions.length)];
                // Stable = Lower steps (Polygons or simple stars)
                primaryShape.step = (primaryShape.sides >= 6) ? 2 : 1;
            }

            primaryShape.radiusScale = 0.75f;
            primaryShape.rotation = -1.57f; // Point up
            primaryShape.drawCircle = rng.nextBoolean();

            // --- Secondary Shape Generation ---
            // Often related to primary (e.g., Primary*2, Primary/2, or Coprime)
            if (rng.nextBoolean()) {
                // Harmonic resonance (Same geometry, different size/rotation)
                secondaryShape.sides = primaryShape.sides;
                secondaryShape.step = primaryShape.step == 1 ? (primaryShape.sides >= 5 ? 2 : 1) : 1;
                secondaryShape.rotation = primaryShape.rotation + (float)Math.PI / primaryShape.sides;
            } else {
                // Inner core
                secondaryShape.sides = rng.nextBoolean() ? 3 : 4;
                if (primaryShape.sides % 3 == 0) secondaryShape.sides = 3;
                if (primaryShape.sides % 4 == 0) secondaryShape.sides = 4;
                secondaryShape.step = 1;
                secondaryShape.rotation = rng.nextFloat() * 3.14f;
            }

            secondaryShape.radiusScale = primaryShape.radiusScale * 0.5f;
            secondaryShape.drawCircle = true;

            // Invert primary for dark magic
            if (isDark && primaryShape.sides % 2 != 0) {
                primaryShape.rotation += (float)Math.PI;
            }
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

        long seed = currentRecipeHash.hashCode();
        Random rng = new Random(seed);

        float maxRadius = (SIZE / 2.0f) * 0.96f;

        // --- Layer 1: Background Runes ---
        drawRuneRing(pixels, CENTER, CENTER, maxRadius, analysis.secondaryColor, seed);
        drawRing(pixels, CENTER, CENTER, maxRadius * 0.88f, analysis.primaryColor, 0.9f, CORE_THICKNESS);

        // --- Layer 2: Primary Geometry (Outer) ---
        ShapeProfile p1 = analysis.primaryShape;
        drawProceduralPolygon(pixels, CENTER, CENTER, maxRadius * p1.radiusScale,
                p1.sides, p1.step, p1.rotation,
                analysis.primaryColor, analysis.secondaryColor, 1);

        if (p1.drawCircle) {
            drawRing(pixels, CENTER, CENTER, maxRadius * p1.radiusScale, analysis.primaryColor, 0.5f, 2.0f);
        }

        // --- Layer 3: Secondary Geometry (Inner) ---
        ShapeProfile p2 = analysis.secondaryShape;
        drawProceduralPolygon(pixels, CENTER, CENTER, maxRadius * p2.radiusScale,
                p2.sides, p2.step, p2.rotation,
                analysis.secondaryColor, analysis.primaryColor, 0);

        // --- Layer 4: Inter-Geometry Flux Lines ---
        // If geometry aligns, draw connecting beams
        if (p1.sides == p2.sides || p1.sides % p2.sides == 0) {
            float r1 = maxRadius * p1.radiusScale;
            float r2 = maxRadius * p2.radiusScale;
            float[] v1 = getPolygonVerts(p1.sides, r1, p1.rotation);
            float[] v2 = getPolygonVerts(p1.sides, r2, p2.rotation); // Use p1 count to match vertices

            for(int i=0; i<p1.sides; i++) {
                drawLine(pixels, v1[i*2] + CENTER, v1[i*2+1] + CENTER,
                        v2[i*2] + CENTER, v2[i*2+1] + CENTER,
                        analysis.primaryColor, 0.4f, 1.5f);
            }
        }

        // --- Layer 5: Inputs ---
        if (!analysis.inputWeights.isEmpty()) {
            drawInputs(pixels, maxRadius * 0.74f, analysis.inputWeights, analysis.primaryColor, analysis.secondaryColor, rng);
        }

        // --- Layer 6: Output Highlights ---
        drawOutputHighlights(pixels, CENTER, CENTER, maxRadius * 0.45f, analysis.outputCount, analysis.secondaryColor, rng);

        // Center Core
        drawRing(pixels, CENTER, CENTER, 6f, 0xFFFFFFFF, 1.0f, 4.0f);

        BitmapData bitmap = BitmapData.createBitmap(SIZE, SIZE);
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                bitmap.setPixel(x, y, pixels[y * SIZE + x]);
            }
        }
        return new SmartTexture(bitmap);
    }

    /**
     * The master geometry drawer. Handles Polygons, Simple Stars, and Complex Stars.
     * @param recursionLevel 1 = Draw sub-shapes at corners. 0 = Just lines.
     */
    private void drawProceduralPolygon(int[] pixels, float cx, float cy, float r, int sides, int step, float angleOffset, int color, int altColor, int recursionLevel) {
        if (r < 5.0f) return;

        float[] verts = getPolygonVerts(sides, r, angleOffset);

        // GCD determines how many independent loops are needed
        // (e.g. Hexagram {6/2} needs 2 triangles. Pentagram {5/2} needs 1 loop).
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

                drawLine(pixels, x1, y1, x2, y2, color, 1.0f, CORE_THICKNESS);

                // Recursion: Draw small glyphs at vertices
                if (recursionLevel > 0) {
                    drawRing(pixels, x1, y1, r * 0.08f, altColor, 0.8f, 2.0f);
                }
                currentIdx = nextIdx;
            }
        }
    }

    private void drawInputs(int[] pixels, float r, List<Float> weights, int color, int subColor, Random rng) {
        float totalInputMass = 0;
        for(float w : weights) totalInputMass += w;

        // Start opposite to Primary Shape rotation usually
        float currentAngle = (float)Math.PI;

        for (Float weight : weights) {
            float share = (weight / totalInputMass);
            float arc = share * (float)(Math.PI * 2);
            float angle = currentAngle + (arc / 2.0f);

            // Random wobble based on weight (heavier items stabilize)
            float dist = r + (rng.nextFloat() * 10f - 5f);

            float nx = CENTER + (float)Math.cos(angle) * dist;
            float ny = CENTER + (float)Math.sin(angle) * dist;
            float nSize = 14f + (float)Math.log(weight) * 4f;

            drawRing(pixels, nx, ny, nSize, color, 1.0f, CORE_THICKNESS);
            drawLine(pixels, nx, ny, CENTER, CENTER, subColor, 0.3f, 2.0f);

            if (nSize > 25) drawComplexRune(pixels, nx, ny, nSize * 0.6f, subColor, rng);
            currentAngle += arc;
        }
    }

    private void drawOutputHighlights(int[] pixels, float cx, float cy, float r, int count, int color, Random rng) {
        if (count <= 0) return;
        if (count == 1) {
            // Central Nova
            drawRing(pixels, cx, cy, 14f, color, 1.0f, 6.0f);
            return;
        }

        // Orbiting Moons
        float angleStep = (float)(Math.PI * 2) / count;
        float startAngle = rng.nextFloat() * 6.28f;

        for (int i = 0; i < count; i++) {
            float a = startAngle + (i * angleStep);
            float ox = cx + (float)Math.cos(a) * r;
            float oy = cy + (float)Math.sin(a) * r;
            // Beam to center
            drawLine(pixels, cx, cy, ox, oy, color, 0.4f, 1.5f);
            // Orb
            drawRing(pixels, ox, oy, 9.0f, color, 1.0f, 3.0f);
            drawRing(pixels, ox, oy, 3.0f, 0xFFFFFFFF, 1.0f, 2.0f);
        }
    }

    // --- Utility Methods ---

    private void drawRuneRing(int[] pixels, float cx, float cy, float r, int color, long seed) {
        float circumference = 2 * (float)Math.PI * r;
        int count = (int)(circumference / (RUNE_SIZE * 1.15f));
        float step = (float)(Math.PI * 2) / count;
        Random rng = new Random(seed);
        for (int i = 0; i < count; i++) {
            float angle = i * step;
            float rx = cx + (float)Math.cos(angle) * (r - (RUNE_SIZE * 0.6f));
            float ry = cy + (float)Math.sin(angle) * (r - (RUNE_SIZE * 0.6f));
            drawComplexRune(pixels, rx, ry, RUNE_SIZE, color, rng);
        }
    }

    private void drawComplexRune(int[] pixels, float cx, float cy, float size, int color, Random rng) {
        float s2 = size / 2.0f;
        float[] gridX = {-s2, 0, s2};
        float[] gridY = {-s2, 0, s2};
        int strokes = 2 + rng.nextInt(3);
        float rot = (rng.nextFloat() - 0.5f) * 0.4f;
        float cos = (float)Math.cos(rot); float sin = (float)Math.sin(rot);

        for (int i = 0; i < strokes; i++) {
            int p1 = rng.nextInt(9); int p2 = rng.nextInt(9);
            if (p1 == p2) p2 = (p1 + 1) % 9;
            float lx1 = gridX[p1 % 3]; float ly1 = gridY[p1 / 3];
            float lx2 = gridX[p2 % 3]; float ly2 = gridY[p2 / 3];
            float x1 = cx + (lx1 * cos - ly1 * sin); float y1 = cy + (lx1 * sin + ly1 * cos);
            float x2 = cx + (lx2 * cos - ly2 * sin); float y2 = cy + (lx2 * sin + ly2 * cos);
            drawLine(pixels, x1, y1, x2, y2, color, 0.9f, RUNE_THICKNESS);
        }
    }

    private void drawRing(int[] pixels, float cx, float cy, float r, int color, float intensity, float thickness) {
        int range = (int)(r + GLOW_FALLOFF + thickness);
        int minX = Math.max(0, (int)(cx - range)); int maxX = Math.min(SIZE, (int)(cx + range));
        int minY = Math.max(0, (int)(cy - range)); int maxY = Math.min(SIZE, (int)(cy + range));
        for (int y = minY; y < maxY; y++) {
            for (int x = minX; x < maxX; x++) {
                float dist = (float)Math.sqrt((x-cx)*(x-cx) + (y-cy)*(y-cy));
                float alpha = getLaserGlow(Math.abs(dist - r), thickness) * intensity;
                if (alpha > 0.01f) blendPixel(pixels, x, y, color, alpha);
            }
        }
    }

    private void drawLine(int[] pixels, float x1, float y1, float x2, float y2, int color, float intensity, float thickness) {
        int range = (int)(GLOW_FALLOFF + thickness + 2);
        int minX = Math.max(0, (int)(Math.min(x1, x2) - range)); int maxX = Math.min(SIZE, (int)(Math.max(x1, x2) + range));
        int minY = Math.max(0, (int)(Math.min(y1, y2) - range)); int maxY = Math.min(SIZE, (int)(Math.max(y1, y2) + range));
        for (int y = minY; y < maxY; y++) {
            for (int x = minX; x < maxX; x++) {
                float d = distToSegment(x, y, x1, y1, x2, y2);
                float alpha = getLaserGlow(d, thickness) * intensity;
                if (alpha > 0.01f) blendPixel(pixels, x, y, color, alpha);
            }
        }
    }

    private float getLaserGlow(float dist, float thickness) {
        float coreLimit = thickness * 0.5f;
        if (dist < coreLimit) return 1.0f;
        float t = (dist - coreLimit) / GLOW_FALLOFF;
        if (t >= 1.0f) return 0.0f;
        return (1.0f - t) * (1.0f - t);
    }

    private void blendPixel(int[] pixels, int x, int y, int color, float alpha) {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) return;
        int idx = y * SIZE + x;
        int current = pixels[idx];
        int r1 = (current >> 16) & 0xFF; int g1 = (current >> 8) & 0xFF; int b1 = current & 0xFF;
        int r2 = (color >> 16) & 0xFF; int g2 = (color >> 8) & 0xFF; int b2 = color & 0xFF;

        int rOut = Math.min(255, r1 + (int)(r2 * alpha));
        int gOut = Math.min(255, g1 + (int)(g2 * alpha));
        int bOut = Math.min(255, b1 + (int)(b2 * alpha));
        int aOut = Math.min(255, Math.max((current >> 24) & 0xFF, (int)(255 * alpha)));
        pixels[idx] = (aOut << 24) | (rOut << 16) | (gOut << 8) | bOut;
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
        float l2 = (vx-wx)*(vx-wx) + (vy-wy)*(vy-wy);
        if (l2 == 0) return (float)Math.sqrt((px-vx)*(px-vx) + (py-vy)*(py-vy));
        float t = Math.max(0, Math.min(1, ((px-vx)*(wx-vx) + (py-vy)*(wy-vy)) / l2));
        float projX = vx + t * (wx-vx); float projY = vy + t * (wy-vy);
        return (float)Math.sqrt((px-projX)*(px-projX) + (py-projY)*(py-projY));
    }

    private int gcd(int a, int b) { return b == 0 ? a : gcd(b, a % b); }

    private static int HSBtoRGB(float h, float s, float b) {
        float c = b * s; float hp = (h % 1.0f) * 6; float x = c * (1 - Math.abs((hp % 2) - 1));
        float r = 0, g = 0, bl = 0;
        if (hp < 1) { r = c; g = x; bl = 0; } else if (hp < 2) { r = x; g = c; bl = 0; }
        else if (hp < 3) { r = 0; g = c; bl = x; } else if (hp < 4) { r = 0; g = x; bl = c; }
        else if (hp < 5) { r = x; g = 0; bl = c; } else { r = c; g = 0; bl = x; }
        float m = b - c;
        return ((int)((r+m)*255)<<16) | ((int)((g+m)*255)<<8) | (int)((bl+m)*255);
    }
}