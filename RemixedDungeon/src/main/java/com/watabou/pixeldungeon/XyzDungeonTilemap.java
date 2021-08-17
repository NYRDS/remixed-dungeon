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

        Image wall = new Image(getTexture());
        wall.frame(getTileset().get(mWallsMap[pos]));

        img.addLayer(wall);

        return img;
    }


    private final Integer[] floorTiles = {48,49,50};
    private final Integer[] floorSpTiles = {112,113,114};

    private int currentBaseCell(int cell) {

        switch (level.map[cell]) {
            case Terrain.EMPTY:
            case Terrain.EMBERS:
            case Terrain.STATUE:
            case Terrain.EMPTY_DECO:
            case Terrain.WELL:
            case Terrain.ENTRANCE:
            case Terrain.GRASS:
            case Terrain.PEDESTAL:
            case Terrain.BARRICADE:
            case Terrain.EMPTY_WELL:
            case Terrain.HIGH_GRASS:
            case Terrain.BOOKSHELF:
                return Random.oneOf(floorTiles);
            case Terrain.EMPTY_SP:
            case Terrain.STATUE_SP:
                return Random.oneOf(floorSpTiles);
            default:
                return 173;
        }
    }

    private final Integer[] wallSTiles = {32,33,34};
    private final Integer[] wallNTiles = {179};
    private final Integer[] wallVerticalTiles = {0,16};

    private final Integer[] wallVerticalCrossTiles = {3,19};
    private final Integer[] wallVerticalLeftTiles = {2,18};
    private final Integer[] wallVerticalRightTiles = {1,17};

    private final Integer[] wallVerticalCrossSolidTiles = {128};
    private final Integer[] wallVerticalLeftSolidTiles = {5,21};
    private final Integer[] wallVerticalRightSolidTiles = {4,20};

    enum VerticalWallKind {None, Cross, Left, Right, CrossSolid, LeftSolid, RightSolid};

    private int currentWallsCell(int cell) {
        if(isWallCell(cell)) {
                if(cellSEmpty(cell)) {
                    return Random.oneOf(wallSTiles);
                }

                switch (cellVerticalKind(cell)) {
                    case Cross:
                        return Random.oneOf(wallVerticalCrossTiles);
                    case Left:
                        return Random.oneOf(wallVerticalLeftTiles);
                    case Right:
                        return Random.oneOf(wallVerticalRightTiles);
                    case CrossSolid:
                        return Random.oneOf(wallVerticalCrossSolidTiles);
                    case LeftSolid:
                        return Random.oneOf(wallVerticalLeftSolidTiles);
                    case RightSolid:
                        return Random.oneOf(wallVerticalRightSolidTiles);

                    case None:
                        return Random.oneOf(wallVerticalTiles);
                }


        }
        return 173;
    }

    boolean isWallCell(int cell) {
        if(!level.cellValid(cell)) {
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

    private VerticalWallKind cellVerticalKind(int cell) {
        int cellL = cell - 1;
        int cellR = cell + 1;

        boolean cellLS = false;
        boolean cellRS = false;

        if(isWallCell(cellL) ) {
            cellLS = true;
        }

        if(isWallCell(cellR) ) {
            cellRS = true;
        }

        if(cellLS && cellRS) {
            if(isWallCell(cellL + level.getWidth()) || isWallCell(cellR + level.getWidth())) {
                return VerticalWallKind.CrossSolid;
            }
            return VerticalWallKind.Cross;
        }

        if(cellLS) {
            if(isWallCell(cellL + level.getWidth())) {
                return VerticalWallKind.LeftSolid;
            }
            return VerticalWallKind.Left;
        }

        if(cellRS) {
            if(isWallCell(cellR + level.getWidth())) {
                return VerticalWallKind.RightSolid;
            }

            return VerticalWallKind.Right;
        }

        return VerticalWallKind.None;
    }


    private Integer[] roofNTiles      = {11,27};
    private Integer[] roofNTilesRight  = {9,25};
    private Integer[] roofNTilesLeft = {10,26};
    private Integer[] roofNTilesCross = {8,24};


    private int currentRoofCell(int cell) {
        int cellS = cell + level.getWidth();

        if(isWallCell(cellS)) {
            if(cellNEmpty(cellS)) {
                if(isWallCell(cellS + 1) && isWallCell(cellS - 1)) {
                    return Random.oneOf(roofNTilesCross);
                }

                if(isWallCell(cellS - 1)) {
                    return Random.oneOf(roofNTilesLeft);
                }

                if(isWallCell(cellS + 1)) {
                    return Random.oneOf(roofNTilesRight);
                }

                return Random.oneOf(roofNTiles);
            }
        }
        return 173;
    }


    private boolean cellSEmpty(int cell) {
        int cellN = cell + level.getWidth();
        if(!level.cellValid(cellN)) {
            return false;
        }

        if(TerrainFlags.is(level.map[cellN], TerrainFlags.PASSABLE)) {
            return true;
        }
        return false;
    }

    private boolean cellNEmpty(int cell) {
        int cellN = cell - level.getWidth();
        if(!level.cellValid(cellN)) {
            return false;
        }

        if(TerrainFlags.is(level.map[cellN], TerrainFlags.PASSABLE)) {
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

    @Override
    public void brightness(float value) {
        super.brightness(value);

        mWallsLayer.brightness(value);
        mRoofLayer.brightness(value);
        mCornersLayer.brightness(value);
        mDoorsLayer.brightness(value);
    }
}
