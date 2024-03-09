
package com.watabou.pixeldungeon.actors.mobs;

import com.nyrds.LuaInterface;
import com.nyrds.generated.BundleHelper;
import com.nyrds.pixeldungeon.ai.AiState;
import com.nyrds.pixeldungeon.ai.Horrified;
import com.nyrds.pixeldungeon.ai.Hunting;
import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.RunningAmok;
import com.nyrds.pixeldungeon.ai.Sleeping;
import com.nyrds.pixeldungeon.ai.Wandering;
import com.nyrds.pixeldungeon.game.ModQuirks;
import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.items.common.Library;
import com.nyrds.pixeldungeon.items.common.armor.NecromancerRobe;
import com.nyrds.pixeldungeon.items.necropolis.BlackSkull;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.common.IDepthAdjustable;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.platform.util.TrackedRuntimeException;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Sleep;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.effects.Flare;
import com.watabou.pixeldungeon.effects.Pushing;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.features.Chasm;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.InterlevelScene;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.MobSpriteDef;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import lombok.SneakyThrows;

public abstract class Mob extends Char {

    public static final String TXT_RAGE = "#$%^";

    private static final float SPLIT_DELAY = 1f;
    public static final String LOOT = "loot";

    protected Object spriteClass;

    protected int expForKill = 1;
    protected int maxLvl = 50;

    public static final float TIME_TO_WAKE_UP = 1f;

    private static final String STATE = "state";
    private static final String FRACTION = "fraction";
    protected int dmgMin = 0;
    protected int dmgMax = 0;
    protected int attackSkill = 0;
    protected int dr = 0;
    protected boolean isBoss = false;

    public Mob() {
        super();
        setupCharData();
        getScript().run("fillStats");
        if (ModQuirks.mobLeveling) {
            lvl(Random.Int(1, (int) RemixedDungeon.getDifficultyFactor()+1));
        }
    }

