/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon.actors.mobs;

import com.nyrds.Packable;
import com.nyrds.android.lua.LuaEngine;
import com.nyrds.android.util.JsonHelper;
import com.nyrds.android.util.ModdingMode;
import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.ai.AiState;
import com.nyrds.pixeldungeon.ai.Fleeing;
import com.nyrds.pixeldungeon.ai.Hunting;
import com.nyrds.pixeldungeon.ai.Passive;
import com.nyrds.pixeldungeon.ai.Sleeping;
import com.nyrds.pixeldungeon.ai.Wandering;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.items.common.Library;
import com.nyrds.pixeldungeon.items.common.armor.NecromancerRobe;
import com.nyrds.pixeldungeon.items.necropolis.BlackSkull;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.common.IDepthAdjustable;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.actors.buffs.Regeneration;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.actors.buffs.Sleep;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.effects.Flare;
import com.watabou.pixeldungeon.effects.Pushing;
import com.watabou.pixeldungeon.effects.Wound;
import com.watabou.pixeldungeon.items.Generator;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.levels.features.Door;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.MobSpriteDef;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.json.JSONException;
import org.json.JSONObject;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class Mob extends Char {

	public static final String TXT_RAGE = "#$%^";

	private static final float SPLIT_DELAY = 1f;
	private static final String DEFAULT_MOB_SCRIPT = "scripts/mobs/Dummy";

	public AiState SLEEPING  = new Sleeping(this);
	public AiState HUNTING   = new Hunting(this);
	public AiState WANDERING = new Wandering(this);
	public AiState FLEEING   = new Fleeing(this);
	public AiState PASSIVE   = new Passive(this);

	@Packable (defaultValue = DEFAULT_MOB_SCRIPT)
	protected String scriptFile = DEFAULT_MOB_SCRIPT;

	private LuaTable mobScript;
	private LuaValue scriptResult = LuaValue.NIL;

	private AiState state = SLEEPING;

	protected Object spriteClass;

	public int target = -1;

	protected int defenseSkill = 0;

	protected int exp    = 1;
	protected int maxLvl = 50;

	@Nullable
	private PetOwner owner;

	@NonNull
	private Char enemy = DUMMY;

	public boolean enemySeen;

	private boolean alerted = false;

	public static final float TIME_TO_WAKE_UP = 1f;

	static private Map<String, JSONObject> defMap = new HashMap<>();

	// Unreachable target
	public static final Mob DUMMY = new Mob() {
		{
			setPos(Level.INVALID_CELL);
		}
	};

	private static final String STATE      = "state";
	private static final String TARGET     = "target";
	private static final String ENEMY_SEEN = "enemy_seen";
	private static final String FRACTION   = "fraction";

	public Mob() {
		readCharData();
	}

	public Fraction fraction() {
		return fraction;
	}

	@NonNull
	public static Mob makePet(@NonNull Mob pet, @NonNull Hero hero) {
		if (pet.canBePet()) {
			pet.setFraction(Fraction.HEROES);
			pet.owner = hero;
			pet.owner.addPet(pet);
		}
		return pet;
	}

	public void setFraction(Fraction fr) {
		fraction = fr;
		setEnemy(DUMMY);
	}

	private String state2tag(AiState state) {
		if (state == SLEEPING) {
			return Sleeping.TAG;
		} else if (state == WANDERING) {
			return Wandering.TAG;
		} else if (state == HUNTING) {
			return Hunting.TAG;
		} else if (state == FLEEING) {
			return Fleeing.TAG;
		} else if (state == PASSIVE) {
			return Passive.TAG;
		}
		return Passive.TAG;
	}

	@Override
	public void storeInBundle(Bundle bundle) {

		super.storeInBundle(bundle);

		bundle.put(STATE,  state2tag(getState()));
		bundle.put(TARGET, target);

		bundle.put(ENEMY_SEEN, enemySeen);
		bundle.put(FRACTION, fraction.ordinal());

		if (loot instanceof Item) {
			bundle.put(LOOT, (Item) loot);
		}
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {

		super.restoreFromBundle(bundle);

		String state = bundle.getString(STATE);
		setState(state);

		target = bundle.getInt(TARGET);

		if (bundle.contains(ENEMY_SEEN)) {
			enemySeen = bundle.getBoolean(ENEMY_SEEN);
		}

		fraction = Fraction.values()[bundle.optInt(FRACTION, Fraction.DUNGEON.ordinal())];

		if (bundle.contains(LOOT)) {
			loot = bundle.get(LOOT);
			lootChance = 1;
		}
	}

	protected void setState(String state) {
		switch (state) {
			case Sleeping.TAG:
				this.setState(SLEEPING);
				break;
			case Wandering.TAG:
				this.setState(WANDERING);
				break;
			case Hunting.TAG:
				this.setState(HUNTING);
				break;
			case Fleeing.TAG:
				this.setState(FLEEING);
				break;
			case Passive.TAG:
				this.setState(PASSIVE);
				break;
		}
	}

	protected int getKind() {
		return 0;
	}

	public CharSprite sprite() {

		try {
			{
				String descName = "spritesDesc/" + getMobClassName() + ".json";
				if (ModdingMode.isResourceExist(descName) || ModdingMode.isAssetExist(descName)) {
					return new MobSpriteDef(descName, getKind());
				}
			}

			if (spriteClass instanceof Class) {
				CharSprite sprite = (CharSprite) ((Class<?>) spriteClass).newInstance();
				sprite.selectKind(getKind());
				return sprite;
			}

			if (spriteClass instanceof String) {
				return new MobSpriteDef((String) spriteClass, getKind());
			}

			throw new TrackedRuntimeException(String.format("sprite creation failed - mob class %s", getMobClassName()));

		} catch (Exception e) {
			throw new TrackedRuntimeException(e);
		}
	}

	@Override
	protected boolean act() {

		super.act();

		boolean justAlerted = alerted;
		alerted = false;

		getSprite().hideAlert();

		if (paralysed) {
			enemySeen = false;
			spend(TICK);
			return true;
		}

		setEnemy(chooseEnemy());

		boolean enemyInFOV = getEnemy().isAlive() && Dungeon.level.cellValid(getEnemy().getPos()) && Dungeon.level.fieldOfView[getEnemy().getPos()]
				&& getEnemy().invisible <= 0;
/*
		if(enemyInFOV) {
			GLog.i("%s, enemy %s in fov %b", toString(), enemy.toString(), enemyInFOV);
		}
*/
		return getState().act(enemyInFOV, justAlerted);
	}

	private Char chooseNearestEnemyFromFraction(Fraction enemyFraction) {
		Level level = Dungeon.level;

		Char bestEnemy = DUMMY;
		int dist = level.getWidth() + level.getHeight();

		if (enemyFraction.belongsTo(Fraction.HEROES)) {
			Hero hero = Dungeon.hero;
			if (Dungeon.level.fieldOfView[hero.getPos()] && !friendly(hero)) {
				bestEnemy = hero;
				dist = level.distance(getPos(), bestEnemy.getPos());
			}
		}

		for (Mob mob : level.mobs) {
			if(Dungeon.level.fieldOfView[mob.getPos()]) {
				if (!mob.friendly(this)) {
					int candidateDist = level.distance(getPos(), mob.getPos());
					if (candidateDist < dist) {
						bestEnemy = mob;
						dist = candidateDist;
					}
				}
			}
		}

		return bestEnemy;
	}

	private Char chooseEnemyHeroes() {
		Char newEnemy = chooseNearestEnemyFromFraction(Fraction.DUNGEON);

		if (newEnemy != DUMMY && Dungeon.visible[newEnemy.getPos()]) {
			return newEnemy;
		}

		setState(WANDERING);
		target = Dungeon.hero.getPos();

		return DUMMY;
	}

	protected Char chooseEnemy() {
		if (!getEnemy().isAlive()) {
			setEnemy(DUMMY);
		}

		Terror terror = buff(Terror.class);
		if (terror != null) {
			return terror.source;
		}

		if (hasBuff(Amok.class)) {
			return chooseNearestEnemyFromFraction(Fraction.ANY);
		}

		if(getEnemy() != DUMMY) {
			return getEnemy();
		}

		switch (fraction) {
			default:
			case DUNGEON:
				return chooseNearestEnemyFromFraction(Fraction.HEROES);
			case HEROES:
				return chooseEnemyHeroes();
		}
	}

	public boolean moveSprite(int from, int to) {

		if (getSprite().isVisible()
				&& (Dungeon.visible[from] || Dungeon.visible[to])) {
			getSprite().move(from, to);
		} else {
			getSprite().place(to);
		}
		return true;
	}

	@Override
	public void add(Buff buff) {
		super.add(buff);

		if (!GameScene.isSceneReady()) {
			return;
		}

		if (buff instanceof Amok) {
			getSprite().showStatus(CharSprite.NEGATIVE, TXT_RAGE);
			setState(HUNTING);
		} else if (buff instanceof Terror) {
			setState(FLEEING);
		} else if (buff instanceof Sleep) {
			new Flare(4, 32).color(0x44ffff, true).show(getSprite(), 2f);
			setState(SLEEPING);
			postpone(Sleep.SWS);
		}
	}

	@Override
	public void remove(Buff buff) {
		super.remove(buff);
		if (buff instanceof Terror) {
			getSprite().showStatus(CharSprite.NEGATIVE, TXT_RAGE);
			setState(HUNTING);
		}
	}

	public boolean canAttack(Char enemy) {
		return Dungeon.level.adjacent(getPos(), enemy.getPos()) && !pacified;
	}

	public boolean getCloser(int target) {

		if (hasBuff(Roots.class)) {
			return false;
		}
		int step = Dungeon.findPath(this, getPos(), target, walkingType.passableCells(Dungeon.level));

		if (step != -1) {
			move(step);
			return true;
		} else {
			return false;
		}
	}

	public boolean getFurther(int target) {
		int step = Dungeon.flee(this, getPos(), target, walkingType.passableCells(Dungeon.level));

		if (step != -1) {
			move(step);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void move(int step) {
		runMobScript("onMove", step);

		super.move(step);
	}

	protected float attackDelay() {
		return 1f;
	}

	public boolean doAttack(Char enemy) {

		if (Dungeon.level.distance( getPos(), enemy.getPos() ) <= 1) {
			getSprite().attack(enemy.getPos());
		} else {
			getSprite().zap( enemy.getPos() );
		}

		spend(attackDelay());

		return false;
	}

	@Override
	public final void onAttackComplete() {
		attack(getEnemy());
		super.onAttackComplete();
	}

	@Override
	public final void onZapComplete() {
		zap(getEnemy());
		super.onZapComplete();
	}


	@Override
	public int defenseSkill(Char enemy) {
		return enemySeen && !paralysed ? defenseSkill : 0;
	}

	@Override
	public int attackProc(@NonNull Char enemy, int damage) {
		runMobScript("onAttackProc", enemy, damage);

		if(scriptResult.isnumber()) {
			return scriptResult.checknumber().toint();
		}

		return damage;
	}

	@Override
	public int defenseProc(Char enemy, int damage) {
		if (!enemySeen && enemy == Dungeon.hero
				&& ((Hero) enemy).subClass == HeroSubClass.ASSASSIN) {
			damage += Random.Int(1, damage);
			Wound.hit(this);
		}

		if(owner!=enemy) {
			setEnemy(enemy);
		}

		runMobScript("onDefenceProc", enemy, damage);

		if(scriptResult.isnumber()) {
			return scriptResult.checknumber().toint();
		}

		return damage;
	}

	@Override
	public void damage(int dmg, Object src) {

		runMobScript("onDamage", dmg, src);

		Terror.recover(this);

		AiState state = getState();

		if (state == SLEEPING || state == PASSIVE) {
			setState(HUNTING);
		}
		alerted = true;

		super.damage(dmg, src);
	}

	@Override
	public void destroy() {

		super.destroy();

		Dungeon.level.mobs.remove(this);
	}

	public void remove() {
		super.die(this);
	}

	private boolean mobScriptLoaded;
	protected boolean runMobScript(String method, Object arg1, Object arg2) {
		if (!mobScriptLoaded) {
			mobScript=LuaEngine.module(scriptFile, DEFAULT_MOB_SCRIPT);
			mobScriptLoaded = true;
		}

		if(mobScript != null) {
			scriptResult = mobScript.invokemethod(method, new LuaValue[]{
					CoerceJavaToLua.coerce(this),
					CoerceJavaToLua.coerce(arg1),
					CoerceJavaToLua.coerce(arg2)})
					.arg1();

			if(scriptResult.isboolean()) {
				return scriptResult.checkboolean();
			}
			return true;
		}
		return false;
	}

	protected boolean runMobScript(String method, Object arg) {
		return runMobScript(method, arg, null);
	}

	protected boolean runMobScript(String method) {
		return runMobScript(method, null, null);
	}

	@Override
	public void die(Object cause) {
		runMobScript("onDie",cause);

		Hero hero = Dungeon.hero;

		{
			//TODO we should move this block out of Mob class ( in script for example )
			if (hero.heroClass == HeroClass.NECROMANCER){
				if (hero.isAlive()) {
					if(hero.belongings.armor instanceof NecromancerRobe){
						hero.accumulateSoulPoints();
					}
				}
			}
			for (Item item : hero.belongings) {
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

				if (!(cause instanceof Mob) || hero.heroClass == HeroClass.NECROMANCER) {
					if (hero.lvl() <= maxLvl && exp > 0) {
						hero.earnExp(exp);
					}
				}
			}
		}

		if(owner!=null) {
			owner.removePet(this);
		}

		super.die(cause);

		Library.identify(Library.MOB,getMobClassName());

		if (hero.lvl() <= maxLvl + 2) {
			dropLoot();
		}

		if (hero.isAlive() && !Dungeon.visible[getPos()]) {
			GLog.i(Game.getVar(R.string.Mob_Died));
		}
	}

	private final String LOOT = "loot";

	protected Object loot       = null;
	protected float  lootChance = 0;

	public Mob split(int cell, int damage) {
		Mob clone;
		try {
			clone = MobFactory.mobByName(getMobClassName());
		} catch (Exception e) {
			throw new TrackedRuntimeException("split issue");
		}

		clone.hp(Math.max((hp() - damage) / 2, 1));
		clone.setPos(cell);
		clone.setState(clone.HUNTING);

		clone.ensureOpenDoor();

		Dungeon.level.spawnMob(clone, SPLIT_DELAY, getPos());

		if (hasBuff(Burning.class)) {
			Buff.affect(clone, Burning.class).reignite(clone);
		}
		if (hasBuff(Poison.class)) {
			Buff.affect(clone, Poison.class).set(2);
		}

		return clone;
	}

	protected void resurrect() {
		resurrect(this);
	}

	public void resurrect(Char parent) {

		int spawnPos = Dungeon.level.getEmptyCellNextTo(parent.getPos());
		Mob new_mob;
		try {
			new_mob = this.getClass().newInstance();
		} catch (Exception e) {
			throw new TrackedRuntimeException("resurrect issue");
		}

		if (Dungeon.level.cellValid(spawnPos)) {
			new_mob.setPos(spawnPos);
			if (parent instanceof Hero) {
				Mob.makePet(new_mob, (Hero) parent);
				Actor.addDelayed(new Pushing(new_mob, parent.getPos(), new_mob.getPos()), -1);
			}
			Dungeon.level.spawnMob(new_mob);
		}
	}

	@SuppressWarnings("unchecked")
	protected void dropLoot() {
		if (loot != null && Random.Float() <= lootChance) {
			Item item;
			if (loot instanceof Generator.Category) {
				item = Generator.random((Generator.Category) loot);
			} else if (loot instanceof Class<?>) {
				item = Generator.random((Class<? extends Item>) loot);
			} else {
				item = (Item) loot;
			}
			Dungeon.level.drop(item, getPos()).sprite.drop();
		}
	}

	public boolean reset() {
		return false;
	}

	public void beckon(int cell) {

		notice();

		if (getState() != HUNTING) {
			setState(WANDERING);
		}
		target = cell;
	}

	public String description() {
		return description;
	}

	public void notice() {
		getSprite().showAlert();
	}


	public void fromJson(JSONObject mobDesc) throws JSONException, InstantiationException, IllegalAccessException {
		if (mobDesc.has("loot")) {
			loot = ItemFactory.createItemFromDesc(mobDesc.getJSONObject("loot"));
			lootChance = (float) mobDesc.optDouble("lootChance", 1f);
		}

		if (this instanceof IDepthAdjustable) {
			((IDepthAdjustable) this).adjustStats(mobDesc.optInt("level", 1));
		}

		setState(mobDesc.optString("aiState",state2tag(getState())).toUpperCase(Locale.ROOT));
	}

	public AiState getState() {
		return state;
	}

	public void setState(AiState state) {
		this.state = state;
	}

	protected JSONObject getClassDef(){
		if (!defMap.containsKey(getMobClassName())) {
			defMap.put(getMobClassName(), JsonHelper.tryReadJsonFromAssets("mobsDesc/" + getMobClassName() + ".json"));
		}

		return defMap.get(getMobClassName());
	}

	public void onSpawn(Level level) {
		Buff.affect(this, Regeneration.class);
		runMobScript("onSpawn",level);
	}

	public void loot(Item loot) {
		this.loot = loot;
	}

	public boolean isPet() {
		return fraction == Fraction.HEROES;
	}

	@Override
	public boolean friendly(Char chr) {

		if(chr == this) {
			return true;
		}

		if(hasBuff(Amok.class) || chr.hasBuff(Amok.class)) {return false;}

		if(getEnemy() == chr) {return false;}

		if(chr instanceof Hero) {
			return isPet() || ((Hero)chr).heroClass.friendlyTo(getMobClassName());
		}
		return !this.fraction.isEnemy(chr.fraction);
	}

	public boolean canBePet() {
		return true;
	}

	public boolean swapPosition(final Char chr) {

		if(!walkingType.canSpawnAt(Dungeon.level,chr.getPos())) {
			return false;
		}

		if(hasBuff(Roots.class)) {
			return false;
		}

		int curPos = getPos();

		moveSprite(getPos(), chr.getPos());
		move(chr.getPos());

		chr.getSprite().move(chr.getPos(), curPos);
		chr.move(curPos);

		ensureOpenDoor();

		float timeToSwap = 1 / chr.speed();
		chr.spend(timeToSwap);
		spend(timeToSwap);
		setState(WANDERING);
		return true;
	}

	private void ensureOpenDoor() {
		if (Dungeon.level.map[getPos()] == Terrain.DOOR) {
			Door.enter(getPos());
		}
	}

	public boolean interact(Hero hero) {

		if(runMobScript("onInteract", hero)) {
			return true;
		}

		if (friendly(hero)) {
			swapPosition(hero);
			return true;
		}

		return false;
	}


	@NonNull
	public Char getEnemy() {
		return enemy;
	}

	public void setEnemy(@NonNull Char enemy) {
		/*
		if(enemy != this.enemy && enemy != DUMMY) {
			enemy.getSprite().showStatus(CharSprite.NEGATIVE, "FUCK!");
			GLog.i("%s  my enemy is %s now ", this.getName(), enemy.getName());
		}
		*/
		this.enemy = enemy;
	}

	public String getMobClassName() {
		return getClass().getSimpleName();
	}

	@Override
	public boolean attack(@NonNull Char enemy) {

		if (enemy == DUMMY) {
			EventCollector.logException("attacking dummy enemy");
			return false;
		}
		return super.attack(enemy);
	}

	public boolean zap(@NonNull Char enemy) {

		if(zapHit(enemy)) {
			int damage = zapProc(enemy,damageRoll());
			enemy.damage(damage, this);
			return true;
		}

		return false;
	}

	public int zapProc(@NonNull Char enemy, int damage) {
		runMobScript("onZapProc", enemy, damage);

		if(scriptResult.isnumber()) {
			return scriptResult.checknumber().toint();
		}

		return damage;
	}

	protected boolean zapHit(@NonNull Char enemy) {
		if (enemy == DUMMY) {
			EventCollector.logException("zapping dummy enemy");
			return false;
		}

		if (hit(this, enemy, true)) {
			return true;
		} else {
			enemy.getSprite().showStatus( CharSprite.NEUTRAL,  enemy.defenseVerb() );
			return false;
		}
	}
}
