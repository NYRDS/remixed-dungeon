package com.watabou.pixeldungeon;

import com.nyrds.pixeldungeon.levels.XTilemapConfiguration;
import com.nyrds.util.ModdingMode;
import com.watabou.noosa.CompositeImage;
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
    private final Tilemap mDecoLayer;

    private final XTilemapConfiguration xTilemapConfiguration;

    private final Level level;

    private final int[] mDecoMap;

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
            throw ModdingMode.modException(e);
        }

        data = new int[level.getWidth()*level.getHeight()];
        map(buildGroundMap(),level.getWidth());

        mDecoLayer = new Tilemap(tiles, new TextureFilm(tiles, SIZE, SIZE));
        mDecoMap = new int[mSize];
        mDecoLayer.map(buildDecoMap(), level.getWidth());
    }

    @Override
    public Image tile(int pos) {
        CompositeImage img = new CompositeImage(getTexture());
        img.frame(getTileset().get(data[pos]));

        Image deco = new Image(getTexture());
        deco.frame(getTileset().get(mDecoMap[pos]));

        img.addLayer(deco);

        return img;
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
        for (int i = 0; i < data.length; i++) {
            data[i] = currentBaseCell(i);
        }

        return data;
    }

    @Override
    public void draw() {
        super.draw();
        mDecoLayer.draw();
    }

    public void updateAll() {
        buildGroundMap();
        buildDecoMap();
        updateRegion().set(0, 0, level.getWidth(), level.getHeight());
        mDecoLayer.updateRegion().set(0, 0, level.getWidth(), level.getHeight());
    }

    public void updateCell(int cell, Level level) {
        int x = level.cellX(cell);
        int y = level.cellY(cell);

        data[cell] = currentBaseCell(cell);
        mDecoMap[cell] = currentDecoCell(cell);

        updateRegion().union(x, y);
        mDecoLayer.updateRegion().union(x, y);
    }

    @Override
    public void brightness(float value) {
        super.brightness(value);
        mDecoLayer.brightness(value);
    }

    @Override
    public int getDecoTileForTerrain(int terrain) {
        return xTilemapConfiguration.getDecoTileForTerrain(terrain);
    }
}
