package com.nyrds.platform.gfx;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class PseudoPixmapPacker extends PixmapPacker {
    public PseudoPixmapPacker() {
        super(1024, 1024, Pixmap.Format.RGBA8888, 0, false);
    }

    @Override
    public synchronized void updateTextureRegions (Array<TextureRegion> regions, Texture.TextureFilter minFilter, Texture.TextureFilter magFilter,
                                                   boolean useMipMaps) {
    }
}
