
package com.watabou.pixeldungeon.actors.hero;

import com.nyrds.LuaInterface;
import com.nyrds.Packable;
import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Sleeping;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.nyrds.pixeldungeon.utils.EntityIdSource;
import com.nyrds.pixeldungeon.utils.ItemsList;
import com.nyrds.pixeldungeon.utils.Position;
import com.nyrds.pixeldungeon.windows.MovieRewardTask;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.ModdingMode;
import com.nyrds.util.Scrambler;
import com.watabou.noosa.Camera;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Bones;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Facilitations;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Bleeding;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Charm;
import com.watabou.pixeldungeon.actors.buffs.Cripple;
import com.watabou.pixeldungeon.actors.buffs.Fury;
import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.actors.buffs.ManaRegeneration;
import com.watabou.pixeldungeon.actors.buffs.Ooze;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.actors.buffs.Regeneration;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.actors.buffs.Vertigo;
import com.watabou.pixeldungeon.actors.buffs.Weakness;
import com.watabou.pixeldungeon.actors.mobs.Fraction;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.npcs.MirrorImage;
import com.watabou.pixeldungeon.effects.CheckedCell;
import com.watabou.pixeldungeon.effects.SpellSprite;
import com.watabou.pixeldungeon.items.Ankh;
import com.watabou.pixeldungeon.items.DewVial;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.items.food.Food;
import com.watabou.pixeldungeon.items.potions.PotionOfStrength;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfRecharging;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.levels.features.Chasm;
import com.watabou.pixeldungeon.levels.traps.TrapHelper;
import com.watabou.pixeldungeon.scenes.CellSelector;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.InterlevelScene;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.HeroSpriteDef;
import com.watabou.pixeldungeon.ui.AttackIndicator;
import com.watabou.pixeldungeon.ui.QuickSlot;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.windows.WndResurrect;
import com.watabou.pixeldungeon.windows.WndSaveSlotSelect;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.watabou.utils.SystemTime;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class Hero extends Char {
    private static final String TXT_EXP = "%+dEXP";

    private static final int STARTING_STR = 10;

    private static final float TIME_TO_REST = 1f;
    private static final float TIME_TO_SEARCH = 2f;

    @Nullable
    static public Runnable doOnNextAction;

    public Set<Mob> initialAlies = new HashSet<>();

    private HeroClass heroClass = HeroClass.ROGUE;
    private HeroSubClass subClass = HeroSubClass.NONE;

    private boolean ready = false;

    @Packable(defaultValue = "-1")//EntityIdSource.INVALID_ID
    private int controlTargetId;

    @Packable
    public static boolean movieRewardPending;

    @Packable
    public static Item movieRewardPrize = ItemsList.DUMMY;

    public Armor.Glyph killerGlyph = null;

    public boolean restoreHealth = false;

    private float awareness;

    private int scrambledExp = Scrambler.scramble(0);
    private int sp = Scrambler.scramble(0);
    private int maxSp = Scrambler.scramble(0);

    @Packable(defaultValue = "unknown")
    public String levelId;

    @Packable
    public Position portalLevelPos;

    private int difficulty;

    public Hero() {
        super();
        setupCharData();

        fraction = Fraction.HEROES;

        STR(STARTING_STR);
        awareness = 0.1f;
        baseDefenseSkill = 5;
        baseAttackSkill = 10;

        controlTargetId = getId();
    }

    public Hero(int difficulty) {
        this();
        setDifficulty(difficulty);

        if (getDifficulty() != 0) {
            hp(ht(20));
        } else {
            hp(ht(30));
        }
        live();
    }

    public int effectiveSTR() {
        int weaknessLevel = buffLevel(BuffFactory.WEAKNESS);
        return (int) Math.max(STR() - Math.log(Math.pow(weaknessLevel + 1, RemixedDungeon.getDifficultyFactor())), 1);
    }

    public void STR(int sTR) {
        baseStr = Scrambler.scramble(sTR);
    }

    public int STR() {
        return Scrambler.descramble(baseStr);
    }

    private static final String STRENGTH = "STR";
    private static final String EXPERIENCE = "exp";
    public static final String DIFFICULTY = "difficulty";
    private static final String SP = "sp";
    private static final String MAX_SP = "maxsp";
    private static final String MAGIC_LEVEL = "magicLvl";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);

        heroClass.storeInBundle(bundle);
        subClass.storeInBundle(bundle);

        bundle.put(STRENGTH, STR());

        bundle.put(EXPERIENCE, getExpForLevelUp());
        bundle.put(DIFFICULTY, getDifficulty());


        bundle.put(SP, getSkillPoints());
        bundle.put(MAX_SP, getSkillPointsMax());

        bundle.put(MAGIC_LEVEL, skillLevel());
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);

        if (controlTargetId == EntityIdSource.INVALID_ID) {
            controlTargetId = getId();
        }

        setHeroClass(HeroClass.restoreFromBundle(bundle));
        setSubClass(HeroSubClass.restoreFromBundle(bundle));

        STR(bundle.getInt(STRENGTH));
        updateAwareness();

        setExp(bundle.getInt(EXPERIENCE));
        setDifficulty(bundle.optInt(DIFFICULTY, 2));

        sp = Scrambler.scramble(bundle.optInt(SP, 0));
        maxSp = Scrambler.scramble(bundle.optInt(MAX_SP, 10));

        setSkillLevel(bundle.getInt(MAGIC_LEVEL));
    }

    @Override
    public int getGender() {
        return heroClass.getGender();
    }

    public String className() {
        return subClass == null || subClass == HeroSubClass.NONE ? heroClass.title() : subClass.title();
    }

    private void live() {

        Buff.affect(this, ManaRegeneration.class);
        Buff.affect(this, Regeneration.class);
        if (!Dungeon.isFacilitated(Facilitations.NO_HUNGER)) {
            Buff.affect(this, Hunger.class);
        }
    }

    @Override
    public int defenseSkill(Char enemy) {

        float skillFactor = 1;
        if (getDifficulty() == 0) {
            skillFactor *= 1.2f;
        }

        return (int) (super.defenseSkill(enemy) * skillFactor);
    }

    @Override
    public int attackSkill(Char target) {

        float attackSkillFactor = 1;
        if (getDifficulty() == 0) {
            attackSkillFactor *= 1.2;
        }

        return (int) (super.attackSkill(target) * attackSkillFactor);
    }


    @Override
    public int dr() {
        return Math.max(getItemFromSlot(Belongings.Slot.ARMOR).effectiveDr(), 0);
    }

    @Override
    public float speed() {

        int aEnc = getItemFromSlot(Belongings.Slot.ARMOR).requiredSTR() - effectiveSTR();
        if (aEnc > 0) {
            return (float) (super.speed() * Math.pow(1.3, -aEnc));
        } else {
            float speed = super.speed();
            return getHeroSprite().sprint(subClass == HeroSubClass.FREERUNNER && !isStarving()) ? 1.6f * speed : speed;
        }
    }

    @Override
    public void spendAndNext(float time) {
        busy();
        super.spendAndNext(time);
    }

    @Override
    public boolean act() {
        if (controlTargetId == getId()) {
            super.act();
        }

        observe();

        if (paralysed) {
            setCurAction(null);
            spendAndNext(TICK);
            return false;
        }

        if (controlTargetId != getId()) {
            setCurAction(null);
        }

        if (getCurAction() == null) {
            if (restoreHealth) {
                if (isStarving() || hp() >= ht() || level().isSafe()) {
                    restoreHealth = false;
                } else {
                    spend(TIME_TO_REST);
                    next();
                    return false;
                }
            }

            if (Dungeon.realtime() ||
                    (controlTargetId != getId() && getControlTarget().getCurAction() != null)) {
                if (!ready) {
                    readyAndIdle();
                }
                spend(TICK);
                next();
            } else {
                readyAndIdle();
            }
            return false;
        }

        SystemTime.updateLastActionTime();

        restoreHealth = false;
        if (!Dungeon.realtime()) {
            busy();
        }

        GLog.debug("action: %s", getCurAction());

        return getCurAction().act(this);
    }

    @Override
    public void busy() {
        GLog.debug("busy");
        ready = false;
    }

    public void readyAndIdle() {
        setCurAction(null);
        ready = true;

        GameScene.ready();
        getSprite().idle();
    }

    @Override
    public void interrupt() {
        if (getCurAction() != null && getCurAction().dst != getPos()) {
            lastAction = getCurAction();
        }

        setCurAction(null);
    }

    public void resume() {
        setCurAction(lastAction);
        lastAction = null;

        getControlTarget().setCurAction(getCurAction());
        getControlTarget().act();
    }

    public void itemPickedUp(Item item) {
        if (item.announcePickUp()) {
            if ((item instanceof ScrollOfUpgrade && ((ScrollOfUpgrade) item).isKnown())
                    || (item instanceof PotionOfStrength && ((PotionOfStrength) item).isKnown())) {
                GLog.p(StringsManager.getVar(R.string.Hero_YouNowHave), item.name());
            } else {
                GLog.i(StringsManager.getVar(R.string.Hero_YouNowHave), item.name() );
            }
        }
    }

    @Override
    public Char makeClone() {
        return new MirrorImage(this);
    }

    public void rest(boolean tillHealthy) {
        spendAndNext(TIME_TO_REST);
        if (!tillHealthy) {
            getSprite().showStatus(CharSprite.DEFAULT, StringsManager.getVar(R.string.Hero_Wait));
        }
        restoreHealth = tillHealthy;
    }

    @Override
    public void damage(int dmg, @NotNull NamedEntityKind src) {
        if (!isAlive()) {
            return;
        }

        restoreHealth = false;
        super.damage(dmg, src);


        setControlTarget(this);

        if (!myMove()) {
            interrupt();
        }

        for (Item item : getBelongings()) {
            if (item.isEquipped(this)) {
                if (!(src instanceof Hunger)) {
                    item.ownerTakesDamage(dmg);
                }
            }
        }
    }

    @Override
    public boolean checkVisibleEnemies() {
        boolean newMob = super.checkVisibleEnemies();

        if (newMob) {
            interrupt();
            restoreHealth = false;
        }

        AttackIndicator.updateState(this);
        return newMob;
    }

    @Override
    protected boolean getFurther(int cell) {
        return false;
    }

    @Override
    public void handle(int cell) {

        if (doOnNextAction != null) {
            doOnNextAction.run();
            doOnNextAction = null;
            return;
        }

        if (movieRewardPending) {
            new MovieRewardTask(true).run();
            movieRewardPending = false;
            return;
        }

        if (!isReady()) {
            return;
        }

        Level level = level();

        if (!level.cellValid(cell)) {
            return;
        }

        level.updateFieldOfView(getControlTarget());

        var action = CharUtils.actionForCell(this, cell, level);
        if (action.valid()) {
            nextAction(action);
        }
    }

    @Override
    public void earnExp(int exp) {

        this.setExp(this.getExpForLevelUp() + exp);

        showStatus(CharSprite.POSITIVE, TXT_EXP, exp);

        boolean levelUp = false;

        while (this.getExpForLevelUp() >= expToLevel()) {
            this.setExp(this.getExpForLevelUp() - expToLevel());
            lvl(lvl() + 1);

            EventCollector.levelUp(heroClass.name() + "_" + subClass.name(), lvl());

            ht(ht() + 5);
            heal(5, this);

            if (lvl() < 10) {
                updateAwareness();
            }

            levelUp = true;
        }

        if (levelUp) {

            GLog.p(StringsManager.getVar(R.string.Hero_NewLevel), lvl());
            getSprite().showStatus(CharSprite.POSITIVE, StringsManager.getVar(R.string.Hero_LevelUp));
            Sample.INSTANCE.play(Assets.SND_LEVELUP);

            if (getSkillPointsMax() > 0) {
                setMaxSkillPoints(getSkillPointsMax() + 1);
                accumulateSkillPoints(getSkillPointsMax() / 3);
            }

            if (lvl() % 5 == 0 && heroClass == HeroClass.GNOLL) {
                skillLevelUp();
            }

            Badges.validateLevelReached();
        }

        if (subClass == HeroSubClass.WARLOCK) {
            int value = Math.min(ht() - hp(), 1 + (Dungeon.depth - 1) / 5);
            heal(value, this);
            hunger().satisfy(10);
        }
    }

    @Override
    public int expToLevel() {
        if (getDifficulty() != 0) {
            return 5 + lvl() * 5;
        } else {
            return 5 + lvl() * 4;
        }
    }

    void updateAwareness() {
        awareness = (float) (1 - Math.pow((heroClass == HeroClass.ROGUE ? 0.85 : 0.90), (1 + Math.min(lvl(), 9)) * 0.5));
    }

    @Override
    public boolean add(Buff buff) {
        super.add(buff);

        if (!isOnStage()) {
            return false;
        }

        if (buff instanceof Burning) {
            GLog.w(StringsManager.getVar(R.string.Hero_StaBurning));
            interrupt();
        } else if (buff instanceof Paralysis) {
            GLog.w(StringsManager.getVar(R.string.Hero_StaParalysis));
            interrupt();
        } else if (buff instanceof Poison) {
            GLog.w(StringsManager.getVar(R.string.Hero_StaPoison));
            interrupt();
        } else if (buff instanceof Ooze) {
            GLog.w(StringsManager.getVar(R.string.Hero_StaOoze));
        } else if (buff instanceof Roots) {
            GLog.w(StringsManager.getVar(R.string.Hero_StaRoots));
        } else if (buff instanceof Weakness) {
            GLog.w(StringsManager.getVar(R.string.Hero_StaWeakness));
        } else if (buff instanceof Blindness) {
            GLog.w(StringsManager.getVar(R.string.Hero_StaBlindness));
        } else if (buff instanceof Fury) {
            GLog.w(StringsManager.getVar(R.string.Hero_StaFury));
            getSprite().showStatus(CharSprite.POSITIVE, StringsManager.getVar(R.string.Hero_StaFurious));
        } else if (buff instanceof Charm) {
            GLog.w(StringsManager.getVar(R.string.Hero_StaCharm));
        } else if (buff instanceof Cripple) {
            GLog.w(StringsManager.getVar(R.string.Hero_StaCripple));
        } else if (buff instanceof Bleeding) {
            GLog.w(StringsManager.getVar(R.string.Hero_StaBleeding));
        } else if (buff instanceof Vertigo) {
            GLog.w(StringsManager.getVar(R.string.Hero_StaVertigo));
            interrupt();
        }

        return true;
    }

    @Override
    public void die(@NotNull NamedEntityKind cause) {

        Map<String, String> deathDesc = new HashMap<>();

        deathDesc.put("class", heroClass.name());
        deathDesc.put("subClass", subClass.name());
        deathDesc.put("level", Dungeon.level.levelId);
        deathDesc.put("cause", cause.getEntityKind());
        deathDesc.put("duration", Float.toString(Statistics.duration));

        deathDesc.put("difficulty", Integer.toString(GameLoop.getDifficulty()));
        deathDesc.put("version", Game.version);
        deathDesc.put("mod", ModdingMode.activeMod());
        deathDesc.put("modVersion", Integer.toString(ModdingMode.activeModVersion()));

        deathDesc.put("donation", Integer.toString(GamePreferences.donated()));
        deathDesc.put("heroLevel", Integer.toString(lvl()));
        deathDesc.put("gameId", Dungeon.gameId);


        EventCollector.logEvent("HeroDeath", deathDesc);

        clearActions();

        if (DewVial.autoDrink(this)) {
            resurrectAnim();
            return;
        }

        if (getSubClass() == HeroSubClass.LICH && getSkillPoints() == getSkillPointsMax()) {
            setSkillPoints(0);
            GameScene.show(new WndResurrect(null, cause));
            return;
        }

        Actor.fixTime();
        super.die(cause);

        if (!Ankh.resurrect(this, cause)) {
            Dungeon.deleteGame(false);
            Hero.reallyDie(this, cause);
        }
    }

    public void clearActions() {
        setCurAction(null);
        lastAction = null;
    }

    public static void reallyDie(Hero hero, final NamedEntityKind cause) {

        if (hero.getDifficulty() < 2 && !Game.isPaused()) {
            GameScene.show(new WndSaveSlotSelect(false, StringsManager.getVar(R.string.Hero_AnotherTry)));
            return;
        }

        Dungeon.level.discover();

        Bones.leave(hero);

        Dungeon.observe();

        hero.getBelongings().identify();

        GameScene.gameOver();

        if (cause instanceof Doom) {
            ((Doom) cause).onHeroDeath();
        }

        Dungeon.gameOver();
    }

    @Override
    public void move(int step) {
        super.move(step);

        if (!isFlying()) {

            if (level().water[getPos()]) {
                Sample.INSTANCE.play(Assets.SND_WATER, 1, 1, Random.Float(0.8f, 1.25f));
            } else {
                Sample.INSTANCE.play(Assets.SND_STEP);
            }
        }
    }

    @Override
    public void onMotionComplete() {
        observe();
        search(false);

        super.onMotionComplete();
    }

    @Override
    public void onAttackComplete() {
        super.onAttackComplete();

        AttackIndicator.target(getEnemy());
        setEnemy(CharsList.DUMMY);
    }

    public boolean search(boolean intentional) {

        boolean smthFound = false;

        int distance = 1 + buffLevel(BuffFactory.RING_OF_DETECTION);

        float searchLevel = intentional ? (2 * awareness - awareness * awareness) : awareness;
        if (distance <= 0) {
            searchLevel /= 2 - distance;
            distance = 1;
        }

        Level level = Dungeon.level;

        int cx = level.cellX(getPos());
        int cy = level.cellY(getPos());
        int ax = cx - distance;
        if (ax < 0) {
            ax = 0;
        }
        int bx = cx + distance;
        if (bx >= level.getWidth()) {
            bx = level.getWidth() - 1;
        }
        int ay = cy - distance;
        if (ay < 0) {
            ay = 0;
        }
        int by = cy + distance;
        if (by >= level.getHeight()) {
            by = level.getHeight() - 1;
        }

        for (int y = ay; y <= by; y++) {
            for (int x = ax, p = ax + y * level.getWidth(); x <= bx; x++, p++) {

                if (Dungeon.isCellVisible(p)) {

                    if (intentional) {
                        GameScene.addToMobLayer(new CheckedCell(p));
                    }

                    if (intentional || Random.Float() < searchLevel) {

                        if (level.secret[p]) {
                            int oldValue = level.map[p];
                            GameScene.discoverTile(p);
                            level.set(p, Terrain.discover(oldValue));
                            GameScene.updateMapPair(p);
                            ScrollOfMagicMapping.discover(p);
                            smthFound = true;
                        }

                        LevelObject obj = level.getTopLevelObject(p);
                        if (obj != null && obj.secret()) {
                            obj.discover();
                            ScrollOfMagicMapping.discover(p);
                            smthFound = true;
                        }
                    }
                }
            }
        }

        if (intentional) {
            getSprite().showStatus(CharSprite.DEFAULT, StringsManager.getVar(R.string.Hero_Search));

            float time = TIME_TO_SEARCH;
            if (smthFound) {
                time = Random.Float() < searchLevel ? TIME_TO_SEARCH : TIME_TO_SEARCH * 2;
            }

            doOperate(time);
        }

        if (smthFound) {
            GLog.w(StringsManager.getVar(R.string.Hero_NoticedSmth));
            Sample.INSTANCE.play(Assets.SND_SECRET);
            interrupt();
        }

        return smthFound;
    }

    public void resurrect() {
        hp(ht());
        setExp(0);

        live();
    }

    @Override
    public Set<String> resistances() {
        Set<String> resistances = super.resistances();
        resistances.addAll(heroClass.resistances());
        resistances.addAll(subClass.resistances());
        return resistances;
    }

    @Override
    public Set<String> immunities() {
        Set<String> immunities = super.immunities();
        immunities.addAll(heroClass.immunities());
        immunities.addAll(subClass.immunities());
        return immunities;
    }

    @Override
    public String getDescription() {
        return getHeroClass().getDescription();
    }

    @Override
    public CharSprite newSprite() {
        return HeroSpriteDef.createHeroSpriteDef(this);
    }

    public HeroSpriteDef getHeroSprite() {
        return (HeroSpriteDef) getSprite();
    }

    @Override
    public CharSprite getSprite() {
        CharSprite sprite = super.getSprite();
        sprite.setVisible(true);
        return sprite;
    }

    public int getExpForLevelUp() {
        return Scrambler.descramble(scrambledExp);
    }

    public void setExp(int exp) {
        this.scrambledExp = Scrambler.scramble(exp);
    }

    @Override
    public void eat(@NotNull Item food, float energy, String message) {
        food.detach(getBelongings().backpack);

        hunger().satisfy(energy);

        GLog.i(message);

        switch (heroClass) {
            case WARRIOR:
                if (hp() < ht()) {
                    heal(5, food);
                }
                break;
            case MAGE:
                getBelongings().charge(false);
                ScrollOfRecharging.charge(this);
                break;
            default:
                break;
        }

        doOperate(Food.TIME_TO_EAT);

        SpellSprite.show(this, SpellSprite.FOOD);
        Sample.INSTANCE.play(Assets.SND_EAT);

        Statistics.foodEaten++;
        Badges.validateFoodEaten();
    }

    @Override
    public void setControlTarget(Char controlTarget) {
        if (getControlTarget().valid()){
            if (getControlTarget() instanceof Mob) {
                Mob controlledMob = (Mob) getControlTarget();
                controlledMob.releasePet();
                controlledMob.setState(MobAi.getStateByClass(Sleeping.class));
            }
            Camera.main.focusOn(controlTarget.getSprite());
        }
        this.controlTargetId = controlTarget.getId();

    }

    @NotNull
    @Override
    public Char getControlTarget() {

        Char controlTarget = CharsList.getById(controlTargetId);
        if (controlTarget.invalid()) {
            controlTargetId = getId();
            return this;
        }

        return controlTarget;
    }

    public float getAwareness() {
        return awareness;
    }

    @Override
    public HeroClass getHeroClass() {
        return heroClass;
    }

    public void setHeroClass(HeroClass heroClass) {
        EventCollector.setSessionData("class", heroClass.name());
        this.heroClass = heroClass;
    }

    @Override
    public HeroSubClass getSubClass() {
        return subClass;
    }

    @LuaInterface
    public void setSubClass(String subClass) { //To conceal strange behavior in RemixedRPG
        EventCollector.setSessionData("subclass", subClass);
        EventCollector.logException("RemixedPRG setSubClass bug");
        try {
            setSubClass(HeroSubClass.valueOf(subClass));
        } catch (IllegalArgumentException e) {
            setSubClass(HeroSubClass.NONE);
        }
    }

    public void setSubClass(HeroSubClass subClass) {
        EventCollector.setSessionData("subClass", subClass.name());
        this.subClass = subClass;
    }

    @Override
    public void updateSprite() {
        super.updateSprite();
        getHeroSprite().heroUpdated(this);
        readyAndIdle();
    }

    public boolean isReady() {
        return isAlive() && ready;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
        GameLoop.setDifficulty(difficulty);
    }

    public void accumulateSkillPoints() {
        accumulateSkillPoints(1);
    }

    @Override
    public void accumulateSkillPoints(int n) {
        setSkillPoints(Math.min(Scrambler.descramble(sp) + n, getSkillPointsMax()));
    }

    @LuaInterface
    public int getSkillPoints() {
        return Scrambler.descramble(sp);
    }

    public int getSkillPointsMax() {
        return Scrambler.descramble(maxSp);
    }

    public void spendSkillPoints(int cost) {
        setSkillPoints(Scrambler.descramble(sp) - cost);
    }

    public boolean enoughSP(int cost) {
        return getSkillPoints() >= cost;
    }

    @LuaInterface
    public void setSkillPoints(int points) {
        sp = Scrambler.scramble(Math.max(0, Math.min(points, getSkillPointsMax())));
        QuickSlot.refresh(this);
    }

    @LuaInterface
    public void setMaxSkillPoints(int points) {
        maxSp = Scrambler.scramble(points);
    }

    @Override
    protected boolean timeout() {
        if (SystemTime.now() - SystemTime.getLastActionTime() > Dungeon.moveTimeout()) {
            SystemTime.updateLastActionTime();
            spend(TIME_TO_REST);
            return true;
        }
        return false;
    }

    public void setPortalLevelCoordinates(Position position) {
        portalLevelPos = position;
    }

    @Override
    protected void moveSprite(int oldPos, int pos) {
        getSprite().move(oldPos, getPos());

    }

    @Override
    public boolean getCloser(int target, boolean ignorePets) {
        if (hasBuff(BuffFactory.ROOTS)) {
            return false;
        }

        int step = -1;

        Level level = level();

        Buff wallWalkerBuff = null;

        if (!level.isBossLevel()) {
            wallWalkerBuff = buff(BuffFactory.RING_OF_STONE_WALKING);
        }

        if (level.adjacent(getPos(), target)) {
            if (Actor.findChar(target) == null) {
                if (!hasBuff(BuffFactory.BLINDNESS)) {
                    if (level.pit[target] && !isFlying() && !Chasm.jumpConfirmed) {
                        Chasm.heroJump(this);
                        interrupt();
                        return false;
                    }
                    if (TrapHelper.isVisibleTrap(level, target) && !isFlying() && !TrapHelper.stepConfirmed) {
                        TrapHelper.heroTriggerTrap(this);
                        interrupt();
                        return false;
                    }
                }

                if (wallWalkerBuff == null && (level.passable[target] || level.avoid[target])) {
                    step = target;
                }
                if (wallWalkerBuff != null && level.solid[target]) {
                    step = target;
                }

                LevelObject obj = level.getTopLevelObject(target);

                if (obj != null && obj.pushable(this)) {

                    if (obj.pushable(this)) {
                        interrupt();
                        if (!obj.push(this)) {
                            return false;
                        }
                    }

                    if (obj.nonPassable(this)) {
                        interrupt();
                        return false;
                    }
                }
            }

        } else {

            int len = level.getLength();
            boolean[] p = wallWalkerBuff != null ? level.solid : level.passable;
            boolean[] v = level.visited;
            boolean[] m = level.mapped;
            boolean[] passable = new boolean[len];
            for (int i = 0; i < len; i++) {
                passable[i] = p[i] && (v[i] || m[i]);
            }

            step = Dungeon.findPath(this, target, passable, level.fieldOfView);
        }

        if (level.cellValid(step)) {

            int oldPos = getPos();

            LevelObject obj = level.getTopLevelObject(step);
            if (obj != null) {

                if (obj.nonPassable(this)) {
                    interrupt();
                    return false;
                }

                if (step == target) {
                    interrupt();
                    if (!obj.interact(this)) {
                        return false;
                    }
                } else {
                    if (!obj.stepOn(this)) {
                        interrupt();
                        return false;
                    }
                }
            }

            Char actor = Actor.findChar(step);
            if (actor instanceof Mob) {
                Mob mob = ((Mob) actor);
                if (actor.friendly(this)) {
                    if (!mob.swapPosition(this)) {
                        return false;
                    }
                    observe();
                }
            }

            move(step);
            moveSprite(oldPos, getPos());

            if (wallWalkerBuff != null) {
                int dmg = Math.max(hp() / 2, 2);
                damage(dmg, wallWalkerBuff);
            }

            spend(1 / speed());

            return true;
        }

        return false;

    }

    public void skillLevelUp() {
        setSkillLevel(skillLevel() + 1);
    }

    @Override
    public boolean friendly(@NotNull Char chr) {
        if (chr instanceof Mob) {
            Mob mob = (Mob) chr;
            return heroClass.friendlyTo(mob.getEntityKind());
        }
        return super.friendly(chr);
    }

    @Override
    public boolean ignoreDr() {
        return rangedWeapon.valid() && subClass == HeroSubClass.SNIPER;
    }


    @Override
    public boolean collect(@NotNull Item item) {
        if (super.collect(item)) {
            QuickSlot.refresh(this);
            return true;
        }
        return false;
    }

    @LuaInterface
    public void teleportTo(@NotNull Position newPos) {
        if (newPos.levelId.equals(levelId)) {
            newPos.computeCell(level());
            WandOfBlink.appear(this, newPos.cellId);
            level().press(newPos.cellId, this);
            observe();
        } else {
            InterlevelScene.returnTo = new Position(newPos);
            InterlevelScene.Do(InterlevelScene.Mode.RETURN);
        }
    }

    @Override
    public void selectCell(CellSelector.Listener listener) {
        GameScene.selectCell(listener, this);
    }

    public static void performTests() {
        int difficulty = GameLoop.getDifficulty();
        var hero = new Hero(2);
        var buffsNames = BuffFactory.getAllBuffsNames();

        for (var buffName : buffsNames) {
            try {
                var buff = Buff.affect(hero, buffName, 10);
                buff.detach();
            } catch (Exception e) {
                GLog.toFile("Buffs auto-test: %s caused %s", buffName, e);
            }
        }
        GameLoop.setDifficulty(difficulty);
    }

    @Override
    public void setPos(int pos) {
        super.setPos(pos);
        if (!Dungeon.isLoading()) {
            level().visited[pos] = true;
        }
    }

    public boolean isSpellUser() {
        return !heroClass.getMagicAffinity().isEmpty();
    }

    @Override
    public void observe() {
        Dungeon.observe();
    }

    @Override
    public void collectAnimated(@NotNull Item item) {
        if (item.doPickUp( this )) {
            itemPickedUp(item);
        } else {
            item.doDrop(this);
        }
    }
}
