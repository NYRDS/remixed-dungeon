
package com.watabou.pixeldungeon.actors.mobs;

import com.nyrds.LuaInterface;
import com.nyrds.Packable;
import com.nyrds.generated.BundleHelper;
import com.nyrds.pixeldungeon.ai.AiState;
import com.nyrds.pixeldungeon.ai.Horrified;
import com.nyrds.pixeldungeon.ai.Hunting;
import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.RemoteControlled;
import com.nyrds.pixeldungeon.ai.RunningAmok;
import com.nyrds.pixeldungeon.ai.Sleeping;
import com.nyrds.pixeldungeon.ai.Wandering;
import com.nyrds.pixeldungeon.game.ModQuirks;
import com.nyrds.pixeldungeon.items.Carcass;
import com.nyrds.pixeldungeon.items.ItemUtils;
import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.items.common.Library;
import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.common.IDepthAdjustable;
import com.nyrds.pixeldungeon.mobs.common.IZapper;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.audio.MusicManager;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.platform.util.TrackedRuntimeException;
import com.nyrds.util.JsonHelper;
import com.nyrds.util.ModdingBase;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.effects.Flare;
import com.watabou.pixeldungeon.effects.Pushing;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.keys.SkeletonKey;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfPsionicBlast;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.pixeldungeon.levels.features.Chasm;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.InterlevelScene;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.HeroSpriteDef;
import com.watabou.pixeldungeon.sprites.MobSpriteDef;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.luaj.vm2.LuaValue;

public class Mob extends Char implements IZapper {

    public static final String TXT_RAGE = "#$%^";

    private static final float SPLIT_DELAY = 1f;
    public static final String LOOT = "loot";

    private static final String STATE = "state";
    private static final String FRACTION = "fraction";
    private static final String KIND_TAG = "KIND";

    protected String spriteClass;

    @Packable(defaultValue = "1")
    public int expForKill = 1;

    // level the mob's current pos belongs to (pets keep their spot on game reload)
    @Packable(defaultValue = "unknown")
    public String levelId = "unknown";

    protected int maxLvl = 50;
    protected float carcassChance = ModdingBase.inMod() ? ModQuirks.defaultCarcassChance : 0.5f;

    // caveman: scripts must be able to flag mobs as carcass-free (chess pieces)
    @LuaInterface
    public void setCarcassChance(float chance) {
        carcassChance = chance;
    }

    public static final float TIME_TO_WAKE_UP = 1f;

    protected int dmgMin = 0;
    protected int dmgMax = 0;
    protected final int attackSkill = 0;
    protected int dr = 0;
    protected boolean isBoss = false;

    // sprite variant (`var` json key, KIND bundle tag): picks the frame in the sprite def
    private int kind = 0;

    // data-mob state (authored via mobsDesc json); plain java mobs keep engine defaults
    private float attackDelay = 1;
    private int spriteLayer = 0;

    @Packable
    public String mobClass = "Unknown";

    private boolean canBePet = true;
    private boolean friendly;
    private boolean immortal = false;
    private boolean humanoid = false;
    private boolean hasBodyParts = true;

    // statue-style sprite: hero-layers + currently equipped item
    private boolean heroSprite = false;

    // hero-look sprite layers (MirrorImage clones): set by Hero.makeClone,
    // persisted like the java MirrorImage look/deathEffect fields were
    @Packable
    public String[] heroLook = new String[0];
    @Packable
    public String heroDeathEffect;

    // survive Level.reset (statues stay, ordinary mobs are removed)
    private boolean persistOnReset = false;

    // NPC profile (npc: true in the mob def): static town-folk behavior —
    // steps off objects/stairs in act, never beckoned, immune to buffs, not
    // petable. Replaces the deleted java NPC base-class behavior.
    private boolean npc = false;

    // true once a mobsDesc json was applied to this mob (json stat formulas
    // diverge from the engine ones: raw dr field, range+LOS canAttack)
    private boolean dataDriven = false;

    // boss battle track, played while the boss is Hunting (java Boss parity)
    @Nullable
    private String battleMusic = "";

    @LuaInterface
    public void setDmgMax(int value) {
        dmgMax = value;
    }

    @LuaInterface
    public int getDmgMin() {
        return dmgMin;
    }

    @LuaInterface
    public int getDmgMax() {
        return dmgMax;
    }

    @LuaInterface
    public void setDmgMin(int value) {
        dmgMin = value;
    }

    @LuaInterface
    public void setDr(int value) {
        dr = value;
    }

