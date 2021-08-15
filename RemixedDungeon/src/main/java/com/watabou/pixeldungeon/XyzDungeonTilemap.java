package com.watabou.pixeldungeon;

import com.nyrds.pixeldungeon.levels.XyzTilemapConfiguration;
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

public class XyzDungeonTilemap extends DungeonTilemap {

    private final Tilemap mWallsLayer;
    private final Tilemap mRoofLayer;
    private final Tilemap mCornersLayer;
    private final Tilemap mDoorsLayer;

    private final XyzTilemapConfiguration xyzTilemapConfiguration;

    private final Level level;

    private final int[] mWallsMap;
    private final int[] mRoofMap;
    private final int[] mCornersMap;
    private final int[] mDoorsMap;

    public XyzDungeonTilemap(Level level, String tiles) {
        super(level, tiles);
        this.level = level;

        int mSize = level.getWidth() * level.getHeight();

        try {
            String tilemapConfig = "tilemapDesc/" + tiles.replace(".png", ".json");
            if (!ModdingMode.isResourceExist(tilemapConfig)) {
                tilemapConfig = "tilemapDesc/tiles_xyz_default.json";
            }
            xyzTilemapConfiguration = XyzTilemapConfiguration.readConfig(tilemapConfig);
        } catch (JSONException e) {
            throw ModdingMode.modException(e);
        }

        data = new int[level.getWidth()*level.getHeight()];
        map(buildGroundMap(),level.getWidth());

        mWallsLayer = new Tilemap(tiles, new TextureFilm(tiles, SIZE, SIZE));
        mRoofLayer = new Tilemap(tiles, new TextureFilm(tiles, SIZE, SIZE));;
        mCornersLayer = new Tilemap(tiles, new TextureFilm(tiles, SIZE, SIZE));
        mDoorsLayer = new Tilemap(tiles, new TextureFilm(tiles, SIZE, SIZE));

        mWallsMap= new int[mSize];
        mRoofMap = new int[mSize];
        mCornersMap = new int[mSize];
        mDoorsMap = new int[mSize];

        mWallsLayer.map(buildWallsMap(), level.getWidth());
        mRoofLayer.map(buildRoofMap(), level.getWidth());
        mCornersLayer.map(buildCornersMap(), level.getWidth());
        mDoorsLayer.map(buildDoordMap(), level.getWidth());
    }

    private int[] buildDoordMap() {
        return mDoorsMap;
    }

    private int[] buildCornersMap() {
        return mCornersMap;
    }

    private int[] buildRoofMap() {
        return mRoofMap;
    }

    private int[] buildWallsMap() {
        return mWallsMap;
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
        return xyzTilemapConfiguration.decoTile(level, cell);
    }

    private int[] buildDecoMap() {
        for (int i = 0; i < mDecoMap.length; i++) {
            mDecoMap[i] = currentDecoCell(i);
        }

        return mDecoMap;
    }

    private int currentBaseCell(int cell) {
        return xyzTilemapConfiguration.baseTile(level, cell);
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
        mWallsLayer.draw();
        mRoofLayer.draw();
        mCornersLayer.draw();
        mDoorsLayer.draw();
    }

    public void updateAll() {
        buildGroundMap();
        buildDecoMap();
        final int width = level.getWidth();
        final int height = level.getHeight();

        updateRegion().set(0, 0, width, height);
        mWallsLayer.updateRegion().set(0, 0, width, height);
        mRoofLayer.updateRegion().set(0, 0, width, height);
        mCornersLayer.updateRegion().set(0, 0, width, height);
        mDoorsLayer.updateRegion().set(0, 0, width, height);;

    }

    public void updateCell(int cell, Level level) {
        int x = level.cellX(cell);
        int y = level.cellY(cell);

        data[cell] = currentBaseCell(cell);
        mDecoMap[cell] = currentDecoCell(cell);

        updateRegion().union(x, y);

        mWallsLayer.updateRegion().union(x, y);
        mRoofLayer.updateRegion().union(x, y);
        mCornersLayer.updateRegion().union(x, y);
        mDoorsLayer.updateRegion().union(x, y);
    }

    @Override
    public void brightness(float value) {
        super.brightness(value);

        mWallsLayer.brightness(value);
        mRoofLayer.brightness(value);
        mCornersLayer.brightness(value);
        mDoorsLayer.brightness(value);
    }
}
