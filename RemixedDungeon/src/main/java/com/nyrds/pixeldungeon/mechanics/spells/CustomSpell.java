package com.nyrds.pixeldungeon.mechanics.spells;

import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.watabou.noosa.StringsManager;
import com.watabou.pixeldungeon.actors.Char;

import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.LuaTable;

/**
 * Created by mike on 02.06.2018.
 * This file is part of Remixed Pixel Dungeon.
 */
public class CustomSpell extends Spell {

    private String scriptFile;

    private LuaScript script;

    public CustomSpell(String scriptFile) {
        this.scriptFile = scriptFile;

        script = new LuaScript("scripts/spells/" + scriptFile, this);

        script.run("spellDesc", null, null);
        LuaTable desc = script.getResult().checktable();

        image = desc.rawget("image").checkint();
        imageFile = desc.rawget("imageFile").checkjstring();
        name = StringsManager.maybeId(desc.rawget("name").checkjstring());
        info = StringsManager.maybeId(desc.rawget("info").checkjstring());
        magicAffinity = StringsManager.maybeId(desc.rawget("magicAffinity").checkjstring());
        targetingType = desc.rawget("targetingType").checkjstring();
        cooldown      = (float) desc.rawget("cooldown").checkdouble();

        level = (int) desc.rawget("level").checkdouble();
        spellCost = (int) desc.rawget("spellCost").checkdouble();
        castTime  = (float) desc.rawget("castTime").checkdouble();
    }

    @Override
    protected boolean cast(Char chr, int cell) {
        script.run("castOnCell", chr, cell);
        boolean ret = script.getResult().checkboolean();
        if(ret) {
            castCallback(chr);
        }
        return ret;
    }

    @Override
    public boolean cast(@NotNull Char chr) {
        if(!super.cast(chr)) {
            return false;
        }

        script.run("cast", chr);
        boolean ret = script.getResult().checkboolean();
        if(ret) {
            castCallback(chr);
        }
        return ret;
    }

    @Override
    public String getClassName() {
        return scriptFile;
    }
}
