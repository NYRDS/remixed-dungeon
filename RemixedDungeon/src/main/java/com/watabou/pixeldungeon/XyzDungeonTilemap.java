package com.watabou.pixeldungeon;

import com.watabou.noosa.CompositeImage;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.Tilemap;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;

/**
 * Created by mike on 15.02.2018.
 * This file is part of Remixed Pixel Dungeon.
 */

public class XyzDungeonTilemap extends DungeonTilemap {

    private final Tilemap mWallsLayer;
    private final Tilemap mRoofLayer;
    private final Tilemap mCornersLayer;
    private final Tilemap mDoorsLayer;

    //private final XyzTilemapConfiguration xyzTilemapConfiguration;

    private final Level level;

    private final int[] mWallsMap;
    private final int[] mRoofMap;
    private final int[] mCornersMap;
    private final int[] mDoorsMap;

    public XyzDungeonTilemap(Level level, String tiles) {
        super(level, tiles);
        this.level = level;

        final int width = level.getWidth();
        int mSize = width * level.getHeight();
/*
        try {
            String tilemapConfig = "tilemapDesc/" + tiles.replace(".png", ".json");
            if (!ModdingMode.isResourceExist(tilemapConfig)) {
                tilemapConfig = "tilemapDesc/tiles_xyz_default.json";
            }
            xyzTilemapConfiguration = XyzTilemapConfiguration.readConfig(tilemapConfig);
        } catch (JSONException e) {
            throw ModdingMode.modException(e);
        }
*/
        data = new int[mSize];
        map(buildGroundMap(), width);

        mWallsLayer = new Tilemap(tiles, new TextureFilm(tiles, SIZE, SIZE));
        mRoofLayer = new Tilemap(tiles, new TextureFilm(tiles, SIZE, SIZE));;
        mCornersLayer = new Tilemap(tiles, new TextureFilm(tiles, SIZE, SIZE));
        mDoorsLayer = new Tilemap(tiles, new TextureFilm(tiles, SIZE, SIZE));

        mWallsMap= new int[mSize];
        mRoofMap = new int[mSize];
        mCornersMap = new int[mSize];
        mDoorsMap = new int[mSize];

        mWallsLayer.map(buildWallsMap(), width);
        mRoofLayer.map(buildRoofMap(), width);
        mCornersLayer.map(buildCornersMap(), width);
        mDoorsLayer.map(buildDoordMap(), width);
    }

    private int[] buildDoordMap() {
        for (int i = 0; i < mDoorsMap.length; i++) {
            mDoorsMap[i] = currentDoorsCell(i);
        }

        return mDoorsMap;
    }

    private int[] buildCornersMap() {
        for (int i = 0; i < mCornersMap.length; i++) {
            mCornersMap[i] = currentCornersCell(i);
        }

        return mCornersMap;
    }

    private int[] buildRoofMap() {
        for (int i = 0; i < mRoofMap.length; i++) {
            mRoofMap[i] = currentRoofCell(i);
        }

        return mRoofMap;
    }

    private int[] buildWallsMap() {
        for (int i = 0; i < mWallsMap.length; i++) {
            mWallsMap[i] = currentWallsCell(i);
        }

        return mWallsMap;
    }

    @Override
    public Image tile(int pos) {
        CompositeImage img = new CompositeImage(getTexture());
        img.frame(getTileset().get(data[pos]));

        return img;
    }


    private int currentBaseCell(int cell) {

        switch (level.map[cell]) {
            case Terrain.EMPTY:
                return 49;
            case Terrain.EMBERS:
                return 50;
            default:
                return 173;
        }

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
        //mRoofLayer.draw();
        //mCornersLayer.draw();
        //mDoorsLayer.draw();
    }

    public void updateAll() {
        buildGroundMap();
        buildWallsMap();
        buildRoofMap();
        buildCornersMap();
        buildRoofMap();

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
        mWallsMap[cell] = currentWallsCell(cell);
        mRoofMap[cell] = currentRoofCell(cell);
        mCornersMap[cell] = currentCornersCell(cell);
        mDoorsMap[cell] = currentDoorsCell(cell);

        updateRegion().union(x, y);
        mWallsLayer.updateRegion().union(x, y);
        mRoofLayer.updateRegion().union(x, y);
        mCornersLayer.updateRegion().union(x, y);
        mDoorsLayer.updateRegion().union(x, y);
    }

    private int currentDoorsCell(int cell) {
        return 173;
    }

    private int currentCornersCell(int cell) {
        return 173;
    }

    private int currentRoofCell(int cell) {
        return 173;
    }

    private int currentWallsCell(int cell) {
        return 173;
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
