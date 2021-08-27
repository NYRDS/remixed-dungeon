package com.nyrds.pixeldungeon.levels.objects;

import androidx.annotation.Keep;

import com.nyrds.Packable;
import com.nyrds.lua.LuaEngine;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

public class CustomObject extends Deco {
    @Packable
    private String scriptFile;

    private LuaScript script;

    @Keep
    CustomObject() {
    }

    public CustomObject(String scriptFile) {
        this.scriptFile = scriptFile;
        initObject();
    }

    private void initObject() {
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
}
