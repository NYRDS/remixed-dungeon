
package com.watabou.pixeldungeon.items.wands;
import com.nyrds.LuaInterface;
import com.nyrds.Packable;
import com.nyrds.pixeldungeon.items.ItemUtils;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.items.common.UnknownItem;
import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.Scrambler;
import com.watabou.noosa.Group;
import com.watabou.noosa.particles.Emitter;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.effects.DeathRay;
import com.watabou.pixeldungeon.effects.MagicMissile;
import com.watabou.pixeldungeon.effects.particles.PurpleParticle;
import com.watabou.pixeldungeon.effects.particles.SnowParticle;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.ItemStatusHandler;
import com.watabou.pixeldungeon.items.ItemStatusHandlerKinds;
import com.watabou.pixeldungeon.items.KindOfWeapon;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.ui.QuickSlot;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndBag;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.luaj.vm2.LuaValue;

/**
 * Data wands: one java kind-parameterized framework, per-kind data and
 * behavior in scripts/items/<Kind>.lua built with scripts/lib/wand.lua
 * (was 15 per-wand java classes; ChaosStaff keeps its java subclass with an
 * absent kind script falling back to the empty stub). Kind == the legacy
 * class simple name, so name/info resources and old saves resolve unchanged;
 * the identification handler keeps the legacy
 * "class com.watabou...wands.<Kind>" bundle keys verbatim.
 *
 * Lua hooks (all optional, self = the script table, item = the wand):
 *   desc(self) -> { fx, hitChars, hitObjects, initialCharges, identified,
 *                   image, imageFile, price, disenchants }
 *   zap(self, item, cell, victim)    -> the zap effect (was onZap)
 *   destCell(self, item, src, tgt)   -> override cell after the base
 *                                       Ballistica cast (Blink clamp)
 *   affectTarget(self, item)         -> SHAMAN mastery gate (default true)
 *   emitter / emitterInterval        -> idle particle (name string, seconds)
 *
 * fx kinds map onto MagicMissile statics ("blue","purple","earth","white",
 * "fire","wool","poison","foliage","slowness","force","coldLight","ice",
 * "shadow"); the special flows ("deathray","lightning","blink") stay
 * java-side, and grid-sweep zaps are @LuaInterface bridges on ItemUtils.
 */
public class Wand extends KindOfWeapon implements UnknownItem {

    private static final String AC_ZAP = "Wand_ACZap";

    private static final float TIME_TO_ZAP = 1f;
    static final float TIME_TO_CHARGE = 40f;

    // legacy class package: identification bundle keys keep this exact form
    private static final String LEGACY_PACKAGE = "com.watabou.pixeldungeon.items.wands";

    private static final String[] wands = {"WandOfTeleportation",
            "WandOfSlowness", "WandOfFirebolt", "WandOfPoison",
            "WandOfRegrowth", "WandOfBlink", "WandOfLightning",
            "WandOfAmok", "WandOfTelekinesis", "WandOfFlock",
            "WandOfDisintegration", "WandOfAvalanche"};

    private static final Integer[] images = {ItemSpriteSheet.WAND_HOLLY,
            ItemSpriteSheet.WAND_YEW, ItemSpriteSheet.WAND_EBONY,
            ItemSpriteSheet.WAND_CHERRY, ItemSpriteSheet.WAND_TEAK,
            ItemSpriteSheet.WAND_ROWAN, ItemSpriteSheet.WAND_WILLOW,
            ItemSpriteSheet.WAND_MAHOGANY, ItemSpriteSheet.WAND_BAMBOO,
            ItemSpriteSheet.WAND_PURPLEHEART, ItemSpriteSheet.WAND_OAK,
            ItemSpriteSheet.WAND_BIRCH};

    private static final Set<String> handledKinds = new HashSet<>(Arrays.asList(wands));

    private static ItemStatusHandlerKinds handler;

    private String wood;

    public static void initWoods() {
        handler = new ItemStatusHandlerKinds(LEGACY_PACKAGE, wands, images);
    }

    public static void save(Bundle bundle) {
        handler.save(bundle);
    }

