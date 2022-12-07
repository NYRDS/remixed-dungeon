package com.nyrds.retrodungeon.levels.objects;

import com.nyrds.Packable;
import com.nyrds.android.util.Util;
import com.nyrds.retrodungeon.levels.objects.sprites.LevelObjectSprite;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class LevelObject implements Bundlable, Presser {

    @Packable
    private int pos = -1;

    @Packable
    protected int layer = 0;

    @Packable(defaultValue = "levelObjects/objects.png")
    protected String textureFile = "levelObjects/objects.png";

    @Packable(defaultValue = "0")
    protected int imageIndex = 0;

    public LevelObjectSprite sprite;

    public LevelObject(int pos) {
        this.pos = pos;
    }

    public int image() {
        return imageIndex;
    }

    void setupFromJson(Level level, JSONObject obj) throws JSONException {
        textureFile = obj.optString("textureFile", textureFile);
        imageIndex = obj.optInt("imageIndex", imageIndex);
    }

    public boolean interact(Char hero) {
        return true;
    }

    public boolean stepOn(Char hero) {
        return true;
    }

    public boolean nonPassable() {
        return false;
    }

    protected void remove() {
        Dungeon.level.remove(this);
        sprite.kill();
    }

    public void burn() {
    }

    public void freeze() {
    }

    public void poison() {
    }

    public void bump(Presser presser) {
    }

    public void discover() {
    }

    public boolean secret() {
        return false;
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
    }

    @Override
    public void storeInBundle(Bundle bundle) {
    }

    public boolean dontPack() {
        return false;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        if (sprite != null) {
            sprite.move(this.pos, pos);
            Dungeon.level.levelObjectMoved(this);
        }

        this.pos = pos;
    }

    public abstract String desc();

    public abstract String name();

    public String texture() {
        return textureFile;
    }

    public boolean pushable(Char hero) {
        return false;
    }

    public boolean push(Char hero) {
        Level level = Dungeon.level;

        int hx = level.cellX(hero.getPos());
        int hy = level.cellY(hero.getPos());

        int x = level.cellX(getPos());
        int y = level.cellY(getPos());

        int dx = x - hx;
        int dy = y - hy;

        if (dx * dy != 0) {
            return false;
        }

        int nextCell = level.cell(x + Util.signum(dx), y + Util.signum(dy));

        if (!level.cellValid(nextCell)) {
            return false;
        }

        if (level.solid[nextCell] || level.getLevelObject(nextCell, layer) != null) {
            return false;
        } else {
            level.press(nextCell, this);

            setPos(nextCell);
            level.levelObjectMoved(this);
        }

        return true;
    }

    public void fall() {
        if (sprite != null) {
            sprite.fall();
        }
        Dungeon.level.remove(this);
    }

    @Override
    public boolean affectLevelObjects() {
        return false;
    }

    public int getSpriteXS() {
        return 16;
    }

    public int getSpriteYS() {
        return 16;
    }

    public int getLayer() {
        return layer;
    }
}
