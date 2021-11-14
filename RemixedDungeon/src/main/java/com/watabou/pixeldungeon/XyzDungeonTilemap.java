package com.watabou.pixeldungeon;

import com.nyrds.util.SeededRandom;
import com.watabou.noosa.CompositeImage;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.Tilemap;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.levels.TerrainFlags;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by mike on 15.02.2018.
 * This file is part of Remixed Pixel Dungeon.
 */

public class XyzDungeonTilemap extends DungeonTilemap {

    private final Tilemap mWallsLayer;
    private final Tilemap mDecoLayer;
    private final Tilemap mRoofLayer;
    private final Tilemap mCornersLayer;
    private final Tilemap mDoorsLayer;

    private final Level level;

    private final int[] mWallsMap;
    private final int[] mDecoMap;
    private final int[] mRoofMap;
    private final int[] mCornersMap;
    private final int[] mDoorsMap;

    private final DungeonTilemap roofTilemap;
    SeededRandom random = new SeededRandom();

    public XyzDungeonTilemap(Level level, String tiles) {
        super(level, tiles);
        this.level = level;

        final int width = level.getWidth();
        int mSize = width * level.getHeight();

        data = new int[mSize];
        map(buildGroundMap(), width);

        mWallsLayer = new Tilemap(tiles, new TextureFilm(tiles, SIZE, SIZE));
        mDecoLayer = new Tilemap(tiles, new TextureFilm(tiles, SIZE, SIZE));
        mRoofLayer = new Tilemap(tiles, new TextureFilm(tiles, SIZE, SIZE));

        mCornersLayer = new Tilemap(tiles, new TextureFilm(tiles, SIZE, SIZE));
        mDoorsLayer = new Tilemap(tiles, new TextureFilm(tiles, SIZE, SIZE));

        mWallsMap = new int[mSize];
        mDecoMap = new int[mSize];
        mRoofMap = new int[mSize];
        mCornersMap = new int[mSize];
        mDoorsMap = new int[mSize];

        mWallsLayer.map(buildWallsMap(), width);
        mDecoLayer.map(buildDecoMap(), width);
        mRoofLayer.map(buildRoofMap(), width);
        mCornersLayer.map(buildCornersMap(), width);
        mDoorsLayer.map(buildDoorsMap(), width);

        roofTilemap = new XyzRoofTileMap(level, tiles);
    }

    private int[] buildDecoMap() {
        for (int i = 0; i < mDecoMap.length; i++) {
            mDecoMap[i] = currentDecoCell(i);
        }

        return mDecoMap;
    }

    private int[] buildDoorsMap() {
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

        Image deco = new Image(getTexture());
        deco.frame(getTileset().get(mDecoMap[pos]));

        img.addLayer(deco);

        Image roof = new Image(getTexture());
        roof.frame(getTileset().get(mRoofMap[pos]));

        img.addLayer(roof);

        Image corner = new Image(getTexture());
        corner.frame(getTileset().get(mCornersMap[pos]));

        img.addLayer(corner);

        Image doors = new Image(getTexture());
        doors.frame(getTileset().get(mDoorsMap[pos]));

        img.addLayer(doors);


        return img;
    }


    private final Integer[] floorTiles = {48, 49, 50};
    private final Integer[] floorSpTiles = {112, 113, 114};

    private int currentBaseCell(int cell) {

        if(level.map[cell] >= Terrain.WATER_TILES) {
            int tile = 208;
            for (int j = 0; j < Level.NEIGHBOURS4.length; j++) {
                final int mapTile = level.map[cell + Level.NEIGHBOURS4[j]];
                if ((TerrainFlags.flags[mapTile] & (TerrainFlags.LIQUID | TerrainFlags.PIT | TerrainFlags.SOLID))!= 0 && !(mapTile == Terrain.DOOR || mapTile == Terrain.OPEN_DOOR)) {
                    tile += 1 << j;
                }
            }

            return tile;
        }

        if(TerrainFlags.is(level.map[cell], TerrainFlags.PIT)) {
            return 173;
        }

        switch (level.map[cell]) {
            case Terrain.ALCHEMY:
                return 138;
            case Terrain.WELL:
                return 137;

            case Terrain.EMPTY_SP:
            case Terrain.STATUE_SP:
                return random.oneOf(cell,floorSpTiles);
            default:
                return random.oneOf(cell,floorTiles);
        }
    }

    private final Integer[] wallSTiles = {32, 33, 34};
    private final Integer[] wallVerticalTiles = {0, 16};

    private final Integer[] wallVerticalCrossTiles = {3, 19};
    private final Integer[] wallVerticalLeftTiles = {2, 18};
    private final Integer[] wallVerticalRightTiles = {1, 17};

    private final Integer[] wallVerticalCrossSolidTiles = {128};
    private final Integer[] wallVerticalLeftSolidTiles = {5, 21};
    private final Integer[] wallVerticalRightSolidTiles = {4, 20};

    private final Integer[] w7_23 = {7, 23};
    private final Integer[] w6_22 = {6, 22};

