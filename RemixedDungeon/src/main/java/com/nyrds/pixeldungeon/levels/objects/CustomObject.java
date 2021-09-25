package com.nyrds.pixeldungeon.levels.objects;

import androidx.annotation.Keep;

import com.nyrds.lua.LuaEngine;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import lombok.SneakyThrows;

public class CustomObject extends Deco {

    private LuaScript script;

    @Keep
    CustomObject() {
        super(Level.INVALID_CELL);
    }

    @Keep
    public CustomObject(int cell) {
        super(cell);
    }

    @SneakyThrows
    private void initObject() {
        String scriptFile = defMap.get(objectDesc).getString("script");
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
    public boolean interact(Char hero) {
        return script.runOptional("interact", false, hero);
    }

    @Override
    public int image() {
        return script.runOptional("image", super.image());
    }

    @Override
    public void burn() {
        script.runOptional("burn");
    }

    @Override
    public boolean losBlocker() {
        return script.runOptional("losBlocker", super.losBlocker());
    }
}
