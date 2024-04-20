
package com.watabou.pixeldungeon.actors;

import static com.watabou.pixeldungeon.Dungeon.level;

import com.nyrds.LuaInterface;
import com.nyrds.Packable;
import com.nyrds.lua.LuaEngine;
import com.nyrds.lua.LuaUtils;
import com.nyrds.pixeldungeon.ai.AiState;
import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Passive;
import com.nyrds.pixeldungeon.ai.Sleeping;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.items.ItemOwner;
import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.items.artifacts.IActingItem;
import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.nyrds.pixeldungeon.levels.objects.Presser;
import com.nyrds.pixeldungeon.mechanics.HasPositionOnLevel;
import com.nyrds.pixeldungeon.mechanics.LevelHelpers;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKindWithId;
import com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory;
import com.nyrds.pixeldungeon.mechanics.spells.Spell;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.ml.actions.CharAction;
import com.nyrds.pixeldungeon.ml.actions.Move;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.nyrds.pixeldungeon.utils.EntityIdSource;
import com.nyrds.pixeldungeon.utils.ItemsList;
import com.nyrds.pixeldungeon.utils.Position;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.platform.util.TrackedRuntimeException;
import com.nyrds.util.JsonHelper;
import com.nyrds.util.Scrambler;
import com.nyrds.util.Util;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Facilitations;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.BuffCallback;
import com.watabou.pixeldungeon.actors.buffs.CharModifier;
import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.actors.buffs.Levitation;
import com.watabou.pixeldungeon.actors.buffs.Light;
import com.watabou.pixeldungeon.actors.buffs.Regeneration;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.actors.mobs.Fraction;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.WalkingType;
import com.watabou.pixeldungeon.actors.mobs.npcs.NPC;
import com.watabou.pixeldungeon.effects.Flare;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.effects.Wound;
import com.watabou.pixeldungeon.items.EquipableItem;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.weapon.melee.KindOfBow;
import com.watabou.pixeldungeon.items.weapon.missiles.Arrow;
import com.watabou.pixeldungeon.items.weapon.missiles.MissileWeapon;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.levels.features.Door;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.mechanics.ShadowCaster;
import com.watabou.pixeldungeon.scenes.CellSelector;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.InterlevelScene;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.Glowing;
import com.watabou.pixeldungeon.ui.QuickSlot;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;
import com.watabou.utils.SystemTime;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import org.json.JSONObject;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.val;


public abstract class Char extends Actor implements HasPositionOnLevel, Presser, ItemOwner, NamedEntityKindWithId {

    public static final String IMMUNITIES = "immunities";
    public static final String RESISTANCES = "resistances";
    protected static final String LEVEL = "lvl";
    private static final String DEFAULT_MOB_SCRIPT = "scripts/mobs/Dummy";
    static private final Map<String, JSONObject> defMap = new HashMap<>();
    public EquipableItem rangedWeapon = ItemsList.DUMMY;

    public CharAction lastAction = null;
    @Packable(defaultValue = "false")
    public boolean enemySeen;
    @Packable(defaultValue = "-1")//EntityIdSource.INVALID_ID
    protected int enemyId = EntityIdSource.INVALID_ID;

    @Packable(defaultValue = "0")
    protected int expForLevelUp = 0;

    protected LuaScript script;
    protected int baseStr = 10;
    protected int attackRange = 1;


    @Packable(defaultValue = "-1")//Level.INVALID_CELL
    @LuaInterface
    @Setter
    @Getter
    private int target = Level.INVALID_CELL;

    @NotNull
    protected ArrayList<Char> visibleEnemies = new ArrayList<>();
    protected AiState state = MobAi.getStateByClass(Sleeping.class);

    private Belongings belongings;


    @Packable(defaultValue = "-1")//EntityIdSource.INVALID_ID
    private int owner = EntityIdSource.INVALID_ID;

    @Packable(defaultValue = "-1")//Level.INVALID_CELL
    @Getter
    private int pos = Level.INVALID_CELL;

    @Packable(defaultValue = "0")
    private long layersMask = 0;

    transient private int prevPos = Level.INVALID_CELL;

    @Packable(defaultValue = "-1")//EntityIdSource.INVALID_ID
    private int id = EntityIdSource.INVALID_ID;

    protected int baseAttackSkill = 0;
    protected int baseDefenseSkill = 0;

    public Fraction fraction = Fraction.DUNGEON;

    protected CharSprite sprite;

    protected WalkingType walkingType = WalkingType.NORMAL;

    private int HT;
    private int HP;

    protected float baseSpeed = 1;
    protected boolean movable = true;

    public boolean paralysed = false;
    public boolean pacified = false;
    protected boolean flying = false;
    public int invisible = 0;

    private int viewDistance = 8;

    protected final Set<String> immunities = new HashSet<>();
    protected final Set<String> resistances = new HashSet<>();

    protected final Set<Buff> buffs = new HashSet<>();

    private Map<String, Number> spellsUsage = new HashMap<>();

    private CharAction curAction = null;

    private int lvl = Scrambler.scramble(1);
    private int magicLvl = Scrambler.scramble(1);
    private float lightness = 0.5f;
    private int glowColor = 0;
    private float glowPeriod = 0.0f;

    @Getter
    private int buffsUpdatedCount;

    public Char() {
        fillMobStats(false);
    }

    protected void fillMobStats(boolean b) {
    }

    @LuaInterface
    public boolean canSpawnAt(Level level, int cell) {
        boolean ret = walkingType.canSpawnAt(level, cell)
                && level.map[cell] != Terrain.ENTRANCE;
        GLog.debug("%s %d %b", getEntityKind(), cell, ret);
        return ret;
    }

    public int respawnCell(Level level) {
        return walkingType.respawnCell(level);
    }

    public void spendAndNext(float time) {
        spend(time);
        next();
    }

    public boolean checkVisibleEnemies() {
        ArrayList<Char> visible = new ArrayList<>();

        boolean newMob = false;

        for (Mob m : level().mobs) {
            if (m == this) {
                continue;
            }

            if (level().fieldOfView[m.getPos()] && !m.friendly(this) && m.invisible <= 0) {
                visible.add(m);
                if (!visibleEnemies.contains(m)) {
                    newMob = true;
                }
            }

        }

        visibleEnemies = visible;

        return newMob;
    }

    @Override
    public boolean act() {
        level().updateFieldOfView(this);
/*
        if (sprite == null) {
            if (Util.isDebug()) {
                throw new TrackedRuntimeException(Utils.format("%s act on %s without sprite", getEntityKind(), level().levelId));
            }
        }
*/
        checkVisibleEnemies();

        getScript().runOptional("onAct");

        forEachBuff(CharModifier::charAct);

        for (Item item : getBelongings()) {
            item.charAct();
        }

        if (getBelongings().encumbranceCheck().valid()) {
            Buff.permanent(this, "Encumbrance");
        } else {
            Buff.detach(this, "Encumbrance");
        }

        return false;
    }

