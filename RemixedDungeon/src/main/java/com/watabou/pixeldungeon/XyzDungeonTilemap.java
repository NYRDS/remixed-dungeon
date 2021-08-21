package com.watabou.pixeldungeon;

import com.watabou.noosa.CompositeImage;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.Tilemap;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.levels.TerrainFlags;
import com.watabou.utils.Random;

/**
 * Created by mike on 15.02.2018.
 * This file is part of Remixed Pixel Dungeon.
 */

public class XyzDungeonTilemap extends DungeonTilemap {

    private final Tilemap mWallsLayer;
    private final Tilemap mRoofLayer;
    private final Tilemap mCornersLayer;
    private final Tilemap mDoorsLayer;

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

        data = new int[mSize];
        map(buildGroundMap(), width);

        mWallsLayer = new Tilemap(tiles, new TextureFilm(tiles, SIZE, SIZE));
        mRoofLayer = new Tilemap(tiles, new TextureFilm(tiles, SIZE, SIZE));
        ;
        mCornersLayer = new Tilemap(tiles, new TextureFilm(tiles, SIZE, SIZE));
        mDoorsLayer = new Tilemap(tiles, new TextureFilm(tiles, SIZE, SIZE));

        mWallsMap = new int[mSize];
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

        Image wall = new Image(getTexture());
        wall.frame(getTileset().get(mWallsMap[pos]));

        img.addLayer(wall);

        Image roof = new Image(getTexture());
        roof.frame(getTileset().get(mRoofMap[pos]));

        img.addLayer(roof);

        Image corner = new Image(getTexture());
        corner.frame(getTileset().get(mCornersMap[pos]));

        img.addLayer(corner);

