package com.nyrds.pixeldungeon.items;

import androidx.annotation.Keep;
import com.nyrds.Packable;
import com.nyrds.lua.LuaEngine;
import com.nyrds.pixeldungeon.items.artifacts.IActingItem;
import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.ModError;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.items.EquipableItem;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.CellSelector;
import com.watabou.pixeldungeon.sprites.Glowing;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

/**
 * Created by mike on 26.05.2018.
 * This file is part of Remixed Pixel Dungeon.
 */
public class CustomItem extends EquipableItem implements IActingItem {

    @Packable
    public String scriptFile = Utils.EMPTY_STRING;

    private boolean upgradable;

    private boolean initialIdentified;

    @Packable
    public boolean identified = false;

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
        applyInitialFlags();
    }

    // IActingItem: equipped artifacts (CandleOfMindVision) drain via the
    // script's spend hook each tick
    @Override
    public void spend(@NotNull Char hero, float time) {
        script.runOptionalNoRet("spend", hero, time);
    }

    // ctor-time desc flag (cursed): applied once - restoreFromBundle's
    // initItem must not re-curse an uncursed item
    private void applyInitialFlags() {
        if (script == null || !script.hasMethod("desc")) {
            return;
        }
        LuaValue desc = script.run("desc").checktable();
        if (desc.rawget("cursed").optboolean(false)) {
            setCursed(true);
            setCursedKnown(true);
        }
    }

    private void initItem() {
        script = new LuaScript("scripts/items/" +scriptFile, this);
        script.asInstance();

        LuaTable desc = script.run("itemDesc").checktable();

        image        = desc.rawget("image").checkint();
        imageFile    = desc.rawget("imageFile").checkjstring();
        name         = StringsManager.maybeId(desc.rawget("name").checkjstring());
        info         = StringsManager.maybeId(desc.rawget("info").checkjstring());
        stackable    = desc.rawget("stackable").checkboolean();
        upgradable   = desc.rawget("upgradable").checkboolean();
        initialIdentified = desc.rawget("identified").checkboolean();
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
        if (script != null && script.hasMethod("isIdentified")) {
            return script.runOptional("isIdentified", Boolean.FALSE);
        }
        if (initialIdentified) {
            return true;
        }
        return identified;
    }

    // kinds with a custom identify pipeline (ChaosCrystal's 3-level counter)
    // bypass the default identified flag + Library side effects
    @Override
    public Item identify() {
        if (script != null && script.hasMethod("identify")) {
            script.runOptionalNoRet("identify");
            return this;
        }
        identified = true;
        return super.identify();
    }

    @Override
    public Belongings.Slot slot(Belongings belongings) {
        String slotName = "";
        try {
            slotName = script.runOptional("slot",_slot().name(), belongings);
            return Belongings.Slot.valueOf(slotName);
        } catch (Exception e) {
            ModError.doReport(Utils.format("%s: not a valid slot value", slotName), e);
        }
        return Belongings.Slot.NONE;
    }

    public Belongings.Slot blockSlot() {
        String slotName = "";
        try {
            slotName = script.runOptional("blockSlot",super.blockSlot().name());
            return Belongings.Slot.valueOf(slotName);
        } catch (Exception e) {
            ModError.doReport(Utils.format("%s: not a valid blockSlot value", slotName), e);
        }
        return Belongings.Slot.NONE;
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
            actions.remove(CommonActions.AC_EQUIP);
            actions.remove(CommonActions.AC_UNEQUIP);
        }

        // optional per-kind removal list (e.g. Amulet drops throw/drop)
        if (script.hasMethod("removeActions")) {
            LuaValue remove = script.run("removeActions", hero);
            LuaEngine.forEach(remove, (key, val) -> actions.remove(val.tojstring()));
        }

        LuaValue ret = script.run("actions", hero);

        LuaEngine.forEach(ret, (key,val)->actions.add(val.tojstring()));

        return actions;
    }

    // --- hooks for the framework interlocks of migrated misc kinds ---

    // boolean query into this item's script state (vial isFull, ...); callable
    // from java death-flow helpers and from other items' scripts alike
    public boolean itemBooleanHook(String hook, boolean defVal) {
        return script != null && script.runOptional(hook, defVal);
    }

    // string query into this item's script state (SpellBook raw spell kind);
    // a null defVal is NOT usable here - runOptional discards the return when
    // the default is null, pass "" and treat it as absent
    public String itemStringHook(String hook, String defVal) {
        return script != null ? script.runOptional(hook, defVal) : defVal;
    }

    // invoke a stateful hook on this item's script (vial drink/fill/collectDew);
    // fixed arities - luajava reflection cannot call varargs methods
    public void itemHook(String hook) {
        if (script != null) {
            script.runOptionalNoRet(hook);
        }
    }

    public void itemHook(String hook, Object arg) {
        if (script != null) {
            script.runOptionalNoRet(hook, arg);
        }
    }

    // replaces Item.doPickUp entirely when the kind defines it (Dewdrop:
    // collected by the vial or drunk, not bagged); onPickUp stays the
    // post-pickup notification it always was
    @Override
    public boolean doPickUp(@NotNull Char hero) {
        if (script.hasMethod("doPickUp")) {
            return script.run("doPickUp", hero).toboolean();
        }
        script.runOptionalNoRet("onPickUp", hero);
        return super.doPickUp(hero);
    }

    // returning true from the hook means fully handled (DarkGold melt on
    // depth 0); false/nil falls through to the java default
    @Override
    public void doDrop(@NotNull Char chr) {
        if (script.hasMethod("doDrop") && script.run("doDrop", chr).toboolean()) {
            return;
        }
        super.doDrop(chr);
    }

    @Override
    public String status() {
        return script != null ? script.runOptional("status", super.status()) : super.status();
    }

    // ItemSlot artifact status line ("x/100" charge counters); kinds define
    // getText/getColor hooks - a nil getText keeps the slot textless
    @Override
    public String getText() {
        if (script != null && script.hasMethod("getText")) {
            return script.run("getText").optjstring(super.getText());
        }
        return super.getText();
    }

    @Override
    public int getColor() {
        if (script != null && script.hasMethod("getColor")) {
            return script.run("getColor").optint(super.getColor());
        }
        return super.getColor();
    }

    // Dewdrop: grass/plant dew never announces (was Dewdrop.announcePickUp)
    @Override
    public boolean announcePickUp() {
        return script != null ? script.runOptional("announcePickUp", super.announcePickUp()) : super.announcePickUp();
    }

    // DewVial window title carried "name (x/10)" - keep the suffix for kinds
    // that define a status hook
    @NotNull
    @Override
    public String toString() {
        String str = super.toString();
        if (script != null && script.hasMethod("status")) {
            str = str + " (" + status() + ")";
        }
        return str;
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
        } else {
            // kind migrated from a java class: no lua state yet - let the
            // kind read the legacy java fields (vial volume, beacon returnTo)
            // straight from the bundle
            script.runOptionalNoRet("loadLegacy", bundle);
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
    protected void onThrow(int cell, @NotNull Char thrower, Char enemy) {
        script.run("onThrow", cell, thrower, enemy);
    }

    @Override
    public String getEntityKind() {
        return scriptFile;
    }

    @Override
    public int price() {
        if (price == 0) {
            return 0; // desc price 0 = authored free/not sellable; adjustPrice would floor it to 1
        }
        return script.runOptional("price", adjustPrice(price * quantity()));
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
    public String imageFile() {
        return script.runOptional("imageFile",super.imageFile());
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
    protected void act() {
        float pre_act_time = actorTime();
        script.runOptional("act");
        if(pre_act_time == actorTime()) { //fix up for items that don't implement act (Epic ones)
            spend(10);
        }
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

    public void charDied(Char chr, NamedEntityKind cause) {
        script.runOptionalNoRet("charDied", chr, cause);
    }

    public void pickedUp(@NotNull Char hero) {
        script.runOptionalNoRet("pickedUp", hero);
    }

    @Override
    public float time2equipBase() {
        return script.runOptional("time2equip",super.time2equipBase());
    }

    @Override
    public boolean statsRequirementsSatisfied() {
        return script.runOptional("statsRequirementsSatisfied", super.statsRequirementsSatisfied());
    }

    @Override
    public String knownStatsText() {
        return script.runOptional("knownStatsText", super.knownStatsText());
    }

    @Override
    public String unknownStatsText() {
        return script.runOptional("unknownStatsText", super.unknownStatsText());
    }

    @Override
    public boolean getBoolean(String key) {
        return script.runOptional("getBoolean", super.getBoolean(key), key);
    }

    @Override
    public Image getCustomImage() {
        return script.runOptional("getCustomImage", super.getCustomImage());
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
            } else {
                // cancel path (ChaosCrystal spent its time even when canceled)
                script.runOptionalNoRet("cellCanceled", action);
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

        public boolean mayBeCanceled() {
            return true;
        }
    }
}
