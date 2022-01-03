package com.nyrds.pixeldungeon.levels.objects;

import androidx.annotation.Keep;

import com.nyrds.lua.LuaEngine;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import lombok.SneakyThrows;

public class CustomObject extends Deco {

    private LuaScript script;

    @Keep
    public CustomObject() {
        super(Level.INVALID_CELL);
    }

    @Keep
    public CustomObject(int cell) {
        super(cell);
    }

    @SneakyThrows
    private void initObject() {
        final JSONObject objectDef = defMap.get(objectDesc);
        String scriptFile = objectDef.getString("script");
        script = new LuaScript("scripts/objects/"+ scriptFile, this);
        script.asInstance();
    }

    protected void readObjectDesc() throws JSONException {
        super.readObjectDesc();

        initObject();
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(LuaEngine.LUA_DATA, script.run("saveData").checkjstring());
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        initObject();

        String luaData = bundle.optString(LuaEngine.LUA_DATA,null);
        if(luaData!=null) {
            script.run("loadData",luaData);
        }
    }

    @Override
    void setupFromJson(Level level, JSONObject obj) throws JSONException {
        super.setupFromJson(level, obj);
        String data = Utils.EMPTY_STRING;
        if(obj.has("data")) {
            data = obj.getString("data");
        }
        script.runOptionalNoRet("init", level, data);
    }

    @Override
    protected boolean act() {
        script.runOptional("act");
        return true;
    }

    @Override
    public boolean stepOn(Char hero) {
        return script.runOptional("stepOn", false, hero);
    }

    @Override
    public boolean nonPassable(Char ch) {
        return script.runOptional("nonPassable", false, ch);
    }

    @Override
    public boolean interact(Char hero) {
        return script.runOptional("interact", false, hero);
    }

    @Override
    public int image() {
        return script.runOptional("image", super.image(), level());
    }

    @Override
    public void burn() {
        script.runOptional("burn");
    }

    @Override
    public boolean losBlocker() {
        return script.runOptional("losBlocker", super.losBlocker());
    }

    @Override
    public boolean flammable() {
        return script.runOptional("flammable", super.flammable());
    }

    @Override
    public boolean avoid() {
        return script.runOptional("avoid", super.avoid());
    }


    @Override
    public String getEntityKind() {
        return objectDesc;
    }

    @Override
    public void bump(Presser presser) {
        script.runOptionalNoRet("bump", presser);
    }

    @Override
    public String name() {
        return script.runOptional("name", super.name(), level());
    }

    @Override
    public String desc() {
        return script.runOptional("info", super.desc(), level());
    }

    @Override
    public String getTextureFile() {
        return script.runOptional("textureFile", super.getTextureFile(), level());
    }

    @Override
    public boolean ignoreIsometricShift() {
        return script.runOptional("ignoreIsometricShift", super.ignoreIsometricShift());
    }

    @Override
    public void addedToScene() {
        //GLog.debug("%s - addedToScene", getEntityKind());
        script.runOptionalNoRet("addedToScene");
    }

    @Override
    public int getLayer() {
        return script.runOptional("getLayer", super.getLayer());
    }

    @Override
    public boolean affectItems() {
        return script.runOptional("affectItems", super.affectItems());
    }
}