    @LuaInterface
    public void setExpForKill(int value) {
        expForKill = value;
    }

    @LuaInterface
    public int getExpForKill() {
        return expForKill;
    }

    @LuaInterface
    public void setMaxLvl(int value) {
        maxLvl = value;
    }

    @LuaInterface
    public int getMaxLvl() {
        return maxLvl;
    }

    private Carcass carcassRef;

    // visual hint for MobSprite: a carcass replaced the corpse, fade it quicker
    private boolean droppedCarcass = false;

    public boolean droppedCarcass() {
        return droppedCarcass;
    }

    // caveman: remote-control watchdog - see RemoteControlled state.
    // remoteRevertAfter persists in the bundle; counters do not (reset on load).
    public static final String REMOTE_REVERT_AFTER = "remoteRevertAfter";

    @Packable(defaultValue = "0")
    public int remoteRevertAfter = 0;

    public transient int remoteIdleTurns = 0;
    public String remoteRevertStateTag = null;
    public boolean remoteReverted = false;

    public Mob() {
        super();
        mobClass = getClass().getSimpleName();
        // java-registered classes may carry a mobsDesc/<SimpleName>.json
        // profile (ServiceManNPC npc:true) - the Char-ctor fillMobStats ran
        // on "Unknown" and was a no-op
        fillMobStats(false);
        setupCharData();
        // explicit stat only: mobsDesc "baseStr" key; STR() in java class or
        // fillStats lua script overrides later in the ctor chain
        baseStr = getClassDef().optInt("baseStr", baseStr);
        getScript().run("fillStats");
        if (ModQuirks.mobLeveling) {
            lvl(Random.Int(1, (int) RemixedDungeon.getDifficultyFactor() + 1));
        }
    }

    // data-mob construction path (MobFactory): kind names the mobsDesc json
    public Mob(String mobClass) {
        this();
        this.mobClass = mobClass;
        fillMobStats(false);
        getScript().run("fillStats");
    }

    public void releasePet() {
        setFraction(Fraction.DUNGEON);
        setOwnerId(getId());
    }

    public void revertRemoteControl() {
        remoteIdleTurns = 0;
        remoteReverted = true;
        setCurAction(null);
        String tag = remoteRevertStateTag != null ? remoteRevertStateTag : "WANDERING";
        remoteRevertStateTag = null;
        remoteRevertAfter = 0;
        setState(MobAi.getStateByTag(tag));
    }

    public boolean isRemoteControlled() {
        return MobAi.getStateByClass(RemoteControlled.class)
            .getTag().equals(getState().getTag());
    }

    @LuaInterface
    @NotNull
    public static Mob makePet(@NotNull Mob pet, @NotNull Char owner) {
        return makePet(pet, owner.getId());
    }

    @LuaInterface
    @NotNull
    public Mob makePet(@NotNull Char owner) {
        return Mob.makePet(this, owner);
    }

    @NotNull
    public static Mob makePet(@NotNull Mob pet, int ownerId) {
        var owner = CharsList.getById(ownerId);
        if(owner.fraction()==Fraction.HEROES) {
            pet.expForKill = 0;
        }
        if (pet.canBePet()) {

            pet.setFraction(owner.fraction());
            pet.setOwnerId(ownerId);
        }
        return pet;
    }

    @Override
    public boolean followOnLevelChanged(InterlevelScene.Mode changeMode) {
        return getOwner() instanceof Hero;
    }

    public void setFraction(Fraction fr) {
        fraction = fr;
        setEnemy(CharsList.DUMMY);
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);

        bundle.put(STATE, getState().getTag());
        bundle.put(FRACTION, fraction.ordinal());
        bundle.put(REMOTE_REVERT_AFTER, remoteRevertAfter);
        bundle.put(KIND_TAG, kind);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {

        super.restoreFromBundle(bundle);

        kind = bundle.optInt(KIND_TAG, kind);

        String state = bundle.getString(STATE);
        setState(state);

        fraction = Fraction.values()[bundle.optInt(FRACTION, Fraction.DUNGEON.ordinal())];

        remoteRevertAfter = bundle.optInt(REMOTE_REVERT_AFTER, 0);
        remoteIdleTurns = 0; // watchdog counts live world turns only

        if (bundle.contains(LOOT)) { //pre 29.6 saves compatibility
            loot(bundle.get(LOOT), 1);
        }

        // java Boss fixup parity: a save predating the key must not brick the stair
        if (isBoss && !mobClass.equals(MobFactory.SHADOW_LORD) && getBelongings().getItem(SkeletonKey.class) == null) {
            collect(new SkeletonKey());
        }
    }