    interface isTileKind {
        boolean is(int cell);
    }
    
    private int currentWallsCell(int cell) {

        if (isWallCell(cell)) {
            return wallTileKind(cell, this::isAnyWallCell);
        }


        if(isSpWallCell(cell)) {
            int ret = wallTileKind(cell, this::isAnyWallCell);
            if(ret != 128) {
                return ret + 64;
            }
            return ret;
        }


        return 173;
    }

    private int wallTileKind(int cell, isTileKind is) {
        final int width = level.getWidth();
        final boolean c_plus_w = is.is(cell + width);

        if (!c_plus_w) {
            return random.oneOf(cell,wallSTiles);
        }

        final boolean c_plus_w_minus_1 = is.is(cell + width - 1);
        final boolean c_plus_w_plus_1 = is.is(cell + width + 1);

        final boolean c_minus_1 = is.is(cell - 1);
        final boolean c_plus_1 = is.is(cell + 1);

        if (c_plus_w_minus_1 && c_plus_w_plus_1
                && !c_minus_1 && !c_plus_1
        ) {

            return random.oneOf(cell,wallVerticalCrossTiles);
        }

        if (c_plus_w_minus_1 && c_plus_w_plus_1 && !c_minus_1
        ) {
            return random.oneOf(cell,w6_22);
        }

        if (c_plus_w_minus_1 && c_plus_w_plus_1 && !c_plus_1
        ) {
            return random.oneOf(cell,w7_23);
        }

        if (!c_plus_w_minus_1 && !c_plus_w_plus_1) {
            return random.oneOf(cell,wallVerticalTiles);
        }

        if (!c_plus_w_minus_1) {
            if (c_plus_1) {
                return random.oneOf(cell,wallVerticalRightSolidTiles);
            }
            return random.oneOf(cell,wallVerticalRightTiles);
        }

        if (!c_plus_w_plus_1) {
            if (c_minus_1) {
                return random.oneOf(cell,wallVerticalLeftSolidTiles);
            }
            return random.oneOf(cell,wallVerticalLeftTiles);
        }

        return random.oneOf(cell,wallVerticalCrossSolidTiles);
    }

    boolean isAnyWallCell(int cell) {
        return isSpWallCell(cell) || isWallCell(cell);
    }

    boolean isSpWallCell(int cell) {
        if (!level.cellValid(cell)) {
            return true;
        }

        switch (level.map[cell]) {
            case Terrain.BOOKSHELF:
            case Terrain.SECRET_DOOR:
                return true;
        }

        return false;
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

        if(level.cellValid(cellS)) {
            if (!level.isCellMapped(cellS)) {
                return 128;
            }
        }

        if (isWallCell(cellS)) {
            if (!isAnyWallCell(cell)) {
                return roofCellKind(cellS);
            }
        }

        if (isSpWallCell(cellS)) {
            if (!isAnyWallCell(cell)) {
                return roofCellKind(cellS) + 64;
            }
        }

        return 173;
    }

    private int roofCellKind(int cellS) {
        if (isAnyWallCell(cellS + 1) && isAnyWallCell(cellS - 1)) {
            return random.oneOf(cellS,roofNTilesCross);
        }

        if (isAnyWallCell(cellS - 1)) {
            return random.oneOf(cellS,roofNTilesLeft);
        }

        if (isAnyWallCell(cellS + 1)) {
            return random.oneOf(cellS,roofNTilesRight);
        }

        return random.oneOf(cellS,roofNTiles);
    }


    private int currentCornersCell(int cell) {
        final int width = level.getWidth();


        if(level.cellValid(cell + width)) {
            if (!level.mapped[cell + width]) {
                return 128;
            }
        }

        final boolean c_plus_w = isAnyWallCell(cell + width);
        final boolean c_plus_1 = isAnyWallCell(cell + 1);
        final boolean c_plus_1_plus_w = isAnyWallCell(cell + 1 + width);
        final boolean c_minus_1 = isAnyWallCell(cell - 1);
        final boolean c_minus_1_plus_w = isAnyWallCell(cell - 1 + width);

        int x = cornerCellKind(c_plus_w, c_plus_1, c_plus_1_plus_w, c_minus_1, c_minus_1_plus_w);
        if(x==173) {
            return 173;
        }

        if (isWallCell(cell)) {
            return x;
        }

        if (isSpWallCell(cell)) {
            return x + 64;
        }

        return 173;
    }


    private int cornerCellKind(boolean c_plus_w, boolean c_plus_1, boolean c_plus_1_plus_w, boolean c_minus_1, boolean c_minus_1_plus_w) {
        if (!c_plus_w) {
            if (!c_minus_1 && !c_plus_1) {
                return 30;
            }
            if (!c_minus_1) {
                return 28;
            }
            if (!c_plus_1) {
                return 29;
            }
        }

        if (c_plus_1 && c_plus_w && !c_plus_1_plus_w && c_minus_1 && !c_minus_1_plus_w) {
            return 14;
        }

        if (c_plus_1 && c_plus_w && !c_plus_1_plus_w) {
            return 13;
        }

        if (c_minus_1 && c_plus_w && !c_minus_1_plus_w) {
            return 12;
        }
        return 173;
    }