    public static void restore(Bundle bundle) {
        handler = new ItemStatusHandlerKinds(LEGACY_PACKAGE, wands, images, bundle);
    }

    private int maxCharges = Scrambler.scramble(2);
    private int curCharges = Scrambler.scramble(2);

    @Packable
    public boolean curChargeKnown = false;

    @NotNull
    private final String wandKind;

    // per-instance wrapper around the shared per-kind module (see Food);
    // falls back to an empty stub for class-served wands (ChaosStaff)
    private final LuaScript kindScript;

    // per-kind data from the desc table
    private String fxKind = "blue";
    private boolean hitChars = true;
    private boolean hitObjects = false;
    private boolean alwaysKnown;
    private boolean disenchants;
    private float basePrice = 50;
    private int initialCharges = 2;

    private final boolean directional = true;

    public Wand() {
        this(wands[0]);
    }

    public Wand(@NotNull String kind) {
        this(kind, "scripts/lib/emptykind");
    }

    public Wand(@NotNull String kind, String fallbackScript) {
        wandKind = kind;
        setDefaultAction(AC_ZAP);
        animation_class = WAND_ATTACK;
        kindScript = new LuaScript("scripts/items/" + kind, fallbackScript, this);

        applyKind();

        maxCharges = Scrambler.scramble(initialCharges);
        curCharges = Scrambler.scramble(Scrambler.descramble(maxCharges));

        if (handledKinds.contains(kind)) {
            initWandSpecifics();
        }
    }

    private void applyKind() {
        // Item's name/info initialize before the kind is known - re-resolve
        name = getClassParam("Name", StringsManager.getVar(R.string.Item_Name), false);
        info = getClassParam("Info", StringsManager.getVar(R.string.Item_Info), false);

        if (!kindScript.hasMethod("desc")) {
            return;
        }
        LuaValue desc = kindScript.run("desc").checktable();
        fxKind         = desc.rawget("fx").optjstring("blue");
        hitChars       = desc.rawget("hitChars").optboolean(true);
        hitObjects     = desc.rawget("hitObjects").optboolean(false);
        initialCharges = desc.rawget("initialCharges").optint(2);
        alwaysKnown    = desc.rawget("identified").optboolean(false);
        disenchants    = desc.rawget("disenchants").optboolean(false);
        basePrice      = (float) desc.rawget("price").optdouble(50);

        int imageOverride = desc.rawget("image").optint(-1);
        if (imageOverride >= 0) {
            image = imageOverride;
        }
        LuaValue imageFileOverride = desc.rawget("imageFile");
        if (!imageFileOverride.isnil()) {
            imageFile = imageFileOverride.tojstring();
        }
    }

    private void initWandSpecifics() {
        // handler is null until initWoods()/restore() runs; followers'
        // belongings are bundle-restored before that on web, where the
        // null-deref is a JS error that escapes catch(Exception)
        if (handler != null) {
            image = handler.index(wandKind);
            wood = StringsManager.getVars(R.array.Wand_Wood_Types)[ItemStatusHandler.indexByImage(image, images)];
        }
    }

