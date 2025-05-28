package com.watabou.pixeldungeon;

import com.nyrds.platform.gfx.BitmapData;
import com.nyrds.platform.gl.Gl;
import com.nyrds.platform.gl.Texture;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Image;

import java.util.Arrays;

public class FogOfWar extends Image {

    private static final int VISIBLE = 0xFF;
    private static final int VISITED = 0x60;
    private static final int MAPPED = 0x30;
    private static final int INVISIBLE = 0x00;

    private int[] pixels;
    private int[] s_pixels;
    private int[] old_pixels;

    private int pWidth;
    private int pHeight;

    private int mWidth;
    private int mHeight;
    final private int MARGIN = 4;

    public FogOfWar(int mapWidth, int mapHeight) {
        super();
        reinit(mapWidth, mapHeight);
    }

    public int getLength() {
        return mWidth * mHeight;
    }

    public void reinit(int mapWidth, int mapHeight) {
        mWidth = mapWidth;
        mHeight = mapHeight;

        pWidth = 2*mapWidth + MARGIN * 2;
        pHeight = 2*mapHeight + MARGIN * 2;

        float size = (float) DungeonTilemap.SIZE / 2;
        setWidth(pWidth * size);
        setHeight(pHeight * size);

        pixels = new int[(int) (pWidth * pHeight)];
        s_pixels = new int[(int) (pWidth * pHeight)];
        old_pixels = new int[(int) (pWidth * pHeight)];

        Arrays.fill(pixels, INVISIBLE);
        Arrays.fill(old_pixels, INVISIBLE);

        texture(new FogTexture());

        setScaleXY(size,size);

        setX(-MARGIN* size);
        setY(-MARGIN* size);
    }

    public void updateVisibility(boolean[] visible, boolean[] visited, boolean[] mapped) {
        dirty = true;

        Arrays.fill(pixels, INVISIBLE);

        boolean noVisited = Dungeon.isChallenged(Challenges.NO_MAP);


        for (int j = 0; j < mHeight; j++) {
            for (int i = 0; i < mWidth; i++) {
                int pos =  i + j * mWidth;
                int c = INVISIBLE;

                if (visible[pos]) {
                    c = VISIBLE;
                } else {

                    if (!noVisited) {
                        if (mapped[pos]) {
                            c = MAPPED;
                        }

                        if (visited[pos]) {
                            c = VISITED;
                        }
                    }
                }

                pixels[(2*i+MARGIN) + (2*j+MARGIN) * pWidth] = c;
                pixels[(2*i+1+MARGIN) + (2*j+MARGIN) * pWidth] = c;
                pixels[(2*i+MARGIN) + (2*j+1+MARGIN) * pWidth] = c;
                pixels[(2*i+1+MARGIN) + (2*j+1+MARGIN) * pWidth] = c;
            }
        }
        applySmoothing();
    }

    private void applySmoothing() {

        int[][] weights = {{1, 1, 1}, {1, 1, 1}, {1, 1, 1}};

        int radius = 1; // Kernel radius

        for (int y = 0; y < pHeight; y++) {
            for (int x = 0; x < pWidth; x++) {
                int aSum = 0;
                int weightSum = 0;

                // Get original alpha for this pixel
                int originalAlpha = pixels[y * pWidth + x];
                s_pixels[y * pWidth + x] = originalAlpha;
                if (originalAlpha > 0) {
                    // Sample neighboring pixels
                    for (int dy = -radius; dy <= radius; dy++) {
                        for (int dx = -radius; dx <= radius; dx++) {
                            int px = x + dx;
                            int py = y + dy;
                            if (px >= 0 && px < pWidth && py >= 0 && py < pHeight) {
                                int weight = weights[dy + radius][dx + radius];
                                int sampleAlpha = pixels[py * pWidth + px];


                                if (originalAlpha == 0xff) {
                                    if (sampleAlpha < originalAlpha) {
                                        aSum += sampleAlpha * weight;
                                        weightSum += weight;
                                    }
                                } else {
                                    aSum += sampleAlpha * weight;
                                    weightSum += weight;
                                }
                            }
                        }
                    }

                    int smoothedAlpha = weightSum > 0 ?
                            Math.min(originalAlpha, aSum / weightSum) :
                            originalAlpha;

                    s_pixels[y * pWidth + x] = (smoothedAlpha << 24);
                }
            }
        }
    }

    @Override
    public void draw() {
        if (dirty || !Arrays.equals(s_pixels, old_pixels)) {
            texture.pixels(pWidth, pHeight, s_pixels);
            System.arraycopy(s_pixels, 0, old_pixels, 0, pixels.length);
        }
        Gl.fowBlend();
        super.draw();

/*
        FogShader script = FogShader.get();
        script.texSize(pWidth, pHeight);
        texture.bind();

        script.resetCamera();
        script.camera(camera());

        script.uModel.valueM4(matrix);
        script.lighting(
                rm, gm, bm, am,
                ra, ga, ba, aa);

        updateFrame();
        updateVerticesBuffer();

        script.drawQuad(verticesBuffer);
*/
        Gl.blendSrcAlphaOneMinusAlpha();
    }

    private BitmapData toDispose;

    private class FogTexture extends SmartTexture {

        public FogTexture() {
            super(toDispose = BitmapData.createBitmap(pWidth,pHeight));
            filter(Texture.LINEAR, Texture.LINEAR);
            TextureCache.add(FogOfWar.class, this);
        }
    }
}
