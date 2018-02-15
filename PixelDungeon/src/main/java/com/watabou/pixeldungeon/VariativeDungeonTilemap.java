package com.watabou.pixeldungeon;

import com.nyrds.android.util.ModdingMode;
import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.levels.XTilemapConfiguration;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.Tilemap;
import com.watabou.pixeldungeon.levels.Level;

import org.json.JSONException;

/**
 * Created by mike on 15.02.2018.
 * This file is part of Remixed Pixel Dungeon.
 */

public class VariativeDungeonTilemap extends DungeonTilemap {
    private Tilemap mBaseLayer;
    private Tilemap mDecoLayer;

    private XTilemapConfiguration xTilemapConfiguration;

    private Level level;

    private int[] mBaseMap;
    private int[] mDecoMap;

    public VariativeDungeonTilemap(Level level, String tiles) {
        super(level, tiles);
        this.level = level;

        int mSize = level.getWidth() * level.getHeight();

        try {
            String tilemapConfig = "tilemapDesc/" + tiles.replace(".png", ".json");
            if (!ModdingMode.isResourceExist(tilemapConfig)) {
                tilemapConfig = "tilemapDesc/tiles_x_default.json";
            }
            xTilemapConfiguration = XTilemapConfiguration.readConfig(tilemapConfig);
        } catch (JSONException e) {
            throw new TrackedRuntimeException(e);
        }

        mBaseLayer = new Tilemap(tiles, new TextureFilm(tiles, SIZE, SIZE));

        mBaseMap = new int[mSize];
        mBaseLayer.map(buildGroundMap(), level.getWidth());

        mDecoLayer = new Tilemap(tiles, new TextureFilm(tiles, SIZE, SIZE));
        mDecoMap = new int[mSize];
        mDecoLayer.map(buildDecoMap(), level.getWidth());
    }

    @Override
    protected Image tile(int pos, int oldValue) {
        return null;
    }

    private int currentDecoCell(int cell) {
        return xTilemapConfiguration.decoTile(level, cell);
    }

    private int[] buildDecoMap() {
        for (int i = 0; i < mDecoMap.length; i++) {
            mDecoMap[i] = currentDecoCell(i);
        }

        return mDecoMap;
    }

    private int currentBaseCell(int cell) {
        return xTilemapConfiguration.baseTile(level, cell);
    }

    private int[] buildGroundMap() {
        for (int i = 0; i < mBaseMap.length; i++) {
            mBaseMap[i] = currentBaseCell(i);
        }

        return mBaseMap;
    }

    @Override
    public void draw() {
        mBaseLayer.draw();
        mDecoLayer.draw();
    }

    public void updateAll() {
        buildGroundMap();
        buildDecoMap();
        mBaseLayer.updateRegion().set(0, 0, level.getWidth(), level.getHeight());
        mDecoLayer.updateRegion().set(0, 0, level.getWidth(), level.getHeight());
    }

    public void updateCell(int cell, Level level) {
        int x = level.cellX(cell);
        int y = level.cellY(cell);
        mBaseMap[cell] = currentBaseCell(cell);
        mDecoMap[cell] = currentDecoCell(cell);
        mBaseLayer.updateRegion().union(x, y);
        mDecoLayer.updateRegion().union(x, y);
    }
}