    private boolean isDoorCell(int cell) {
        if(!level.cellValid(cell)) {
            return false;
        }

        switch (level.map[cell]) {
            case Terrain.DOOR:
            case Terrain.OPEN_DOOR:
            case Terrain.LOCKED_DOOR:
                return true;
        }
        return false;
    }

    private int currentDoorsCell(int cell) {
        if(isDoorCell(cell)) {
            if (isWallCell(cell + 1) && isWallCell(cell - 1)) {
                switch (level.map[cell]) {
                    case Terrain.DOOR:
                        return 56;
                    case Terrain.OPEN_DOOR:
                        return 58;
                    case Terrain.LOCKED_DOOR:
                        return 57;
                }
            }

            if (isWallCell(cell + level.getWidth()) && isWallCell(cell - level.getWidth())) {
                switch (level.map[cell]) {
                    case Terrain.DOOR:
                        return 59;
                    case Terrain.OPEN_DOOR:
                        return 61;
                    case Terrain.LOCKED_DOOR:
                        return 60;
                }
            }
        }

        int cellS = cell + level.getWidth();

        if(isDoorCell(cellS)) {
            if (isWallCell(cellS +  1) && isWallCell(cellS - 1)) {
                switch (level.map[cellS]) {
                    case Terrain.DOOR:
                        return 40;
                    case Terrain.OPEN_DOOR:
                        return 42;
                    case Terrain.LOCKED_DOOR:
                        return 41;
                }
            }

            if (isWallCell(cellS + level.getWidth())  && isWallCell(cellS - level.getWidth())) {
                switch (level.map[cellS]) {
                    case Terrain.DOOR:
                        return 43;
                    case Terrain.OPEN_DOOR:
                        return 45;
                    case Terrain.LOCKED_DOOR:
                        return 44;
                }
            }
        }

        return 173;
    }

    private int currentDecoCell(int cell) {
        switch (level.map[cell]) {
            case Terrain.ENTRANCE:
                return 133;
            case Terrain.EXIT:
                return 134;
            case Terrain.LOCKED_EXIT:
                return 136;
            case Terrain.UNLOCKED_EXIT:
                return 135;
            case Terrain.CHASM:
                return 128;
            case Terrain.CHASM_WALL:
                return 129;
            case Terrain.CHASM_FLOOR:
                return 130;
            case Terrain.CHASM_FLOOR_SP:
                return 131;
            case Terrain.CHASM_WATER:
                return 132;
            case Terrain.EMBERS:
                return random.oneOf(cell,16*12, 16*12 + 1,16*12 +2);
            case Terrain.GRASS:
                return random.oneOf(cell,195, 196, 197);
            case Terrain.HIGH_GRASS:
                return random.oneOf(cell,198, 199, 200);
            case Terrain.EMPTY_DECO:
                return random.oneOf(cell,144, 145, 146);
            case Terrain.WALL_DECO:
                return random.oneOf(cell,160, 161, 162);
        }
        return 173;
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
        mDecoLayer.draw();
        mDoorsLayer.draw();
    }

    public void updateAll() {
        buildGroundMap();
        buildWallsMap();
        buildDoorsMap();
        buildCornersMap();
        buildRoofMap();

        final int width = level.getWidth();
        final int height = level.getHeight();

        updateRegion().set(0, 0, width, height);
        mWallsLayer.updateRegion().set(0, 0, width, height);
        mDecoLayer.updateRegion().set(0, 0, width, height);
        mRoofLayer.updateRegion().set(0, 0, width, height);
        mCornersLayer.updateRegion().set(0, 0, width, height);
        mDoorsLayer.updateRegion().set(0, 0, width, height);
    }

    public void updateCell(int cell, Level level) {
        int x = level.cellX(cell);
        int y = level.cellY(cell);

        data[cell] = currentBaseCell(cell);
        mWallsMap[cell] = currentWallsCell(cell);
        mDecoMap[cell] = currentDecoCell(cell);
        mRoofMap[cell] = currentRoofCell(cell);
        mCornersMap[cell] = currentCornersCell(cell);
        mDoorsMap[cell] = currentDoorsCell(cell);

        updateRegion().union(x, y);
        mWallsLayer.updateRegion().union(x, y);
        mDecoLayer.updateRegion().union(x, y);
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

    public DungeonTilemap roofTilemap() {
        return roofTilemap;
    }

    class XyzRoofTileMap extends DungeonTilemap {

        public XyzRoofTileMap(@NotNull Level level, String tiles) {
            super(level, tiles);
        }

        @Override
        public @Nullable Image tile(int pos) {
            return null;
        }

        @Override
        public void updateCell(int cell, Level level) { }

        @Override
        public void updateAll() { }

        @Override
        public void draw() {
            mRoofLayer.draw();
            mCornersLayer.draw();

        }
    }
}