        return img;
    }


    private final Integer[] floorTiles = {48, 49, 50};
    private final Integer[] floorSpTiles = {112, 113, 114};

    private int currentBaseCell(int cell) {

        if(level.map[cell] >= Terrain.WATER_TILES) {
            int tile = 208;
            for (int j = 0; j < Level.NEIGHBOURS4.length; j++) {
                if ((TerrainFlags.flags[level.map[cell + Level.NEIGHBOURS4[j]]] & TerrainFlags.LIQUID) != 0) {
                    tile += 1 << j;
                }
            }

            return tile;
        }

        if(TerrainFlags.is(level.map[cell], TerrainFlags.PIT)) {
            return 173;
        }

        switch (level.map[cell]) {
            case Terrain.EMPTY_SP:
            case Terrain.STATUE_SP:
                return Random.oneOf(floorSpTiles);

            default:
                return Random.oneOf(floorTiles);
        }
    }

    private final Integer[] wallSTiles = {32, 33, 34};
    private final Integer[] wallNTiles = {179};
    private final Integer[] wallVerticalTiles = {0, 16};

    private final Integer[] wallVerticalCrossTiles = {3, 19};
    private final Integer[] wallVerticalLeftTiles = {2, 18};
    private final Integer[] wallVerticalRightTiles = {1, 17};

    private final Integer[] wallVerticalCrossSolidTiles = {128};
    private final Integer[] wallVerticalLeftSolidTiles = {5, 21};
    private final Integer[] wallVerticalRightSolidTiles = {4, 20};

    private final Integer[] w7_23 = {7, 23};
    private final Integer[] w6_22 = {6, 22};

    private int currentWallsCell(int cell) {
        if (isWallCell(cell)) {
            final int width = level.getWidth();
            final boolean c_plus_w = isWallCell(cell + width);

            if (!c_plus_w) {
                return Random.oneOf(wallSTiles);
            }

            final boolean c_plus_w_minus_1 = isWallCell(cell + width - 1);
            final boolean c_plus_w_plus_1 = isWallCell(cell + width + 1);

            final boolean c_minus_1 = isWallCell(cell - 1);
            final boolean c_plus_1 = isWallCell(cell + 1);

            if (c_plus_w_minus_1 && c_plus_w_plus_1
                    && !c_minus_1 && !c_plus_1
            ) {

                return Random.oneOf(wallVerticalCrossTiles);
            }

            if (c_plus_w_minus_1 && c_plus_w_plus_1 && !c_minus_1
            ) {
                return Random.oneOf(w6_22);
            }

            if (c_plus_w_minus_1 && c_plus_w_plus_1 && !c_plus_1
            ) {
                return Random.oneOf(w7_23);
            }

            if (!c_plus_w_minus_1 && !c_plus_w_plus_1) {
                return Random.oneOf(wallVerticalTiles);
            }

            if (!c_plus_w_minus_1) {
                if (c_plus_1) {
                    return Random.oneOf(wallVerticalRightSolidTiles);
                }
                return Random.oneOf(wallVerticalRightTiles);
            }

            if (!c_plus_w_plus_1) {
                if (c_minus_1) {
                    return Random.oneOf(wallVerticalLeftSolidTiles);
                }
                return Random.oneOf(wallVerticalLeftTiles);
            }

            return Random.oneOf(wallVerticalCrossSolidTiles);
        }
        return 173;
    }

    boolean isWallCell(int cell) {
        if (!level.cellValid(cell)) {
            return true;
        }

        switch (level.map[cell]) {
            case Terrain.WALL:
            case Terrain.WALL_DECO:
            case Terrain.SECRET_DOOR:
                return true;
        }
        return false;
    }


    private final Integer[] roofNTiles = {11, 27};
    private final Integer[] roofNTilesRight = {9, 25};
    private final Integer[] roofNTilesLeft = {10, 26};
    private final Integer[] roofNTilesCross = {8, 24};


    private int currentRoofCell(int cell) {
        int cellS = cell + level.getWidth();

        if (isWallCell(cellS)) {
            if (cellNEmpty(cellS)) {
                if (isWallCell(cellS + 1) && isWallCell(cellS - 1)) {
                    return Random.oneOf(roofNTilesCross);
                }

                if (isWallCell(cellS - 1)) {
                    return Random.oneOf(roofNTilesLeft);
                }

                if (isWallCell(cellS + 1)) {
                    return Random.oneOf(roofNTilesRight);
                }

                return Random.oneOf(roofNTiles);
            }
        }
        return 173;
    }


    private int currentCornersCell(int cell) {
        if (isWallCell(cell)) {
            boolean csle = cellSEmpty(cell - 1);
            boolean csre = cellSEmpty(cell + 1);

            if (cellSEmpty(cell)) {
                if (csle && csre) {
                    return 30;
                }
                if (csle) {
                    return 28;
                }
                if (csre) {
                    return 29;
                }
            }

            final boolean c_plus_w = isWallCell(cell + level.getWidth());
            final boolean c_plus_1 = isWallCell(cell + 1);
            final boolean c_plus_1_plus_w = isWallCell(cell + 1 + level.getWidth());
            final boolean c_minus_1 = isWallCell(cell - 1);
            final boolean c_minus_1_plus_w = isWallCell(cell - 1 + level.getWidth());

            if (c_plus_1 && c_plus_w && !c_plus_1_plus_w && c_minus_1 && !c_minus_1_plus_w) {
                return 14;
            }

            if (c_plus_1 && c_plus_w && !c_plus_1_plus_w) {
                return 13;
            }

            if (c_minus_1 && c_plus_w && !c_minus_1_plus_w) {
                return 12;
            }
        }
        return 173;
    }

    private boolean cellSEmpty(int cell) {
        int cellN = cell + level.getWidth();
        if (!level.cellValid(cellN)) {
            return false;
        }

        if (TerrainFlags.is(level.map[cellN], TerrainFlags.PASSABLE)) {
            return true;
        }
        return false;
    }

    private boolean cellNEmpty(int cell) {
        int cellN = cell - level.getWidth();
        if (!level.cellValid(cellN)) {
            return false;
        }

        if (TerrainFlags.is(level.map[cellN], TerrainFlags.PASSABLE)) {
            return true;
        }
        return false;
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
        mDoorsLayer.updateRegion().set(0, 0, width, height);
        ;

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


    @Override
    public void brightness(float value) {
        super.brightness(value);

        mWallsLayer.brightness(value);
        mRoofLayer.brightness(value);
        mCornersLayer.brightness(value);
        mDoorsLayer.brightness(value);
    }
}
