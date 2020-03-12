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
package com.watabou.pixeldungeon.actors.hero;

import com.nyrds.Packable;
import com.nyrds.android.util.ModdingMode;
import com.nyrds.android.util.Scrambler;
import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Sleeping;
import com.nyrds.pixeldungeon.items.artifacts.IActingItem;
import com.nyrds.pixeldungeon.items.chaos.IChaosItem;
import com.nyrds.pixeldungeon.items.common.RatKingCrown;
import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.nyrds.pixeldungeon.utils.DungeonGenerator;
import com.nyrds.pixeldungeon.utils.EntityIdSource;
import com.nyrds.pixeldungeon.utils.Position;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Bones;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.GamesInProgress;
import com.watabou.pixeldungeon.Rankings;
import com.watabou.pixeldungeon.RemixedDungeon;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Bleeding;
import com.watabou.pixeldungeon.actors.buffs.Blessed;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Charm;
import com.watabou.pixeldungeon.actors.buffs.Cripple;
import com.watabou.pixeldungeon.actors.buffs.Fury;
import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.actors.buffs.Ooze;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.actors.buffs.Regeneration;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.actors.buffs.Vertigo;
import com.watabou.pixeldungeon.actors.buffs.Weakness;
import com.watabou.pixeldungeon.actors.mobs.Fraction;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.Rat;
import com.watabou.pixeldungeon.actors.mobs.npcs.MirrorImage;
import com.watabou.pixeldungeon.actors.mobs.npcs.NPC;
import com.watabou.pixeldungeon.effects.CheckedCell;
import com.watabou.pixeldungeon.effects.Flare;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.effects.SpellSprite;
import com.watabou.pixeldungeon.items.Amulet;
import com.watabou.pixeldungeon.items.Ankh;
import com.watabou.pixeldungeon.items.DewVial;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Heap.Type;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.items.food.Food;
import com.watabou.pixeldungeon.items.keys.GoldenKey;
import com.watabou.pixeldungeon.items.keys.IronKey;
import com.watabou.pixeldungeon.items.keys.Key;
import com.watabou.pixeldungeon.items.keys.SkeletonKey;
import com.watabou.pixeldungeon.items.potions.PotionOfStrength;
import com.watabou.pixeldungeon.items.rings.RingOfAccuracy;
import com.watabou.pixeldungeon.items.rings.RingOfDetection;
import com.watabou.pixeldungeon.items.rings.RingOfEvasion;
import com.watabou.pixeldungeon.items.rings.RingOfHaste;
import com.watabou.pixeldungeon.items.rings.RingOfStoneWalking;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfRecharging;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.watabou.pixeldungeon.items.weapon.melee.KindOfBow;
import com.watabou.pixeldungeon.items.weapon.melee.SpecialWeapon;
import com.watabou.pixeldungeon.items.weapon.missiles.Arrow;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.levels.features.AlchemyPot;
import com.watabou.pixeldungeon.levels.features.Chasm;
import com.watabou.pixeldungeon.levels.traps.TrapHelper;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.InterlevelScene;
import com.watabou.pixeldungeon.scenes.SurfaceScene;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.HeroSpriteDef;
import com.watabou.pixeldungeon.ui.AttackIndicator;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.ui.QuickSlot;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.windows.WndMessage;
import com.watabou.pixeldungeon.windows.WndResurrect;
import com.watabou.pixeldungeon.windows.WndSaveSlotSelect;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.watabou.utils.SystemTime;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Hero extends Char {
	private static final String TXT_EXP = "%+dEXP";

	public static final int STARTING_STR = 10;

	private static final float TIME_TO_REST = 1f;
	private static final float TIME_TO_SEARCH = 2f;

	@Nullable
	static public Runnable doOnNextAction;

	private HeroClass heroClass = HeroClass.ROGUE;
	private HeroSubClass subClass = HeroSubClass.NONE;

	public boolean spellUser;

	@Packable
	private int attackSkill = 10;

	@Packable
	private int defenseSkill = 5;

	private boolean    ready      = false;
	public CharAction  lastAction = null;

	private Char enemy;

	@Packable(defaultValue = "-1")//EntityIdSource.INVALID_ID
	private int controlTargetId;

	public Armor.Glyph killerGlyph = null;

	private Item theKey;

	public boolean restoreHealth = false;

	private Belongings belongings;

	private int STR;

	private float awareness;

	private int magicLvl = Scrambler.scramble(1);
	private int exp = Scrambler.scramble(0);
	private int sp = Scrambler.scramble(0);
	private int maxSp = Scrambler.scramble(0);

	@Packable(defaultValue = "unknown")
	public String levelId;

	@Packable
	public Position portalLevelPos;

	private int difficulty;

	public Hero() {
		setupCharData();
		name = Game.getVar(R.string.Hero_Name);
		name_objective = Game.getVar(R.string.Hero_Name_Objective);

		fraction = Fraction.HEROES;

		STR(STARTING_STR);
		awareness = 0.1f;

		controlTargetId = getId();

		belongings = new Belongings(this);
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
		int str = Scrambler.descramble(STR);

		return hasBuff(Weakness.class) ? str - 2 : str;
	}

	public void STR(int sTR) {
		STR = Scrambler.scramble(sTR);
	}

	public int STR() {
		return Scrambler.descramble(STR);
	}

	private static final String STRENGTH = "STR";
	private static final String LEVEL = "lvl";
	private static final String EXPERIENCE = "exp";
	private static final String DIFFICULTY = "difficulty";
	private static final String SP = "sp";
	private static final String MAX_SP = "maxsp";
	private static final String IS_SPELL_USER = "isspelluser";
	private static final String MAGIC_LEVEL = "magicLvl";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);

		heroClass.storeInBundle(bundle);
		subClass.storeInBundle(bundle);

		bundle.put(STRENGTH, STR());

		bundle.put(LEVEL, lvl());
		bundle.put(EXPERIENCE, getExp());
		bundle.put(DIFFICULTY, getDifficulty());


		bundle.put(SP, getSkillPoints());
		bundle.put(MAX_SP, getSkillPointsMax());

		belongings.storeInBundle(bundle);

		bundle.put(IS_SPELL_USER, spellUser);
		bundle.put(MAGIC_LEVEL, skillLevel());
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);

		if(controlTargetId==EntityIdSource.INVALID_ID) {
			controlTargetId = getId();
		}

		heroClass = HeroClass.restoreFromBundle(bundle);
		subClass = HeroSubClass.restoreFromBundle(bundle);

		STR(bundle.getInt(STRENGTH));
		updateAwareness();

		lvl(bundle.getInt(LEVEL));
		setExp(bundle.getInt(EXPERIENCE));
		setDifficulty(bundle.optInt(DIFFICULTY, 2));

		{ // handle pre 28.6 saves
			ArrayList<Mob> loadedPets = new ArrayList<>(bundle.getCollection("pets", Mob.class));

			for (Mob pet : loadedPets) {
				Mob.makePet(pet, getId());
			}
		}

		sp = Scrambler.scramble(bundle.optInt(SP, 0));
		maxSp = Scrambler.scramble(bundle.optInt(MAX_SP, 10));

		belongings.restoreFromBundle(bundle);

		gender = heroClass.getGender();

		spellUser = bundle.optBoolean(IS_SPELL_USER, false);

		setSkillLevel(bundle.getInt(MAGIC_LEVEL));
	}

	public static void preview(GamesInProgress.Info info, Bundle bundle) {
		info.level = bundle.getInt(LEVEL);
	}

	public String className() {
		return subClass == null || subClass == HeroSubClass.NONE ? heroClass.title() : subClass.title();
	}

	private void live() {
		Buff.affect(this, Regeneration.class);
		Buff.affect(this, Hunger.class);
	}

	public int tier() {
		return belongings.armor == null ? 0 : belongings.armor.tier;
	}

	@Override
	public int attackSkill(Char target) {

		int bonus = buffLevel(RingOfAccuracy.Accuracy.class)
				  + buffLevel(Blessed.class);

		float accuracy = (float) Math.pow(1.4, bonus);

		if (rangedWeapon != null && level().distance(getPos(), target.getPos()) == 1) {
			accuracy *= 0.5f;
		}

		if (getDifficulty() == 0) {
			accuracy *= 1.2;
		}

		accuracy = getActiveWeapon().impactAccuracyFactor(this, accuracy);
		accuracy = getSecondaryWeapon().impactAccuracyFactor(this, accuracy);

		return (int) (attackSkill * accuracy);
	}

	@Override
	public int defenseSkill(Char enemy) {

		int bonus = buffLevel(RingOfEvasion.Evasion.class) + buffLevel(Blessed.class);

		float evasion = bonus == 0 ? 1 : (float) Math.pow(1.2, bonus);
		if (paralysed) {
			evasion /= 2;
		}

		if (getDifficulty() == 0) {
			evasion *= 1.2;
		}

		int aEnc = belongings.armor != null ? belongings.armor.STR - effectiveSTR() : 0;

		if (aEnc > 0) {
			return (int) (defenseSkill * evasion / Math.pow(1.5, aEnc));
		} else {

			if (heroClass == HeroClass.ROGUE) {

				if (curAction != null && subClass == HeroSubClass.FREERUNNER && !isStarving()) {
					evasion *= 2;
				}

				return (int) ((defenseSkill - aEnc) * evasion);
			} else {
				return (int) (defenseSkill * evasion);
			}
		}
	}

	@Override
	public int dr() {
		return belongings.armor != null ? Math.max(belongings.armor.DR, 0) : 0;
	}

	@Override
	public float speed() {

		int aEnc = belongings.armor != null ? belongings.armor.STR - effectiveSTR() : 0;
		if (aEnc > 0) {

			return (float) (super.speed() * Math.pow(1.3, -aEnc));

		} else {

			float speed = super.speed();
			return getHeroSprite().sprint(subClass == HeroSubClass.FREERUNNER && !isStarving()) ? 1.6f * speed : speed;

		}
	}

	@Override
	public void spend(float time) {
		int hasteLevel = 0;

		if (heroClass == HeroClass.ELF) {
			hasteLevel++;
			if (subClass == HeroSubClass.SCOUT) {
				hasteLevel++;
			}
		}

		hasteLevel+= buffLevel(RingOfHaste.Haste.class);

		for (Item item : belongings) {
			if (item instanceof IActingItem && item.isEquipped(this)) {
				((IActingItem) item).spend(this, time);
			}
		}

		QuickSlot.refresh();

		super.spend(hasteLevel == 0 ? time : (float) (time * Math.pow(1.1, -hasteLevel)));
	}

	@Override
	public void spendAndNext(float time) {
		busy();
		super.spendAndNext(time);
	}

	@Override
	public boolean act() {
		if(controlTargetId == getId()) {
			super.act();
		}

		if (paralysed) {
			curAction = null;
			spendAndNext(TICK);
			return false;
		}

		checkVisibleEnemies();

		if(controlTargetId != getId()) {
			curAction = null;
		}

		if (curAction == null) {

			if (restoreHealth) {
				if (isStarving() || hp() >= ht() || level().isSafe()) {
					restoreHealth = false;
				} else {
					spend(TIME_TO_REST);
					next();
					return false;
				}
			}

			if (Dungeon.realtime() || (controlTargetId != getId() && getControlTarget().curAction!=null) ) {
				if (!ready) {
					readyAndIdle();
				}
				spend(TICK);
				next();
			} else {
				readyAndIdle();
			}
			return false;

		} else {

			SystemTime.updateLastActionTime();

			restoreHealth = false;
			if(!Dungeon.realtime()) {
				busy();
			}

			if (curAction instanceof CharAction.Move) {

				return actMove((CharAction.Move) curAction);

			} else if (curAction instanceof CharAction.Interact) {

				return actInteract((CharAction.Interact) curAction);

			} else if (curAction instanceof CharAction.PickUp) {

				return actPickUp((CharAction.PickUp) curAction);

			} else if (curAction instanceof CharAction.OpenChest) {

				return actOpenChest((CharAction.OpenChest) curAction);

			} else if (curAction instanceof CharAction.Unlock) {

				return actUnlock((CharAction.Unlock) curAction);

			} else if (curAction instanceof CharAction.Descend) {

				return actDescend((CharAction.Descend) curAction);

			} else if (curAction instanceof CharAction.Ascend) {

				return actAscend((CharAction.Ascend) curAction);

			} else if (curAction instanceof CharAction.Attack) {

				return actAttack((CharAction.Attack) curAction);

			} else if (curAction instanceof CharAction.Cook) {

				return actCook((CharAction.Cook) curAction);

			}
		}
		return false;
	}

	@Override
	public void busy() {
		ready = false;
	}

	private void ready() {
		curAction = null;
		ready = true;

		GameScene.ready();
	}

	public void readyAndIdle() {
		ready();
		getSprite().idle();
	}

	public void interrupt() {
		if (curAction != null && curAction.dst != getPos()) {
			lastAction = curAction;
		}
		getSprite().interruptMotion();

		curAction = null;
	}

	public void resume() {
		curAction = lastAction;
		lastAction = null;

		getControlTarget().curAction = curAction;
		getControlTarget().act();
	}

	private boolean actMove(CharAction.Move action) {
		if (getCloser(action.dst)) {
			return true;
		} else {
			readyAndIdle();
			return false;
		}
	}

	private boolean actInteract(CharAction.Interact action) {

		Char chr = action.chr;

		if (Dungeon.level.adjacent(getPos(), chr.getPos())) {

			readyAndIdle();
			getSprite().turnTo(getPos(), chr.getPos());
			if (!chr.interact(this)) {
				actAttack(new CharAction.Attack(chr));
			}
			return false;

		} else {

			if (Dungeon.level.fieldOfView[chr.getPos()] && getCloser(chr.getPos())) {
				return true;
			} else {
				readyAndIdle();
				return false;
			}
		}
	}

	private boolean actCook(CharAction.Cook action) {
		int dst = action.dst;
		if (Dungeon.visible[dst]) {

			readyAndIdle();
			AlchemyPot.operate(this, dst);
			return false;

		} else if (getCloser(dst)) {

			return true;

		} else {
			readyAndIdle();
			return false;
		}
	}

	private boolean actPickUp(CharAction.PickUp action) {
		int dst = action.dst;
		if (getPos() == dst) {

			Heap heap = Dungeon.level.getHeap(getPos());
			if (heap != null) {
				Item item = heap.pickUp();
				item = item.pick(this, getPos());
				if (item != null) {
					if (item.doPickUp(this)) {

						itemPickedUp(item);

						if (!heap.isEmpty()) {
							GLog.i(Game.getVar(R.string.Hero_SomethingElse));
						}
						curAction = null;
					} else {
						Heap newHeap = Dungeon.level.drop(item, getPos());

						newHeap.sprite.drop();
						newHeap.pickUpFailed();

						readyAndIdle();
					}
				} else {
					readyAndIdle();
				}
			} else {
				readyAndIdle();
			}

			return false;

		} else if (getCloser(dst)) {

			return true;

		} else {
			readyAndIdle();
			return false;
		}
	}

	public void itemPickedUp(Item item) {
		if (item.announcePickUp()) {
			if ((item instanceof ScrollOfUpgrade && ((ScrollOfUpgrade) item).isKnown())
					|| (item instanceof PotionOfStrength && ((PotionOfStrength) item).isKnown())) {
				GLog.p(Game.getVar(R.string.Hero_YouNowHave), item.name());
			} else {
				GLog.i(getHeroYouNowHave(), item.name());
			}
		}
	}

	@Override
	public Char makeClone() {
		return new MirrorImage(this);
	}

	private boolean actOpenChest(CharAction.OpenChest action) {
		int dst = action.dst;
		if (Dungeon.level.adjacent(getPos(), dst) || getPos() == dst) {

			Heap heap = Dungeon.level.getHeap(dst);
			if (heap != null && (heap.type == Type.CHEST || heap.type == Type.TOMB || heap.type == Type.SKELETON
					|| heap.type == Type.LOCKED_CHEST || heap.type == Type.CRYSTAL_CHEST || heap.type == Type.MIMIC)) {

				theKey = null;

				if (heap.type == Type.LOCKED_CHEST || heap.type == Type.CRYSTAL_CHEST) {

					theKey = belongings.getKey(GoldenKey.class, Dungeon.depth, Dungeon.level.levelId);

					if (theKey == null) {
						GLog.w(Game.getVar(R.string.Hero_LockedChest));
						readyAndIdle();
						return false;
					}
				}

				switch (heap.type) {
					case TOMB:
						Sample.INSTANCE.play(Assets.SND_TOMB);
						Camera.main.shake(1, 0.5f);
						break;
					case SKELETON:
						break;
					default:
						Sample.INSTANCE.play(Assets.SND_UNLOCK);
				}

				spend(Key.TIME_TO_UNLOCK);
				getSprite().operate(dst);

			} else {
				readyAndIdle();
			}

			return false;

		} else if (getCloser(dst)) {

			return true;

		} else {
			readyAndIdle();
			return false;
		}
	}

	private boolean actUnlock(CharAction.Unlock action) {
		int doorCell = action.dst;

		if (Dungeon.level.adjacent(getPos(), doorCell)) {
			theKey = null;
			int door = Dungeon.level.map[doorCell];

			if (door == Terrain.LOCKED_DOOR) {
				theKey = belongings.getKey(IronKey.class, Dungeon.depth, Dungeon.level.levelId);
			} else if (door == Terrain.LOCKED_EXIT) {
				theKey = belongings.getKey(SkeletonKey.class, Dungeon.depth, Dungeon.level.levelId);
			}

			if (theKey != null) {
				spend(Key.TIME_TO_UNLOCK);
				getSprite().operate(doorCell);
				Sample.INSTANCE.play(Assets.SND_UNLOCK);
			} else {
				GLog.w(Game.getVar(R.string.Hero_LockedDoor));
				readyAndIdle();
			}

			return false;

		} else if (getCloser(doorCell)) {
			return true;
		} else {
			readyAndIdle();
			return false;
		}
	}

	private boolean actDescend(CharAction.Descend action) {

		int stairs = action.dst;
		if (getPos() == stairs && Dungeon.level.isExit(getPos())) {

			Dungeon.level.onHeroDescend(getPos());

			clearActions();

			if (!Dungeon.level.isSafe()) {
				hunger().satisfy(-Hunger.STARVING / 10);
			}

			InterlevelScene.Do(InterlevelScene.Mode.DESCEND);

			return false;

		} else if (getCloser(stairs)) {

			return true;

		} else {
			readyAndIdle();
			return false;
		}
	}

	private boolean actAscend(CharAction.Ascend action) {

		int stairs = action.dst;
		if (getPos() == stairs && getPos() == Dungeon.level.entrance) {

			Position nextLevel = DungeonGenerator.ascend(Dungeon.currentPosition());

			if (nextLevel.levelId.equals("0")) {

				if (belongings.getItem(Amulet.class) == null) {
					GameScene.show(new WndMessage(Game.getVar(R.string.Hero_Leave)));
					readyAndIdle();
				} else {
					Dungeon.win(ResultDescriptions.getDescription(ResultDescriptions.Reason.WIN), Rankings.gameOver.WIN_HAPPY);

					Dungeon.gameOver();

					Game.switchScene(SurfaceScene.class);
				}

			} else {

				clearActions();

				if (!Dungeon.level.isSafe()) {
					hunger().satisfy(-Hunger.STARVING / 10);
				}

				InterlevelScene.Do(InterlevelScene.Mode.ASCEND);
			}

			return false;

		} else if (getCloser(stairs)) {

			return true;

		} else {
			readyAndIdle();
			return false;
		}
	}

	private boolean getCloserToEnemy(int pos) {
		if (Dungeon.level.fieldOfView[pos] && getCloser(pos)) {
			return true;
		} else {
			readyAndIdle();
			return false;
		}
	}

	private boolean actMeleeAttack(Char enemy) {

		if (canAttack(enemy)) {
			spend(attackDelay());
			getSprite().attack(enemy.getPos());

			return false;
		}
		return getCloserToEnemy(enemy.getPos());
	}

	private boolean actBowAttack(Char enemy) {

		KindOfBow kindOfBow = (KindOfBow) belongings.weapon;

		Class<? extends Arrow> arrowType = kindOfBow.arrowType();

		Arrow arrow = belongings.getItem(arrowType);
		if(arrow==null || arrow.quantity() == 0) {
			arrow = belongings.getItem(Arrow.class);
		}

		if (arrow != null && arrow.quantity() > 0) { // We have arrows!
			arrow.cast(this, enemy.getPos());
			ready();
			return false;
		} // no arrows? Go Melee

		return actMeleeAttack(enemy);
	}

    private boolean actAttack(CharAction.Attack action) {

        enemy = action.target;

        if (enemy.isAlive() && !pacified) {

            if (bowEquipped()) {
                if (level().adjacent(getPos(), enemy.getPos()) && belongings.weapon.goodForMelee()) {
                    return actMeleeAttack(enemy);
                }
                return actBowAttack(enemy);
            }
            return actMeleeAttack(enemy);
        }
        return getCloserToEnemy(enemy.getPos());
    }

	public void rest(boolean tillHealthy) {
		spendAndNext(TIME_TO_REST);
		if (!tillHealthy) {
			getSprite().showStatus(CharSprite.DEFAULT, Game.getVar(R.string.Hero_Wait));
		}
		restoreHealth = tillHealthy;
	}

	@Override
	public int attackProc(@NotNull Char enemy, int damage) {

		damage = super.attackProc(enemy,damage);

		if (!(enemy instanceof NPC)) {
			for (Item item : belongings) {
				if (item instanceof IChaosItem && item.isEquipped(this)) {
					((IChaosItem) item).ownerDoesDamage(this, damage);
				}
			}
		}

		return damage;
	}

	@Override
	public void damage(int dmg, @NotNull NamedEntityKind src) {
		restoreHealth = false;
		super.damage(dmg, src);

		setControlTarget(this);

		checkIfFurious();
		interrupt();

		for (Item item : belongings) {
			if (item instanceof IChaosItem && item.isEquipped(this)) {
				if (!(src instanceof Hunger)) {
					((IChaosItem) item).ownerTakesDamage(dmg);
				}
			}
		}
	}

	public void checkVisibleEnemies() {
		ArrayList<Char> visible = new ArrayList<>();

		boolean newMob = false;

		for (Mob m : level().mobs) {
			if (level().fieldOfView[m.getPos()] && !m.friendly(this) && m.invisible <= 0) {
				visible.add(m);
				if (!visibleEnemies.contains(m)) {
					newMob = true;
				}
			}
		}

		if (newMob) {
			interrupt();
			restoreHealth = false;
		}

		visibleEnemies = visible;

		AttackIndicator.updateState(this);
	}

	public Char getNearestEnemy() {

		Char nearest = null;
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

	protected boolean getCloser(final int target) {

		if (hasBuff(Roots.class)) {
			return false;
		}

		int step = -1;

		Level level = level();
		Buff wallWalkerBuff = null;

		if (!level.isBossLevel()) {
			wallWalkerBuff = buff(RingOfStoneWalking.StoneWalking.class);
		}

		if (level.adjacent(getPos(), target)) {

			if (Actor.findChar(target) == null) {
				if (!hasBuff(Blindness.class)) {
					if (level.pit[target] && !isFlying() && !Chasm.jumpConfirmed) {
						Chasm.heroJump(this);
						interrupt();
						return false;
					}
					if (TrapHelper.isVisibleTrap(level.map[target]) && !isFlying() && !TrapHelper.stepConfirmed) {
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
					interrupt();
					if (!obj.push(this)) {
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

			step = Dungeon.findPath(this, getPos(), target, passable, level.fieldOfView);
		}

		if (step != -1) {

			int oldPos = getPos();

			LevelObject obj = level.getTopLevelObject(step);
			if (obj != null) {

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
					if(!mob.swapPosition(this)) {
						return false;
					}
					Dungeon.observe();
				}
			}

			move(step);
			moveSprite(oldPos,getPos());


			if (wallWalkerBuff != null) {
				int dmg = hp() / 2 > 2 ? hp() / 2 : 2;
				damage(dmg, wallWalkerBuff);
			}

			spend(1 / speed());

			return true;

		} else {
			return false;
		}
	}

    @Override
    protected boolean getFurther(int cell) {
        return false;
    }

    public boolean handle(int cell) {

		if (doOnNextAction != null) {
			doOnNextAction.run();
			doOnNextAction = null;
			return false;
		}

		Level level = level();

		if (!level.cellValid(cell)) {
			return false;
		}

		Char ch;
		Heap heap;

		level.updateFieldOfView(getControlTarget());

		if (level.map[cell] == Terrain.ALCHEMY && cell != getPos()) {

			curAction = new CharAction.Cook(cell);

		} else if (level.fieldOfView[cell] && (ch = Actor.findChar(cell)) != null && ch != getControlTarget()) {

			if (ch.friendly(getControlTarget())) {
				curAction = new CharAction.Interact(ch);
			} else {
				curAction = new CharAction.Attack(ch);
			}

		} else if ((heap = level.getHeap(cell)) != null) {

			if (heap.type == Type.HEAP) {
				curAction = new CharAction.PickUp(cell);
			} else {
				curAction = new CharAction.OpenChest(cell);
			}

		} else if (level.map[cell] == Terrain.LOCKED_DOOR || level.map[cell] == Terrain.LOCKED_EXIT) {

			curAction = new CharAction.Unlock(cell);

		} else if (level.isExit(cell)) {

			curAction = new CharAction.Descend(cell);

		} else if (cell == level.entrance) {

			curAction = new CharAction.Ascend(cell);

		} else {

			curAction = new CharAction.Move(cell);
			lastAction = null;

		}

		getControlTarget().curAction = curAction;
		return act();
	}

	public void earnExp(int exp) {

		this.setExp(this.getExp() + exp);

		getSprite().showStatus(CharSprite.POSITIVE, TXT_EXP, exp);

		boolean levelUp = false;
		while (this.getExp() >= maxExp()) {
			this.setExp(this.getExp() - maxExp());
			lvl(lvl() + 1);

			EventCollector.levelUp(heroClass.name()+"_"+subClass.name(),lvl());

			ht(ht() + 5);
			heal(5, this);

			attackSkill++;
			defenseSkill++;

			if (lvl() < 10) {
				updateAwareness();
			}

			levelUp = true;
		}

		if (levelUp) {

			GLog.p(Game.getVar(R.string.Hero_NewLevel), lvl());
			getSprite().showStatus(CharSprite.POSITIVE, Game.getVar(R.string.Hero_LevelUp));
			Sample.INSTANCE.play(Assets.SND_LEVELUP);

			if (this.getSkillPointsMax() > 0) {
				this.setMaxSkillPoints(getSkillPointsMax() + 1);
				this.accumulateSkillPoints(getSkillPointsMax() / 3);
			}

			Badges.validateLevelReached();
		}

		if (subClass == HeroSubClass.WARLOCK) {

			int value = Math.min(ht() - hp(), 1 + (Dungeon.depth - 1) / 5);
			if (value > 0) {
				hp(hp() + value);
				getSprite().emitter().burst(Speck.factory(Speck.HEALING), 1);
			}

			hunger().satisfy(10);
		}
	}

	public int maxExp() {
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
	public void add(Buff buff) {
		super.add(buff);

		if (!GameScene.isSceneReady()) {
			return;
		}

		if (buff instanceof Burning) {
			GLog.w(Game.getVar(R.string.Hero_StaBurning));
			interrupt();
		} else if (buff instanceof Paralysis) {
			GLog.w(Game.getVar(R.string.Hero_StaParalysis));
			interrupt();
		} else if (buff instanceof Poison) {
			GLog.w(Game.getVar(R.string.Hero_StaPoison));
			interrupt();
		} else if (buff instanceof Ooze) {
			GLog.w(Game.getVar(R.string.Hero_StaOoze));
		} else if (buff instanceof Roots) {
			GLog.w(Game.getVar(R.string.Hero_StaRoots));
		} else if (buff instanceof Weakness) {
			GLog.w(Game.getVar(R.string.Hero_StaWeakness));
		} else if (buff instanceof Blindness) {
			GLog.w(Game.getVar(R.string.Hero_StaBlindness));
		} else if (buff instanceof Fury) {
			GLog.w(Game.getVar(R.string.Hero_StaFury));
			getSprite().showStatus(CharSprite.POSITIVE, Game.getVar(R.string.Hero_StaFurious));
		} else if (buff instanceof Charm) {
			GLog.w(Game.getVar(R.string.Hero_StaCharm));
		} else if (buff instanceof Cripple) {
			GLog.w(Game.getVar(R.string.Hero_StaCripple));
		} else if (buff instanceof Bleeding) {
			GLog.w(Game.getVar(R.string.Hero_StaBleeding));
		} else if (buff instanceof Vertigo) {
			GLog.w(Game.getVar(R.string.Hero_StaVertigo));
			interrupt();
		}

		BuffIndicator.refreshHero();
	}

	@Override
	public void remove(@Nullable Buff buff) {
		super.remove(buff);

		BuffIndicator.refreshHero();
	}

	@Override
	public void die(NamedEntityKind cause) {

		Map<String, String> deathDesc = new HashMap<>();

		deathDesc.put("class", heroClass.name());
		deathDesc.put("subClass", subClass.name());
		deathDesc.put("level", Dungeon.level.levelId);
		deathDesc.put("cause", cause.getClass().getSimpleName());
		deathDesc.put("duration", Float.toString(Statistics.duration));

		deathDesc.put("difficulty", Integer.toString(Game.getDifficulty()));
		deathDesc.put("version", Game.version);
		deathDesc.put("mod", ModdingMode.activeMod());
		deathDesc.put("modVersion",Integer.toString(ModdingMode.activeModVersion()));

		deathDesc.put("donation",Integer.toString(RemixedDungeon.donated()));
		deathDesc.put("heroLevel", Integer.toString(lvl()));
		deathDesc.put("gameId",    Dungeon.gameId);


		EventCollector.logEvent("HeroDeath", deathDesc);

		clearActions();

		DewVial.autoDrink(this);
		if (isAlive()) {
			new Flare(8, 32).color(0xFFFF66, true).show(getSprite(), 2f);
			return;
		}

		Actor.fixTime();
		super.die(cause);

		Ankh ankh = belongings.getItem(Ankh.class);

		if (ankh == null) {
			if (this.subClass == HeroSubClass.LICH && this.getSkillPoints() == this.getSkillPointsMax()) {
				this.setSoulPoints(0);
				GameScene.show(new WndResurrect(null, cause));
			} else {
				reallyDie(cause);
			}
		} else {
			while (belongings.removeItem(ankh)) {
			}
			GameScene.show(new WndResurrect(ankh, cause));
		}
	}

	@Override
	protected float _attackDelay() {
		return 1f;
	}

	public void clearActions() {
		curAction = null;
		lastAction = null;
	}

	private static void reallyReallyDie(Object cause) {
		Dungeon.level.discover();

		Bones.leave();

		Dungeon.observe();

		Dungeon.hero.belongings.identify();

		GameScene.gameOver();

		if (cause instanceof Hero.Doom) {
			((Hero.Doom) cause).onDeath();
		}

		Dungeon.gameOver();
	}

	public static void reallyDie(final Object cause) {

		if (Dungeon.hero.getDifficulty() < 2 && !Game.isPaused()) {
			GameScene.show(new WndSaveSlotSelect(false, Game.getVar(R.string.Hero_AnotherTry)));
			return;
		}

		reallyReallyDie(cause);
	}

	@Override
	public void move(int step) {
		super.move(step);

		if (!isFlying()) {

			if (Dungeon.level.water[getPos()]) {
				Sample.INSTANCE.play(Assets.SND_WATER, 1, 1, Random.Float(0.8f, 1.25f));
			} else {
				Sample.INSTANCE.play(Assets.SND_STEP);
			}
		}
	}

	@Override
	public void onMotionComplete() {
		Dungeon.observe();
		search(false);

		super.onMotionComplete();
	}

	@Override
	public void onAttackComplete() {

		if (enemy != null) { // really strange crash here

			if (enemy instanceof Rat && hasBuff(RatKingCrown.RatKingAuraBuff.class)) {
				Rat rat = (Rat) enemy;
				Mob.makePet(rat, getId());
			} else {
				AttackIndicator.target(enemy);

				if (belongings.weapon instanceof SpecialWeapon) {
					((SpecialWeapon) belongings.weapon).preAttack(this, enemy);
				}

				if (attack(enemy)) {
					if (belongings.weapon instanceof SpecialWeapon) {
						((SpecialWeapon) belongings.weapon).postAttack(this, enemy);
					}
				}

			}
		} else {
			EventCollector.logException("hero attacks null enemy");
		}

		curAction = null;
		enemy     = null;

		Invisibility.dispel(this);

		super.onAttackComplete();
	}

	@Override
	public void onOperateComplete() {

		if (curAction instanceof CharAction.Unlock) {

			if (theKey != null) {
				theKey.detach(belongings.backpack);
				theKey = null;
			}

			int doorCell = ((CharAction.Unlock) curAction).dst;
			int door = Dungeon.level.map[doorCell];

			switch (door) {
				case Terrain.LOCKED_DOOR:
					Dungeon.level.set(doorCell, Terrain.DOOR);
					break;
				case Terrain.LOCKED_EXIT:
					Dungeon.level.set(doorCell, Terrain.UNLOCKED_EXIT);
					break;
				default:
					EventCollector.logException("trying to unlock tile:" + door);
			}
			GameScene.updateMap(doorCell);

		} else if (curAction instanceof CharAction.OpenChest) {

			if (theKey != null) {
				theKey.detach(belongings.backpack);
				theKey = null;
			}

			Heap heap = Dungeon.level.getHeap(((CharAction.OpenChest) curAction).dst);
			if (heap != null) {
				if (heap.type == Type.SKELETON) {
					Sample.INSTANCE.play(Assets.SND_BONES);
				}
				heap.open(this);
			}
		}
		curAction = null;

		super.onOperateComplete();
	}

	public boolean search(boolean intentional) {

		boolean smthFound = false;

		int distance = 1 + buffLevel(RingOfDetection.Detection.class);

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

				if (Dungeon.visible[p]) {

					if (intentional) {
						getSprite().getParent().addToBack(new CheckedCell(p));
					}

					if (intentional || Random.Float() < searchLevel) {

						if (level.secret[p]) {
							int oldValue = level.map[p];
							GameScene.discoverTile(p);
							level.set(p, Terrain.discover(oldValue));
							GameScene.updateMap(p);
							ScrollOfMagicMapping.discover(p);
							smthFound = true;
						}

						LevelObject obj = level.getLevelObject(p);
						if (obj != null && obj.secret()) {
							obj.discover();
							smthFound = true;
						}
					}
				}
			}
		}

		if (intentional) {
			getSprite().showStatus(CharSprite.DEFAULT, Game.getVar(R.string.Hero_Search));
			getSprite().operate(getPos());
			if (smthFound) {
				spendAndNext(Random.Float() < searchLevel ? TIME_TO_SEARCH : TIME_TO_SEARCH * 2);
			} else {
				spendAndNext(TIME_TO_SEARCH);
			}

		}

		if (smthFound) {
			GLog.w(Game.getVar(R.string.Hero_NoticedSmth));
			Sample.INSTANCE.play(Assets.SND_SECRET);
			interrupt();
		}

		return smthFound;
	}

	public void resurrect(int resetLevel) {
		belongings.resurrect(resetLevel);

		hp(ht());
		setExp(0);

		live();
	}

	@Override
	public Set<String> resistances() {
		Set <String> resistances = super.resistances();
		resistances.addAll(heroClass.resistances());
		resistances.addAll(subClass.resistances());
		return resistances;
	}

	@Override
	public Set<String> immunities() {
		Set <String> immunities = super.immunities();
		immunities.addAll(heroClass.immunities());
		immunities.addAll(subClass.immunities());
		return immunities;
	}

	@Override
    public CharSprite sprite() {
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

	public static String getHeroYouNowHave() {
		return Game.getVar(R.string.Hero_YouNowHave);
	}

	public int getExp() {
		return Scrambler.descramble(exp);
	}

	public void setExp(int exp) {
		this.exp = Scrambler.scramble(exp);
	}

	public boolean canAttack(Char enemy) {
		if (level().adjacent(getPos(), enemy.getPos())) {
			return true;
		}

		if (belongings.weapon instanceof SpecialWeapon) {
			SpecialWeapon weapon = (SpecialWeapon) belongings.weapon;

			Ballistica.cast(getPos(), enemy.getPos(), false, true);

			for (int i = 1; i <= Math.min(Ballistica.distance, weapon.getRange()); i++) {
				Char chr = Actor.findChar(Ballistica.trace[i]);
				if (chr == enemy) {
					return true;
				}
			}
		}

		if(belongings.weapon instanceof KindOfBow) {
			if(belongings.getItem(Arrow.class)!=null) {
				return enemy.getPos() == Ballistica.cast(getPos(), enemy.getPos(), false, true);
			}
		}

		return false;
	}

	public void eat(Item food, float energy, String message) {
		food.detach( belongings.backpack );

		hunger().satisfy(energy);

		GLog.i( message );

		switch (heroClass) {
        case WARRIOR:
            if (hp() < ht()) {
            	heal(5, food);
            }
            break;
        case MAGE:
            belongings.charge( false );
            ScrollOfRecharging.charge(this);
            break;
        default:
            break;
        }

		getSprite().operate( getPos() );
		busy();
		SpellSprite.show(this, SpellSprite.FOOD );
		Sample.INSTANCE.play( Assets.SND_EAT );

		spend( Food.TIME_TO_EAT );

		Statistics.foodEaten++;
		Badges.validateFoodEaten();
	}

	public void setControlTarget(Char controlTarget) {
		if(getControlTarget() instanceof Mob) {
			Mob controlledMob = (Mob) getControlTarget();
			controlledMob.releasePet();
			controlledMob.setState(MobAi.getStateByClass(Sleeping.class));
		}
		Camera.main.focusOn(controlTarget.getSprite());
		this.controlTargetId = controlTarget.getId();

	}

	@NotNull
	public Char getControlTarget() {

		Char controlTarget = CharsList.getById(controlTargetId);
		if(controlTarget.getId() == EntityIdSource.INVALID_ID) {
            EventCollector.logException("invalid control target");
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
        this.heroClass = heroClass;
    }

    @Override
    public HeroSubClass getSubClass() {
        return subClass;
    }

    public void setSubClass(HeroSubClass subClass) {
        this.subClass = subClass;
    }

    public interface Doom extends NamedEntityKind{
		void onDeath();
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

	public void setGender(int gender) {
		this.gender = gender;
	}

	private void setDifficulty(int difficulty) {
		this.difficulty = difficulty;
		Dungeon.setDifficulty(difficulty);
	}

	public void accumulateSkillPoints() {
		accumulateSkillPoints(1);
	}

	public void accumulateSkillPoints(int n) {
		setSoulPoints(Math.min(Scrambler.descramble(sp)+n, getSkillPointsMax()));
	}

	public int getSkillPoints() {
		return Scrambler.descramble(sp);
	}

	public int getSkillPointsMax() {
		return Scrambler.descramble(maxSp);
	}

	public void spendSkillPoints(int cost) {
		setSoulPoints(Scrambler.descramble(sp) - cost);
	}

	public boolean enoughSP(int cost) {
		return getSkillPoints() >= cost;
	}

	public void setSoulPoints(int points) {
		sp = Scrambler.scramble(Math.max(0,Math.min(points, getSkillPointsMax())));
		QuickSlot.refresh();
	}

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

	public int skillLevel() {
		return Scrambler.descramble(magicLvl);
	}

	@Override
	protected void moveSprite(int oldPos, int pos) {
		getSprite().move(oldPos, getPos());

	}

	public void setSkillLevel(int level) {
		magicLvl = Scrambler.scramble(level);
	}

	public void skillLevelUp() {
		setSkillLevel(skillLevel() + 1);
	}

    @NotNull
	@Override
    public Belongings getBelongings() {
        return belongings;
    }

	@Override
	public boolean friendly(@NotNull Char chr) {
		if(chr instanceof Mob) {
			Mob mob = (Mob)chr;
			return heroClass.friendlyTo(mob.getEntityKind());
		}
		return super.friendly(chr);
	}

	@Override
	public boolean ignoreDr() {
		return rangedWeapon != null && subClass == HeroSubClass.SNIPER;
	}

	@Override
	public boolean collect(Item item) {
		if(super.collect(item)) {
			QuickSlot.refresh();
			return true;
		}
		return false;
	}
}