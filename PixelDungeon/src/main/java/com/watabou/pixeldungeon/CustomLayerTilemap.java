package com.watabou.pixeldungeon;

import com.watabou.noosa.CompositeImage;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.levels.Level;

import java.util.ArrayList;

/**
 * Created by mike on 15.02.2018.
 * This file is part of Remixed Pixel Dungeon.
 */

public class CustomLayerTilemap extends DungeonTilemap {

    private ArrayList<CustomLayerTilemap> mLayers = new ArrayList<>();

    public CustomLayerTilemap(Level level, String tiles, int[] map) {
        super(level, tiles);
        map(map, level.getWidth());
    }

    public void addLayer(String tiles, int[] map) {
        mLayers.add(new CustomLayerTilemap(level,tiles,map));
    }

    @Override
    public Image tile(int pos) {
        ArrayList<Image> imgs = new ArrayList<>();

        if(data[pos] >= 0) {
            Image img = new Image(getTexture());
            img.frame(getTileset().get(data[pos]));
            imgs.add(img);
        }

        for (CustomLayerTilemap layer : mLayers) {
            if(layer.data[pos] >= 0) {
                Image img = new Image(getTexture());
                img.frame(getTileset().get(layer.data[pos]));
                imgs.add(img);
            }
        }
        if(!imgs.isEmpty()) {
            return new CompositeImage(imgs);
        }
        return null;
    }

    @Override
    public void draw() {
        super.draw();

        for (CustomLayerTilemap layer: mLayers) {
            layer.draw();
        }
    }

    public void updateAll() {
        updated.set(0, 0, level.getWidth(), level.getHeight());

        for (CustomLayerTilemap layer: mLayers) {
            layer.updated.set(0, 0, level.getWidth(), level.getHeight());
        }
    }

    public void updateCell(int cell, Level level) {
        int x = level.cellX(cell);
        int y = level.cellY(cell);

        updated.union(x, y);
        for (CustomLayerTilemap layer: mLayers) {
            layer.updated.union(x,y);
        }
    }
}
