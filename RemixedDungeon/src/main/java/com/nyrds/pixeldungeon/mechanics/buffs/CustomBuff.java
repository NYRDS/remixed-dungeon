package com.nyrds.pixeldungeon.mechanics.buffs;

import com.nyrds.Packable;
import com.nyrds.android.util.ModError;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.watabou.noosa.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.utils.Bundle;

import org.luaj.vm2.LuaTable;

import androidx.annotation.Keep;

public class CustomBuff extends Buff {

    static public final String REGENERATION = "Regeneration";

    private String name;
    private String info;

    private int icon;

    @Packable
    private String scriptFile;

    private LuaScript script;


    @Keep
    public CustomBuff() {
    }

    public CustomBuff(String scriptFile) {
        initFromFile(scriptFile);
    }

    private void initFromFile(String scriptFile) {
        try {
            this.scriptFile = scriptFile;

            script = new LuaScript("scripts/buffs/" + scriptFile, this);

            script.run("buffDesc");
            LuaTable desc = script.getResult().checktable();

            icon = desc.rawget("icon").checkint();
            name = StringsManager.maybeId(desc.rawget("name").checkjstring());
            info = StringsManager.maybeId(desc.rawget("info").checkjstring());
        } catch (Exception e){
            throw new ModError("Buff",e);
        }
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        initFromFile(scriptFile);
    }

    @Override
    public int icon() {
        return icon;
    }

    @Override
    public String getEntityKind() {
        return scriptFile;
    }

    @Override
    public boolean attachTo(Char target) {
        if(super.attachTo(target)) {
            script.run("attachTo",target);
            return script.getResult().checkboolean();
        }
        return false;
    }

    @Override
    public void detach() {
        super.detach();
        script.run("detach");
    }

    @Override
    public boolean act() {
        script.run("act");
        return true;
    }

    @Override
    public int drBonus() {
        script.run("drBonus");
        return script.getResult().checkint();
    }

    @Override
    public String name() {
        return name;
    }
}