    @LuaInterface
    public void setState(String state) {
        setState(MobAi.getStateByTag(state));
    }

    protected int getKind() {
        return kind;
    }

    @Override
    public String getEntityKind() {
        return mobClass;
    }

    @SneakyThrows
    public CharSprite newSprite() {

        if (heroSprite) {
            if (heroLook.length > 0 && heroDeathEffect != null && !heroDeathEffect.isEmpty()) {
                return HeroSpriteDef.createHeroSpriteDef(heroLook, heroDeathEffect);
            }
            var item = getItemFromSlot(Belongings.Slot.WEAPON);
            if (!item.valid()) {
                item = getItemFromSlot(Belongings.Slot.ARMOR);
            }
            return HeroSpriteDef.createHeroSpriteDef(item);
        }

        if (spriteClass!= null && !spriteClass.isEmpty()) {
            return new MobSpriteDef(spriteClass, getKind());
        }

        String descName = "spritesDesc/" + getEntityKind() + ".json";
        if (ModdingMode.isResourceExist(descName) || ModdingMode.isAssetExist(descName)) {
            return new MobSpriteDef(descName, getKind());
        }

        throw new TrackedRuntimeException(String.format("sprite creation failed - me class %s sprite class %s", getEntityKind(), spriteClass));
    }

    @Override
    public void act() {
        if (npc) {
            int pos = getPos();

            ItemUtils.throwItemAway(pos);

            LevelObject levelObject = level().getTopLevelObject(pos);
            if (levelObject != null) {
                int newPos = level().getEmptyNonStairsCellNextTo(pos);
                if (level().cellValid(newPos) && newPos != pos) {
                    WandOfBlink.appear(this, newPos);
                }
            }

            if (Dungeon.hero != null) {
                getSprite().turnTo(pos, Dungeon.hero.getPos());
            }
        }

        if (isBoss && !battleMusic.isEmpty() && getState() instanceof Hunting) {
            MusicManager.INSTANCE.play(battleMusic, true);
        }

        super.act(); //Calculate FoV

        if (!isAlive()) {
            return; // died during buff/item processing — don't run AI on a corpse
        }

        getSprite().hideAlert();

        if (paralysed) {
            enemySeen = false;
            spend(TICK);
            return;
        }

        float timeBeforeAct = actorTime();
        StringBuilder tags = new StringBuilder();

        String aiTag = getState().getTag();

        tags.append(aiTag);

        int tryCount = 5;
        for (int i = 0;i < tryCount;i++) {
            GLog.debug("%s is %s", getEntityKind(), getState().getTag());
            getState().act(this);
            if(actorTime() != timeBeforeAct) {
                return;
            }
            String newTag = getState().getTag();
            if(aiTag.equals(newTag)) { //mob decided to do nothing, this is ok
                spend(TICK);
                return;
            }
            aiTag = newTag;
            tags.append(aiTag);
        }


        // Pack diagnostics INTO the exception message: Crashlytics only collects this string, no
        // separate logs, so the state sequence alone isn't enough to tell why no state spent time.
        Char enemy = getEnemy();
        StringBuilder buffNames = new StringBuilder();
        int shown = 0;
        for (Buff b : buffs()) {
            if (shown++ > 0) {
                buffNames.append(',');
            }
            buffNames.append(b.getEntityKind());
            if (shown >= 10) {
                break;
            }
        }
        var error = String.format(
                "actor %s really confused! states=%s enemy=%s enemyValid=%s humanoid=%s items=%d wands=%d speed=%.3f buffs=[%s]",
                getEntityKind(), tags,
                enemy == null ? "null" : enemy.getEntityKind(),
                enemy == null ? "null" : enemy.valid(),
                isHumanoid(), getBelongings().itemCount(), getBelongings().wandCount(),
                (double) speed(), buffNames.toString());
        spend(TICK);
        EventCollector.logException(error);
    }


    public void moveSprite(int from, int to) {

        if (getSprite().isVisible()
                && (Dungeon.isPathVisible(from, to))) {
            getSprite().move(from, to);
        } else {
            getSprite().place(to);
        }
    }

