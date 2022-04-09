package com.nyrds.pixeldungeon.items;

import androidx.annotation.Keep;

import com.nyrds.Packable;
import com.nyrds.lua.LuaEngine;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.items.EquipableItem;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.CellSelector;
import com.watabou.pixeldungeon.sprites.Glowing;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;

import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.util.ArrayList;

/**
 * Created by mike on 26.05.2018.
 * This file is part of Remixed Pixel Dungeon.
 */
public class CustomItem extends EquipableItem {

    @Packable
    private String scriptFile = Utils.EMPTY_STRING;

    private boolean upgradable;
    private boolean identified;

    private String equipable;

    private LuaScript script;
    private int price;
    private float heapScale;

    @Keep
    public CustomItem() {
    }

    public CustomItem(String scriptFile) {
        this.scriptFile = scriptFile;
        initItem();
    }

    private void initItem() {
        script = new LuaScript("scripts/items/"+scriptFile, this);
        script.asInstance();

        LuaTable desc = script.run("itemDesc").checktable();

        image        = desc.rawget("image").checkint();
        imageFile    = desc.rawget("imageFile").checkjstring();
        name         = StringsManager.maybeId(desc.rawget("name").checkjstring());
        info         = StringsManager.maybeId(desc.rawget("info").checkjstring());
        stackable    = desc.rawget("stackable").checkboolean();
        upgradable   = desc.rawget("upgradable").checkboolean();
        identified   = desc.rawget("identified").checkboolean();
        gender       = Utils.genderFromString(getClassParam("Gender", "neuter", false));

        setDefaultAction(desc.rawget("defaultAction").checkjstring());

        price        = desc.rawget("price").checkint();
        equipable    = desc.rawget("equipable").checkjstring();
        heapScale    = (float) desc.rawget("heapScale").optdouble(1.);
    }

    @Override
    public Glowing glowing () {
        return script.runOptional("glowing", Glowing.NO_GLOWING);
    }

    @Override
    public boolean isUpgradable() {
        return upgradable;
    }

    @Override
    public boolean isIdentified() {
        return identified;
    }

    @Override
    public Belongings.Slot slot(Belongings belongings) {
        return Belongings.Slot.valueOf(script.runOptional("slot",_slot().name(), belongings));
    }

    public Belongings.Slot blockSlot() {
        return Belongings.Slot.valueOf(script.runOptional("blockSlot",super.blockSlot().name()));
    }

    private Belongings.Slot _slot() {
        for(Belongings.Slot slot: Belongings.Slot.values()) {
            if (equipable.equalsIgnoreCase(slot.toString())) {
                return slot;
            }
        }

        return Belongings.Slot.NONE;
    }

    @Override
    public void activate(@NotNull Char ch) {
        super.activate(ch);
        script.run("activate", ch);
    }

    @Override
    public void deactivate(@NotNull Char ch) {
        script.run("deactivate", ch);
        super.deactivate(ch);
    }

    @Override
    public void _execute(@NotNull Char chr, @NotNull String action) {
        super._execute(chr,action);
        script.run("execute", chr, action);
    }

    @Override
    public ArrayList<String> actions(Char hero) {
        ArrayList<String> actions = super.actions(hero);

        if(equipable.isEmpty()) {
            actions.remove(AC_EQUIP);
            actions.remove(AC_UNEQUIP);
        }

        LuaValue ret = script.run("actions", hero);

        LuaEngine.forEach(ret, (key,val)->actions.add(val.tojstring()));

        return actions;
    }

    public void selectCell(String action,String prompt) {
        CellSelector.Listener cellSelectorListener = new CustomItemCellListener(action, prompt);

        getOwner().selectCell(cellSelectorListener);
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(LuaEngine.LUA_DATA, script.run("saveData").checkjstring());
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        initItem();

        String luaData = bundle.optString(LuaEngine.LUA_DATA,null);
        if(luaData!=null) {
            script.run("loadData",luaData);
        }
    }

    private Item applyOnItem(int cell,String effect) {
        LuaValue ret = script.run(effect, cell);
        if(ret.isnil()) {
            return null;
        }

        Item item = (Item) ret.checkuserdata(Item.class);
        item.quantity(quantity());
        return item;
    }


    @Override
    public Item burn(int cell) {
        return applyOnItem(cell,"burn");
    }

    @Override
    public Item freeze(int cell) {
        return applyOnItem(cell,"freeze");
    }

    @Override
    public Item poison(int cell) {
        return applyOnItem(cell,"poison");
    }

