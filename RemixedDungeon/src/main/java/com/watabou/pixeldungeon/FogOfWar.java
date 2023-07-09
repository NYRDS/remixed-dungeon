/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLES31;
import android.view.ViewDebug;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.gl.Texture;
import com.nyrds.util.Util;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.utils.GLog;

import java.util.Arrays;



public class FogOfWar extends Image {

    private static final int VISIBLE	= 0x00000000;
    private static final int VISITED    = 0xCC111111;
    private static final int MAPPED		= 0xCC442211;
    private static final int INVISIBLE	= 0xFF000000;

    private final int[] pixels;

    private final int pWidth;
    private final int pHeight;

    private int width2;
    private int height2;

    private final int mWidth;
    private final int mHeight;

    public FogOfWar(int mapWidth, int mapHeight) {

        super();

        mWidth = mapWidth;
        mHeight = mapHeight;

        pWidth = mapWidth + 1;
        pHeight = mapHeight + 1;

        width2 = 1;
        while (width2 < pWidth) {
            width2 <<= 1;
        }

        height2 = 1;
        while (height2 < pHeight) {
            height2 <<= 1;
        }

        float size = DungeonTilemap.SIZE;
        setWidth(width2 * size);
        setHeight(height2 * size);

        pixels = new int[width2 * height2];
        Arrays.fill(pixels, INVISIBLE);

        texture(new FogTexture());

        setScaleXY(
                DungeonTilemap.SIZE,
                DungeonTilemap.SIZE);

        setX(-size / 2);
        setY(-size / 2);
    }

    public void updateVisibility(boolean[] visible, boolean[] visited, boolean[] mapped, boolean firstRowHack) {
        dirty = true;

        if (firstRowHack) {
            int pos = 0;
            for (int j = 1; j < mWidth; j++) {
                pos++;
                int c = INVISIBLE;

                if (visible[pos + pWidth]) {
                    c = VISIBLE;
                } else if (visited[pos + pWidth]) {
                    c = VISITED;
                } else if (mapped[pos + pWidth]) {
                    c = MAPPED;
                }
                pixels[j] = c;
            }
        }

        for (int i = 1; i < pHeight - 1; i++) {
            int w_minus_one = pWidth - 1;
            int pos = w_minus_one * i;
            for (int j = 1; j < w_minus_one; j++) {
                pos++;
                int c = INVISIBLE;


                int p_minus_w_minus_one = pos - w_minus_one;
                if (visible[pos] && visible[p_minus_w_minus_one] &&
                        visible[pos - 1] && visible[p_minus_w_minus_one - 1]) {
                    c = VISIBLE;
                } else if (visited[pos] || visited[p_minus_w_minus_one] ||
                        visited[pos - 1] || visited[p_minus_w_minus_one - 1]) {
                    c = VISITED;
                } else if (mapped[pos] && mapped[p_minus_w_minus_one] &&
                        mapped[pos - 1] && mapped[p_minus_w_minus_one - 1]) {
                    c = MAPPED;
                }

                if (Util.isDebug()) {
                    var candidates = Dungeon.level.candidates;
                    if ((candidates.contains(pos) || candidates.contains(pos - 1)
                            || candidates.contains(p_minus_w_minus_one) || candidates.contains(p_minus_w_minus_one - 1))
                    ) {
                        c = 0xaa444499;
                    }
                }

                pixels[i * width2 + j] = c;
            }
        }
    }

    @Override
    public void draw() {
        //if(dirty) {
            texture.pixels(width2, height2, pixels);
        //}
        super.draw();
    }

    private class FogTexture extends SmartTexture {

        public FogTexture() {
            super(Bitmap.createBitmap(width2, height2, Bitmap.Config.ARGB_8888));
            //filter( Texture.NEAREST, Texture.NEAREST );
            filter(Texture.LINEAR, Texture.LINEAR);
            TextureCache.add(FogOfWar.class, this);
        }
    }
}