    @Override
    public boolean add(Buff buff) {
        if (npc) {
            // scripted NPCs (RatKing) may punch through the blanket immunity
            if (!getScript().runOptional("onAllowBuff", false, buff)) {
                return false;
            }
        }

        super.add(buff);

        if (!isOnStage()) {
            return true;
        }

        if (BuffFactory.AMOK.equals(buff.getEntityKind())) {
            getSprite().showStatus(CharSprite.NEGATIVE, TXT_RAGE);
            setState(MobAi.getStateByClass(RunningAmok.class));
        } else if (BuffFactory.TERROR.equals(buff.getEntityKind())) {
            setState(MobAi.getStateByClass(Horrified.class));
        } else if (BuffFactory.SLEEP.equals(buff.getEntityKind())) {
            new Flare(4, 32).color(0x44ffff, true).show(getSprite(), 2f);

            // Use regular Sleeping AI (it will handle pain immunity internally)
            setState(MobAi.getStateByClass(Sleeping.class));
            postpone(1.5f); // was Sleep.SWS
        }
        return true;
    }

    @Override
    @LuaInterface
    public boolean canAttack(@NotNull Char enemy) {

        if(friendly(enemy)) {
            return false;
        }

        // pacified mobs never attack (Mob.canAttack contract; an older
        // override used to silently drop the gate)
        if(pacified) {
            return false;
        }

        // script replaces the range+LOS check entirely (pumped Goo reach, ray attacks)
        LuaValue scripted = getScript().run("onCanAttack", enemy);
        if (scripted.isboolean()) {
            return scripted.toboolean();
        }

        if (!dataDriven) {
            return super.canAttack(enemy);
        }

        int enemyPos = enemy.getPos();
        int distance = level().distance(getPos(), enemyPos);

        return distance <= attackRange && Ballistica.cast(getPos(), enemyPos, false, true) == enemyPos;
    }

    // script takes the attack entirely (Goo pump: spends and poses itself)
    @Override
    @LuaInterface
    public void doAttack(Char enemy) {
        if (getScript().runOptional("onDoAttack", Boolean.FALSE, enemy)) {
            return;
        }
        super.doAttack(enemy);
    }

    @Override
    public int attackSkill(Char target) {
        LuaValue scripted = getScript().run("onAttackSkill", target);
        if (scripted.isnumber()) {
            return scripted.toint();
        }
        return super.attackSkill(target);
    }

    @Override
    @LuaInterface
    public int damageRoll() {
        LuaValue scripted = getScript().run("onDamageRoll");
        if (scripted.isnumber()) {
            return scripted.toint();
        }
        int dmg = Random.NormalIntRange(dmgMin, dmgMax) + Random.NormalIntRange(0, lvl());

        dmg += getActiveWeapon().damageRoll(this);

        if (!rangedWeapon.valid()) {
            dmg += getSecondaryWeapon().damageRoll(this);
        }

        return dmg;
    }

    // dynamic stats of owner-scaled summons (Deathling) live in the script
    @Override
    public int defenseSkill(Char enemy) {
        LuaValue scripted = getScript().run("onDefenseSkill", enemy);
        if (scripted.isnumber()) {
            return scripted.toint();
        }

        // pets fight their own battles: once they hold an enemy they keep full
        // evasion wherever the hero looks; a visible attacker is required,
        // unseen (invisible) ones keep the sneak hit
        if (isPet() && enemy.invisible <= 0
                && getEnemy().valid() && getEnemy().isAlive()) {
            return super.defenseSkill(enemy);
        }
        return enemySeen ? super.defenseSkill(enemy) : 0;
    }

    @Override
    public boolean friendly(@NotNull Char chr, int r_level) {

        if (friendly) {
            return true;
        }

        if (r_level > 7) {
            EventCollector.logException("too high r_level in Mob::friendly");
            return false;
        }

        if (chr == this) {
            return true;
        }

        if (hasBuff(BuffFactory.AMOK) || chr.hasBuff(BuffFactory.AMOK)) {
            return false;
        }

        if (getOwnerId() == chr.getId() || getId() == chr.getId()) {
            return true;
        }

        if (getEnemy() == chr) {
            return false;
        }

        if (getOwnerId() != getId()) {
            Char owner = getOwner();
            // Don't recurse into a stale/DUMMY owner (getOwner() returns DUMMY for unresolved ids)
            // or back into ourselves — both extend/cycle the chain and trip the r_level circuit-breaker.
            if (owner != this && owner.valid() && owner.friendly(chr, r_level + 1)) {
                return true;
            }
        }

        if (chr instanceof Hero) {
            if(chr.getHeroClass().friendlyTo(getEntityKind())) {
                return true;
            }
        }

        return super.friendly(chr, r_level);
    }

