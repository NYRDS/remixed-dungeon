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

import android.support.annotation.NonNull;

import com.nyrds.android.util.JsonHelper;
import com.nyrds.android.util.ModdingMode;
import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.items.necropolis.BlackSkull;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.common.IDepthAdjustable;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Challenges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Poison;
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
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.levels.features.Door;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.MobSpriteDef;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public abstract class Mob extends Char {

	private static final String TXT_DIED = Game.getVar(R.string.Mob_Died);

	protected static final String TXT_RAGE = "#$%^";
	protected static final String TXT_EXP  = "%+dEXP";

	private static final float SPLIT_DELAY = 1f;

	public AiState SLEEPING  = new Sleeping();
	public AiState HUNTING   = new Hunting();
	public AiState WANDERING = new Wandering();
	public AiState FLEEING   = new Fleeing();
	public AiState PASSIVE   = new Passive();

	private AiState state = SLEEPING;

	protected Object spriteClass;

	protected int target = -1;

	protected int defenseSkill = 0;

	protected int EXP    = 1;
	protected int maxLvl = 30;

	@NonNull
	private Char enemy = DUMMY;

	protected boolean enemySeen;

	protected boolean alerted = false;

	protected static final float TIME_TO_WAKE_UP = 1f;

	static protected Map<Class, JSONObject> defMap = new HashMap<>();

	// Unreachable target
	public static final Mob DUMMY = new Mob() {
		{
			setPos(-1);
		}
	};

	private static final String STATE      = "state";
	private static final String TARGET     = "target";
	private static final String ENEMY_SEEN = "enemy_seen";
	private static final String FRACTION   = "fraction";

	protected Fraction fraction = Fraction.DUNGEON;

	public Mob() {
		readCharData();
	}

	public Fraction fraction() {
		return fraction;
	}

	public static Mob makePet(@NonNull Mob pet, @NonNull Hero hero) {
		if (pet.canBePet()) {
			pet.setFraction(Fraction.HEROES);
			hero.addPet(pet);
		}
		return pet;
	}

	public void setFraction(Fraction fr) {
		fraction = fr;
		setEnemy(DUMMY);
	}

	@Override
	public void storeInBundle(Bundle bundle) {

		super.storeInBundle(bundle);

		if (getState() == SLEEPING) {
			bundle.put(STATE, Sleeping.TAG);
		} else if (getState() == WANDERING) {
			bundle.put(STATE, Wandering.TAG);
		} else if (getState() == HUNTING) {
			bundle.put(STATE, Hunting.TAG);
		} else if (getState() == FLEEING) {
			bundle.put(STATE, Fleeing.TAG);
		} else if (getState() == PASSIVE) {
			bundle.put(STATE, Passive.TAG);
		}
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

	protected int getKind() {
		return 0;
	}

	public CharSprite sprite() {

		try {
			{
				String descName = "spritesDesc/" + getClass().getSimpleName() + ".json";
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

			throw new TrackedRuntimeException(String.format("sprite creation failed - mob class %s", getClass().getCanonicalName()));

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

		return getState().act(enemyInFOV, justAlerted);
	}

	private Char chooseNearestEnemyFromFraction(Fraction enemyFraction) {

		Char bestEnemy = DUMMY;
		int dist = Dungeon.level.getWidth() + Dungeon.level.getHeight();

		if (enemyFraction.belongsTo(Fraction.HEROES)) {
			bestEnemy = Dungeon.hero;
			dist = Dungeon.level.distance(getPos(), bestEnemy.getPos());
		}

		for (Mob mob : Dungeon.level.mobs) {
			if (mob.fraction.equals(enemyFraction) && mob != this) {
				int candidateDist = Dungeon.level.distance(getPos(), mob.getPos());
				if (candidateDist < dist) {
					bestEnemy = mob;
					dist = candidateDist;
				}
			}
		}

		return bestEnemy;
	}

	private Char chooseEnemyDungeon() {
		Char newEnemy = chooseNearestEnemyFromFraction(Fraction.HEROES);

		if (newEnemy != DUMMY) {
			return newEnemy;
		}

		return Dungeon.hero;
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

		if (buff(Amok.class) != null) {
			if (getEnemy() == Dungeon.hero) {
				return chooseNearestEnemyFromFraction(Fraction.ANY);
			}
		}

		if (getEnemy() instanceof Mob) {
			Mob enemyMob = (Mob) getEnemy();
			if (enemyMob.fraction.belongsTo(fraction)) {
				setEnemy(DUMMY);
			}
		}

		switch (fraction) {
			default:
			case DUNGEON:
				return chooseEnemyDungeon();
			case HEROES:
				return chooseEnemyHeroes();
		}
	}

	protected boolean moveSprite(int from, int to) {

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

	protected boolean canAttack(Char enemy) {
		return Dungeon.level.adjacent(getPos(), enemy.getPos()) && !pacified;
	}

	protected boolean getCloser(int target) {

		if (rooted) {
			return false;
		}
		int step;

		if (isAbsoluteWalker()) {
			step = Dungeon.findPath(this, getPos(), target, Dungeon.level.allCells, null);
		} else {
			if (!isWallWalker()) {
				step = Dungeon.findPath(this, getPos(), target, Dungeon.level.passable, null);
			} else {
				step = Dungeon.findPath(this, getPos(), target, Dungeon.level.solid, null);
			}
		}

		if (step != -1) {
			move(step);
			return true;
		} else {
			return false;
		}
	}

	protected boolean getFurther(int target) {
		int step;

		if (isAbsoluteWalker()) {
			step = Dungeon.flee(this, getPos(), target, Dungeon.level.allCells, null);
		} else {
			if (!isWallWalker()) {
				step = Dungeon.flee(this, getPos(), target, Dungeon.level.passable, null);
			} else {
				step = Dungeon.flee(this, getPos(), target, Dungeon.level.solid, null);
			}
		}

		if (step != -1) {
			move(step);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void move(int step) {
		super.move(step);

		if (!flying) {
			Dungeon.level.mobPress(this);
		}
	}

	protected float attackDelay() {
		return 1f;
	}

	protected boolean doAttack(Char enemy) {

		boolean visible = Dungeon.visible[getPos()];

		if (visible) {
			getSprite().attack(enemy.getPos());
		} else {
			attack(enemy);
		}

		spend(PixelDungeon.realtime() ? attackDelay() * 10 : attackDelay());

		return !visible;
	}

	@Override
	public void onAttackComplete() {
		attack(getEnemy());
		super.onAttackComplete();
	}

	@Override
	public int defenseSkill(Char enemy) {
		return enemySeen && !paralysed ? defenseSkill : 0;
	}

	@Override
	public int defenseProc(Char enemy, int damage) {
		if (!enemySeen && enemy == Dungeon.hero
				&& ((Hero) enemy).subClass == HeroSubClass.ASSASSIN) {
			damage += Random.Int(1, damage);
			Wound.hit(this);
		}
		return damage;
	}

	@Override
	public void damage(int dmg, Object src) {

		Terror.recover(this);

		if (getState() == SLEEPING) {
			setState(WANDERING);
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

	@Override
	public void die(Object cause) {

		{
			//TODO we should move this block out of Mob class
			Hero hero = Dungeon.hero;
			if (hero != null && hero.isAlive()) {
				hero.accumulateSoulPoints();
				for (Item item : hero.belongings) {
					if (item instanceof BlackSkull && item.isEquipped(hero)) {
						((BlackSkull) item).mobDied(this, hero);
					}
				}
			}
		}

		{
			Hero hero = Dungeon.hero;
			if (hero.isAlive()) {
				if (isHostile()) {
					Statistics.enemiesSlain++;
					Badges.validateMonstersSlain();
					Statistics.qualifiedForNoKilling = false;

					if (Dungeon.nightMode) {
						Statistics.nightHunt++;
					} else {
						Statistics.nightHunt = 0;
					}
					Badges.validateNightHunter();
				}

				if (!(cause instanceof Mob) || hero.heroClass == HeroClass.NECROMANCER) {
					if (hero.lvl() <= maxLvl && EXP > 0) {
						hero.getSprite().showStatus(CharSprite.POSITIVE, TXT_EXP,
								EXP);
						hero.earnExp(EXP);
					}
				}
			}
		}

		super.die(cause);

		if (Dungeon.hero.lvl() <= maxLvl + 2) {
			dropLoot();
		}

		if (Dungeon.hero.isAlive() && !Dungeon.visible[getPos()]) {
			GLog.i(TXT_DIED);
		}
	}

	private final String LOOT = "loot";

	protected Object loot       = null;
	protected float  lootChance = 0;

	public Mob split(int cell, int damage) {
		Mob clone;
		try {
			clone = getClass().newInstance();
		} catch (Exception e) {
			throw new TrackedRuntimeException("split issue");
		}

		clone.hp((hp() - damage) / 2);
		clone.setPos(cell);
		clone.setState(clone.HUNTING);

		if (Dungeon.level.map[clone.getPos()] == Terrain.DOOR) {
			Door.enter(clone.getPos());
		}

		Dungeon.level.spawnMob(clone, SPLIT_DELAY);
		Actor.addDelayed(new Pushing(clone, getPos(), clone.getPos()), -1);

		if (buff(Burning.class) != null) {
			Buff.affect(clone, Burning.class).reignite(clone);
		}
		if (buff(Poison.class) != null) {
			Buff.affect(clone, Poison.class).set(2);
		}

		if (isPet()) {
			Mob.makePet(clone, Dungeon.hero);
		}

		return clone;
	}

	public void ressurrect() {
		ressurrect(this);
	}

	public void ressurrect(Char parent) {

		int spawnPos = Dungeon.level.getEmptyCellNextTo(parent.getPos());
		Mob new_mob;
		try {
			new_mob = this.getClass().newInstance();
		} catch (Exception e) {
			throw new TrackedRuntimeException("resurrect issue");
		}

		if (Dungeon.level.cellValid(spawnPos)) {
			new_mob.setPos(spawnPos);
			Dungeon.level.spawnMob(new_mob);
			if (parent instanceof Hero) {
				Mob.makePet(new_mob, (Hero) parent);
				Actor.addDelayed(new Pushing(new_mob, parent.getPos(), new_mob.getPos()), -1);
			}
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

	public void yell(String str) {
		GLog.n(Game.getVar(R.string.Mob_Yell), getName(), str);
	}

	public void say(String str) {
		GLog.i(Game.getVar(R.string.Mob_Yell), getName(), str);
	}

	public boolean isHostile() {
		return fraction.belongsTo(Fraction.DUNGEON) && !isPet();
	}

	public void fromJson(JSONObject mobDesc) throws JSONException, InstantiationException, IllegalAccessException {
		if (mobDesc.has("loot")) {
			loot = ItemFactory.createItemFromDesc(mobDesc.getJSONObject("loot"));
			lootChance = 1;
		}

		if (this instanceof IDepthAdjustable) {
			((IDepthAdjustable) this).adjustStats(mobDesc.optInt("level", 1));
		}
	}

	public AiState getState() {
		return state;
	}

	public void setState(AiState state) {
		this.state = state;
	}

	public interface AiState {
		boolean act(boolean enemyInFOV, boolean justAlerted);

		String status();
	}

	private class Sleeping implements AiState {

		public static final String TAG = "SLEEPING";

		@Override
		public boolean act(boolean enemyInFOV, boolean justAlerted) {
			if (enemyInFOV
					&& Random.Int(distance(getEnemy()) + getEnemy().stealth()
					+ (getEnemy().flying ? 2 : 0)) == 0) {

				enemySeen = true;

				notice();
				setState(HUNTING);
				target = getEnemy().getPos();

				if (Dungeon.isChallenged(Challenges.SWARM_INTELLIGENCE)) {
					for (Mob mob : Dungeon.level.mobs) {
						if (mob != Mob.this) {
							mob.beckon(target);
						}
					}
				}

				spend(TIME_TO_WAKE_UP);

			} else {

				enemySeen = false;

				spend(TICK);

			}
			return true;
		}

		@Override
		public String status() {
			return Utils.format(Game.getVar(R.string.Mob_StaSleepingStatus),
					getName());
		}
	}

	private class Wandering implements AiState {

		public static final String TAG = "WANDERING";

		@Override
		public boolean act(boolean enemyInFOV, boolean justAlerted) {
			if (enemyInFOV
					&& (justAlerted || Random.Int(distance(getEnemy()) / 2
					+ getEnemy().stealth()) == 0)) {

				enemySeen = true;

				notice();
				setState(HUNTING);
				target = getEnemy().getPos();

			} else {

				enemySeen = false;

				int oldPos = getPos();
				if (Dungeon.level.cellValid(target) && getCloser(target)) {
					spend(1 / speed());
					return moveSprite(oldPos, getPos());
				} else {
					target = Dungeon.level.randomDestination();
					spend(TICK);
				}

			}
			return true;
		}

		@Override
		public String status() {
			return Utils.format(Game.getVar(R.string.Mob_StaWanderingStatus),
					getName());
		}
	}

	class Hunting implements AiState {

		public static final String TAG = "HUNTING";

		@Override
		public boolean act(boolean enemyInFOV, boolean justAlerted) {
			enemySeen = enemyInFOV;
			if (enemyInFOV && canAttack(getEnemy())) {

				return doAttack(getEnemy());

			} else {

				if (enemyInFOV) {
					target = getEnemy().getPos();
				}

				int oldPos = getPos();
				if (target != -1 && getCloser(target)) {

					spend(1 / speed());
					return moveSprite(oldPos, getPos());

				} else {

					spend(TICK);
					setState(WANDERING);
					target = Dungeon.level.randomDestination();
					return true;
				}
			}
		}

		@Override
		public String status() {
			return Utils.format(Game.getVar(R.string.Mob_StaHuntingStatus),
					getName());
		}
	}

	protected class Fleeing implements AiState {

		public static final String TAG = "FLEEING";

		@Override
		public boolean act(boolean enemyInFOV, boolean justAlerted) {
			enemySeen = enemyInFOV;
			if (enemyInFOV) {
				target = getEnemy().getPos();
			}

			int oldPos = getPos();
			if (target != -1 && getFurther(target)) {

				spend(1 / speed());
				return moveSprite(oldPos, getPos());

			} else {

				spend(TICK);
				nowhereToRun();

				return true;
			}
		}

		protected void nowhereToRun() {
		}

		@Override
		public String status() {
			return Utils.format(Game.getVar(R.string.Mob_StaFleeingStatus),
					getName());
		}
	}

	private class Passive implements AiState {

		public static final String TAG = "PASSIVE";

		@Override
		public boolean act(boolean enemyInFOV, boolean justAlerted) {
			enemySeen = false;
			spend(TICK);
			return true;
		}

		@Override
		public String status() {
			return Utils.format(Game.getVar(R.string.Mob_StaPassiveStatus),
					getName());
		}
	}

	public boolean isWallWalker() {
		return false;
	}

	public boolean isAbsoluteWalker() {
		return false;
	}

	public boolean isPet() {
		return fraction == Fraction.HEROES;
	}

	public boolean canBePet() {
		return true;
	}

	protected void swapPosition(final Hero hero) {

		int curPos = getPos();

		moveSprite(getPos(), hero.getPos());
		move(hero.getPos());

		hero.getSprite().move(hero.getPos(), curPos);
		hero.move(curPos);

		hero.spend(1 / hero.speed());
		hero.busy();
	}

	public boolean interact(Hero hero) {
		if (fraction == Fraction.HEROES) {
			swapPosition(hero);
			return true;
		}

		return false;
	}

	@NonNull
	protected Char getEnemy() {
		return enemy;
	}

	protected void setEnemy(@NonNull Char enemy) {
		this.enemy = enemy;
	}

	@Override
	protected void readCharData() {
		super.readCharData();

		if (!defMap.containsKey(getClass())) {
			defMap.put(getClass(), JsonHelper.tryReadJsonFromAssets("mobsDesc/" + getClass().getSimpleName() + ".json"));
		}
	}
}
