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
package com.watabou.pixeldungeon.actors;

import com.nyrds.android.util.Scrambler;
import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Bleeding;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Cripple;
import com.watabou.pixeldungeon.actors.buffs.Frost;
import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.actors.buffs.Levitation;
import com.watabou.pixeldungeon.actors.buffs.Light;
import com.watabou.pixeldungeon.actors.buffs.MindVision;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.actors.buffs.Shadows;
import com.watabou.pixeldungeon.actors.buffs.Sleep;
import com.watabou.pixeldungeon.actors.buffs.Slow;
import com.watabou.pixeldungeon.actors.buffs.Speed;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.buffs.Vertigo;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.actors.mobs.Boss;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.particles.PoisonParticle;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.levels.features.Door;
import com.watabou.pixeldungeon.plants.Earthroot;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Char extends Actor {

	private static final String TXT_HIT[]    = Game.getVars(R.array.Char_Hit);
	private static final String TXT_KILL[]   = Game.getVars(R.array.Char_Kill);
	private static final String TXT_DEFEAT[] = Game.getVars(R.array.Char_Defeat);

	private static final String TXT_YOU_MISSED = Game.getVar(R.string.Char_YouMissed);
	private static final String TXT_SMB_MISSED = Game.getVar(R.string.Char_SmbMissed);

	private static final String TXT_OUT_OF_PARALYSIS = Game.getVar(R.string.Char_OutParalysis);

	private int pos = 0;

	protected CharSprite sprite;


	protected String name           = Game.getVar(R.string.Char_Name);
	protected String name_objective = Game.getVar(R.string.Char_Name_Objective);

	protected String description = Game.getVar(R.string.Mob_Desc);
	private   String defenceVerb = null;

	protected int gender = Utils.NEUTER;

	private int HT;
	private int HP;

	protected float baseSpeed = 1;

	public boolean paralysed = false;
	public boolean pacified  = false;
	public boolean rooted    = false;
	public boolean flying    = false;
	public int     invisible = 0;

	public int viewDistance = 8;

	protected HashSet<Buff> buffs = new HashSet<>();

	@Override
	protected boolean act() {
		Dungeon.level.updateFieldOfView(this);
		return false;
	}

	private static final String POS    = "pos";
	private static final String TAG_HP = "HP";
	private static final String TAG_HT = "HT";
	private static final String BUFFS  = "buffs";

	@Override
	public void storeInBundle(Bundle bundle) {

		super.storeInBundle(bundle);

		bundle.put(POS, getPos());
		bundle.put(TAG_HP, hp());
		bundle.put(TAG_HT, ht());
		bundle.put(BUFFS, buffs);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {

		super.restoreFromBundle(bundle);

		setPos(bundle.getInt(POS));
		hp(bundle.getInt(TAG_HP));
		ht(bundle.getInt(TAG_HT));

		boolean hungerAttached = false;
		boolean hungerBugSend = false;

		for (Buff b : bundle.getCollection(BUFFS, Buff.class)) {
			if (b != null) {
				if (b instanceof Hunger) {
					if (!hungerAttached) {
						hungerAttached = true;
					} else {
						if (!hungerBugSend) {
							EventCollector.logEvent("bug", "hunger count");
							hungerBugSend = true;
							continue;
						}
					}
				}
				b.attachTo(this);
			}
		}

		readCharData();
	}

	private String getClassParam(String paramName, String defaultValue, boolean warnIfAbsent) {
		return Utils.getClassParam(this.getClass().getSimpleName(), paramName, defaultValue, warnIfAbsent);
	}

	protected void readCharData() {

		name = getClassParam("Name", "Unnamed", true);
		name_objective = getClassParam("Name_Objective", name, true);

		description = getClassParam("Desc", description, true);

		gender = Utils.genderFromString(getClassParam("Gender", "masculine", true));

		defenceVerb = getClassParam("Defense", null, false);
	}

	public boolean attack(Char enemy) {

		boolean visibleFight = Dungeon.visible[getPos()] || Dungeon.visible[enemy.getPos()];

		if (hit(this, enemy, false)) {

			if (visibleFight) {
				GLog.i(TXT_HIT[gender], name, enemy.getName_objective());
			}

			// FIXME
			int dr = this instanceof Hero && ((Hero) this).rangedWeapon != null && ((Hero) this).subClass == HeroSubClass.SNIPER ? 0 :
					Random.IntRange(0, enemy.dr());

			int dmg = damageRoll();
			int effectiveDamage = Math.max(dmg - dr, 0);

			effectiveDamage = attackProc(enemy, effectiveDamage);
			effectiveDamage = enemy.defenseProc(this, effectiveDamage);
			enemy.damage(effectiveDamage, this);

			if (visibleFight) {
				Sample.INSTANCE.play(Assets.SND_HIT, 1, 1, Random.Float(0.8f, 1.25f));
			}

			enemy.getSprite().bloodBurstA(getSprite().center(), effectiveDamage);
			enemy.getSprite().flash();

			if (!enemy.isAlive() && visibleFight) {
				if (enemy == Dungeon.hero) {

					if (Dungeon.hero.killerGlyph != null) {

						Dungeon.fail(Utils.format(ResultDescriptions.GLYPH, Dungeon.hero.killerGlyph.name(), Dungeon.depth));
						GLog.n(TXT_KILL[Dungeon.hero.gender], Dungeon.hero.killerGlyph.name());

					} else {
						if (this instanceof Boss) {
							Dungeon.fail(Utils.format(ResultDescriptions.BOSS, name, Dungeon.depth));
						} else {
							Dungeon.fail(Utils.format(ResultDescriptions.MOB,
									Utils.indefinite(name), Dungeon.depth));
						}

						GLog.n(TXT_KILL[gender], name);
					}

				} else {
					GLog.i(TXT_DEFEAT[gender], name, enemy.getName_objective());
				}
			}

			return true;

		} else {

			if (visibleFight) {
				String defense = enemy.defenseVerb();
				enemy.getSprite().showStatus(CharSprite.NEUTRAL, defense);
				if (this == Dungeon.hero) {
					GLog.i(TXT_YOU_MISSED, enemy.name, defense);
				} else {
					GLog.i(TXT_SMB_MISSED, enemy.name, defense, name);
				}

				Sample.INSTANCE.play(Assets.SND_MISS);
			}

			return false;

		}
	}

	public static boolean hit(Char attacker, Char defender, boolean magic) {
		float acuRoll = Random.Float(attacker.attackSkill(defender));
		float defRoll = Random.Float(defender.defenseSkill(attacker));
		return (magic ? acuRoll * 2 : acuRoll) >= defRoll;
	}

	public int attackSkill(Char target) {
		return 0;
	}

	public int defenseSkill(Char enemy) {
		return 0;
	}

	public String defenseVerb() {
		if (defenceVerb != null) {
			return defenceVerb;
		}
		return Game.getVars(R.array.Char_StaDodged)[gender];
	}

	public int dr() {
		return 0;
	}

	public int damageRoll() {
		return 1;
	}

	public int attackProc(Char enemy, int damage) {
		return damage;
	}

	public int defenseProc(Char enemy, int damage) {

		Earthroot.Armor armor = buff(Earthroot.Armor.class);
		if (armor != null) {
			damage = armor.absorb(damage);
		}

		return damage;
	}

	public float speed() {
		return buff(Cripple.class) == null ? baseSpeed : baseSpeed * 0.5f;
	}

	public void damage(int dmg, Object src) {

		if (!isAlive()) {
			return;
		}

		Buff.detach(this, Frost.class);

		Class<?> srcClass = src.getClass();
		if (immunities().contains(srcClass)) {
			dmg = 0;
		} else if (resistances().contains(srcClass)) {
			dmg = Random.IntRange(0, dmg);
		}

		if (buff(Paralysis.class) != null) {
			if (Random.Int(dmg) >= Random.Int(hp())) {
				Buff.detach(this, Paralysis.class);
				if (Dungeon.visible[getPos()]) {
					GLog.i(TXT_OUT_OF_PARALYSIS, getName_objective());
				}
			}
		}

		hp(hp() - dmg);
		if (dmg > 0 || src instanceof Char) {
			getSprite().showStatus(hp() > ht() / 2 ?
							CharSprite.WARNING :
							CharSprite.NEGATIVE,
					Integer.toString(dmg));
		}
		if (hp()<=0) {
			die(src);
		}
	}

	public void destroy() {
		hp(0);
		Actor.remove(this);
		Actor.freeCell(getPos());
	}

	public void die(Object src) {
		destroy();
		getSprite().die();
	}

	public boolean isAlive() {
		return hp() > 0;
	}

	@Override
	protected void spend(float time) {

		float timeScale = 1f;
		if (buff(Slow.class) != null) {
			timeScale *= 0.5f;
		}
		if (buff(Speed.class) != null) {
			timeScale *= 2.0f;
		}

		super.spend(time / timeScale);
	}

	public HashSet<Buff> buffs() {
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


	public void add(Buff buff) {

		buffs.add(buff);
		Actor.add(buff);

		if (!GameScene.isSceneReady()) {
			return;
		}

		if (buff instanceof Poison) {

			CellEmitter.center(getPos()).burst(PoisonParticle.SPLASH, 5);
			getSprite().showStatus(CharSprite.NEGATIVE, Game.getVar(R.string.Char_StaPoisoned));

		} else if (buff instanceof Amok) {

			getSprite().showStatus(CharSprite.NEGATIVE, Game.getVar(R.string.Char_StaAmok));

		} else if (buff instanceof Slow) {

			getSprite().showStatus(CharSprite.NEGATIVE, Game.getVar(R.string.Char_StaSlowed));

		} else if (buff instanceof MindVision) {

			getSprite().showStatus(CharSprite.POSITIVE, Game.getVar(R.string.Char_StaMind));
			getSprite().showStatus(CharSprite.POSITIVE, Game.getVar(R.string.Char_StaVision));

		} else if (buff instanceof Paralysis) {

			getSprite().add(CharSprite.State.PARALYSED);
			getSprite().showStatus(CharSprite.NEGATIVE, Game.getVar(R.string.Char_StaParalysed));

		} else if (buff instanceof Terror) {

			getSprite().showStatus(CharSprite.NEGATIVE, Game.getVar(R.string.Char_StaFrightened));

		} else if (buff instanceof Roots) {

			getSprite().showStatus(CharSprite.NEGATIVE, Game.getVar(R.string.Char_StaRooted));

		} else if (buff instanceof Cripple) {

			getSprite().showStatus(CharSprite.NEGATIVE, Game.getVar(R.string.Char_StaCrippled));

		} else if (buff instanceof Bleeding) {

			getSprite().showStatus(CharSprite.NEGATIVE, Game.getVar(R.string.Char_StaBleeding));

		} else if (buff instanceof Vertigo) {

			getSprite().showStatus(CharSprite.NEGATIVE, Game.getVar(R.string.Char_StaDizzy));

		} else if (buff instanceof Sleep) {
			getSprite().idle();
		} else if (buff instanceof Light) {
			getSprite().add(CharSprite.State.ILLUMINATED);
		} else if (buff instanceof Burning) {
			getSprite().add(CharSprite.State.BURNING);
		} else if (buff instanceof Levitation) {
			getSprite().add(CharSprite.State.LEVITATING);
		} else if (buff instanceof Frost) {
			getSprite().add(CharSprite.State.FROZEN);
		} else if (buff instanceof Invisibility) {
			if (!(buff instanceof Shadows)) {
				getSprite().showStatus(CharSprite.POSITIVE, Game.getVar(R.string.Char_StaInvisible));
			}
			getSprite().add(CharSprite.State.INVISIBLE);
		}

	}

	public void remove(Buff buff) {

		buffs.remove(buff);
		Actor.remove(buff);

		if (buff instanceof Burning) {
			getSprite().remove(CharSprite.State.BURNING);
		} else if (buff instanceof Levitation) {
			getSprite().remove(CharSprite.State.LEVITATING);
		} else if (buff instanceof Invisibility && invisible <= 0) {
			getSprite().remove(CharSprite.State.INVISIBLE);
		} else if (buff instanceof Paralysis) {
			getSprite().remove(CharSprite.State.PARALYSED);
		} else if (buff instanceof Frost) {
			getSprite().remove(CharSprite.State.FROZEN);
		} else if (buff instanceof Light) {
			getSprite().remove(CharSprite.State.ILLUMINATED);
		}
	}

	public void remove(Class<? extends Buff> buffClass) {
		for (Buff buff : buffs(buffClass)) {
			remove(buff);
		}
	}

	@Override
	protected void onRemove() {
		for (Buff buff : buffs.toArray(new Buff[buffs.size()])) {
			buff.detach();
		}
	}

	public void updateSpriteState() {
		getSprite().removeAllStates();
		for (Buff buff : buffs) {
			if (buff instanceof Burning) {
				getSprite().add(CharSprite.State.BURNING);
			} else if (buff instanceof Levitation) {
				getSprite().add(CharSprite.State.LEVITATING);
			} else if (buff instanceof Invisibility) {
				getSprite().add(CharSprite.State.INVISIBLE);
			} else if (buff instanceof Paralysis) {
				getSprite().add(CharSprite.State.PARALYSED);
			} else if (buff instanceof Frost) {
				getSprite().add(CharSprite.State.FROZEN);
			} else if (buff instanceof Light) {
				getSprite().add(CharSprite.State.ILLUMINATED);
			}
		}
	}

	public int stealth() {
		return 0;
	}

	public void move(int step) {

		if (buff(Vertigo.class) != null && Dungeon.level.adjacent(getPos(), step)) { //ignore vertigo when blinking or teleporting
			List<Integer> candidates = new ArrayList<>();
			for (int dir : Level.NEIGHBOURS8) {
				int p = getPos() + dir;
				if (Dungeon.level.cellValid(p)) {
					if ((Dungeon.level.passable[p] || Dungeon.level.avoid[p]) && Actor.findChar(p) == null) {
						candidates.add(p);
					}
				}
			}

			if (candidates.isEmpty()) { // Nowhere to move? just stay then
				return;
			}

			step = Random.element(candidates);
		}

		if (Dungeon.level.map[getPos()] == Terrain.OPEN_DOOR) {
			Door.leave(getPos());
		}

		setPos(step);

		if (flying && Dungeon.level.map[getPos()] == Terrain.DOOR) {
			Door.enter(getPos());
		}

		if (this != Dungeon.hero) {
			getSprite().setVisible(Dungeon.visible[getPos()]);
		}
	}

	public int distance(Char other) {
		return Dungeon.level.distance(getPos(), other.getPos());
	}

	public void onMotionComplete() {
		next();
	}

	public void onAttackComplete() {
		next();
	}

	public void onOperateComplete() {
		next();
	}

	protected Set<Class<?>> IMMUNITIES  = new HashSet<>();
	protected Set<Class<?>> RESISTANCES = new HashSet<>();

	public Set<Class<?>> resistances() {
		return RESISTANCES;
	}

	public Set<Class<?>> immunities() {
		return IMMUNITIES;
	}

	public void updateSprite(){
		updateSprite(getSprite());
	}

	private void updateSprite(CharSprite sprite){
		sprite.setVisible(Dungeon.visible[getPos()]);
		GameScene.addMobSpriteDirect(sprite);
		sprite.link(this);
	}

	public void regenSprite() {
		sprite = null;
	}

	public CharSprite getSprite() {
		if (sprite == null) {

			if(!GameScene.isSceneReady()) {
				throw new TrackedRuntimeException("scene not ready for "+ this.getClass().getSimpleName());
			}
			sprite = sprite();
		}

		if(sprite.getParent()==null) {
			updateSprite(sprite);
		}

		return sprite;
	}

	protected abstract CharSprite sprite();

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

	public String getName() {
		return name;
	}

	public String getName_objective() {
		return name_objective;
	}

	public int getGender() {
		return gender;
	}

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public void onZapComplete() {
		next();
	}
}