    @Override
    public float speed() {
        float base = super.speed();
        // terrain-conditional speeds (water/earth elementals) live in the script
        return (float) getScript().run("onSpeed", base).optdouble(base);
    }

    @Override
    public void damage(int dmg, @NotNull NamedEntityKind src) {
        if(immortal) {
            return;
        }

        // scripted mobs may consume the hit (RatKing anger gate)
        if (getScript().runOptional("onBlockDamage", false, dmg, src)) {
            return;
        }

        super.damage(dmg, src);
    }

    @Override
    public int dr() {
        LuaValue scripted = getScript().run("onDr");
        if (scripted.isnumber()) {
            return scripted.toint();
        }
        if (dataDriven) {
            // authored DR is the whole story for data mobs (no armor formula)
            return dr;
        }
        return getItemFromSlot(Belongings.Slot.ARMOR).effectiveDr() + dr + lvl() / 2;
    }

    @LuaInterface
    public boolean canBePet() {
        return canBePet;
    }

    public boolean getCloser(int target, boolean ignorePets) {
        // script hook may take the step (Succubus blink) - true = moved, false = java pathfind
        Boolean scriptStepped = getScript().runOptional("onGetCloser", Boolean.FALSE, target, ignorePets);
        if (Boolean.TRUE.equals(scriptStepped)) {
            return true;
        }

        int step = Dungeon.findPath(this, target, walkingType.passableCells(level()));
        return _doStep(step);
    }

    @LuaInterface
    public boolean getFurther(int target) {
        int step = Dungeon.flee(this, target, walkingType.passableCells(level()));
        return _doStep(step);
    }

    public boolean _doStep(int step) {
        if (level().cellValid(step)) {
            move(step);
            return true;
        }
        return false;
    }


    @Override
    public final void onZapComplete() {
        Char enemy = getEnemy();
        if(enemy.valid()) {
            zap(enemy);
        }
        super.onZapComplete();
    }

    @Override
    public void destroy() {
        level().mobs.remove(this);
        super.destroy();
    }

    @LuaInterface
    public void remove() {
        super.die(this);
    }

    // java Boss die-flow parity: level music back, banner, open the sealed stair
    @Override
    public void die(@NotNull NamedEntityKind cause) {
        if (isBoss) {
            GameScene.playLevelMusic();
            GameScene.bossSlain();
            level().unseal();
        }

        // quest kill counting lives in lua scripts now (mob.installOnDieCallback
        // - ScarecrowNPC.lua rat/gnoll gate is the last consumer)

        Badges.validateRare(this);

        Hero hero = Dungeon.hero;

        if (isPet()) { // pets die quietly otherwise - the hero often can't even see the fight
            GLog.n(Utils.format(R.string.Mob_PetDied, getName()));
        }

        if (!cause.getEntityKind().equals(Chasm.class.getSimpleName())) {
            hero.getBelongings().forEachEquipped(item -> item.charDied(this, hero));
        }

        if (hero.isAlive()) {
            if (!friendly(hero)) {
                Statistics.enemiesSlain++;
                Badges.validateMonstersSlain();
                Statistics.qualifiedForNoKilling = false;

                if (Dungeon.nightMode) {
                    Statistics.nightHunt++;
                    Badges.validateNightHunter();
                } else {
                    Statistics.nightHunt = 0;
                }

                if (!(cause instanceof Mob) || hero.getHeroClass() == HeroClass.NECROMANCER) {
                    if (hero.lvl() <= (maxLvl + lvl()) && expForKill > 0) {
                        hero.earnExp(expForKill);
                    }
                }
            }
        }

        super.die(cause);

        Library.identify(Library.MOB, getEntityKind());

        if (!(cause instanceof Chasm)) {
            // caveman: no carcass if the body just came back (BlackSkull), and
            // pets never leave one - reanimating a pet carcass is an undead factory
            if (!resurrectedOnDeath
                && getOwnerId() == getId()
                && Random.Float(1) <= carcassChance) {
                Item carcass = carcass();
                if (carcass.valid()) {
                    level().drop(carcass, getPos());
                    droppedCarcass = true;
                }
            }

            getBelongings().dropAll();
        }

        if (hero.isAlive() && !CharUtils.isVisible(this)) {
            GLog.i(StringsManager.getVar(R.string.Mob_Died));
        }
    }

    // boss intro yells etc; sprite alert already played by the base
    @Override
    public void notice() {
        super.notice();
        getScript().runOptionalNoRet("onNotice");
    }