    private static final String TAG_HP = "HP";
    private static final String TAG_HT = "HT";
    private static final String BUFFS = "buffs";
    private static final String SPELLS_USAGE = "spells_usage";

    @LuaInterface
    public int getOwnerPos() {
        return getOwner().getPos();
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        getId(); // Ensure id
        super.storeInBundle(bundle);

        bundle.put(TAG_HP, hp());
        bundle.put(TAG_HT, ht());
        bundle.put(BUFFS, buffs);
        bundle.put(SPELLS_USAGE, spellsUsage);
        bundle.put(LEVEL, lvl());

        getBelongings().storeInBundle(bundle);

        bundle.put(LuaEngine.LUA_DATA, getScript().run("saveData").checkjstring());
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);

        if (id != EntityIdSource.INVALID_ID) {
            if (!CharsList.add(this, id)) {
                id = EntityIdSource.DUPLICATE_ID;
            }
        }

        if (this instanceof Hero && id == EntityIdSource.DUPLICATE_ID) { //Hack for 30.1.fix.[9,10] saves
            id = EntityIdSource.INVALID_ID;
        }

        getId(); // ensure id

        fillMobStats(true);

        hp(bundle.getInt(TAG_HP));
        ht(bundle.getInt(TAG_HT));
        lvl(bundle.getInt(LEVEL));

        for (Buff b : bundle.getCollection(BUFFS, Buff.class)) {
            b.attachTo(this);
        }

        spellsUsage = bundle.getMap(SPELLS_USAGE);

        setupCharData();

        getBelongings().restoreFromBundle(bundle);

        //pre 32 save compatibility
        expForLevelUp = bundle.optInt("exp", expForLevelUp);


