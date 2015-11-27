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

import java.util.HashSet;

import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Challenges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.PixelDungeon;
import com.nyrds.android.util.ModdingMode;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Sleep;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.effects.Flare;
import com.watabou.pixeldungeon.effects.Wound;
import com.watabou.pixeldungeon.items.Generator;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.MobSpriteDef;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public abstract class Mob extends Char {

	private static final String TXT_DIED = Game.getVar(R.string.Mob_Died);

	protected static final String TXT_NOTICE1 = "?!";
	protected static final String TXT_RAGE = "#$%^";
	protected static final String TXT_EXP = "%+dEXP";

	public AiState SLEEPEING = new Sleeping();
	public AiState HUNTING = new Hunting();
	public AiState WANDERING = new Wandering();
	public AiState FLEEING = new Fleeing();
	public AiState PASSIVE = new Passive();

	public AiState state = SLEEPEING;

	// public Class<? extends CharSprite> spriteClass;
	protected Object spriteClass;

	protected int target = -1;

	protected int defenseSkill = 0;

	protected int EXP = 1;
	protected int maxLvl = 30;

	private Char enemy;
	protected boolean enemySeen;
	protected boolean alerted = false;

	protected static final float TIME_TO_WAKE_UP = 1f;

	public boolean hostile = true;

	// Unreachable target
	public static final Mob DUMMY = new Mob() {
		{
			pos = -1;
		}
	};

	private static final String STATE = "state";
	private static final String TARGET = "target";
	private static final String ENEMY_SEEN = "enemy_seen";
	private static final String FRACTION   = "fraction";
	
	protected Fraction fraction = Fraction.DUNGEON;
	
	public Mob() {
		readCharData();
	}
	
	public static Mob makePet(Mob pet, Hero hero) {
		pet.fraction = Fraction.HEROES;
		pet.setEnemy(DUMMY);
		hero.addPet(pet);
		
		return pet;
	}
	
	@Override
	public void storeInBundle(Bundle bundle) {

		super.storeInBundle(bundle);

		if (state == SLEEPEING) {
			bundle.put(STATE, Sleeping.TAG);
		} else if (state == WANDERING) {
			bundle.put(STATE, Wandering.TAG);
		} else if (state == HUNTING) {
			bundle.put(STATE, Hunting.TAG);
		} else if (state == FLEEING) {
			bundle.put(STATE, Fleeing.TAG);
		} else if (state == PASSIVE) {
			bundle.put(STATE, Passive.TAG);
		}
		bundle.put(TARGET, target);

		bundle.put(ENEMY_SEEN, enemySeen);
		bundle.put(FRACTION, fraction.ordinal());
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {

		super.restoreFromBundle(bundle);

		String state = bundle.getString(STATE);
		if (state.equals(Sleeping.TAG)) {
			this.state = SLEEPEING;
		} else if (state.equals(Wandering.TAG)) {
			this.state = WANDERING;
		} else if (state.equals(Hunting.TAG)) {
			this.state = HUNTING;
		} else if (state.equals(Fleeing.TAG)) {
			this.state = FLEEING;
		} else if (state.equals(Passive.TAG)) {
			this.state = PASSIVE;
		}

		target = bundle.getInt(TARGET);

		if (bundle.contains(ENEMY_SEEN)) {
			enemySeen = bundle.getBoolean(ENEMY_SEEN);
		}
		
		fraction = Fraction.values()[bundle.optInt(FRACTION, Fraction.DUNGEON.ordinal())];
		
	}

	protected int getKind() {
		return 0;
	}

	public CharSprite sprite() {
		CharSprite sprite = null;
		try {
			
			String descName = "spritesDesc/"+getClass().getSimpleName()+".json";
			if(ModdingMode.isResourceExist(descName)) {
				return new MobSpriteDef(descName, getKind());
			}
			
			if(spriteClass instanceof Class){
				sprite = (CharSprite) ((Class<?>)spriteClass).newInstance();
				sprite.selectKind(getKind());
			}
			
			if(spriteClass instanceof String){
				sprite = new MobSpriteDef((String)spriteClass, getKind());
			}
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return sprite;
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

		boolean enemyInFOV = getEnemy().isAlive() && Dungeon.level.fieldOfView[getEnemy().pos]
				&& getEnemy().invisible <= 0;

		return state.act(enemyInFOV, justAlerted);
	}

	private Char chooseNearestEnemy() {

		Char bestEnemy = Dungeon.hero;
		int dist   = Dungeon.level.distance(pos, enemy.pos);
		
		for (Mob mob : Dungeon.level.mobs) {
			if(mob != this) {
				int candidateDist = Dungeon.level.distance(pos, mob.pos);
				if (candidateDist <= dist) {
					bestEnemy = mob;
					dist = candidateDist;
				}
			}
		}
		
		return bestEnemy;
	}
	
	private Char chooseEnemyFromFraction( Fraction enemyFraction ) {
		HashSet<Mob> enemies = new HashSet<Mob>();
		for (Mob mob : Dungeon.level.mobs) {
			if (Dungeon.level.fieldOfView[mob.pos] && mob.fraction.equals(enemyFraction) && mob != this) {
				enemies.add(mob);
			}
		}
		
		if (enemies.size() > 0) {
			return Random.element(enemies);
		}
		
		return DUMMY;
	}
	
	private Char chooseEnemyDungeon() {
		if (getEnemy() == Dungeon.hero || getEnemy() == DUMMY) {
			Char newEnemy = chooseEnemyFromFraction(Fraction.HEROES);
			
			if(newEnemy != DUMMY) {
				return newEnemy;
			}
		}
		
		return Dungeon.hero;
	}
	
	private Char chooseEnemyHeroes() {
		if (getEnemy() == DUMMY ) {
			Char newEnemy = chooseEnemyFromFraction(Fraction.DUNGEON);
			
			if(newEnemy != DUMMY) {
				return newEnemy;
			}
			
			state = WANDERING;
			target = Dungeon.hero.pos;
			
			return DUMMY;
		}
		
		return getEnemy();
	}
	
	protected Char chooseEnemy() {
		if (getEnemy() == null) {
			setEnemy(DUMMY);
		}
		
		if(!getEnemy().isAlive()) {
			setEnemy(DUMMY);
		}
		
		Terror terror = (Terror) buff(Terror.class);
		if (terror != null) {
			return terror.source;
		}
		
		if (buff(Amok.class) != null) {
			if(getEnemy()==Dungeon.hero) {
				return chooseNearestEnemy();
			}
		}
		
		if(getEnemy() instanceof Mob) {
			Mob enemyMob = (Mob) getEnemy();
			if(enemyMob.fraction == fraction) {
				setEnemy(DUMMY);
			}
		}
		
		switch (fraction) {
		case DUNGEON:
			return chooseEnemyDungeon();
		case HEROES:
			return chooseEnemyHeroes();
		default:
			return chooseEnemyDungeon();
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
		if (buff instanceof Amok) {
			if (getSprite() != null) {
				getSprite().showStatus(CharSprite.NEGATIVE, TXT_RAGE);
			}
			state = HUNTING;
		} else if (buff instanceof Terror) {
			state = FLEEING;
		} else if (buff instanceof Sleep) {
			if (getSprite() != null) {
				new Flare(4, 32).color(0x44ffff, true).show(getSprite(), 2f);
			}
			state = SLEEPEING;
			postpone(Sleep.SWS);
		}
	}

	@Override
	public void remove(Buff buff) {
		super.remove(buff);
		if (buff instanceof Terror) {
			getSprite().showStatus(CharSprite.NEGATIVE, TXT_RAGE);
			state = HUNTING;
		}
	}

	protected boolean canAttack(Char enemy) {
		return Dungeon.level.adjacent(pos, enemy.pos) && !pacified;
	}

	protected boolean getCloser(int target) {

		if (rooted) {
			return false;
		}
		int step = -1;

		if (!isWallWalker()) {
			step = Dungeon.findPath(this, pos, target, Dungeon.level.passable,null);
		} else {
			step = Dungeon.findPath(this, pos, target, Dungeon.level.solid,null);
		}

		if (step != -1) {
			move(step);
			return true;
		} else {
			return false;
		}
	}

	protected boolean getFurther(int target) {
		int step = -1;

		if (!isWallWalker()) {
			step = Dungeon.flee(this, pos, target, Dungeon.level.passable,null);
		} else {
			step = Dungeon.flee(this, pos, target, Dungeon.level.solid,null);
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

		boolean visible = Dungeon.visible[pos];

		if (visible) {
			getSprite().attack(enemy.pos);
		} else {
			attack(enemy);
		}
		
		spend(PixelDungeon.realtime() ? attackDelay()*10 : attackDelay());

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

		if (state == SLEEPEING) {
			state = WANDERING;
		}
		alerted = true;

		super.damage(dmg, src);
	}

	@Override
	public void destroy() {

		super.destroy();

		Dungeon.level.mobs.remove(this);

		if (Dungeon.hero.isAlive()) {

			if (hostile) {
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

			if (Dungeon.hero.lvl <= maxLvl && EXP > 0) {
				Dungeon.hero.getSprite().showStatus(CharSprite.POSITIVE, TXT_EXP,
						EXP);
				Dungeon.hero.earnExp(EXP);
			}
		}
	}

	public void remove() {
		super.die(this);
	}
	
	@Override
	public void die(Object cause) {

		super.die(cause);

		if (Dungeon.hero.lvl <= maxLvl + 2) {
			dropLoot();
		}

		if (Dungeon.hero.isAlive() && !Dungeon.visible[pos]) {
			GLog.i(TXT_DIED);
		}
	}

	protected Object loot = null;
	protected float lootChance = 0;

	@SuppressWarnings("unchecked")
	protected void dropLoot() {
		if (loot != null && Random.Float() < lootChance) {
			Item item = null;
			if (loot instanceof Generator.Category) {

				item = Generator.random((Generator.Category) loot);

			} else if (loot instanceof Class<?>) {

				item = Generator.random((Class<? extends Item>) loot);

			} else {

				item = (Item) loot;

			}
			Dungeon.level.drop(item, pos).sprite.drop();
		}
	}

	public boolean reset() {
		return false;
	}

	public void beckon(int cell) {

		notice();

		if (state != HUNTING) {
			state = WANDERING;
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

	public interface AiState {
		public boolean act(boolean enemyInFOV, boolean justAlerted);

		public String status();
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
				state = HUNTING;
				target = getEnemy().pos;

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
			return String.format(Game.getVar(R.string.Mob_StaSleepingStatus),
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
				state = HUNTING;
				target = getEnemy().pos;

			} else {

				enemySeen = false;

				int oldPos = pos;
				if ( Dungeon.level.cellValid(target) && getCloser(target)) {
					spend(1 / speed());
					return moveSprite(oldPos, pos);
				} else {
					target = Dungeon.level.randomDestination();
					spend(TICK);
				}

			}
			return true;
		}

		@Override
		public String status() {
			return String.format(Game.getVar(R.string.Mob_StaWanderingStatus),
					getName());
		}
	}

	private class Hunting implements AiState {

		public static final String TAG = "HUNTING";

		@Override
		public boolean act(boolean enemyInFOV, boolean justAlerted) {
			enemySeen = enemyInFOV;
			if (enemyInFOV && canAttack(getEnemy())) {

				return doAttack(getEnemy());

			} else {

				if (enemyInFOV) {
					target = getEnemy().pos;
				}

				int oldPos = pos;
				if (target != -1 && getCloser(target)) {

					spend(1 / speed());
					return moveSprite(oldPos, pos);

				} else {

					spend(TICK);
					state = WANDERING;
					target = Dungeon.level.randomDestination();
					return true;
				}
			}
		}

		@Override
		public String status() {
			return String.format(Game.getVar(R.string.Mob_StaHuntingStatus),
					getName());
		}
	}

	protected class Fleeing implements AiState {

		public static final String TAG = "FLEEING";

		@Override
		public boolean act(boolean enemyInFOV, boolean justAlerted) {
			enemySeen = enemyInFOV;
			if (enemyInFOV) {
				target = getEnemy().pos;
			}

			int oldPos = pos;
			if (target != -1 && getFurther(target)) {

				spend(1 / speed());
				return moveSprite(oldPos, pos);

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
			return String.format(Game.getVar(R.string.Mob_StaFleeingStatus),
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
			return String.format(Game.getVar(R.string.Mob_StaPassiveStatus),
					getName());
		}
	}

	public boolean isWallWalker() {
		return false;
	}

	public boolean isPet() {
		return fraction == Fraction.HEROES;
	}

	protected void swapPosition(final Hero hero) {
		
		int curPos = pos;
		
		moveSprite( pos, hero.pos );
		move( hero.pos );
		
		hero.getSprite().move( hero.pos, curPos );
		hero.move( curPos );
		
		hero.spend( 1 / hero.speed() );
		hero.busy();
	}
	
	public boolean interact(Hero hero) {
		if (fraction == Fraction.HEROES) {
			swapPosition(hero);
			return true;
		}
		
		return false;
	}

	protected Char getEnemy() {
		return enemy;
	}

	protected void setEnemy(Char enemy) {
		this.enemy = enemy;
	}
}