    @LuaInterface
    public Mob split(int cell, int damage) {

        Mob clone = (Mob) makeClone();

        // caveman: split must not duplicate gear - Multiplicity glyph on pet armor
        // turned this into an enchanted-armor farm (precedent: Carcass.reanimate
        // clears belongings too)
        clone.getBelongings().clear();

        clone.hp(Math.max((hp() - damage) / 2, 1));
        clone.setPos(cell);
        clone.setState(MobAi.getStateByClass(Hunting.class));

        clone.ensureOpenDoor();

        level().spawnMob(clone, SPLIT_DELAY, getPos());

        return clone;
    }

    // split whose copy always turns hostile: a hero pet (Moongrace plant,
    // exploding Moongrace spider hit) leaves a feral copy, not a free minion
    @LuaInterface
    public Mob splitHostile(int cell, int damage) {
        Mob clone = split(cell, damage);
        clone.setOwnerId(clone.getId());
        if (clone.fraction() == Fraction.HEROES) {
            clone.setFraction(Fraction.DUNGEON);
        }
        return clone;
    }

    @LuaInterface
    public void resurrect() {
        resurrectAnim();

        int spawnPos = getPos();
        Mob new_mob = MobFactory.mobByName(getEntityKind());

        if (level().cellValid(spawnPos)) {
            new_mob.setPos(spawnPos);
            level().spawnMob(new_mob);
            level().press(spawnPos, new_mob);
        }
    }

    // caveman: set when this dying body spawned a replacement (BlackSkull) -
    // suppresses the carcass roll in die(), not persisted
    private boolean resurrectedOnDeath = false;

    @Override
    public void resurrect(Char parent) {

        int spawnPos = getPos();
        Mob new_mob = MobFactory.mobByName(getEntityKind());

        if (level().cellValid(spawnPos)) {
            new_mob.setPos(spawnPos);
            Mob.makePet(new_mob, parent.getId());
            new_mob.setUndead(true); // caveman: skull-raised pets are undead - no combat XP, same as necromancy
            Actor.addDelayed(new Pushing(new_mob, parent.getPos(), new_mob.getPos()), -1);
            level().spawnMob(new_mob);
            level().press(spawnPos, new_mob);
            resurrectedOnDeath = true;
        }
    }

    public boolean reset() {
        return persistOnReset;
    }

	@LuaInterface
	public void beckon(int cell) {
		// furniture opts out via movable:false; NPCs are never beckoned
		if (npc || !movable) {
			return;
		}
		notice();
		setState(MobAi.getStateByClass(Wandering.class));
		setTarget(cell);
	}

    @SneakyThrows
    public void fromJson(JSONObject mobDesc) {
        Bundle descBundle = new Bundle();
        BundleHelper.Pack(this, descBundle);
        descBundle.mergeWith(mobDesc);
        BundleHelper.UnPack(this, descBundle);

        if (mobDesc.has(LOOT)) {
            JSONObject lootDesc = mobDesc.getJSONObject(LOOT);
            if (lootDesc.has("category")) {
                // category roll at creation: item rides in the inventory
                // and drops with the corpse (Monk food sack)
                if (Random.Float() <= mobDesc.optDouble("lootChance", 1f)) {
                    collect(Treasury.getLevelTreasury().random(Treasury.Category.valueOf(lootDesc.getString("category"))));
                }
            } else {
                float lootChance = (float) mobDesc.optDouble("lootChance", 1f);
                loot(ItemFactory.createItemFromDesc(lootDesc), lootChance);
            }
        }

        if(mobDesc.has("undead")) {
            if(mobDesc.getBoolean("undead")) {
                setUndead(true);
                naturalUndead = true; // authored as an undead creature - not a necromancy artifact
            }
        }

        getBelongings().setupFromJson(mobDesc);

        if (this instanceof IDepthAdjustable) {
            ((IDepthAdjustable) this).adjustStats(mobDesc.optInt("level", 1));
        }

        setState(mobDesc.optString("aiState", getState().getTag()));
    }

    @LuaInterface
    public AiState getState() {
        return state;
    }


    public boolean isPet() {
        return fraction == Fraction.HEROES;
    }

    @Override
    public boolean friendly(@NotNull Char chr) {
        return friendly(chr, 0);
    }

