package com.watabou.pixeldungeon;

import com.nyrds.platform.gfx.BitmapData;
import com.nyrds.platform.gl.Gl;
import com.nyrds.platform.gl.Texture;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Image;

import java.util.Arrays;

public class FogOfWar extends Image {

    private static final int VISIBLE = 0xFF000000;
    private static final int VISITED = 0x60000000;
    private static final int MAPPED = 0x30000000;
    private static final int INVISIBLE = 0x00000000;

    private int[] pixels;
    private int[] old_pixels;

    private int pWidth;
    private int pHeight;

    private int mWidth;
    private int mHeight;

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

        pWidth = mapWidth + 1;
        pHeight = mapHeight + 1;


        float size = DungeonTilemap.SIZE;
        setWidth(width * size);
        setHeight(height * size);

        pixels = new int[(int) (mWidth * mHeight)];
        old_pixels = new int[(int) (mWidth * mHeight)];

        Arrays.fill(pixels, INVISIBLE);
        Arrays.fill(old_pixels, VISIBLE);

        texture(new FogTexture());

        setScaleXY(
                DungeonTilemap.SIZE,
                DungeonTilemap.SIZE);

        setX(-size / 2);
        setY(-size / 2);
    }

    public void updateVisibility(boolean[] visible, boolean[] visited, boolean[] mapped, boolean firstRowHack) {
        dirty = true;

        Arrays.fill(pixels, INVISIBLE);
        boolean noVisited = Dungeon.isChallenged(Challenges.NO_MAP);

        if (firstRowHack) {
            int pos = 0;
            for (int j = 1; j < mWidth; j++) {
                pos++;
                int c = INVISIBLE;

                if (visible[pos + pWidth]) {
                    c = VISIBLE;
                } else {
                    if (!noVisited) {
                        if (mapped[pos + pWidth]) {
                            c = MAPPED;
                        }

                        if (visited[pos + pWidth]) {
                            c = VISITED;
                        }
                    }
                }


                pixels[j] = c;
            }
        }

        for (int i = 1; i < pHeight - 1; i++) {

            int pos = mWidth * i;
            for (int j = 1; j < mWidth; j++) {
                pos++;
                int c = INVISIBLE;

                int p_minus_w_minus_one = pos - mWidth;
                if (visible[pos] && visible[p_minus_w_minus_one] &&
                        visible[pos - 1] && visible[p_minus_w_minus_one - 1]) {
                    c = VISIBLE;
                } else {
                    if(!noVisited) {
                        if (mapped[pos] && mapped[p_minus_w_minus_one] &&
                                mapped[pos - 1] && mapped[p_minus_w_minus_one - 1]) {
                            c = MAPPED;
                        }

                        if (visited[pos] || visited[p_minus_w_minus_one] ||
                                visited[pos - 1] || visited[p_minus_w_minus_one - 1]) {
                            c = VISITED;
                        }
                    }
                }
/*
                if (Util.isDebug()) {
                    var candidates = Dungeon.level.candidates;
                    if ((candidates.contains(pos) || candidates.contains(pos - 1)
                            || candidates.contains(p_minus_w_minus_one) || candidates.contains(p_minus_w_minus_one - 1))
                    ) {
                        c = 0xaa444499;
                    }
                }
*/
                pixels[i * mWidth + j] = c;
            }
        }
    }

    @Override
    public void draw() {
        if (dirty || !Arrays.equals(pixels, old_pixels)) {
            texture.pixels(mWidth, mHeight, pixels);
            System.arraycopy(pixels, 0, old_pixels, 0, pixels.length);
        }
        Gl.fowBlend();
        super.draw();
        Gl.blendSrcAlphaOneMinusAlpha();
    }

    private BitmapData toDispose;

    private class FogTexture extends SmartTexture {

        public FogTexture() {
            super(toDispose = BitmapData.createBitmap(mWidth,mHeight));
            filter(Texture.LINEAR, Texture.LINEAR);
            TextureCache.add(FogOfWar.class, this);
        }
    }
}
