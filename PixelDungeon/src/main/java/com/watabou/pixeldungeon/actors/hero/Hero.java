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

import android.support.annotation.NonNull;

import com.nyrds.android.util.Scrambler;
import com.nyrds.pixeldungeon.items.artifacts.IActingItem;
import com.nyrds.pixeldungeon.items.chaos.IChaosItem;
import com.nyrds.pixeldungeon.items.common.RatKingCrown;
import com.nyrds.pixeldungeon.items.common.armor.SpiderArmor;
import com.nyrds.pixeldungeon.items.guts.HeartOfDarkness;
import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.guts.SpiritOfPain;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Bones;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.GamesInProgress;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.Rankings;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.Web;
import com.watabou.pixeldungeon.actors.buffs.Barkskin;
import com.watabou.pixeldungeon.actors.buffs.Bleeding;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Charm;
import com.watabou.pixeldungeon.actors.buffs.Combo;
import com.watabou.pixeldungeon.actors.buffs.Cripple;
import com.watabou.pixeldungeon.actors.buffs.Fury;
import com.watabou.pixeldungeon.actors.buffs.GasesImmunity;
import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.actors.buffs.Ooze;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.actors.buffs.Regeneration;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.actors.buffs.SnipersMark;
import com.watabou.pixeldungeon.actors.buffs.Vertigo;
import com.watabou.pixeldungeon.actors.buffs.Weakness;
import com.watabou.pixeldungeon.actors.hero.HeroAction.Attack;
import com.watabou.pixeldungeon.actors.mobs.Fraction;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.Rat;
import com.watabou.pixeldungeon.actors.mobs.npcs.NPC;
import com.watabou.pixeldungeon.effects.CheckedCell;
import com.watabou.pixeldungeon.effects.Flare;
import com.watabou.pixeldungeon.effects.Pushing;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.Amulet;
import com.watabou.pixeldungeon.items.Ankh;
import com.watabou.pixeldungeon.items.DewVial;
import com.watabou.pixeldungeon.items.Dewdrop;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Heap.Type;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.KindOfWeapon;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.items.keys.GoldenKey;
import com.watabou.pixeldungeon.items.keys.IronKey;
import com.watabou.pixeldungeon.items.keys.Key;
import com.watabou.pixeldungeon.items.keys.SkeletonKey;
import com.watabou.pixeldungeon.items.potions.PotionOfStrength;
import com.watabou.pixeldungeon.items.quest.CorpseDust;
import com.watabou.pixeldungeon.items.rings.RingOfAccuracy;
import com.watabou.pixeldungeon.items.rings.RingOfDetection;
import com.watabou.pixeldungeon.items.rings.RingOfElements;
import com.watabou.pixeldungeon.items.rings.RingOfEvasion;
import com.watabou.pixeldungeon.items.rings.RingOfHaste;
import com.watabou.pixeldungeon.items.rings.RingOfShadows;
import com.watabou.pixeldungeon.items.rings.RingOfStoneWalking;
import com.watabou.pixeldungeon.items.rings.RingOfThorns;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfRecharging;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.items.weapon.melee.Bow;
import com.watabou.pixeldungeon.items.weapon.melee.MeleeWeapon;
import com.watabou.pixeldungeon.items.weapon.melee.SpecialWeapon;
import com.watabou.pixeldungeon.items.weapon.missiles.Arrow;
import com.watabou.pixeldungeon.items.weapon.missiles.MissileWeapon;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.levels.TerrainFlags;
import com.watabou.pixeldungeon.levels.features.AlchemyPot;
import com.watabou.pixeldungeon.levels.features.Chasm;
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
import com.watabou.pixeldungeon.windows.WndTradeItem;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class Hero extends Char {

	private static final String TXT_LEAVE = Game.getVar(R.string.Hero_Leave);

	private static final String TXT_LEVEL_UP  = Game.getVar(R.string.Hero_LevelUp);
	private static final String TXT_NEW_LEVEL = Game.getVar(R.string.Hero_NewLevel);

	public static final String TXT_YOU_NOW_HAVE = Game.getVar(R.string.Hero_YouNowHave);

	private static final String TXT_SOMETHING_ELSE = Game.getVar(R.string.Hero_SomethingElse);
	private static final String TXT_LOCKED_CHEST   = Game.getVar(R.string.Hero_LockedChest);
	private static final String TXT_LOCKED_DOOR    = Game.getVar(R.string.Hero_LockedDoor);
	private static final String TXT_NOTICED_SMTH   = Game.getVar(R.string.Hero_NoticedSmth);

	private static final String TXT_WAIT   = Game.getVar(R.string.Hero_Wait);
	private static final String TXT_SEARCH = Game.getVar(R.string.Hero_Search);

	public static final int STARTING_STR = 10;

	private static final float TIME_TO_REST   = 1f;
	private static final float TIME_TO_SEARCH = 2f;

	public HeroClass    heroClass = HeroClass.ROGUE;
	public HeroSubClass subClass  = HeroSubClass.NONE;

	int attackSkill  = 10;
	int defenseSkill = 5;

	private boolean    ready      = false;
	public  HeroAction curAction  = null;
	public  HeroAction lastAction = null;

	private Char enemy;

	public Armor.Glyph killerGlyph = null;

	private Item theKey;

	public boolean restoreHealth = false;

	public MissileWeapon rangedWeapon = null;
	public Belongings belongings;

	private int STR;
	public boolean weakened = false;

	private float awareness;

	private int lvl = Scrambler.scramble(1);
	private int exp = Scrambler.scramble(0);
	private int sp  = Scrambler.scramble(0);

	public String levelKind;
	public String levelId;

	@NonNull
	private ArrayList<Mob> visibleEnemies = new ArrayList<>();

	@NonNull
	private Collection<Mob> pets = new ArrayList<>();

	public void addPet(@NonNull Mob pet) {
		pets.add(pet);
	}

	private int difficulty;

	public Hero() {
		name = Game.getVar(R.string.Hero_Name);
		name_objective = Game.getVar(R.string.Hero_Name_Objective);

		STR(STARTING_STR);
		awareness = 0.1f;
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

	@Override
	protected void readCharData() {
	}

	public int effectiveSTR() {
		int str = Scrambler.descramble(STR);
		return weakened ? str - 2 : str;
	}

	public void STR(int sTR) {
		STR = Scrambler.scramble(sTR);
	}

	public int STR() {
		return Scrambler.descramble(STR);
	}

	private static final String ATTACK     = "attackSkill";
	private static final String DEFENSE    = "defenseSkill";
	private static final String STRENGTH   = "STR";
	private static final String LEVEL      = "lvl";
	private static final String EXPERIENCE = "exp";
	private static final String LEVEL_KIND = "levelKind";
	private static final String LEVEL_ID   = "levelId";
	private static final String DIFFICULTY = "difficulty";
	private static final String PETS       = "pets";
	private static final String SP         = "sp";

	private void refreshPets() {
		ArrayList<Mob> alivePets = new ArrayList<>();
		for (Mob pet : pets) {
			if (pet.isAlive() && pet.fraction() == Fraction.HEROES) {
				alivePets.add(pet);
			}
		}
		pets = alivePets;
	}

	@NonNull
	public Collection<Mob> getPets() {
		return pets;
	}

	public void releasePets() {
		pets = new ArrayList<>();
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);

		heroClass.storeInBundle(bundle);
		subClass.storeInBundle(bundle);

		bundle.put(ATTACK, attackSkill);
		bundle.put(DEFENSE, defenseSkill);

		bundle.put(STRENGTH, STR());

		bundle.put(LEVEL, lvl());
		bundle.put(EXPERIENCE, getExp());
		bundle.put(LEVEL_KIND, levelKind);
		bundle.put(LEVEL_ID, levelId);
		bundle.put(DIFFICULTY, getDifficulty());

		refreshPets();

		bundle.put(PETS, pets);
		bundle.put(SP, getSoulPoints());

		belongings.storeInBundle(bundle);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);

		heroClass = HeroClass.restoreFromBundle(bundle);
		subClass = HeroSubClass.restoreFromBundle(bundle);

		attackSkill = bundle.getInt(ATTACK);
		defenseSkill = bundle.getInt(DEFENSE);

		STR(bundle.getInt(STRENGTH));
		updateAwareness();

		lvl(bundle.getInt(LEVEL));
		setExp(bundle.getInt(EXPERIENCE));
		levelKind = bundle.getString(LEVEL_KIND);
		levelId = bundle.optString(LEVEL_ID, "unknown");
		setDifficulty(bundle.optInt(DIFFICULTY, 2));

		Collection<Mob> _pets = bundle.getCollection(PETS, Mob.class);

		for (Mob pet : _pets) {
			pets.add(pet);
		}

		sp = Scrambler.scramble(bundle.optInt(SP, 0));

		belongings.restoreFromBundle(bundle);

		gender = heroClass.getGender();
	}

	public static void preview(GamesInProgress.Info info, Bundle bundle) {
		info.level = bundle.getInt(LEVEL);
	}

	public String className() {
		return subClass == null || subClass == HeroSubClass.NONE ? heroClass.title() : subClass.title();
	}

	private void live() {
		if (buff(Regeneration.class) == null) {
			Buff.affect(this, Regeneration.class);
		}
		if (buff(Hunger.class) == null) {
			Buff.affect(this, Hunger.class);
		}
	}

	public int tier() {
		return belongings.armor == null ? 0 : belongings.armor.tier;
	}

	public boolean bowEquiped() {
		return belongings.weapon instanceof Bow;
	}

	public boolean shoot(Char enemy, MissileWeapon wep) {

		rangedWeapon = wep;
		boolean result = attack(enemy);
		rangedWeapon = null;

		return result;
	}

	@Override
	public int attackSkill(Char target) {

		int bonus = 0;
		for (Buff buff : buffs(RingOfAccuracy.Accuracy.class)) {
			bonus += ((RingOfAccuracy.Accuracy) buff).level;
		}
		float accuracy = (bonus == 0) ? 1 : (float) Math.pow(1.4, bonus);
		if (rangedWeapon != null && Dungeon.level.distance(getPos(), target.getPos()) == 1) {
			accuracy *= 0.5f;
		}

		if (getDifficulty() == 0) {
			accuracy *= 1.2;
		}

		KindOfWeapon wep = rangedWeapon != null ? rangedWeapon : belongings.weapon;
		if (wep != null) {
			return (int) (attackSkill * accuracy * wep.accuracyFactor(this));
		} else {
			return (int) (attackSkill * accuracy);
		}
	}

	@Override
	public int defenseSkill(Char enemy) {

		int bonus = 0;
		for (Buff buff : buffs(RingOfEvasion.Evasion.class)) {
			bonus += ((RingOfEvasion.Evasion) buff).level;
		}
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
		int dr = belongings.armor != null ? Math.max(belongings.armor.DR, 0) : 0;
		Barkskin barkskin = buff(Barkskin.class);
		if (barkskin != null) {
			dr += barkskin.level();
		}
		return dr;
	}

	private boolean inFury() {
		return (buff(Fury.class) != null) || (buff(CorpseDust.UndeadRageAuraBuff.class) != null);
	}

	@Override
	public int damageRoll() {
		KindOfWeapon wep = rangedWeapon != null ? rangedWeapon : belongings.weapon;
		int dmg;
		if (wep != null) {
			dmg = wep.damageRoll(this);
		} else {
			dmg = effectiveSTR() > 10 ? Random.IntRange(1, effectiveSTR() - 9) : 1;
		}
		return inFury() ? (int) (dmg * 1.5f) : dmg;
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

	public float attackDelay() {
		KindOfWeapon wep = rangedWeapon != null ? rangedWeapon : belongings.weapon;
		if (wep != null) {

			return wep.speedFactor(this);

		} else {
			return 1f;
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

		for (Buff buff : buffs(RingOfHaste.Haste.class)) {
			hasteLevel += ((RingOfHaste.Haste) buff).level;
		}

		for (Item item : belongings) {
			if (item instanceof IActingItem && item.isEquipped(this)) {
				((IActingItem) item).spend(this,time);
			}
		}

		super.spend(hasteLevel == 0 ? time : (float) (time * Math.pow(1.1, -hasteLevel)));
	}

	public void spendAndNext(float time) {
		busy();
		spend(time);
		next();
	}

	@Override
	public boolean act() {

		super.act();

		if (paralysed) {

			curAction = null;

			spendAndNext(TICK);
			return false;
		}

		checkVisibleMobs();
		AttackIndicator.updateState();

		if (curAction == null) {
			if (restoreHealth) {
				if (isStarving() || hp() >= ht()) {
					restoreHealth = false;
				} else {
					spend(TIME_TO_REST);
					next();
					return false;
				}
			}

			if (PixelDungeon.realtime()) {
				if (!ready) {
					ready();
				}
				spend(TICK);
				next();
			} else {
				ready();
			}
			return false;

		} else {

			restoreHealth = false;

			ready = false;

			if (curAction instanceof HeroAction.Move) {

				return actMove((HeroAction.Move) curAction);

			} else if (curAction instanceof HeroAction.Interact) {

				return actInteract((HeroAction.Interact) curAction);

			} else if (curAction instanceof HeroAction.Buy) {

				return actBuy((HeroAction.Buy) curAction);

			} else if (curAction instanceof HeroAction.PickUp) {

				return actPickUp((HeroAction.PickUp) curAction);

			} else if (curAction instanceof HeroAction.OpenChest) {

				return actOpenChest((HeroAction.OpenChest) curAction);

			} else if (curAction instanceof HeroAction.Unlock) {

				return actUnlock((HeroAction.Unlock) curAction);

			} else if (curAction instanceof HeroAction.Descend) {

				return actDescend((HeroAction.Descend) curAction);

			} else if (curAction instanceof HeroAction.Ascend) {

				return actAscend((HeroAction.Ascend) curAction);

			} else if (curAction instanceof HeroAction.Attack) {

				return actAttack((HeroAction.Attack) curAction);

			} else if (curAction instanceof HeroAction.Cook) {

				return actCook((HeroAction.Cook) curAction);

			}
		}
		return false;
	}

	public void busy() {
		ready = false;
	}

	private void ready() {
		getSprite().idle();

		curAction = null;
		ready = true;

		GameScene.ready();
	}

	public void interrupt() {
		if (curAction != null && curAction.dst != getPos()) {
			lastAction = curAction;
		}
		curAction = null;
	}

	public void resume() {
		curAction = lastAction;
		lastAction = null;
		act();
	}

	private boolean actMove(HeroAction.Move action) {
		if (getCloser(action.dst)) {

			return true;

		} else {
			//TODO remove this in future
			if (Dungeon.level.map[getPos()] == Terrain.SIGN) {
				GameScene.show(new WndMessage(Dungeon.tip()));
			}
			ready();
			return false;
		}
	}

	private boolean actInteract(HeroAction.Interact action) {

		Mob npc = action.npc;

		if (Dungeon.level.adjacent(getPos(), npc.getPos())) {

			ready();
			getSprite().turnTo(getPos(), npc.getPos());
			if (!npc.interact(this)) {
				actAttack(new HeroAction.Attack(npc));
			}
			return false;

		} else {

			if (Dungeon.level.fieldOfView[npc.getPos()] && getCloser(npc.getPos())) {
				return true;
			} else {
				ready();
				return false;
			}
		}
	}

	private boolean actBuy(HeroAction.Buy action) {
		int dst = action.dst;
		if (getPos() == dst || Dungeon.level.adjacent(getPos(), dst)) {

			ready();

			Heap heap = Dungeon.level.getHeap(dst);
			if (heap != null && heap.type == Type.FOR_SALE && heap.size() == 1) {
				GameScene.show(new WndTradeItem(heap, true));
			}

			return false;

		} else if (getCloser(dst)) {

			return true;

		} else {
			ready();
			return false;
		}
	}

	private boolean actCook(HeroAction.Cook action) {
		int dst = action.dst;
		if (Dungeon.visible[dst]) {

			ready();
			AlchemyPot.operate(this, dst);
			return false;

		} else if (getCloser(dst)) {

			return true;

		} else {
			ready();
			return false;
		}
	}

	private boolean actPickUp(HeroAction.PickUp action) {
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
							GLog.i(TXT_SOMETHING_ELSE);
						}
						curAction = null;
					} else {
						Dungeon.level.drop(item, getPos()).sprite.drop();
						ready();
					}
				} else {
					ready();
				}
			} else {
				ready();
			}

			return false;

		} else if (getCloser(dst)) {

			return true;

		} else {
			ready();
			return false;
		}
	}

	public void itemPickedUp(Item item) {
		if (item instanceof Dewdrop) {
			return;
		}

		if ((item instanceof ScrollOfUpgrade && ((ScrollOfUpgrade) item).isKnown())
				|| (item instanceof PotionOfStrength && ((PotionOfStrength) item).isKnown())) {
			GLog.p(TXT_YOU_NOW_HAVE, item.name());
		} else {
			GLog.i(TXT_YOU_NOW_HAVE, item.name());
		}
	}

	private boolean actOpenChest(HeroAction.OpenChest action) {
		int dst = action.dst;
		if (Dungeon.level.adjacent(getPos(), dst) || getPos() == dst) {

			Heap heap = Dungeon.level.getHeap(dst);
			if (heap != null && (heap.type == Type.CHEST || heap.type == Type.TOMB || heap.type == Type.SKELETON
					|| heap.type == Type.LOCKED_CHEST || heap.type == Type.CRYSTAL_CHEST || heap.type == Type.MIMIC)) {

				theKey = null;

				if (heap.type == Type.LOCKED_CHEST || heap.type == Type.CRYSTAL_CHEST) {

					theKey = belongings.getKey(GoldenKey.class, Dungeon.depth, Dungeon.level.levelId);

					if (theKey == null) {
						GLog.w(TXT_LOCKED_CHEST);
						ready();
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
				ready();
			}

			return false;

		} else if (getCloser(dst)) {

			return true;

		} else {
			ready();
			return false;
		}
	}

	private boolean actUnlock(HeroAction.Unlock action) {
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
				GLog.w(TXT_LOCKED_DOOR);
				ready();
			}

			return false;

		} else if (getCloser(doorCell)) {
			return true;
		} else {
			ready();
			return false;
		}
	}

	private boolean actDescend(HeroAction.Descend action) {

		refreshPets();

		int stairs = action.dst;
		if (getPos() == stairs && Dungeon.level.isExit(getPos())) {

			Dungeon.level.onHeroDescend(getPos());

			clearActions();

			Hunger hunger = buff(Hunger.class);
			if (hunger != null && !hunger.isStarving()) {
				hunger.satisfy(-Hunger.STARVING / 10);
			}

			InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
			Game.switchScene(InterlevelScene.class);

			return false;

		} else if (getCloser(stairs)) {

			return true;

		} else {
			ready();
			return false;
		}
	}

	private boolean actAscend(HeroAction.Ascend action) {
		refreshPets();

		int stairs = action.dst;
		if (getPos() == stairs && getPos() == Dungeon.level.entrance) {

			if (Dungeon.depth == 1) {

				if (belongings.getItem(Amulet.class) == null) {
					GameScene.show(new WndMessage(TXT_LEAVE));
					ready();
				} else {
					Dungeon.win(ResultDescriptions.WIN, Rankings.gameOver.WIN_HAPPY);

					Dungeon.gameOver();

					Game.switchScene(SurfaceScene.class);
				}

			} else {

				clearActions();

				Hunger hunger = buff(Hunger.class);
				if (hunger != null && !hunger.isStarving()) {
					hunger.satisfy(-Hunger.STARVING / 10);
				}

				InterlevelScene.mode = InterlevelScene.Mode.ASCEND;
				Game.switchScene(InterlevelScene.class);
			}

			return false;

		} else if (getCloser(stairs)) {

			return true;

		} else {
			ready();
			return false;
		}
	}

	private boolean getCloserToEnemy() {
		if (Dungeon.level.fieldOfView[enemy.getPos()] && getCloser(enemy.getPos())) {
			return true;
		} else {
			ready();
			return false;
		}
	}

	private boolean actMeleeAttack() {

		if (Dungeon.level.adjacent(getPos(), enemy.getPos())) {
			spend(attackDelay());
			getSprite().attack(enemy.getPos());

			return false;
		}
		return getCloserToEnemy();
	}

	private boolean actBowAttack() {

		Bow bow = (Bow) belongings.weapon;

		Class<? extends Arrow> arrowType = bow.arrowType();

		Arrow arrow;

		if (arrowType.equals(Arrow.class)) { // no arrow type selected
			arrow = belongings.getItem(Arrow.class);
		} else {
			arrow = belongings.getItem(arrowType);
			if (arrow == null) {
				arrow = belongings.getItem(Arrow.class);
			}
		}

		if (arrow != null) { // We have arrows!
			arrow.cast(this, enemy.getPos());
			ready();
			return false;
		} // no arrows? just get closer...

		return actMeleeAttack();

	}

	private boolean actAttack(HeroAction.Attack action) {

		enemy = action.target;

		if (enemy.isAlive() && !pacified) {
			if (belongings.weapon instanceof SpecialWeapon) {
				return actSpecialAttack(action);
			}

			if (bowEquiped()
					&& (!Dungeon.level.adjacent(getPos(), enemy.getPos()) || this.heroClass == HeroClass.ELF)) {
				return actBowAttack();
			} else {
				return actMeleeAttack();
			}

		}

		return getCloserToEnemy();
	}

	private boolean applySpecialTo(SpecialWeapon weapon, Char enemy) {
		spend(attackDelay());
		getSprite().attack(enemy.getPos());
		weapon.applySpecial(this, enemy);
		return false;
	}

	private boolean actSpecialAttack(Attack action) {
		SpecialWeapon weapon = (SpecialWeapon) belongings.weapon;

		if (weapon.getRange() == 1) {
			if (Dungeon.level.adjacent(getPos(), enemy.getPos())) {
				return applySpecialTo(weapon, enemy);
			}
			return getCloserToEnemy();
		} else {

			Ballistica.cast(getPos(), action.target.getPos(), false, true);

			for (int i = 1; i <= Math.min(Ballistica.distance, weapon.getRange()); i++) {
				Char chr = Actor.findChar(Ballistica.trace[i]);
				if (chr == enemy) {
					return applySpecialTo(weapon, chr);
				}
			}

			return getCloserToEnemy();
		}
	}

	public void rest(boolean tillHealthy) {
		spendAndNext(TIME_TO_REST);
		if (!tillHealthy) {
			getSprite().showStatus(CharSprite.DEFAULT, TXT_WAIT);
		}
		restoreHealth = tillHealthy;
	}

	@Override
	public int attackProc(Char enemy, int damage) {
		KindOfWeapon wep = rangedWeapon != null ? rangedWeapon : belongings.weapon;
		if (wep != null) {

			wep.proc(this, enemy, damage);

			switch (subClass) {
				case GLADIATOR:
					if (wep instanceof MeleeWeapon) {
						damage += Buff.affect(this, Combo.class).hit(enemy, damage);
					}
					break;
				case BATTLEMAGE:
					if (wep instanceof Wand) {
						Wand wand = (Wand) wep;
						if (wand.curCharges() < wand.maxCharges() && damage > 0) {

							wand.curCharges(wand.curCharges() + 1);
							QuickSlot.refresh();

							ScrollOfRecharging.charge(this);
						}
						damage += wand.curCharges();
					}
					break;
				case SNIPER:
					if (rangedWeapon != null) {
						Buff.prolong(enemy, SnipersMark.class, attackDelay() * 1.1f);
					}
					break;
				case SHAMAN:
					if (wep instanceof Wand) {
						Wand wand = (Wand) wep;
						if (wand.affectTarget()) {
							if (Random.Int(4) == 0) {
								wand.zap(enemy.getPos());
							}
						}
					}
					break;
				default:
			}
		}

		for (Item item : belongings) {
			if (item instanceof IChaosItem && item.isEquipped(this)) {
				((IChaosItem) item).ownerDoesDamage(this, damage);
			}
		}

		return damage;
	}

	@Override
	public int defenseProc(Char enemy, int damage) {
		damage = super.defenseProc(enemy, damage);

		RingOfThorns.Thorns thorns = buff(RingOfThorns.Thorns.class);
		if (thorns != null) {
			int dmg = Random.IntRange(0, damage);
			if (dmg > 0) {
				enemy.damage(dmg, thorns);
			}
		}

		if (buff(HeartOfDarkness.HeartOfDarknessBuff.class) != null) {
			int spiritPos = Dungeon.level.getEmptyCellNextTo(getPos());

			if (Dungeon.level.cellValid(spiritPos)) {
				SpiritOfPain spirit = new SpiritOfPain();
				spirit.setPos(spiritPos);
				Dungeon.level.spawnMob(spirit, 0);
				Actor.addDelayed(new Pushing(spirit, getPos(), spirit.getPos()), -1);
				Mob.makePet(spirit, this);
			}
		}

		if (belongings.armor != null) {
			damage = belongings.armor.proc(enemy, this, damage);
		}

		return damage;
	}

	@Override
	public void damage(int dmg, Object src) {
		restoreHealth = false;
		super.damage(dmg, src);

		checkIfFurious();
		interrupt();

		if (belongings.armor instanceof SpiderArmor) {
			//Armor proc
			if (Random.Int(100) < 50) {
				GameScene.add(Blob.seed(getPos(), Random.Int(5, 7), Web.class));
			}
		}

		for (Item item : belongings) {
			if (item instanceof IChaosItem && item.isEquipped(this)) {
				if (!(src instanceof Hunger)) {
					((IChaosItem) item).ownerTakesDamage(dmg);
				}
			}
		}
	}

	public void checkIfFurious() {
		if (subClass == HeroSubClass.BERSERKER && 0 < hp() && hp() <= ht() * Fury.LEVEL) {
			if (buff(Fury.class) == null) {
				Buff.affect(this, Fury.class);
				ready();
			}
		}
	}

	private void checkVisibleMobs() {
		ArrayList<Mob> visible = new ArrayList<>();

		boolean newMob = false;

		for (Mob m : Dungeon.level.mobs) {
			if (Dungeon.level.fieldOfView[m.getPos()] && m.isHostile()) {
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
	}

	public Mob getNearestEnemy() {

		Mob nearest = null;
		int dist = Integer.MAX_VALUE;
		for (Mob mob : visibleEnemies) {
			int mobDist = Dungeon.level.distance(getPos(), mob.getPos());
			if (mobDist < dist) {
				dist = mobDist;
				nearest = mob;
			}
		}
		return nearest;
	}

	public int visibleEnemies() {
		return visibleEnemies.size();
	}

	public Mob visibleEnemy(int index) {
		return visibleEnemies.get(index % visibleEnemies.size());
	}

	private boolean getCloser(final int target) {

		if (rooted) {
			return false;
		}

		int step = -1;

		Buff wallWalkerBuff = buff(RingOfStoneWalking.StoneWalking.class);

		if (Dungeon.level.adjacent(getPos(), target)) {

			if (Actor.findChar(target) == null) {
				if (Dungeon.level.pit[target] && !flying && !Chasm.jumpConfirmed) {
					Chasm.heroJump(this);
					interrupt();
					return false;
				}

				if (wallWalkerBuff == null && (Dungeon.level.passable[target] || Dungeon.level.avoid[target])) {
					step = target;
				}
				if (wallWalkerBuff != null && Dungeon.level.solid[target]) {
					step = target;
				}

				LevelObject obj = Dungeon.level.objects.get(target);
				if (obj != null && obj.pushable()) {
					interrupt();
					if (!obj.push(this)) {
						return false;
					}
				}
			}

		} else {

			int len = Dungeon.level.getLength();
			boolean[] p = wallWalkerBuff != null ? Dungeon.level.solid : Dungeon.level.passable;
			boolean[] v = Dungeon.level.visited;
			boolean[] m = Dungeon.level.mapped;
			boolean[] passable = new boolean[len];
			for (int i = 0; i < len; i++) {
				passable[i] = p[i] && (v[i] || m[i]);
			}

			step = Dungeon.findPath(this, getPos(), target, passable, Dungeon.level.fieldOfView);
		}

		if (step != -1) {

			int oldPos = getPos();

			LevelObject obj = Dungeon.level.objects.get(step);
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

			move(step);
			getSprite().move(oldPos, getPos());

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

	public boolean handle(int cell) {

		if (!Dungeon.level.cellValid(cell)) {
			return false;
		}

		Char ch;
		Heap heap;

		if (Dungeon.level.map[cell] == Terrain.ALCHEMY && cell != getPos()) {

			curAction = new HeroAction.Cook(cell);

		} else if (Dungeon.level.fieldOfView[cell] && (ch = Actor.findChar(cell)) instanceof Mob) {

			Mob mob = (Mob) ch;

			if (ch instanceof NPC && ((NPC) ch).friendly()) {
				curAction = new HeroAction.Interact(mob);
			} else if (mob.isPet()) {
				curAction = new HeroAction.Interact(mob);
			} else {
				curAction = new HeroAction.Attack(ch);
			}

		} else if ((heap = Dungeon.level.getHeap(cell)) != null) {

			switch (heap.type) {
				case HEAP:
					curAction = new HeroAction.PickUp(cell);
					break;
				case FOR_SALE:
					curAction = heap.size() == 1 && heap.peek().price() > 0 ? new HeroAction.Buy(cell)
							: new HeroAction.PickUp(cell);
					break;
				default:
					curAction = new HeroAction.OpenChest(cell);
			}

		} else if (Dungeon.level.map[cell] == Terrain.LOCKED_DOOR || Dungeon.level.map[cell] == Terrain.LOCKED_EXIT) {

			curAction = new HeroAction.Unlock(cell);

		} else if (Dungeon.level.isExit(cell)) {

			curAction = new HeroAction.Descend(cell);

		} else if (cell == Dungeon.level.entrance) {

			curAction = new HeroAction.Ascend(cell);

		} else {

			curAction = new HeroAction.Move(cell);
			lastAction = null;

		}

		return act();
	}

	public void earnExp(int exp) {

		this.setExp(this.getExp() + exp);

		boolean levelUp = false;
		while (this.getExp() >= maxExp()) {
			this.setExp(this.getExp() - maxExp());
			lvl(lvl() + 1);

			ht(ht() + 5);
			hp(hp() + 5);
			attackSkill++;
			defenseSkill++;

			if (lvl() < 10) {
				updateAwareness();
			}

			levelUp = true;
		}

		if (levelUp) {

			GLog.p(TXT_NEW_LEVEL, lvl());
			getSprite().showStatus(CharSprite.POSITIVE, TXT_LEVEL_UP);
			Sample.INSTANCE.play(Assets.SND_LEVELUP);

			Badges.validateLevelReached();
		}

		if (subClass == HeroSubClass.WARLOCK) {

			int value = Math.min(ht() - hp(), 1 + (Dungeon.depth - 1) / 5);
			if (value > 0) {
				hp(hp() + value);
				getSprite().emitter().burst(Speck.factory(Speck.HEALING), 1);
			}

			buff(Hunger.class).satisfy(10);
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

	public boolean isStarving() {
		return buff(Hunger.class).isStarving();
	}

	@Override
	public void updateSpriteState() {
		super.updateSpriteState();
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
	public void remove(Buff buff) {
		super.remove(buff);

		BuffIndicator.refreshHero();
	}

	@Override
	public int stealth() {
		int stealth = super.stealth();
		for (Buff buff : buffs(RingOfShadows.Shadows.class)) {
			stealth += ((RingOfShadows.Shadows) buff).level;
		}
		return stealth;
	}

	@Override
	public void die(Object cause) {
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
			if (this.subClass == HeroSubClass.LICH && this.getSoulPoints() == this.getSoulPointsMax()) {
				this.spendSoulPoints(this.getSoulPointsMax());
				Dungeon.deleteGame(false);
				GameScene.show(new WndResurrect(null, cause));
			} else {
				reallyDie(cause);
			}
		} else {
			Dungeon.deleteGame(false);
			while (belongings.removeItem(ankh)) {}
			GameScene.show(new WndResurrect(ankh, cause));
		}
	}

	public void clearActions() {
		curAction = null;
		lastAction = null;
	}

	public static void reallyDie(Object cause) {

		int length = Dungeon.level.getLength();
		int[] map = Dungeon.level.map;
		boolean[] visited = Dungeon.level.visited;
		boolean[] discoverable = Dungeon.level.discoverable;

		for (int i = 0; i < length; i++) {
			int terr = map[i];
			if (discoverable[i]) {
				visited[i] = true;
				if ((TerrainFlags.flags[terr] & TerrainFlags.SECRET) != 0) {
					Dungeon.level.set(i, Terrain.discover(terr));
					GameScene.updateMap(i);
				}
			}
		}

		Bones.leave();

		Dungeon.observe();

		Dungeon.hero.belongings.identify();

		GameScene.gameOver();

		if (cause instanceof Hero.Doom) {
			((Hero.Doom) cause).onDeath();
		}

		Dungeon.gameOver();
	}

	@Override
	public void move(int step) {
		super.move(step);

		if (!flying) {

			if (Dungeon.level.water[getPos()]) {
				Sample.INSTANCE.play(Assets.SND_WATER, 1, 1, Random.Float(0.8f, 1.25f));
			} else {
				Sample.INSTANCE.play(Assets.SND_STEP);
			}
			Dungeon.level.pressHero(getPos(), this);
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

		if (enemy instanceof Rat && buff(RatKingCrown.RatKingAuraBuff.class) != null) {
			Rat rat = (Rat) enemy;
			Mob.makePet(rat, this);
		} else {
			AttackIndicator.target(enemy);
			attack(enemy);
		}
		curAction = null;

		Invisibility.dispel(this);

		super.onAttackComplete();
	}

	@Override
	public void onOperateComplete() {

		if (curAction instanceof HeroAction.Unlock) {

			if (theKey != null) {
				theKey.detach(belongings.backpack);
				theKey = null;
			}

			int doorCell = ((HeroAction.Unlock) curAction).dst;
			int door = Dungeon.level.map[doorCell];

			switch (door) {
				case Terrain.LOCKED_DOOR:
					Dungeon.level.set(doorCell, Terrain.DOOR);
					break;
				case Terrain.LOCKED_EXIT:
					Dungeon.level.set(doorCell, Terrain.UNLOCKED_EXIT);
					break;
				default:
					EventCollector.logException(new Exception("trying to unlock tile:" + door));
			}
			GameScene.updateMap(doorCell);

		} else if (curAction instanceof HeroAction.OpenChest) {

			if (theKey != null) {
				theKey.detach(belongings.backpack);
				theKey = null;
			}

			Heap heap = Dungeon.level.getHeap(((HeroAction.OpenChest) curAction).dst);
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

		int positive = 0;
		int negative = 0;
		for (Buff buff : buffs(RingOfDetection.Detection.class)) {
			int bonus = ((RingOfDetection.Detection) buff).level;
			if (bonus > positive) {
				positive = bonus;
			} else if (bonus < 0) {
				negative += bonus;
			}
		}
		int distance = 1 + positive + negative;

		float level = intentional ? (2 * awareness - awareness * awareness) : awareness;
		if (distance <= 0) {
			level /= 2 - distance;
			distance = 1;
		}

		int cx = getPos() % Dungeon.level.getWidth();
		int cy = getPos() / Dungeon.level.getWidth();
		int ax = cx - distance;
		if (ax < 0) {
			ax = 0;
		}
		int bx = cx + distance;
		if (bx >= Dungeon.level.getWidth()) {
			bx = Dungeon.level.getWidth() - 1;
		}
		int ay = cy - distance;
		if (ay < 0) {
			ay = 0;
		}
		int by = cy + distance;
		if (by >= Dungeon.level.getHeight()) {
			by = Dungeon.level.getHeight() - 1;
		}

		for (int y = ay; y <= by; y++) {
			for (int x = ax, p = ax + y * Dungeon.level.getWidth(); x <= bx; x++, p++) {

				if (Dungeon.visible[p]) {

					if (intentional) {
						getSprite().getParent().addToBack(new CheckedCell(p));
					}

					if (Dungeon.level.secret[p] && (intentional || Random.Float() < level)) {

						int oldValue = Dungeon.level.map[p];

						GameScene.discoverTile(p, oldValue);

						Dungeon.level.set(p, Terrain.discover(oldValue));

						GameScene.updateMap(p);

						ScrollOfMagicMapping.discover(p);

						smthFound = true;
					}
				}
			}
		}

		if (intentional) {
			getSprite().showStatus(CharSprite.DEFAULT, TXT_SEARCH);
			getSprite().operate(getPos());
			if (smthFound) {
				spendAndNext(Random.Float() < level ? TIME_TO_SEARCH : TIME_TO_SEARCH * 2);
			} else {
				spendAndNext(TIME_TO_SEARCH);
			}

		}

		if (smthFound) {
			GLog.w(TXT_NOTICED_SMTH);
			Sample.INSTANCE.play(Assets.SND_SECRET);
			interrupt();
		}

		return smthFound;
	}

	public void resurrect(int resetLevel) {

		hp(ht());
		Dungeon.gold(0);
		setExp(0);

		belongings.resurrect(resetLevel);

		live();
	}

	@Override
	public Set<Class<?>> resistances() {
		RingOfElements.Resistance r = buff(RingOfElements.Resistance.class);
		return r == null ? super.resistances() : r.resistances();
	}

	@Override
	public Set<Class<?>> immunities() {
		GasesImmunity buff = buff(GasesImmunity.class);
		return buff == null ? super.immunities() : GasesImmunity.IMMUNITIES;
	}

	@Override
	protected CharSprite sprite() {
		return new HeroSpriteDef(this);
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

	public int lvl() {
		return Scrambler.descramble(lvl);
	}

	private void lvl(int lvl) {
		this.lvl = Scrambler.scramble(lvl);
	}

	public int getExp() {
		return Scrambler.descramble(exp);
	}

	public void setExp(int exp) {
		this.exp = Scrambler.scramble(exp);
	}

	public interface Doom {
		void onDeath();
	}

	public void updateLook() {
		getHeroSprite().heroUpdated(this);
		ready();
	}

	public void collect(Item item) {
		if (!item.collect(this)) {
			if (Dungeon.level != null && getPos() != 0) {
				Dungeon.level.drop(item, getPos()).sprite.drop();
			}
		}
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

	public void spawnPets() {
		refreshPets();

		for (Mob pet : pets) {
			int cell = Dungeon.level.getEmptyCellNextTo(getPos());
			if (cell == -1) {
				cell = getPos();
			}
			pet.setPos(cell);

			pet.setState(pet.WANDERING);
			Dungeon.level.spawnMob(pet);
			pet.regenSprite();
		}
	}

	private void setDifficulty(int difficulty) {
		this.difficulty = difficulty;
		Dungeon.setDifficulty(difficulty);
	}

	public void accumulateSoulPoints() {
		int sp = Scrambler.descramble(this.sp);
		sp = sp + 1;
		if (sp > getSoulPointsMax()) {
			sp = getSoulPointsMax();
		}
		this.sp = Scrambler.scramble(sp);
	}

	public int getSoulPoints() {
		return Scrambler.descramble(sp);
	}

	public int getSoulPointsMax() {
		if (this.subClass == HeroSubClass.LICH) {
			return 50;
		}
		if (this.heroClass == HeroClass.NECROMANCER) {
			return 25;
		}
		return 0;
	}

	public boolean spendSoulPoints(int cost) {
		if (cost > getSoulPoints()) {
			return false;
		}
		sp = Scrambler.scramble(Scrambler.descramble(sp) - cost);
		return true;
	}
}