    @SneakyThrows
    @Override
    protected void fillMobStats(boolean restoring) {
        JSONObject classDesc = getClassDef();
        if(! classDesc.keys().hasNext()) {
            GLog.debug("No mob def: " + mobClass);
            return;
        }

        dataDriven = true;
        canBePet = false;

        baseDefenseSkill = classDesc.optInt("defenseSkill", baseDefenseSkill);
        baseAttackSkill = classDesc.optInt("attackSkill", attackSkill);

        expForKill = classDesc.optInt("exp", expForKill);
        maxLvl = classDesc.optInt("maxLvl", maxLvl);
        dmgMin = classDesc.optInt("dmgMin", dmgMin);
        dmgMax = classDesc.optInt("dmgMax", dmgMax);

        dr = classDesc.optInt("dr", dr);

        baseStr = classDesc.optInt("str", baseStr);

        baseSpeed = (float) classDesc.optDouble("baseSpeed", baseSpeed);
        attackDelay = (float) classDesc.optDouble("attackDelay", attackDelay);

        spriteClass = classDesc.optString("spriteDesc", "spritesDesc/Rat.json");

        flying = classDesc.optBoolean("flying", flying);

        setViewDistance(classDesc.optInt("viewDistance", getViewDistance()));

        walkingType = Enum.valueOf(WalkingType.class, classDesc.optString("walkingType","NORMAL"));

        canBePet = classDesc.optBoolean("canBePet",canBePet);

        attackRange = classDesc.optInt("attackRange",attackRange);
        isBoss = classDesc.optBoolean("isBoss",isBoss);
        if (isBoss) {
            // java Boss ctor semantics: uncapturable, death/psionic-blast proof
            canBePet = false;
            addResistance(Death.class);
            addResistance(ScrollOfPsionicBlast.class);
        }

        battleMusic = classDesc.optString("battleMusic", "");
        if (!battleMusic.isEmpty() && !ModdingMode.isSoundExists(battleMusic)) {
            battleMusic = classDesc.optString("battleMusicFallback", "");
        }

        String scriptFile = classDesc.optString("scriptFile","");
        if(!scriptFile.isEmpty()) {
            script = new LuaScript(scriptFile, this);
            script.asInstance();
        }

        friendly = classDesc.optBoolean("friendly",friendly);
        movable = classDesc.optBoolean("movable",movable);
        immortal = classDesc.optBoolean("immortal",immortal);
        pacified = classDesc.optBoolean("pacified",pacified);

        spriteLayer = classDesc.optInt("spriteLayer",spriteLayer);

        humanoid = classDesc.optBoolean("isHumanoid", humanoid);

        heroSprite = classDesc.optBoolean("heroSprite", heroSprite);

        persistOnReset = classDesc.optBoolean("persistOnReset", persistOnReset);

        npc = classDesc.optBoolean("npc", npc);
        if (npc) {
            canBePet = false;
        }

        kind = classDesc.optInt("var", kind);
        carcassChance = (float) classDesc.optDouble("carcassChance", carcassChance);
        hasBodyParts = classDesc.optBoolean("hasBodyParts", hasBodyParts);

        JsonHelper.readStringSet(classDesc, Char.IMMUNITIES, immunities);
        JsonHelper.readStringSet(classDesc, Char.RESISTANCES, resistances);

        if(!restoring) {
            setFraction(Enum.valueOf(Fraction.class, classDesc.optString("fraction","DUNGEON")));
            hp(ht(classDesc.optInt("ht", 1)));
            fromJson(classDesc);

            if (isBoss && !mobClass.equals(MobFactory.SHADOW_LORD)) {
                // bosses carry the SkeletonKey that drops with their gear;
                // ShadowLord never did (java Boss.restoreFromBundle exclusion)
                collect(new SkeletonKey());
            }
        }
    }

    // hero-interaction hooks (steal etc.): java Crystal.onActionTarget parity
    public void setHeroLook(String[] look, String deathEffect) {
        heroLook = look;
        heroDeathEffect = deathEffect;
    }

    @Override
    public void onActionTarget(String action, Char actor) {
        getScript().runOptionalNoRet("onActionTarget", action, actor);
        super.onActionTarget(action, actor);
    }

    @Override
    public int getSpriteLayer() {
        return spriteLayer;
    }

    @LuaInterface
    public boolean isHumanoid() {
        return humanoid;
    }

    // Mob stores the flag as a field; the isBoss() method lives on Char only
    @LuaInterface
    @Override
    public boolean isBoss() {
        return isBoss;
    }

    @LuaInterface
    @Override
    public boolean isNpc() {
        return npc;
    }

    @LuaInterface
    @Override
    public boolean hasBodyParts() {
        return hasBodyParts;
    }

