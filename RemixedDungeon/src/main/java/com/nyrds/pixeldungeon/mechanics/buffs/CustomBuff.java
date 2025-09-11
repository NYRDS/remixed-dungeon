package com.nyrds.pixeldungeon.mechanics.buffs;

import androidx.annotation.Keep;

import com.nyrds.Packable;
import com.nyrds.lua.LuaEngine;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.mechanics.spells.Spell;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.ModError;
import com.nyrds.util.Util;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.LuaTable;

import java.util.HashSet;
import java.util.Set;

import lombok.val;


public class CustomBuff extends Buff {

    private String name;
    private String desc;

    private int icon;

    @Packable
    public String scriptFile;

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

            if(Util.isDebug()) {
                GLog.debug("Loading buff %s", scriptFile);
            }

            script = new LuaScript("scripts/buffs/" + scriptFile, this);
            script.asInstance();

            LuaTable Desc = script.run("buffDesc").checktable();

            icon = Desc.rawget("icon").checkint();
            name = StringsManager.maybeId(Desc.rawget("name").checkjstring());
            desc = StringsManager.maybeId(Desc.rawget("info").checkjstring());
        } catch (Exception e){
            throw new ModError("Buff",e);
        }
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(LuaEngine.LUA_DATA, script.run("saveData").checkjstring());
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        initFromFile(scriptFile);

        String luaData = bundle.optString(LuaEngine.LUA_DATA,null);
        if(luaData!=null) {
            script.run("loadData",luaData);
        }
    }

    @Override
    public int icon() {
        return script.runOptional("icon",icon);
    }

    @Override
    public String getEntityKind() {
        return scriptFile;
    }

    @Override
    public boolean attachTo(@NotNull Char target) {
        try {
            if (target.immunities().contains(getEntityKind())) {
                return false;
            }

            if (script.run("attachTo", target).checkboolean()) {
                this.target = target;
                return target.add(this);
            }
            return false;
        } catch (Exception e) {
            throw new ModError("Error in "+scriptFile+" attachTo", e);
        }
    }

    @Override
    public void detach() {
        super.detach();
        script.runOptional("detach");
    }

    @Override
    public boolean act() {
        script.runOptional("act");
        return true;
    }

    @Override
    public int drBonus(Char chr) {
        return script.runOptional("drBonus",0, chr);
    }

    @Override
    public int stealthBonus(Char chr) {
        return script.runOptional("stealthBonus",0, chr);
    }

    @Override
    public float speedMultiplier(Char chr) {
        return script.runOptional("speedMultiplier",1.f, chr);
    }

    @Override
    public int regenerationBonus(Char chr) {
        return script.runOptional("regenerationBonus",0, chr);
    }

    @Override
    public void charAct(Char chr) {
        script.runOptional("charAct", chr);
    }

    @Override
    public int defenceProc(Char defender, Char enemy, int damage) {
        return script.runOptional("defenceProc", damage, enemy, damage);
    }

    @Override
    public int attackProc(Char attacker, Char defender, int damage) {
        return script.runOptional("attackProc", damage, defender, damage);
    }

    @Override
    public int defenceSkillBonus(Char chr) {
        return script.runOptional("defenceSkillBonus",super.defenceSkillBonus(chr), chr);
    }

    @Override
    public int attackSkillBonus(Char chr) {
        return script.runOptional("attackSkillBonus",super.attackSkillBonus(chr), chr);
    }

    @Override
    public float hasteLevel(Char chr) {
        return script.runOptional("hasteLevel",super.hasteLevel(chr), chr);
    }

    @Override
    public void spellCasted(Char caster, Spell spell) {
        script.runOptionalNoRet("spellCasted", caster, spell);
    }

    @Override
    public String name() {
        return StringsManager.maybeId(script.runOptional("name",name));
    }

    @Override
    public String desc() { return StringsManager.maybeId(script.runOptional("info", desc)); }
    @Override
    public boolean dontPack() {
        return script.runOptional("dontPack", false);
    }

    @Override
    public Set<String> immunities(Char chr) {
        val table = script.runOptional("immunities", LuaEngine.emptyTable, chr);

        var ret = new HashSet<String>();

        LuaEngine.forEach(table, (key,val)->ret.add(val.checkjstring()));
        return ret;
    }

    @Override
    public Set<String> resistances(Char chr) {
        val table = script.runOptional("resistances", LuaEngine.emptyTable, chr);

        var ret = new HashSet<String>();

        LuaEngine.forEach(table, (key,val)->ret.add(val.checkjstring()));
        return ret;
    }

    @Override
    public String textureLarge() {
        return script.runOptional("textureLarge",super.textureLarge());
    }

    @Override
    public String textureSmall() {
        return script.runOptional("textureSmall",super.textureSmall());
    }


    @Override
    public CharSprite.State charSpriteStatus() {
        return CharSprite.State.valueOf(script.runOptional("charSpriteStatus",super.charSpriteStatus().name()));
    }

    @Override
    public int charGotDamage(int damage, NamedEntityKind src, Char target) {
        return  script.runOptional("damage",super.charGotDamage(damage, src, target), damage, src);
    }
}
