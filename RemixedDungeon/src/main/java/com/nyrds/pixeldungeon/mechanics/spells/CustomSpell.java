package com.nyrds.pixeldungeon.mechanics.spells;

import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;

import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.LuaTable;

/**
 * Created by mike on 02.06.2018.
 * This file is part of Remixed Pixel Dungeon.
 */
public class CustomSpell extends Spell {

    private final String scriptFile;

    private final LuaScript script;

    public CustomSpell(String scriptFile) {
        this.scriptFile = scriptFile;

        script = new LuaScript("scripts/spells/" + scriptFile, this);

        LuaTable desc = script.run("spellDesc").checktable();

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
    protected boolean cast(@NotNull Char chr, int cell) {
        boolean ret = script.run("castOnCell", chr, cell).checkboolean();
        if(ret) {
            castCallback(chr);
        }
        return ret;
    }

    @Override
    protected boolean cast(@NotNull Char chr, @NotNull Char target) {
        boolean ret = script.run("castOnChar", chr, target).checkboolean();
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

        boolean ret = script.run("cast", chr).checkboolean();
        if(ret) {
            castCallback(chr);
        }
        return ret;
    }

    @Override
    public String getEntityKind() {
        return scriptFile;
    }

    @Override
    protected int getImage(Char caster) {
        return script.runOptional("image", super.getImage(caster), caster);
    }
}