    @Override
    public boolean zap(@NotNull Char enemy) {

        // script hook may take the zap entirely (no-damage controller zaps,
        // custom beams) - true = handled, false = base damage flow
        Boolean scriptZapped = getScript().runOptional("onZap", Boolean.FALSE, enemy);
        if (Boolean.TRUE.equals(scriptZapped)) {
            return true;
        }

        if (enemy.valid()) {
            if (zapHit(enemy)) {
                int damage = zapProc(enemy, damageRoll());
                int effectiveDamage = enemy.defenseProc(this, damage);

                enemy.damage(effectiveDamage, this);
                return true;
            } else {
                zapMiss(enemy);
            }
        }
        return false;
    }

    @LuaInterface
    public boolean zapHit(@NotNull Char enemy) {
        if (enemy == CharsList.DUMMY) {
            EventCollector.logException(String.format("%s zapping dummy enemy", getEntityKind()));
            return false;
        }

        if (!level().cellValid(enemy.getPos())) {
            EventCollector.logException(getEntityKind() + " zapping " + enemy.getEntityKind() + " on invalid cell");
            return false;
        }

        if (CharUtils.hit(this, enemy, true)) {
            return true;
        } else {
            enemy.showStatus(CharSprite.NEUTRAL, enemy.defenseVerb());
            return false;
        }
    }

    @Nullable
    @LuaInterface
    public Object getLoot() {
        return getBelongings().randomUnequipped();
    }

    @Override
    public Char makeClone() {

        Bundle storedMob = new Bundle();
        storeInBundle(storedMob);
        Mob new_mob = MobFactory.mobByName(getEntityKind());
        new_mob.setPos(getPos());
        new_mob.restoreFromBundle(storedMob);
        new_mob.getId(); //Ensure valid id

        // bare storeInBundle above skips @Packable fields - keep undead state and kill reward across clones
        new_mob.setUndead(undead);
        new_mob.naturalUndead = naturalUndead;
        new_mob.expForKill = expForKill;

        if (getOwnerId() == getId()) {
            new_mob.setOwnerId(new_mob.getId());
        } else {
            new_mob.setOwnerId(getOwnerId());
        }

        return new_mob;
    }

    @LuaInterface
    public void loot(Object loot, float lootChance) {

        if (Dungeon.hero.lvl() > maxLvl + 2 + lvl() && !isBoss) {
            return;
        }

        if (loot != null && Random.Float() <= lootChance) {
            Item item;
            if (loot instanceof Treasury.Category) {
                item = Treasury.getLevelTreasury().random((Treasury.Category) loot);
            } else if (loot instanceof Class<?>) {
                item = Treasury.getLevelTreasury().random((Class<? extends Item>) loot);
            } else if (loot instanceof String) {
                item = Treasury.getLevelTreasury().random((String) loot);
            } else {
                item = (Item) loot;
            }
            collect(item);
        }
    }

    @Override
    public void earnExp(int exp) {
        int old_lvl = lvl();
        super.earnExp(exp);
        if (!Dungeon.isLoading()) {
            if (level().cellValid(getPos())) {
                if (lvl() >= 5 && lvl() != old_lvl) {
                    if (!hasBuff(BuffFactory.CHAMPION_OF_EARTH) && !hasBuff(BuffFactory.CHAMPION_OF_FIRE)
                            && !hasBuff(BuffFactory.CHAMPION_OF_WATER) && !hasBuff(BuffFactory.CHAMPION_OF_AIR)) {

                        String[] champions = {BuffFactory.CHAMPION_OF_EARTH, BuffFactory.CHAMPION_OF_FIRE, BuffFactory.CHAMPION_OF_WATER, BuffFactory.CHAMPION_OF_AIR};

                        Buff.permanent(this, Random.oneOf(champions));
                    }
                }
            }
        }

    }

    @Override
    @LuaInterface
    public Item carcass() {
        if(carcassRef != null) {
            return carcassRef;
        }
        carcassRef = new Carcass(this);
        return carcassRef;
    }

    public void revive() {
        carcassRef = null;
        droppedCarcass = false;

    }

    public void adjustStats(int depth) {
    }

    @Override
    @NotNull
    public Set<Belongings.Slot> getAvailableEquipmentSlots() {
        if (isHumanoid()) {
            return EnumSet.of(Belongings.Slot.WEAPON, Belongings.Slot.ARMOR,
                    Belongings.Slot.LEFT_HAND, Belongings.Slot.ARTIFACT, Belongings.Slot.LEFT_ARTIFACT);
        }
        return Collections.emptySet();
    }
}