    public void releasePet() {
        setFraction(Fraction.DUNGEON);
        setOwnerId(getId());
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
        if (pet.canBePet()) {
            pet.setFraction(Fraction.HEROES);
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
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {

        super.restoreFromBundle(bundle);

        String state = bundle.getString(STATE);
        setState(state);

        fraction = Fraction.values()[bundle.optInt(FRACTION, Fraction.DUNGEON.ordinal())];

        if (bundle.contains(LOOT)) { //pre 29.6 saves compatibility
            loot(bundle.get(LOOT), 1);
        }
    }

    @LuaInterface
    public void setState(String state) {
        setState(MobAi.getStateByTag(state));
    }

    protected int getKind() {
        return 0;
    }

    @SneakyThrows
    public CharSprite newSprite() {
        String descName = "spritesDesc/" + getEntityKind() + ".json";
        if (ModdingMode.isResourceExist(descName) || ModdingMode.isAssetExist(descName)) {
            return new MobSpriteDef(descName, getKind());
        }

        if (spriteClass instanceof Class) {
            CharSprite sprite = (CharSprite) ((Class<?>) spriteClass).newInstance();
            sprite.selectKind(getKind());
            return sprite;
        }

        if (spriteClass instanceof String) {
            return new MobSpriteDef((String) spriteClass, getKind());
        }

        throw new TrackedRuntimeException(String.format("sprite creation failed - me class %s", getEntityKind()));
    }

    @Override
    public boolean act() {
/*
    	if(Util.isDebug() && !(this instanceof NPC) && !getEntityKind().contains("NPC") && !getEntityKind().contains("Npc")) {
    		if(!(baseAttackSkill > 0 && baseDefenseSkill > 0)) {
    			throw new RuntimeException(Utils.format("bad params for %s", getEntityKind()));
			}
		}
*/
        super.act(); //Calculate FoV

        getSprite().hideAlert();

        if (paralysed) {
            enemySeen = false;
            spend(TICK);
            return true;
        }

        //float timeBeforeAct = actorTime();


        //GLog.debug("%s is %s", getEntityKind(), getState().getTag());
        getState().act(this);

/*		if(actorTime() == timeBeforeAct && Util.isDebug()) {
			var error = String.format("actor %s has same timestamp after %s act!", getEntityKind(), getState().getTag());
			if(Util.isDebug()) {
				throw new ModError(error);
			} else {
				spend(MICRO_TICK);
				EventCollector.logException(error);
			}
		}*/
        return true;
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
        super.add(buff);

        if (!isOnStage()) {
            return true;
        }

        if (buff instanceof Amok) {
            getSprite().showStatus(CharSprite.NEGATIVE, TXT_RAGE);
            setState(MobAi.getStateByClass(RunningAmok.class));
        } else if (buff instanceof Terror) {
            setState(MobAi.getStateByClass(Horrified.class));
        } else if (buff instanceof Sleep) {
            new Flare(4, 32).color(0x44ffff, true).show(getSprite(), 2f);
            setState(MobAi.getStateByClass(Sleeping.class));
            postpone(Sleep.SWS);
        }
        return true;
    }

    public boolean canAttack(@NotNull Char enemy) {
        return !pacified && super.canAttack(enemy);
    }

    public boolean getCloser(int target) {
        int step = Dungeon.findPath(this, target, walkingType.passableCells(level()));
        return _doStep(step);
    }

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
        zap(getEnemy());
        super.onZapComplete();
    }

    @Override
    public int defenseSkill(Char enemy) {
        return enemySeen ? super.defenseSkill(enemy) : 0;
    }


    @Override
    public void destroy() {

        spend(MICRO_TICK);

        super.destroy();

        level().mobs.remove(this);
    }

    public void remove() {
        super.die(this);
    }

    public void die(@NotNull NamedEntityKind cause) {

        spend(Actor.MICRO_TICK);


        Badges.validateRare(this);

        Hero hero = Dungeon.hero;

        if (!cause.getEntityKind().equals(Chasm.class.getSimpleName())) {
            //TODO we should move this block out of Mob class ( in script for example )
            if (hero.getHeroClass() == HeroClass.NECROMANCER) {
                if (hero.isAlive()) {
                    if (hero.getItemFromSlot(Belongings.Slot.ARMOR) instanceof NecromancerRobe) {
                        hero.accumulateSkillPoints();
                    }
                }
            }

            for (Item item : hero.getBelongings()) {
                if (item instanceof BlackSkull && item.isEquipped(hero)) {
                    ((BlackSkull) item).mobDied(this, hero);
                }
            }
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
            getBelongings().dropAll();
        }

        if (hero.isAlive() && !CharUtils.isVisible(this)) {
            GLog.i(StringsManager.getVar(R.string.Mob_Died));
        }
    }

    public Mob split(int cell, int damage) {

        Mob clone = (Mob) makeClone();

        clone.hp(Math.max((hp() - damage) / 2, 1));
        clone.setPos(cell);
        clone.setState(MobAi.getStateByClass(Hunting.class));

        clone.ensureOpenDoor();

        level().spawnMob(clone, SPLIT_DELAY, getPos());

        return clone;
    }

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

    public void resurrect(Char parent) {

        int spawnPos = getPos();
        Mob new_mob = MobFactory.mobByName(getEntityKind());

        if (level().cellValid(spawnPos)) {
            new_mob.setPos(spawnPos);
            Mob.makePet(new_mob, parent.getId());
            Actor.addDelayed(new Pushing(new_mob, parent.getPos(), new_mob.getPos()), -1);
            level().spawnMob(new_mob);
            level().press(spawnPos, new_mob);
        }
    }

    public boolean reset() {
        return false;
    }

    public void beckon(int cell) {
        notice();
        setState(MobAi.getStateByClass(Wandering.class));
        setTarget(cell);
    }

    public void fromJson(JSONObject mobDesc) throws JSONException, InstantiationException, IllegalAccessException {
        Bundle descBundle = new Bundle();
        BundleHelper.Pack(this, descBundle);
        descBundle.mergeWith(mobDesc);
        BundleHelper.UnPack(this, descBundle);

        if (mobDesc.has(LOOT)) {
            float lootChance = (float) mobDesc.optDouble("lootChance", 1f);
            loot(ItemFactory.createItemFromDesc(mobDesc.getJSONObject(LOOT)), lootChance);
        }

        getBelongings().setupFromJson(mobDesc);

        if (this instanceof IDepthAdjustable) {
            ((IDepthAdjustable) this).adjustStats(mobDesc.optInt("level", 1));
        }

        setState(mobDesc.optString("aiState", getState().getTag()));
    }

    public AiState getState() {
        return state;
    }


    public boolean isPet() {
        return fraction == Fraction.HEROES;
    }

    @Override
    public boolean friendly(@NotNull Char chr) {

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
            if (getOwner().friendly(chr)) {
                return true;
            }
        }

        if (chr instanceof Hero) {
            return chr.getHeroClass().friendlyTo(getEntityKind());
        }

        return !this.fraction.isEnemy(chr.fraction);
    }

    @Override
    public boolean canBePet() {
        return true;
    }

    @Override
    public boolean swapPosition(Char chr) {
        if (super.swapPosition(chr)) {
            setState(MobAi.getStateByClass(Wandering.class));
            return true;
        }
        return false;
    }

    public boolean zap(@NotNull Char enemy) {

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

    protected boolean zapHit(@NotNull Char enemy) {
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

        new_mob.restoreFromBundle(storedMob);

        new_mob.getId(); //Ensure valid id

        if(getOwnerId() == getId()) {
            new_mob.setOwnerId(new_mob.getId());
        } else {
            new_mob.setOwnerId(getOwnerId());
        }

        return new_mob;
    }

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
                    if (!hasBuff("ChampionOfEarth") && !hasBuff("ChampionOfFire")
                            && !hasBuff("ChampionOfWater") && !hasBuff("ChampionOfAir")) {

                        String[] champions = {"ChampionOfEarth", "ChampionOfFire", "ChampionOfWater", "ChampionOfAir"};

                        Buff.permanent(this, Random.oneOf(champions));
                    }
                }
            }
        }

    }

    @Override
    public int damageRoll() {
        int dmg = Random.NormalIntRange(dmgMin, dmgMax) + Random.NormalIntRange(0, lvl());

        dmg += getActiveWeapon().damageRoll(this);

        if (!rangedWeapon.valid()) {
            dmg += getSecondaryWeapon().damageRoll(this);
        }

        return dmg;
    }

    @Override
    public int dr() {
        return getItemFromSlot(Belongings.Slot.ARMOR).effectiveDr() + dr + lvl() / 2;
    }
}