    @Override
    protected void onThrow(int cell, @NotNull Char thrower) {
        script.run("onThrow", cell, thrower);
    }

    @Override
    public String getEntityKind() {
        return scriptFile;
    }

    @Override
    public int price() {
        return adjustPrice(price * quantity());
    }

    @Override
    public boolean isLevelKnown() {
        return identified;
    }

    @Override
    public String desc() {
        return StringsManager.maybeId(script.runOptional("info",info));
    }

    @Override
    public String name() {
        return StringsManager.maybeId(script.runOptional("name",name));
    }

    @Override
    public String bag() {
        return script.runOptional("bag",super.bag());
    }

    @Override
    public int typicalSTR() {
        return script.runOptional("typicalSTR", super.typicalSTR());
    }

    @Override
    public int requiredSTR() {
        return script.runOptional("requiredSTR", super.requiredSTR());
    }

    @Override
    public int effectiveDr() {
        return script.runOptional("effectiveDr", super.effectiveDr());
    }

    @Override
    public float accuracyFactor(Char user) {
        return script.runOptional("accuracyFactor", super.accuracyFactor(user), user);
    }

    public float attackDelayFactor(Char user) {
        return script.runOptional("attackDelayFactor", super.attackDelayFactor(user), user);
    }

    public int damageRoll(Char user) {
        return script.runOptional("damageRoll", super.damageRoll(user), user);
    }

    public void attackProc(Char attacker, Char defender, int damage ) {
        script.runOptionalNoRet("attackProc",
                    attacker,
                    defender,
                    damage
                );
    }

    public int defenceProc(Char attacker, Char defender, int damage ) {
        return script.runOptional("defenceProc",
                super.defenceProc(attacker,defender,damage),
                attacker,
                defender,
                damage
        );
    }

    public String getAttackAnimationClass() {
        return script.runOptional("getAttackAnimationClass", NO_ANIMATION);
    }

    @Override
    public String getVisualName() {
        return script.runOptional("getVisualName", super.getVisualName());
    }

    public boolean goodForMelee() {
        return script.runOptional("goodForMelee", false);
    }

    @Override
    public int image() {
        return script.runOptional("image",super.image());
    }

    @Override
    public boolean isFliesStraight() {
        return script.runOptional("isFliesStraight",super.isFliesStraight());
    }

    @Override
    public boolean isFliesFastRotating() {
        return script.runOptional("isFliesFastRotating",super.isFliesFastRotating());
    }

    @Override
    public boolean hasCollar() {
        return script.runOptional("hasCollar",super.hasCollar());
    }

    @Override
    public boolean hasHelmet() {
        return script.runOptional("hasHelmet",super.hasHelmet());
    }

    @Override
    public boolean isCoveringHair() {
        return script.runOptional("isCoveringHair",super.isCoveringHair());
    }

    @Override
    public boolean isCoveringFacialHair() {
        return script.runOptional("isCoveringFacialHair",super.isCoveringHair());
    }

    @Override
    public float heapScale() {
        return heapScale;
    }

    @Override
    protected boolean act() {
        script.runOptional("act");
        return true;
    }

    @Override
    public void charAct() {
        script.runOptional("charAct");
    }

    @Override
    public int range() {
        return script.runOptional("range",super.range());
    }

    @Override
    public void preAttack(Char tgt) {
        script.runOptionalNoRet("preAttack",tgt);
    }

    @Override
    public void postAttack( Char tgt) {
        script.runOptionalNoRet("postAttack",tgt);
    }

    @Override
    public void ownerDoesDamage(int damage) {
        script.runOptionalNoRet("ownerDoesDamage",damage);
    }

    @Override
    public void ownerTakesDamage(int damage) {
        script.runOptionalNoRet("ownerTakesDamage",damage);
    }

    @Override
    public boolean doPickUp(@NotNull Char hero) {
        script.runOptionalNoRet("onPickUp", hero);
        return super.doPickUp(hero);
    }

    @Override
    public float time2equipBase() {
        return script.runOptional("time2equip",super.time2equipBase());
    }

    private class CustomItemCellListener implements CellSelector.Listener {

        private final String action;
        private final String prompt;

        public CustomItemCellListener(String action, String prompt) {
            this.action = action;
            this.prompt = prompt;
        }

        @Override
        public void onSelect(Integer cell, @NotNull Char selector) {
            if(cell!=null) {
                script.run("cellSelected", action, cell);
            }
        }

        @Override
        public String prompt() {
            return StringsManager.maybeId(prompt);
        }

        @Override
        public Image icon() {
            return null;
        }
    }
}