        String luaData = bundle.optString(LuaEngine.LUA_DATA, null);
        if (luaData != null) {
            getScript().run("loadData", luaData);
        }
        getScript().run("fillStats");
    }

    private String getClassParam(String paramName, String defaultValue, boolean warnIfAbsent) {
        return Utils.getClassParam(getEntityKind(), paramName, defaultValue, warnIfAbsent);
    }

    protected void setupCharData() {
        ///freshly created char or pre 28.6 save
        getId();

        if (getOwnerId() < 0) { //fix pre 29.4.fix.6 saves
            setOwnerId(id);
        }

        setBelongings(new Belongings(this));
    }

    @LuaInterface
    public void yell(String str) {
        GLog.n(StringsManager.getVar(R.string.Mob_Yell), getName(), StringsManager.maybeId(str));
    }

    @LuaInterface
    public void say(String str) {
        GLog.i(StringsManager.getVar(R.string.Mob_Yell), getName(), StringsManager.maybeId(str));
    }

    @LuaInterface
    public void yell(String str, int index) {
        GLog.n(StringsManager.getVar(R.string.Mob_Yell), getName(), StringsManager.maybeId(str, index));
    }

    @LuaInterface
    public void say(String str, int index) {
        GLog.i(StringsManager.getVar(R.string.Mob_Yell), getName(), StringsManager.maybeId(str, index));
    }

    @LuaInterface
    public void showStatus(int color, String text) {
        getSprite().showStatus(color, text);
    }

    @LuaInterface
    public void showStatus(int color, String text, Object... args) {
        getSprite().showStatus(color, text, args);
    }

    public boolean ignoreDr() {
        return false;
    }

    public boolean attack(@NotNull Char enemy) {

        if (enemy.invalid()) {
            EventCollector.logException(getEntityKind() + " attacking dummy enemy");
            return false;
        }

        if (!level().cellValid(enemy.getPos())) {
            EventCollector.logException(getEntityKind() + " attacking " + enemy.getEntityKind() + "on invalid cell");
            return false;
        }

        boolean visibleFight = CharUtils.isVisible(this) || CharUtils.isVisible(enemy);

        if (CharUtils.hit(this, enemy, false)) {

            if (visibleFight) {
                GLog.i(StringsManager.getVars(R.array.Char_Hit)[getGender()], getName(), enemy.getName_objective());
            }

            int dmg = damageRoll();

            int effectiveDamage = Math.max(dmg, 0);

            effectiveDamage = attackProc(enemy, effectiveDamage);
            effectiveDamage = enemy.defenseProc(this, effectiveDamage);
            enemy.damage(effectiveDamage, this);

            if (visibleFight) {
                Sample.INSTANCE.play(Assets.SND_HIT, 1, 1, Random.Float(0.8f, 1.25f));

                final CharSprite enemySprite = enemy.getSprite();

                enemySprite.bloodBurstA(
                        getSprite().center(), effectiveDamage);
                enemySprite.flash();
            }

            if (!enemy.isAlive() && visibleFight) {
                Hero hero = Dungeon.hero;
                if (enemy == hero) {

                    if (hero.killerGlyph != null) {
                        Dungeon.fail(Utils.format(ResultDescriptions.getDescription(ResultDescriptions.Reason.GLYPH), hero.killerGlyph.name(), Dungeon.depth));
                        GLog.n(StringsManager.getVars(R.array.Char_Kill)[hero.getGender()], hero.killerGlyph.name());
                    } else {
                        if (isBoss()) {
                            Dungeon.fail(Utils.format(ResultDescriptions.getDescription(ResultDescriptions.Reason.BOSS), getName(), Dungeon.depth));
                        } else {
                            Dungeon.fail(Utils.format(ResultDescriptions.getDescription(ResultDescriptions.Reason.MOB),
                                    Utils.indefinite(getName()), Dungeon.depth));
                        }

                        GLog.n(StringsManager.getVars(R.array.Char_Kill)[getGender()], getName());
                    }

                } else {
                    GLog.i(StringsManager.getVars(R.array.Char_Defeat)[getGender()], getName(), enemy.getName_objective());
                }
            }
            return true;
        } else {

            if (visibleFight) {
                String defense = enemy.defenseVerb();
                enemy.showStatus(CharSprite.NEUTRAL, defense);
                if (this == Dungeon.hero) {
                    GLog.i(StringsManager.getVar(R.string.Char_YouMissed), enemy.getName(), defense);
                } else {
                    GLog.i(StringsManager.getVar(R.string.Char_SmbMissed), enemy.getName(), defense, getName());
                }

                Sample.INSTANCE.play(Assets.SND_MISS);
            }
            return false;
        }
    }

    public boolean shoot(Char enemy, MissileWeapon wep) {

        rangedWeapon = wep;
        boolean result = attack(enemy);
        rangedWeapon = ItemsList.DUMMY;

        return result;
    }

    public boolean bowEquipped() {
        return getItemFromSlot(Belongings.Slot.WEAPON) instanceof KindOfBow;
    }

    public int attackSkill(Char target) {

        int[] bf = {0};
        forEachBuff(b -> bf[0] += b.attackSkillBonus());

        int bonus = bf[0];

        float accuracy = (float) Math.pow(1.4, bonus);

        if (target == null) { // Mainly to mask bug in Remixed RPG
            target = CharsList.DUMMY;
        }

        if (rangedWeapon.valid() && level().distance(getPos(), target.getPos()) == 1) {
            accuracy *= 0.5f;
        }

        float mainAccuracyFactor = getActiveWeapon().accuracyFactor(this);
        float secondaryAccuracyFactor = getSecondaryWeapon().accuracyFactor(this);

        float skillFactor = Utils.min(20f, mainAccuracyFactor, secondaryAccuracyFactor);

        int aSkill = (int) ((baseAttackSkill + lvl()) * accuracy * skillFactor);

        GLog.debug("%s attacking %s with factor %2.2f, resulting skill %d", getEntityKind(), target.getEntityKind(), skillFactor, aSkill);

        return aSkill;
    }

    public int defenseSkill(Char enemy) {

        int defenseSkill = baseDefenseSkill + lvl();

        final int[] bf = {0};
        forEachBuff(b -> bf[0] += b.defenceSkillBonus());

        int bonus = bf[0];

        float evasion = bonus == 0 ? 1 : (float) Math.pow(1.2, bonus);
        if (paralysed) {
            evasion /= 2;
        }

        int aEnc = getItemFromSlot(Belongings.Slot.ARMOR).requiredSTR() - effectiveSTR();

        if (aEnc > 0) {
            return (int) (defenseSkill * evasion / Math.pow(1.5, aEnc));
        } else {

            if (getHeroClass() == HeroClass.ROGUE) {

                if (getCurAction() != null && getSubClass() == HeroSubClass.FREERUNNER && !isStarving()) {
                    evasion *= 2;
                }

                return (int) ((defenseSkill - aEnc) * evasion);
            } else {
                return (int) (defenseSkill * evasion);
            }
        }
    }

    public String defenseVerb() {
        val defenceVerb = getDefenceVerb();
        if (defenceVerb != null) {
            return defenceVerb;
        }
        return StringsManager.getVars(R.array.Char_StaDodged)[getGender()];
    }


    public int defenceRoll(Char enemy) {
        if (enemy.ignoreDr()) {
            return 0;
        }

        final int[] dr = {dr()};
        forEachBuff(b -> dr[0] += b.drBonus());
        return Random.IntRange(0, dr[0]);
    }

    public int dr() {
        return getItemFromSlot(Belongings.Slot.ARMOR).effectiveDr();
    }


    public boolean actMeleeAttack(Char enemy) {
        if (canAttack(enemy)) {

            spend(attackDelay());

            final int enemyPos = enemy.getPos();
            final boolean realtime = Dungeon.realtime();

            if (Dungeon.isCellVisible(enemyPos) && !realtime) {
                getSprite().attack(enemyPos);
            } else {
                onAttackComplete();
            }

            return false;
        }
        return getCloserIfVisible(enemy.getPos());
    }

    public boolean actBowAttack(Char enemy) {

        KindOfBow kindOfBow = (KindOfBow) getItemFromSlot(Belongings.Slot.WEAPON);

        Item arrow = getBelongings().getItem(kindOfBow.arrowType());
        if (arrow == null || arrow.quantity() == 0) {
            arrow = getBelongings().getItem(Arrow.class);
        }

        if (arrow != null && arrow.quantity() > 0) { // We have arrows!
            arrow.cast(this, enemy.getPos());
            readyAndIdle();
            return false;
        } // no arrows? Go Melee

        return actMeleeAttack(enemy);
    }

    public boolean getCloserIfVisible(int pos) {
        if (level.fieldOfView[pos] && getCloser(pos)) {
            return true;
        } else {
            readyAndIdle();
            return false;
        }
    }

    public int attackProc(@NotNull Char enemy, int damage) {
        final int[] dmg = {damage};
        forEachBuff(b -> dmg[0] = b.attackProc(this, enemy, dmg[0]));

        if (!(enemy instanceof NPC)) {
            for (Item item : getBelongings()) {
                if (item.isEquipped(this)) {
                    item.ownerDoesDamage(dmg[0]);
                }
            }
        }

        int d = level().distance(getPos(), enemy.getPos());

        if (d <= getActiveWeapon().range() || rangedWeapon.valid()) {
            getActiveWeapon().attackProc(this, enemy, dmg[0]);
        }

        if (d <= getSecondaryWeapon().range()) {
            getSecondaryWeapon().attackProc(this, enemy, dmg[0]);
        }

        return getScript().run("onAttackProc", enemy, dmg[0]).optint(dmg[0]);
    }

    public int defenseProc(Char enemy, int baseDamage) {
        int dr = defenceRoll(enemy);

        if (!enemySeen && enemy.getSubClass() == HeroSubClass.ASSASSIN) {
            baseDamage += Random.Int(1, baseDamage);
            Wound.hit(this);
        }

        final int[] damage = {baseDamage - dr};

        forEachBuff(b -> damage[0] = b.defenceProc(this, enemy, damage[0]));
        damage[0] = getItemFromSlot(Belongings.Slot.ARMOR).defenceProc(enemy, this, damage[0]);


        if (getOwnerId() != enemy.getId()) {
            setEnemy(enemy);
        }

        return getScript().run("onDefenceProc", enemy, damage[0]).optint(damage[0]);

    }

    @NotNull
    public EquipableItem getActiveWeapon() {
        if (rangedWeapon.valid()) {
            return rangedWeapon;
        }

        return getItemFromSlot(Belongings.Slot.WEAPON);
    }

    @NotNull
    public EquipableItem getSecondaryWeapon() {
        EquipableItem leftItem = getItemFromSlot(Belongings.Slot.LEFT_HAND);

        if (leftItem.goodForMelee()) {
            return leftItem;
        }

        return ItemsList.DUMMY;
    }

    @Override
    public int priceSell(Item item) {
        return getScript().run("priceSell", item, item.price() * 5 * (Dungeon.depth / 5 + 1)).toint();
    }

    public int damageRoll() {
        int dmg = effectiveSTR() > 10 ? Random.IntRange(1, effectiveSTR() - 9) : 1;

        dmg += getActiveWeapon().damageRoll(this);

        if (!rangedWeapon.valid()) {
            dmg += getSecondaryWeapon().damageRoll(this);
        }

        return dmg;
    }

    public float speed() {
        final float[] speed = {baseSpeed};
        forEachBuff(b -> speed[0] *= b.speedMultiplier());

        return speed[0];
    }

    @LuaInterface
    public void heal(int heal) {
        heal(heal, CharsList.DUMMY, false);
    }

    @LuaInterface
    public void heal(int heal, @NotNull NamedEntityKind src) {
        heal(heal, src, false);
    }

    @LuaInterface
    public void heal(int heal, @NotNull NamedEntityKind src, boolean noAnim) {
        if (!isAlive()) {
            return;
        }

        heal = resist(heal, src);
        heal = Math.min(ht() - hp(), heal);

        if (heal <= 0) {
            return;
        }

        if (Util.isDebug()) {
            GLog.i("%s <- heal %d (%s)", getEntityKind(), heal, src.getEntityKind());
        }

        hp(hp() + heal);

        if (!noAnim && hasSprite()) {
            getSprite().emitter().burst(Speck.factory(Speck.HEALING), Math.max(1, heal * 5 / ht()));
        }
    }

    public void damage(int dmg, @NotNull NamedEntityKind src) {

        GLog.debug("%s: <- %d dmg from %s", getEntityKind(), dmg, src.getEntityKind());

        if (!isAlive()) {
            return;
        }

        getScript().run("onDamage", dmg, src);
        getState().gotDamage(this, src, dmg);

        final int[] dmg_ = {dmg};
        forEachBuff(b -> dmg_[0] = b.charGotDamage(dmg_[0], src, this));
        dmg = dmg_[0];

        dmg = resist(dmg, src);

        if (dmg <= 0) {
            GLog.debug("Resisted!!!", getEntityKind(), dmg, src.getEntityKind());
            return;
        }

        hp(hp() - dmg);

        getSprite().showStatus(hp() > ht() / 2 ?
                        CharSprite.WARNING :
                        CharSprite.NEGATIVE,
                Integer.toString(dmg));

        if (hp() <= 0) {
            die(src);
        }
    }

    private int resist(int dmg, @NotNull NamedEntityKind src) {
        String srcName = src.getEntityKind();
        if (immunities().contains(srcName)) {
            dmg = 0;
        } else if (resistances().contains(srcName)) {
            dmg = Random.IntRange(0, dmg);
        }
        return dmg;
    }

    public void destroy() {
        hp(0);
        Actor.remove(this);

        for (Buff buff : buffs.toArray(new Buff[0])) {
            buff.detach();
        }

        Actor.freeCell(getPos());
        CharsList.destroy(getId());
    }

    //Compatibility fix for Epic
    @LuaInterface
    public void die() {
        die(CharsList.DUMMY);
    }

    @LuaInterface
    public void die(@NotNull NamedEntityKind cause) {

        getState().onDie(this);

        if (cause == null) {
            cause = CharsList.DUMMY; //Mods may and will misbehave
            EventCollector.logException("null_death_cause");
        }

        getScript().run("onDie", cause);

        if (level().pit[getPos()]) {
            getSprite().fall();
        } else {
            getSprite().die();
        }
        destroy();
    }

    public boolean isAlive() {
        return valid() && hp() > 0;
    }

    protected float _attackDelay() {
        return 1.f;
    }

    public float attackDelay() {
        float mainDelayFactor = getActiveWeapon().attackDelayFactor(this);
        float secondaryDelayFactor = getSecondaryWeapon().attackDelayFactor(this);

        float delayFactor = Utils.max(0.05f, mainDelayFactor, secondaryDelayFactor);
        final float aDelay = _attackDelay() * delayFactor;


        GLog.debug("%s attackDelay factor %2.2f final delay %2.2f", getEntityKind(), delayFactor, aDelay);

        return aDelay;
    }


    public float timeScale() {
        final int[] bf = {0};
        forEachBuff(b -> bf[0] += b.hasteLevel());

        float hasteLevel = bf[0];

        return Math.min(3, (float) Math.pow(1.1f, -hasteLevel));
    }

    @Override
    public void spend(float time) {
        float scaledTime = time * timeScale();

        for (Item item : getBelongings()) {
            if (item instanceof IActingItem && item.isEquipped(this)) {
                ((IActingItem) item).spend(this, scaledTime);
            }
        }

        for (Map.Entry<String, Number> spell : spellsUsage.entrySet()) {
            spell.setValue(spell.getValue().floatValue() + scaledTime);
        }

        super.spend(scaledTime);

        QuickSlot.refresh(this);

        if (getCurAction() != null) {
            GLog.debug("%s", getCurAction().toString());
        }
    }

    public Set<Buff> buffs() {
        return buffs;
    }

    @SuppressWarnings("unchecked")
    public <T extends Buff> HashSet<T> buffs(Class<T> c) {
        HashSet<T> filtered = new HashSet<>();
        for (Buff b : buffs) {
            if (c.isInstance(b)) {
                filtered.add((T) b);
            }
        }
        return filtered;
    }

    @SuppressWarnings("unchecked")
    public <T extends Buff> T buff(Class<T> c) {
        for (Buff b : buffs) {
            if (c.isInstance(b)) {
                return (T) b;
            }
        }
        return null;
    }


    @LuaInterface
    public Buff buff(String buffName) {
        for (Buff b : buffs) {
            if (buffName.equals(b.getEntityKind())) {
                return b;
            }
        }
        return null;
    }

    @LuaInterface
    public boolean hasBuff(String buffName) {
        for (Buff b : buffs) {
            if (buffName.equals(b.getEntityKind())) {
                return true;
            }
        }
        return false;
    }

    @LuaInterface
    public int buffLevel(String buffName) {
        int level = 0;
        for (Buff b : buffs) {
            if (buffName.equals(b.getEntityKind())) {
                level += b.level();
            }
        }
        return level;
    }

    @Deprecated
    public int buffLevel(Class<? extends Buff> c) {
        int level = 0;
        for (Buff b : buffs) {
            if (c.isInstance(b)) {
                level += b.level();
            }
        }
        return level;
    }

    @Deprecated
    public boolean hasBuff(Class<? extends Buff> c) {
        for (Buff b : buffs) {
            if (c.isInstance(b)) {
                return true;
            }
        }
        return false;
    }

    public void nextAction(CharAction action) {
        setCurAction(action);

        if (getCurAction() instanceof Move) {
            lastAction = null;
        }
        next();

        GLog.debug("action: %s", getCurAction().toString());
        getControlTarget().setCurAction(getCurAction());
    }

    public boolean add(Buff buff) {
        if (!isAlive()) {
            return false;
        }

        //GLog.debug("%s (%s) added to %s", buff.getEntityKind(), buff.getSource().getEntityKind(), getEntityKind());

        buffs.add(buff);
        Actor.add(buff);

        buffsUpdatedCount++;

        if (!isOnStage()) {
            return true;
        }

        buff.attachVisual();
        return true;
    }

    public void remove(@Nullable Buff buff) {
        buffs.remove(buff);
        Actor.remove(buff);

        if (buff != null) {
            buffsUpdatedCount++;
            GLog.debug("%s removed from %s", buff.getEntityKind(), getEntityKind());
        }

        if (buff != null && sprite != null) {
            sprite.remove(buff.charSpriteStatus());
        }
    }

    @NotNull
    public Hunger hunger() {
        if (!(this instanceof Hero)) { //fix it later
            return new Hunger();
        }

        if (!isAlive() || Dungeon.isFacilitated(Facilitations.NO_HUNGER)) {
            return new Hunger();
        }

        Hunger hunger = buff(Hunger.class);

        if (hunger == null) {
            EventCollector.logEvent("null hunger on alive Char!");
            hunger = new Hunger();
            hunger.attachTo(this);
        }

        return hunger;
    }

    public boolean isStarving() {
        Hunger hunger = hunger();

        return hunger.isStarving();
    }

    public int stealth() {
        final int[] bonus = {0};

        forEachBuff(b -> bonus[0] += b.stealthBonus());

        return bonus[0];
    }

    public void placeTo(int cell) {

        final int oldPos = getPos();
        if (level().cellValid(oldPos)) {
            if (level().map[oldPos] == Terrain.OPEN_DOOR) {
                Door.leave(oldPos);
            }
        }

        setPos(cell);

        if (!isFlying()) {
            level().press(cell, this);
        }

        if (isFlying() && level().map[cell] == Terrain.DOOR) {
            Door.enter(getPos());
        }

        if (this != Dungeon.hero) {
            getSprite().setVisible(Dungeon.isCellVisible(cell) && invisible >= 0);
        }
    }

    public void move(int step) {
        if (!isMovable() || hasBuff(Roots.class)) {
            return;
        }

        if (hasBuff(BuffFactory.VERTIGO) && level().adjacent(getPos(), step)) { //ignore vertigo when blinking or teleporting

            List<Integer> candidates = new ArrayList<>();
            for (int dir : Level.NEIGHBOURS8) {
                int p = getPos() + dir;
                if (level().cellValid(p)) {
                    if ((level().passable[p] || level().avoid[p]) && Actor.findChar(p) == null) {
                        candidates.add(p);
                    }
                }
            }

            if (candidates.isEmpty()) { // Nowhere to move? just stay then
                return;
            }

            step = Random.element(candidates);
        }

        getScript().run("onMove", step);
        placeTo(step);
    }

    public int distance(@NotNull Char other) {
        return level().distance(getPos(), other.getPos());
    }

    public void onMotionComplete() {
        next();
    }

    public void onAttackComplete() {
        Char enemy = getEnemy();

        if (enemy.valid()) {
            final EquipableItem weapon = getItemFromSlot(Belongings.Slot.WEAPON);
            weapon.preAttack(enemy);

            if (attack(enemy)) {
                weapon.postAttack(enemy);
            }
        }

        setCurAction(null);

        Invisibility.dispel(this);

        next();
    }

    @LuaInterface
    public void playExtra(String key) {
        if (Dungeon.isCellVisible(getPos())) {
            getSprite().playExtra(key);
        }
    }

    public void playAttack(int cell) {
        final boolean realtime = Dungeon.realtime();

        if (Dungeon.isCellVisible(cell) && !realtime) {
            getSprite().dummyAttack(cell);
        } else {
            next();
        }
    }

    public void doAttack(Char enemy) {

        setEnemy(enemy);
        spend(attackDelay());

        final int pos = getPos();
        final int enemyPos = enemy.getPos();
        final boolean realtime = Dungeon.realtime();

        if (level().distance(pos, enemyPos) <= 1) {
            if (Dungeon.isCellVisible(enemyPos) && !realtime) {
                getSprite().attack(enemyPos);
            } else {
                onAttackComplete();
            }
        } else {
            if (Dungeon.isPathVisible(pos, enemyPos) && !realtime) {
                getSprite().zap(enemyPos);
            } else {
                onZapComplete();
            }
        }
    }

    public void onZapComplete() {
        next();
    }

    public void doOperate() {
        doOperate(0, getPos());
    }

    public void doOperate(float time) {
        doOperate(time, getPos());
    }

    public void doOperate(float time, int cell) {
        spend(time);
        getSprite().operate(cell);

        if (Dungeon.realtime()) {
            onOperateComplete();
        }
    }

    public void doOperate(float time, int cell, Callback onComplete) {
        spend(time);

        if (Dungeon.realtime()) {
            getSprite().operate(cell);
            onComplete.call();
        } else {
            getSprite().operate(cell, onComplete);
        }
    }

    public void onOperateComplete() {
        next();
    }

    public void spendGold(int spend) {
        Belongings belongings = getBelongings();

        Gold gold = belongings.getItem(Gold.class);
        if (gold != null) {
            gold.quantity(gold.quantity() - spend);
        }
    }

    public Set<String> resistances() {
        HashSet<String> ret = new HashSet<>(resistances);

        forEachBuff(b -> ret.addAll(b.resistances()));

        return ret;
    }

    public Set<String> immunities() {
        HashSet<String> ret = new HashSet<>(immunities);

        forEachBuff(b -> ret.addAll(b.immunities()));

        return ret;
    }

    public void updateSprite() {
        Level level = level();

        if (level != null && level.cellValid(getPos()) && valid()) {
            updateSprite(getSprite());
        }
    }

    private void updateSprite(CharSprite sprite) {
        if (level().cellValid(getPos())) {
            sprite.setVisible(Dungeon.isCellVisible(getPos()) && invisible >= 0);
        } else {
            EventCollector.logException("invalid pos for:" + this + ":" + getEntityKind());
        }
        GameScene.addMobSpriteDirect(this, sprite);

        if (isOnStage()) {
            assert (sprite.getParent() != null);
        }


        if (sprite.getParent() == null) {
            String err = String.format("sprite addition failed for %s %b", getEntityKind(), GameScene.isSceneReady());
            GLog.debug(err);
        }

        sprite.link(this);
    }

    public void regenSprite() {
        if (sprite != null) {
            sprite.killAndErase();
        }
        sprite = null;
    }

