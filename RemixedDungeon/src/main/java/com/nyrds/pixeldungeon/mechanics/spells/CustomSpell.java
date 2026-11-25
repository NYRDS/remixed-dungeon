package com.nyrds.pixeldungeon.mechanics.spells;

import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

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
        imageSize = desc.rawget("imageSize").optint(imageSize);
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
    public boolean cast(@NotNull Char chr, int cell) {
        boolean ret = script.run("castOnCell", chr, cell).optboolean(false);
        if(ret) {
            castCallback(chr);
        }
        return ret;
    }

    @Override
    protected boolean cast(@NotNull Char chr, @NotNull Char target) {
        boolean ret = script.run("castOnChar", chr, target).optboolean(false);
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

        boolean ret = script.run("cast", chr).optboolean(false);
        if(ret) {
            castCallback(chr);
        }
        return ret;
    }

    // script veto hook: return false to deny the cast; anything else lets the
    // java base checks (cooldown, SP, possession) decide
    @Override
    public boolean canCast(@NotNull final Char chr, boolean reallyCast) {
        if (script.hasMethod("canCast")) {
            LuaValue verdict = script.run("canCast", chr, reallyCast);
            if (verdict.isboolean() && !verdict.toboolean()) {
                return false;
            }
        }
        return super.canCast(chr, reallyCast);
    }

    // descText(self, spell) returning a string replaces the static Info text
    // (dynamic descriptions, e.g. SummonDeathling's level-scaled summon limit)
    @Override
    public String desc() {
        if (script.hasMethod("descText")) {
            LuaValue text = script.run("descText");
            if (text.isstring()) {
                return text.tojstring();
            }
        }
        return super.desc();
    }

    @Override
    public String getEntityKind() {
        return scriptFile;
    }

    @Override
    protected int getImage() {
        return script.runOptional("image", super.getImage(), Dungeon.hero);
    }
}