    @Override
    public String getEntityKind() {
        return wandKind;
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

        if (disenchants && effectiveLevel() > 0) {
            actions.add(AC_DISENCHANT);
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

        if (action.equals(AC_DISENCHANT)) {
            chr.getBelongings().setSelectedItem(this);
            GameScene.selectItem(chr, itemSelector, WndBag.Mode.WAND, StringsManager.getVar(R.string.WandOfMagicMissile_SelectWand));
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

    protected void onZap(int cell, Char victim) {
        kindScript.runOptionalNoRet("zap", cell, victim);
    }

    public int effectiveLevel() {
        return level() + getOwner().buffLevel(BuffFactory.RING_OF_POWER);
    }

    public boolean isKnown() {
        if (alwaysKnown || !handledKinds.contains(wandKind)) {
            return true;
        }
        return handler.isKnown(wandKind);
    }

    public void setKnown() {
        if (!isKnown() && handler != null) {
            handler.know(wandKind);
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
            sb.append(" (").append(status).append(")");
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
        return initialCharges;
    }

    @LuaInterface
    public void mobWandUse(Char user, final int tgt) {

        if (user.invalid()) {
            EventCollector.logException(Utils.format("%s attempt of use by invalid char", getEntityKind()));
            return;
        }

        setOwner(user);
        final int cell = getDestinationCell(user.getPos(), tgt);
        fx(cell, () -> onZap(cell));
    }

    // Like mobWandUse, but consumes a charge and spends time.
    // Use this for mobs that carry wands as inventory items (MobItemAi).
    // mobWandUse stays free for Crystal/BlackCat/blinkTo which have infinite use.
    public void mobWandUseCharged(Char user, final int tgt) {
        // Spend BEFORE firing the effect: a zap can damage the caster (chain lightning arcing back,
        // blink into a hazard), and that self-damage -> gotDamage -> seekRevenge calls setState()
        // without spending. Spending first guarantees Mob.act's time-progress check passes even
        // when the zap triggers a state flip, preventing the WANDERING<->HUNTING ping-pong that
        // trips the "really confused" breaker.
        user.spend(TIME_TO_ZAP);
        mobWandUse(user, tgt);
        curCharges(curCharges() - 1);
    }

    protected void fx(int cell, Callback callback) {
        switch (fxKind) {
            case "deathray":
                // WandOfDisintegration: the ray draws to the clamped end of the
                // beam and the zap fires immediately (no missile wait)
                int rayCell = Ballistica.trace[Math.min(Ballistica.distance, effectiveLevel() + 4) - 1];
                getOwner().getSprite().getParent().add(new DeathRay(getOwner().getPos(), rayCell));
                callback.call();
                return;
            case "lightning":
                // WandOfLightning: all arc damage happens inside the fx pass,
                // then the Lightning visual completes the callback
                ItemUtils.lightningArc(this, cell, callback);
                return;
            case "blink":
                getOwner().getSprite().setVisible(false);
                MagicMissile.whiteLight(getOwner().getSprite().getParent(), getOwner().getPos(), cell, callback);
                Sample.INSTANCE.play(Assets.SND_ZAP);
                return;
            default:
                magicMissileByName(fxKind, getOwner().getSprite().getParent(), getOwner().getPos(), cell, callback);
                Sample.INSTANCE.play(Assets.SND_ZAP);
        }
    }

    private static void magicMissileByName(String kind, Group parent, int from, int to, Callback callback) {
        switch (kind) {
            case "purple":     MagicMissile.purpleLight(parent, from, to, callback); return;
            case "earth":      MagicMissile.earth(parent, from, to, callback);      return;
            case "white":      MagicMissile.whiteLight(parent, from, to, callback); return;
            case "fire":       MagicMissile.fire(parent, from, to, callback);       return;
            case "wool":       MagicMissile.wool(parent, from, to, callback);       return;
            case "poison":     MagicMissile.poison(parent, from, to, callback);     return;
            case "foliage":    MagicMissile.foliage(parent, from, to, callback);    return;
            case "slowness":   MagicMissile.slowness(parent, from, to, callback);   return;
            case "force":      MagicMissile.force(parent, from, to, callback);      return;
            case "coldLight":  MagicMissile.coldLight(parent, from, to, callback);  return;
            case "ice":        MagicMissile.ice(parent, from, to, callback);        return;
            case "shadow":     MagicMissile.shadow(parent, from, to, callback);     return;
            case "blue":
            default:           MagicMissile.blueLight(parent, from, to, callback);  return;
        }
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

    // was SimpleWand.createRandomSimpleWand: the eight battle wands
    // (Crystal.lua random-drop helper)
    @LuaInterface
    public static Wand createRandomSimpleWand() {
        String[] simpleWands = {"WandOfAmok", "WandOfAvalanche", "WandOfDisintegration",
                "WandOfFirebolt", "WandOfLightning", "WandOfPoison", "WandOfRegrowth",
                "WandOfSlowness"};
        return (Wand) ItemFactory.itemByName(Random.element(simpleWands));
    }

    public static boolean allKnown() {
        return handler.known().size() == wands.length;
    }

    @Override
    public int price() {
        return adjustPrice((int) basePrice);
    }

    private static final String MAX_CHARGES = "maxCharges";
    private static final String LUA_DATA = "luaData";
    private static final String CUR_CHARGES = "curCharges";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(MAX_CHARGES, maxCharges());
        bundle.put(CUR_CHARGES, curCharges());
        // kind-script state (ChaosStaff charge counter), the MeleeWeapon pattern
        if (kindScript.hasMethod("saveData")) {
            bundle.put(LUA_DATA, kindScript.run("saveData").checkjstring());
        }
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        maxCharges(bundle.getInt(MAX_CHARGES));
        curCharges(bundle.getInt(CUR_CHARGES));
        String luaData = bundle.optString(LUA_DATA, null);
        if (luaData != null) {
            kindScript.run("loadData", luaData);
        } else {
            kindScript.runOptionalNoRet("loadLegacy", bundle);
        }
    }

    @Override
    public void ownerTakesDamage(int damage) {
        kindScript.runOptionalNoRet("ownerTakesDamage", damage);
        super.ownerTakesDamage(damage);
    }

    @Override
    public void ownerDoesDamage(int damage) {
        kindScript.runOptionalNoRet("ownerDoesDamage", damage);
        super.ownerDoesDamage(damage);
    }

    protected void wandEffect(final int cell, Char selector) {
        setKnown();

        Char victim = Actor.findChar(cell);

        QuickSlot.target(this, victim);

        if (curCharges() > 0) {
            fx(cell, () -> {
                onZap(cell, victim);
            });
            wandUsed();
            CharUtils.dispelInvisibility(selector);
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
        int cell;
        if (!Dungeon.level.cellValid(target)) {
            cell = src;
        } else {
            cell = Ballistica.cast(src, target, directional, hitChars, hitObjects);
        }

        if (kindScript.hasMethod("destCell")) {
            LuaValue ret = kindScript.run("destCell", src, target);
            if (ret.isnumber()) {
                return ret.toint();
            }
        }
        return cell;
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
        return kindScript.runOptional("affectTarget", true);
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
        return "WandHolster";
    }

    @Override
    protected void act() {
        if (curCharges() < maxCharges()) {
            curCharges(curCharges() + 1);
            QuickSlot.refresh(getOwner());
        }

        float time2charge = getOwner().getHeroClass() == HeroClass.MAGE ? Wand.TIME_TO_CHARGE
                / (float) Math.sqrt(1 + effectiveLevel())
                : Wand.TIME_TO_CHARGE;
        time2charge -= (float) 0;
        spend(time2charge);
    }

    // idle particles (Icebolt: snow, Shadowbolt: purple); the scripts name
    // the factory, the framework owns the java-side Factory objects
    public Emitter.Factory emitter() {
        String name = kindScript.runOptional("emitter", Utils.EMPTY_STRING);
        if ("SnowParticle".equals(name)) {
            return SnowParticle.FACTORY;
        }
        if ("PurpleParticle".equals(name)) {
            return PurpleParticle.FACTORY;
        }
        return super.emitter();
    }

    public float emitterInterval() {
        return kindScript.runOptional("emitterInterval", super.emitterInterval());
    }

    // --- WandOfMagicMissile disenchant flow, kind-flag gated ---

    public static final String AC_DISENCHANT = "WandOfMagicMissile_ACDisenchant";

    private static final float TIME_TO_DISENCHANT = 2f;

    private final WndBag.Listener itemSelector = (item, selector) -> {
        if (item != null) {
            selector.getBelongings().removeItem(this);
            Sample.INSTANCE.play( Assets.SND_EVOKE );
            ScrollOfUpgrade.upgrade( selector );
            ItemUtils.evoke( selector );
            GLog.w(StringsManager.getVar(R.string.WandOfMagicMissile_Desinchanted), item.name() );
            item.upgrade();
            selector.spend( TIME_TO_DISENCHANT );
            Badges.validateItemLevelAcquired( item );
        }
        selector.updateSprite();
        QuickSlot.refresh(selector);
    };
}