//	public Group getSpriteParent() {
//		Group parent = getSprite()
//	}

    @LuaInterface
    public CharSprite getSprite() {

        if (invalid()) {
            if (Util.isDebug()) {
                throw new TrackedRuntimeException(Utils.format("Attempt to get sprite for invalid char %s, id %d", getEntityKind(), getId()));
            }
        }

        if (sprite == null) {

            if (!GameScene.mayCreateSprites()) {
                throw new TrackedRuntimeException("scene not ready for " + getEntityKind());
            }

            if (Util.isDebug()) {
                if (!isAlive()) {
                    //throw new TrackedRuntimeException("its dead! leave it alone! " + getEntityKind());
                }
            }
            sprite = newSprite();
            sprite.lightness(lightness);
            setGlowing(glowColor, glowPeriod);
        }

        if (sprite == null) {
            throw new TrackedRuntimeException("Sprite creation for: " + getEntityKind() + " failed");
        }

        sprite.layersMask = layersMask;

        if (sprite.getParent() == null) {
            updateSprite(sprite);
        }

        assert (sprite.getParent() != null);

        return sprite;
    }

    public boolean hasSprite() {
        return sprite != null;
    }

    public Fraction fraction() {
        return fraction;
    }

    public int getOwnerId() {
        if (owner < 0) {
            setOwnerId(getId());
        }
        return owner;
    }

    @NotNull
    @LuaInterface
    public Char getOwner() {
        return CharsList.getById(getOwnerId());
    }

    public boolean followOnLevelChanged(InterlevelScene.Mode changeMode) {
        return false;
    }

    public abstract CharSprite newSprite();

    public int ht() {
        return Scrambler.descramble(HT);
    }

    public int ht(int hT) {
        HT = Scrambler.scramble(hT);
        return hT;
    }

    public int hp() {
        return Scrambler.descramble(HP);
    }

    public void hp(int hP) {
        HP = Scrambler.scramble(hP);
    }

    public void _stepBack() {
        if (level().cellValid(prevPos)) {
            setPos(prevPos);
        }
    }

    public void setPos(int pos) {
        if (pos == Level.INVALID_CELL) { // level may be not yet available here
            throw new TrackedRuntimeException("Trying to set invalid pos " + pos + " for " + getEntityKind());
        }
        prevPos = this.pos;
        freeCell(this.pos);
        this.pos = pos;
        occupyCell(this);
    }

    public boolean isMovable() {
        return movable;
    }

    public boolean collect(@NotNull Item item) {
        item = Treasury.get().check(item);

        if (!item.collect(this)) {
            Level level = level();
            if (level != null && level.cellValid(getPos())) {
                level.animatedDrop(item, getPos());
            }
            return false;
        }
        return true;
    }

    @LuaInterface
    public void collectAnimated(@NotNull Item item) {
        collect(item);
    }

    //backward compatibility with mods
    @LuaInterface
    public int magicLvl() {
        return skillLevel();
    }

    @Override
    public boolean affectLevelObjects() {
        return true;
    }

    public boolean isFlying() {
        return !paralysed && (flying || hasBuff(Levitation.class));
    }

    @LuaInterface
    public boolean isParalysed() {
        return paralysed;
    }

    public void paralyse(boolean paralysed) {
        this.paralysed = paralysed;
        if (paralysed && isOnStage()) {
            level().press(getPos(), this);
        }
    }

    public void setState(@NotNull AiState state) {
        if (!state.equals(this.state)) {
            //GLog.debug("%s now will %s, was doing %s before", getEntityKind(), this.state.getTag(), state.getTag());
            this.state = state;
        }
        spend(Actor.MICRO_TICK);
    }

    public void onSpawn(Level level) {
        Buff.affect(this, Regeneration.class);
        getScript().run("onSpawn", level);
    }

    protected JSONObject getClassDef() {
        String entityKind = getEntityKind();
        if (!defMap.containsKey(entityKind)) {
            defMap.put(entityKind, JsonHelper.tryReadJsonFromAssets("mobsDesc/" + entityKind + ".json"));
        }

        return defMap.get(entityKind);
    }

    public boolean friendly(@NotNull Char chr) {
        return friendly(chr, 0);
    }

    public boolean friendly(@NotNull Char chr, int r_level) {
        return !fraction.isEnemy(chr.fraction);
    }

    public Level level() {
        return level;
    }

    @LuaInterface
    public boolean valid() {
        return !(this instanceof DummyChar) && id > EntityIdSource.INVALID_ID;
    }

    @LuaInterface
    public boolean doStepTo(final int target) {
        int oldPos = getPos();
        spend(1 / speed());
        if (level().cellValid(target) && getCloser(target)) {

            moveSprite(oldPos, getPos());
            return true;
        }
        return false;
    }

    @LuaInterface
    public boolean doStepFrom(final int target) {
        int oldPos = getPos();
        spend(1 / speed());
        if (level().cellValid(target) && getFurther(target)) {
            moveSprite(oldPos, getPos());
            return true;
        }
        return false;
    }

    public int skillLevel() {
        return Scrambler.descramble(magicLvl);
    }

    public boolean isEnemyInFov() {
        final Char enemy = getEnemy();
        final int enemyPos = enemy.getPos();
        return enemy.valid() && enemy.isAlive() && level().cellValid(enemyPos) && level().fieldOfView[enemyPos]
                && enemy.invisible <= 0;
    }

    protected abstract void moveSprite(int oldPos, int pos);

    public int visibleEnemies() {
        return visibleEnemies.size();
    }

    public Char visibleEnemy(int index) {
        if (index >= visibleEnemies.size()) {
            return CharsList.DUMMY;
        }
        return visibleEnemies.get(index);
    }

    @NotNull
    @LuaInterface
    public Char randomEnemy() {
        if (visibleEnemies.isEmpty()) {
            return CharsList.DUMMY;
        }
        return Random.element(visibleEnemies);
    }

    @LuaInterface
    @NotNull
    public Char getNearestEnemy() {
        Char nearest = CharsList.DUMMY;
        int dist = Integer.MAX_VALUE;
        for (Char mob : visibleEnemies) {
            int mobDist = level().distance(getPos(), mob.getPos());
            if (mobDist < dist) {
                dist = mobDist;
                nearest = mob;
            }
        }
        return nearest;
    }

    public abstract boolean getCloser(final int cell);

    protected abstract boolean getFurther(final int cell);

    @NotNull
    @Override
    public Belongings getBelongings() {
        if (belongings == null) {
            belongings = new Belongings(this);
        }
        return belongings;
    }

    public int gold() {
        Belongings belongings = getBelongings();
        Gold gold = belongings.getItem(Gold.class);
        if (gold != null) {
            return gold.quantity();
        }
        return 0;
    }

    public void spellCasted(Spell spell) {
        getSubClass().spellCasted(this, spell);
        getHeroClass().spellCasted(this, spell);

        forEachBuff((buff) -> buff.spellCasted(this, spell));

        spellsUsage.put(spell.getEntityKind(), 0.f);
    }

    public float spellCooldown(String spellName) {
        if (spellsUsage.containsKey(spellName)) {
            return spellsUsage.get(spellName).floatValue();
        }
        return Float.MAX_VALUE;
    }

    public void addImmunity(String namedEntity) {
        immunities.add(namedEntity);
    }

    public void addImmunity(Class<?> buffClass) {
        immunities.add(buffClass.getSimpleName());
    }

    public void addResistance(Class<?> buffClass) {
        resistances.add(buffClass.getSimpleName());
    }

    public void removeImmunity(Class<?> buffClass) {
        immunities.remove(buffClass.getSimpleName());
    }

    public void removeResistance(Class<?> buffClass) {
        resistances.remove(buffClass.getSimpleName());
    }

    @Override
    public String getEntityKind() {
        return super.getEntityKind();
    }

    @Override
    public String name() {
        return getName();
    }

    @LuaInterface
    public boolean push(Char chr) {

        if (!isMovable()) {
            return false;
        }

        int nextCell = LevelHelpers.pushDst(chr, this, false);

        if (!level().cellValid(nextCell)) {
            return false;
        }

        LevelObject lo = level().getTopLevelObject(nextCell);

        if (lo != null && !lo.push(this)) {
            return false;
        }

        Char ch = Actor.findChar(nextCell);

        if (ch != null) {
            if (!ch.isMovable()) {
                return false;
            }

            if (!ch.push(this)) {
                return false;
            }
        }

        moveSprite(getPos(), nextCell);
        placeTo(nextCell);
        return true;
    }

    public void forEachBuff(BuffCallback cb) {
        Buff[] copyOfBuffsSet = buffs.toArray(new Buff[0]);
        for (Buff b : copyOfBuffsSet) {
            cb.onBuff(b);
        }

        cb.onBuff(getHeroClass());
        cb.onBuff(getSubClass());
    }

    @LuaInterface
    public boolean swapPosition(final Char chr) {
        if (!movable) {
            return false;
        }

        if (!walkingType.canWalkOn(level(), chr.getPos())) {
            return false;
        }

        if (hasBuff(Roots.class)) {
            return false;
        }

        int myPos = getPos(), chPos = chr.getPos();
        moveSprite(myPos, chPos);
        placeTo(chPos);
        ensureOpenDoor();

        chr.getSprite().move(chPos, myPos);
        chr.placeTo(myPos);
        chr.ensureOpenDoor();

        float timeToSwap = 1 / chr.speed();
        chr.spend(timeToSwap);
        spend(timeToSwap);

        return true;
    }

    protected void ensureOpenDoor() {
        if (level().map[getPos()] == Terrain.DOOR) {
            Door.enter(getPos());
        }
    }

    public boolean canBePet() {
        return false;
    }

    public boolean interact(Char chr) {

        if (getScript().run("onInteract", chr).optboolean(true)) {
            return true;
        }

        if (friendly(chr)) {
            swapPosition(chr);
            return true;
        }

        return false;
    }

    public String className() {
        return name();
    }

    @LuaInterface
    public int lvl() {
        return Scrambler.descramble(lvl);
    }

    @LuaInterface
    public void lvl(int lvl) {
        this.lvl = Scrambler.scramble(lvl);
    }

    public HeroClass getHeroClass() {
        return HeroClass.NONE;
    }

    public HeroSubClass getSubClass() {
        return HeroSubClass.NONE;
    }

    @LuaInterface
    static public HeroSubClass getSubClassByName(String subClassName) {
        return HeroSubClass.valueOf(subClassName);
    }

    public int countPets() {
        int ret = 0;
        for (Mob mob : level().mobs) {
            if (mob.getOwnerId() == getId()) {
                ret++;
            }
        }
        return ret;
    }

    @LuaInterface
    @NotNull
    public LuaTable getPets_l() {
        ArrayList<Char> pets = new ArrayList<>();

        for (Integer id : getPets()) {
            pets.add(CharsList.getById(id));
        }

        return LuaUtils.CollectionToTable(pets);
    }

    @NotNull
    public Collection<Integer> getPets() {
        ArrayList<Integer> pets = new ArrayList<>();
        for (Mob mob : level().mobs) {
            if (mob.getOwnerId() == getId()) {
                pets.add(mob.getId());
            }
        }
        return pets;
    }

    public void releasePets() {
        for (Mob mob : level().mobs) {
            if (mob.getOwnerId() == getId()) {
                mob.releasePet();
            }
        }
    }

    public int effectiveSTR() {
        return baseStr + lvl() / 5;
    }

    public void busy() {
    }

    public void interrupt() {
    }

    public void itemPickedUp(Item item) {
    }

    public void setEnemy(@NotNull Char enemy) {
        if (enemy == this) {
            EventCollector.logException(enemy.getEntityKind() + " gonna suicidal");
        }

        if (Util.isDebug()) {

            if (enemy == this) {
                //GLog.i("WTF???");
                throw new TrackedRuntimeException(enemy.getEntityKind());
            }

            if (enemyId != enemy.getId() && enemy.valid()) {
                //GLog.i("%s  my enemy is %s now ", this.getEntityKind(), enemy.getEntityKind());
            }
        }

        enemyId = enemy.getId();
    }

    @NotNull
    public Char getEnemy() {
        return CharsList.getById(enemyId);
    }

    protected void zapMiss(@NotNull Char enemy) {
        getScript().run("onZapMiss", enemy);
    }

    protected int zapProc(@NotNull Char enemy, int damage) {
        return getScript().run("onZapProc", enemy, damage).optint(damage);
    }

    @LuaInterface
    public String getMobClassName() { //Used by some mods, such as RA
        return getEntityKind();
    }


    @LuaInterface
    public boolean isPet() { //Used by some mods, such as RA
        return false;
    }

    public abstract Char makeClone();

    protected void setOwnerId(int owner) {
        this.owner = owner;
    }

    @TestOnly
    public void resetBelongings(Belongings belongings) {
        setBelongings(belongings);
        updateSprite();
    }

    public void selectCell(CellSelector.Listener listener) {
        GLog.w("select cell for %s niy.", getEntityKind());
    }

    @LuaInterface
    public int getSkillPointsMax() {
        return 10;
    }

    @LuaInterface
    public int getSkillPoints() {
        return 10;
    }

    @LuaInterface
    public void spendSkillPoints(int cost) {
    }

    @LuaInterface
    public void setSkillPoints(int i) {
    }

    @LuaInterface
    @Deprecated // keep it for old version of Remixed Additions
    public void setSoulPoints(int i) {
        setSkillPoints(i);
    }

    public boolean canAttack(@NotNull Char enemy) {
        if (enemy.invalid() || enemy.friendly(this)) {
            return false;
        }

        if (adjacent(enemy)) {
            return true;
        }

        var weapon = getItemFromSlot(Belongings.Slot.WEAPON);

        if (weapon.range() > 1) {
            Ballistica.cast(getPos(), enemy.getPos(), false, true);

            for (int i = 1; i <= Math.min(Ballistica.distance, weapon.range()); i++) {
                Char chr = Actor.findChar(Ballistica.trace[i]);
                if (chr == enemy) {
                    return true;
                }
            }
        }

        if (getItemFromSlot(Belongings.Slot.WEAPON) instanceof KindOfBow) {
            if (getBelongings().getItem(Arrow.class) != null) {
                return enemy.getPos() == Ballistica.cast(getPos(), enemy.getPos(), false, true);
            }
        }

        return false;
    }

    @LuaInterface
    public void eat(Item food, float energy, String message) {
    }

    public void setSkillLevel(int level) {
        magicLvl = Scrambler.scramble(level);
    }

    @LuaInterface
    public void skillLevelUp() {
    }

    @LuaInterface
    public void teleportTo(Position target) {
    }

    private final Map<String, String> layersOverrides = new HashMap<>();

    public Map<String, String> getLayersOverrides() {
        return layersOverrides;
    }

    @LuaInterface
    public void overrideSpriteLayer(String layer, String texture) {
        layersOverrides.put(layer, texture);
    }

    public int getViewDistance() {
        int computedViewDistance = viewDistance;

        if (hasBuff(Light.class)) {
            if (level() != null) {
                computedViewDistance = Utils.max(computedViewDistance, Level.MIN_VIEW_DISTANCE + 1, level().getViewDistance());
            } else {
                computedViewDistance = Math.max(computedViewDistance, Level.MIN_VIEW_DISTANCE + 1);
            }
        }

        return Math.min(computedViewDistance, ShadowCaster.MAX_DISTANCE);
    }

    public void setViewDistance(int viewDistance) {
        this.viewDistance = viewDistance;
    }

    @Override
    public boolean useBags() {
        return true;
    }

    @LuaInterface
    public boolean canStepOn() {
        return walkingType.canSpawnAt(level(), getPos());
    }

    public void setBelongings(Belongings belongings) {
        this.belongings = belongings;
    }

    public boolean invalid() {
        return !valid();
    }

    public void readyAndIdle() {
    }

    public void clearActions() {
    }

    public void handle(int cell) {
    }

    public boolean adjacent(@NotNull HasPositionOnLevel chr) {
        return level().adjacent(getPos(), chr.getPos());
    }

    @NotNull
    public Char getControlTarget() {
        return this;
    }

    public void notice() {
        getSprite().showAlert();
    }

    public AiState getState() {
        return MobAi.getStateByClass(Passive.class);
    }

    public int getId() {
        if (id == EntityIdSource.INVALID_ID || id == EntityIdSource.DUPLICATE_ID) {
            id = EntityIdSource.getNextId();
            CharsList.add(this, id);
        }
        return id;
    }

    @LuaInterface
    @NotNull
    public EquipableItem getItemFromSlot(@NotNull String slot) {
        return getBelongings().getItemFromSlot(Belongings.Slot.valueOf(slot));
    }

    @NotNull
    public EquipableItem getItemFromSlot(Belongings.Slot slot) {
        return getBelongings().getItemFromSlot(slot);
    }

    protected void fx(int cell, Callback callback) {
    }


    @LuaInterface
    @NotNull
    public Item getItem(String itemClass) {
        for (Item item : getBelongings()) {
            if (itemClass.equals(item.getEntityKind())) {
                return item;
            }
        }
        return ItemsList.DUMMY;
    }

    @LuaInterface
    @Deprecated
    public String description() { // Still used in Remixed Additions
        return getDescription();
    }

    public void execute(Char chr, String action) {
        CharUtils.execute(this, chr, action);
    }

    public float getAttentionFactor() {
        return 1f;
    }

    public void onActionTarget(String action, Char actor) {
    }

    @Override
    public void generateNewItem() {
    }

    @Override
    public int priceBuy(Item item) {
        return getScript().run("priceBuy", item, (item.price())).toint();
    }


    public String getDescription() {
        var description = getClassParam("Desc", "missing desc", true);

        description = StringsManager.maybeId(getClassDef().optString(description, getEntityKind() + "_Desc"));

        for (Buff buff : buffs) {
            if (buff.getEntityKind().startsWith("Champion")) {
                description += "\n\n" + StringsManager.maybeId(buff.name());
                description += "\n" + StringsManager.maybeId(buff.desc());
            }
        }

        if (!Util.isDebug()) {
            return description + "\n\n" + String.format(StringsManager.getVar(R.string.CharInfo_Level), lvl(), name());
        }

        return description + "\n\n"
                + String.format(StringsManager.getVar(R.string.CharInfo_Level), lvl(), name()) + "\n\n"
                + Utils.format("id: %d owner: %d", getId(), getOwnerId());
    }

    public void setControlTarget(Char controlTarget) {
    }

    @LuaInterface
    public Position getPosition() {
        return new Position(level().levelId, getPos());
    }

    public void resurrectAnim() {
        new Flare(8, 32).color(0xFFFF66, true).show(getSprite(), 2f);
    }

    public abstract void resurrect();

    public void setSubClass(HeroSubClass subClass) {
    }


    @LuaInterface
    public void setMaxSkillPoints(int points) {
    }

    public void STR(int sTR) {
    }

    public int STR() {
        return effectiveSTR();
    }

    public void accumulateSkillPoints(int n) {
    }

    public boolean isBoss() {
        return false;
    }

    @LuaInterface
    public void detachBuff(String buffClass) {
        Buff.detach(this, buffClass);
    }

    @LuaInterface
    public int getAttackRange() {
        return Math.max(attackRange, getBelongings().getItemFromSlot(Belongings.Slot.WEAPON).range());
    }

    public int getExpForLevelUp() {
        return expForLevelUp;
    }

    public void earnExp(int exp) {
        this.expForLevelUp += exp;

        boolean levelUp = false;

        while (this.getExpForLevelUp() >= expToLevel()) {
            this.expForLevelUp -= expToLevel();
            lvl(lvl() + 1);

            ht((int) (ht() + GameLoop.getDifficultyFactor() * 2));
            heal(lvl(), this);

            levelUp = true;
        }

        if (levelUp) {
            getSprite().showStatus(CharSprite.POSITIVE, StringsManager.getVar(R.string.Hero_LevelUp));
        }
    }

    public int expToLevel() {
        return 5 + lvl() * 5;
    }

    public boolean isReady() {
        return true;
    }

    public boolean isSpellUser() {
        return true;
    }

    public void resume() {
    }

    public void lightness(float value) {
        lightness = value;
    }

    public CharAction getCurAction() {
        return curAction;
    }

    public void setCurAction(CharAction curAction) {
        this.curAction = curAction;
    }

    public ArrayList<String> actions(Char hero) {
        ArrayList<String> actions = CharUtils.actions(this, hero);

        LuaValue ret = getScript().run("actionsList", hero);
        LuaEngine.forEach(ret, (key, val) -> actions.add(val.tojstring()));

        return actions;
    }

    @LuaInterface
    public int emptyCellNextTo() {
        return level().getEmptyCellNextTo(getPos());
    }


    @LuaInterface
    public void setLayersMask(long layersMask) {
        this.layersMask = layersMask;
        updateSprite();
    }

    public String getName() {
        return StringsManager.maybeId(getClassDef().optString("name", getEntityKind() + "_Name"));
    }

    public String getName_objective() {
        return StringsManager.maybeId(getClassDef().optString("name_objective", getEntityKind() + "_Name_Objective"));
    }

    public String getDefenceVerb() {
        return StringsManager.maybeId(getClassDef().optString("defenceVerb", StringsManager.getVars(R.array.Char_StaDodged)[getGender()]));
    }

    public int getGender() {
        return Utils.genderFromString(StringsManager.maybeId(getClassDef().optString("gender", getEntityKind() + "_Gender")));
    }

    public WalkingType getWalkingType() {
        return this.walkingType;
    }

    public LuaScript getScript() {
        if (script == null) {
            script = new LuaScript("scripts/mobs/" + getEntityKind(), DEFAULT_MOB_SCRIPT, this);
            script.asInstance();
        }
        return script;
    }

    @LuaInterface
    public void setGlowing(int color, float period) {
        this.glowColor = color;
        this.glowPeriod = period;

        if (sprite != null) {
            if (glowPeriod > 0) {
                getSprite().setGlowing(new Glowing(color, period));
            } else {
                getSprite().setGlowing(Glowing.NO_GLOWING);
            }
        }
    }

    public int getSpriteLayer() {
        return 0;
    }

    public void observe() {
    }

    public void buffsUpdated() {
        buffsUpdatedCount++;
    }
}
