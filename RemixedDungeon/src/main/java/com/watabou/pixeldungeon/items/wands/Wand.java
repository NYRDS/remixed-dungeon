
package com.watabou.pixeldungeon.items.wands;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.items.common.UnknownItem;
import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.Scrambler;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.effects.MagicMissile;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.ItemStatusHandler;
import com.watabou.pixeldungeon.items.KindOfWeapon;
import com.watabou.pixeldungeon.items.bags.WandHolster;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.ui.QuickSlot;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public abstract class Wand extends KindOfWeapon implements UnknownItem {

    private static final String AC_ZAP = "Wand_ACZap";

    private static final float TIME_TO_ZAP = 1f;
    static final float TIME_TO_CHARGE = 40f;

    private int maxCharges = Scrambler.scramble(initialCharges());
    private int curCharges = Scrambler.scramble(maxCharges());

    @Packable
    private boolean curChargeKnown = false;

    protected boolean hitChars = true;
    protected boolean hitObjects = false;

    private final boolean directional = true;

    private static final Class<?>[] wands = {WandOfTeleportation.class,
            WandOfSlowness.class, WandOfFirebolt.class, WandOfPoison.class,
            WandOfRegrowth.class, WandOfBlink.class, WandOfLightning.class,
            WandOfAmok.class, WandOfTelekinesis.class, WandOfFlock.class,
            WandOfDisintegration.class, WandOfAvalanche.class};

    private static final Integer[] images = {ItemSpriteSheet.WAND_HOLLY,
            ItemSpriteSheet.WAND_YEW, ItemSpriteSheet.WAND_EBONY,
            ItemSpriteSheet.WAND_CHERRY, ItemSpriteSheet.WAND_TEAK,
            ItemSpriteSheet.WAND_ROWAN, ItemSpriteSheet.WAND_WILLOW,
            ItemSpriteSheet.WAND_MAHOGANY, ItemSpriteSheet.WAND_BAMBOO,
            ItemSpriteSheet.WAND_PURPLEHEART, ItemSpriteSheet.WAND_OAK,
            ItemSpriteSheet.WAND_BIRCH};

    private static ItemStatusHandler<Wand> handler;

    private String wood;

    @SuppressWarnings("unchecked")
    public static void initWoods() {
        handler = new ItemStatusHandler<>((Class<? extends Wand>[]) wands, images);
    }

    public static void save(Bundle bundle) {
        handler.save(bundle);
    }

    @SuppressWarnings("unchecked")
    public static void restore(Bundle bundle) {
        handler = new ItemStatusHandler<>((Class<? extends Wand>[]) wands, images, bundle);
    }

    public Wand() {
        setDefaultAction(AC_ZAP);
        animation_class = WAND_ATTACK;

        try {
            image = handler.index(this);
            wood = StringsManager.getVars(R.array.Wand_Wood_Types)[ItemStatusHandler.indexByImage(image, images)];

        } catch (Exception e) {
            // Wand of Magic Missile or Wand of Icebolt
        }
    }

    @Override
    public ArrayList<String> actions(Char hero) {
        ArrayList<String> actions = super.actions(hero);
        if (curCharges() > 0 || !curChargeKnown) {
            actions.add(AC_ZAP);
        }

        actions.remove(CommonActions.AC_EQUIP);
        actions.remove(CommonActions.AC_UNEQUIP);

        if (hero.getHeroClass() == HeroClass.MAGE
                || hero.getSubClass() == HeroSubClass.SHAMAN) {

            if (isEquipped(hero)) {
                actions.add(CommonActions.AC_UNEQUIP);
            } else {
                actions.add(CommonActions.AC_EQUIP);
            }
        }
        return actions;
    }

    @Override
    public void _execute(@NotNull Char chr, @NotNull String action) {
        if (action.equals(AC_ZAP)) {
            chr.getBelongings().setSelectedItem(this);
            chr.selectCell(new Zapper(this));
            return;
        }

        super._execute(chr, action);
    }

    public void zapCell(Char chr, int cell) {
        getDestinationCell(chr.getPos(), cell);
        onZap(cell);
    }

    protected void onZap(int cell) {
        onZap(cell, Actor.findChar(cell));
    }

    protected abstract void onZap(int cell, Char victim);

    public int effectiveLevel() {
        return level() + getOwner().buffLevel(BuffFactory.RING_OF_POWER);
    }

    public boolean isKnown() {
        return handler.isKnown(this);
    }

    public void setKnown() {
        if (!isKnown()) {
            handler.know(this);
        }

        Badges.validateAllWandsIdentified();
    }

    @Override
    public Item identify() {

        setKnown();
        curChargeKnown = true;
        super.identify();

        QuickSlot.refresh(getOwner());

        return this;
    }

    @NotNull
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder(super.toString());

        String status = status();
        if (!status.equals(Utils.EMPTY_STRING)) {
            sb.append(" (" + status + ")");
        }

        return sb.toString();
    }

    @Override
    public String name() {
        return isKnown() ? name : Utils.format(R.string.Wand_Name, wood);
    }

    @Override
    public String info() {
        StringBuilder info = new StringBuilder(isKnown() ? desc()
                : Utils.format(R.string.Wand_Wood, wood));
        Char owner = getOwner();
        if (owner.getHeroClass() == HeroClass.MAGE || owner.getSubClass() == HeroSubClass.SHAMAN) {
            damageRoll(owner);
            info.append("\n\n");
            if (isLevelKnown()) {
                info.append(Utils.format(R.string.Wand_Damage, MIN + (MAX - MIN) / 2));
            } else {
                info.append(StringsManager.getVar(R.string.Wand_Weapon));
            }
        }
        return info.toString();
    }

    @Override
    public boolean isIdentified() {
        return super.isIdentified() && isKnown() && curChargeKnown;
    }

    @Override
    public String status() {
        if (isLevelKnown()) {
            return (curChargeKnown ? curCharges() : "?") + "/" + maxCharges();
        } else {
            return Utils.EMPTY_STRING;
        }
    }

    @Override
    public Item upgrade() {

        super.upgrade();

        maxCharges(Math.max(Math.min(maxCharges() + 1, 9), maxCharges()));
        curCharges(Math.max(curCharges(), maxCharges()));

        return this;
    }

    @Override
    public Item degrade() {
        super.degrade();

        maxCharges(Math.max(maxCharges() - 1, 0));
        curCharges(Math.min(curCharges(), maxCharges()));

        return this;
    }

    protected int initialCharges() {
        return 2;
    }

    public void mobWandUse(Char user, final int tgt) {

        if (user.invalid()) {
            EventCollector.logException(Utils.format("%s attempt of use by invalid char", getEntityKind()));
            return;
        }

        setOwner(user);
        final int cell = getDestinationCell(user.getPos(), tgt);
        fx(cell, () -> onZap(cell));
    }

    protected void fx(int cell, Callback callback) {
        MagicMissile.blueLight(getOwner().getSprite().getParent(), getOwner().getPos(), cell, callback);
        Sample.INSTANCE.play(Assets.SND_ZAP);
    }

    protected void wandUsed() {
        curCharges(curCharges() - 1);
        QuickSlot.refresh(getOwner());

        getOwner().spend(TIME_TO_ZAP);
    }

    @Override
    public Item random() {
        if (Random.Float() < 0.5f) {
            upgrade();
            if (Random.Float() < 0.15f) {
                upgrade();
            }
        }

        return this;
    }

    public static boolean allKnown() {
        return handler.known().size() == wands.length;
    }

    @Override
    public int price() {
        return adjustPrice(50);
    }

    private static final String MAX_CHARGES = "maxCharges";
    private static final String CUR_CHARGES = "curCharges";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(MAX_CHARGES, maxCharges());
        bundle.put(CUR_CHARGES, curCharges());
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        maxCharges(bundle.getInt(MAX_CHARGES));
        curCharges(bundle.getInt(CUR_CHARGES));
    }

    protected void wandEffect(final int cell, Char selector) {
        setKnown();

        Char victim = Actor.findChar(cell);

        QuickSlot.target(this, victim);

        if (curCharges() > 0) {
            fx(cell, () -> {
                onZap(cell, victim);
                wandUsed();
            });

            Invisibility.dispel(selector);
        } else {

            selector.spend(TIME_TO_ZAP);
            GLog.w(StringsManager.getVar(R.string.Wand_Fizzles));
            setLevelKnown(true);

            if (Random.Int(5) == 0) {
                identify();
            }

            QuickSlot.refresh(getOwner());
        }

    }

    protected int getDestinationCell(int src, int target) {
        if (!Dungeon.level.cellValid(target)) {
            return src;
        }
        return Ballistica.cast(src, target, directional, hitChars, hitObjects);
    }

    public int curCharges() {
        return Scrambler.descramble(curCharges);
    }

    public void curCharges(int curCharges) {
        this.curCharges = Scrambler.scramble(curCharges);
    }

    public int maxCharges() {
        return Scrambler.descramble(maxCharges);
    }

    public void maxCharges(int maxCharges) {
        this.maxCharges = Scrambler.scramble(maxCharges);
    }

    public boolean affectTarget() {
        return true;
    }

    @Override
    public int damageRoll(Char user) {
        float tier = 1 + effectiveLevel() / 3.0f;
        MIN = (int) (tier + user.skillLevel());
        MAX = (int) ((tier * tier - tier + 10) / 2 + user.skillLevel() * tier + effectiveLevel());

        return super.damageRoll(user);
    }

    @Override
    public void fromJson(JSONObject itemDesc) throws JSONException {
        super.fromJson(itemDesc);

        maxCharges(Math.min(initialCharges() + level(), 9));
        curCharges(maxCharges());

        curCharges(itemDesc.optInt("charges", curCharges()));
        maxCharges(itemDesc.optInt("maxCharges", maxCharges()));
    }

    @Override
    public String getVisualName() {
        return "Wand";
    }

    @Override
    public Belongings.Slot slot(Belongings belongings) {
        return Belongings.Slot.WEAPON;
    }

    @Override
    public Belongings.Slot blockSlot() {
        return Belongings.Slot.LEFT_HAND;
    }

    @Override
    public String bag() {
        return WandHolster.class.getSimpleName();
    }

    @Override
    protected boolean act() {
        if (curCharges() < maxCharges()) {
            curCharges(curCharges() + 1);
            QuickSlot.refresh(getOwner());
        }

        float time2charge = getOwner().getHeroClass() == HeroClass.MAGE ? Wand.TIME_TO_CHARGE
                / (float) Math.sqrt(1 + effectiveLevel())
                : Wand.TIME_TO_CHARGE;
        time2charge -= (float) 0;
        spend(time2charge);

        return true;
    }

}
