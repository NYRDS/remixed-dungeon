package com.nyrds.pixeldungeon.mechanics.buffs;

import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.watabou.noosa.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;

import org.luaj.vm2.LuaTable;

public class CustomBuff extends Buff {
    private final String name;
    private final String info;
    private final int icon;

    private String scriptFile;

    private LuaScript script;

    public CustomBuff(String scriptFile) {
        this.scriptFile = scriptFile;

        script = new LuaScript("scripts/buffs/" + scriptFile, this);

        script.run("buffDesc", null, null);
        LuaTable desc = script.getResult().checktable();

        icon = desc.rawget("icon").checkint();
        name = StringsManager.maybeId(desc.rawget("name").checkjstring());
        info = StringsManager.maybeId(desc.rawget("info").checkjstring());
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
            script.run("attachTo");
            return true;
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
}
