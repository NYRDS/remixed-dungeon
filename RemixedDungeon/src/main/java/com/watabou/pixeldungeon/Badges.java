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
package com.watabou.pixeldungeon;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.necropolis.DreadKnight;
import com.nyrds.pixeldungeon.mobs.spiders.SpiderGuard;
import com.nyrds.pixeldungeon.mobs.spiders.SpiderMindAmber;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.mobs.Acidic;
import com.watabou.pixeldungeon.actors.mobs.Albino;
import com.watabou.pixeldungeon.actors.mobs.Bandit;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.Senior;
import com.watabou.pixeldungeon.actors.mobs.Shielded;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.bags.Keyring;
import com.watabou.pixeldungeon.items.bags.PotionBelt;
import com.watabou.pixeldungeon.items.bags.Quiver;
import com.watabou.pixeldungeon.items.bags.ScrollHolder;
import com.watabou.pixeldungeon.items.bags.SeedPouch;
import com.watabou.pixeldungeon.items.bags.WandHolster;
import com.watabou.pixeldungeon.items.potions.Potion;
import com.watabou.pixeldungeon.items.rings.Ring;
import com.watabou.pixeldungeon.items.rings.RingOfHaggler;
import com.watabou.pixeldungeon.items.rings.RingOfThorns;
import com.watabou.pixeldungeon.items.scrolls.Scroll;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;

import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import lombok.var;

public class Badges {

	public static boolean isSaveNeeded() {
		return saveNeeded;
	}

	public static void setSaveNeeded(boolean saveNeeded) {
		Badges.saveNeeded = saveNeeded;
		if(saveNeeded) {
			saveGlobal();
		}
	}

	public enum Badge {
		MONSTERS_SLAIN_1(StringsManager.getVar(R.string.Badges_MonsterSlain1), 0),
		MONSTERS_SLAIN_2(StringsManager.getVar(R.string.Badges_MonsterSlain2), 1),
		MONSTERS_SLAIN_3(StringsManager.getVar(R.string.Badges_MonsterSlain3), 2),
		MONSTERS_SLAIN_4(StringsManager.getVar(R.string.Badges_MonsterSlain4), 3),
		MONSTERS_SLAIN_5(StringsManager.getVar(R.string.Badges_MonsterSlain5), 73),

		GOLD_COLLECTED_1(StringsManager.getVar(R.string.Badges_GoldColleted1), 4),
		GOLD_COLLECTED_2(StringsManager.getVar(R.string.Badges_GoldColleted2), 5),
		GOLD_COLLECTED_3(StringsManager.getVar(R.string.Badges_GoldColleted3), 6),
		GOLD_COLLECTED_4(StringsManager.getVar(R.string.Badges_GoldColleted4), 7),
		GOLD_COLLECTED_5(StringsManager.getVar(R.string.Badges_GoldCollected5), 74),

		LEVEL_REACHED_1(StringsManager.getVar(R.string.Badges_LevelReached1), 8),
		LEVEL_REACHED_2(StringsManager.getVar(R.string.Badges_LevelReached2), 9),
		LEVEL_REACHED_3(StringsManager.getVar(R.string.Badges_LevelReached3), 10),
		LEVEL_REACHED_4(StringsManager.getVar(R.string.Badges_LevelReached4), 11),
		LEVEL_REACHED_5(StringsManager.getVar(R.string.Badges_LevelReached5), 75),

		ALL_POTIONS_IDENTIFIED(StringsManager.getVar(R.string.Badges_AllPotions), 16),
		ALL_SCROLLS_IDENTIFIED(StringsManager.getVar(R.string.Badges_AllScrolls), 17),
		ALL_RINGS_IDENTIFIED(StringsManager.getVar(R.string.Badges_AllRings), 18),
		ALL_WANDS_IDENTIFIED(StringsManager.getVar(R.string.Badges_AllWands), 19),
		ALL_ITEMS_IDENTIFIED(StringsManager.getVar(R.string.Badges_AllItems), 35, true),
		BAG_BOUGHT_SEED_POUCH,
		BAG_BOUGHT_SCROLL_HOLDER,
		BAG_BOUGHT_WAND_HOLSTER,
		BAG_BOUGHT_POTION_BELT,
		BAG_BOUGHT_KEY_RING,
		BAG_BOUGHT_QUIVER,
		ALL_BAGS_BOUGHT(StringsManager.getVar(R.string.Badges_AllBags), 23),
		DEATH_FROM_FIRE(StringsManager.getVar(R.string.Badges_DeathFire), 24),
		DEATH_FROM_POISON(StringsManager.getVar(R.string.Badges_DeathPoison), 25),
		DEATH_FROM_GAS(StringsManager.getVar(R.string.Badges_DeathGas), 26),
		DEATH_FROM_HUNGER(StringsManager.getVar(R.string.Badges_DeathHunger), 27),
		DEATH_FROM_GLYPH(StringsManager.getVar(R.string.Badges_DeathGlyph), 57),
		DEATH_FROM_FALLING(StringsManager.getVar(R.string.Badges_DeathFalling), 59),
		YASD(StringsManager.getVar(R.string.Badges_Yasd), 34, true),
		BOSS_SLAIN_1_WARRIOR,
		BOSS_SLAIN_1_MAGE,
		BOSS_SLAIN_1_ROGUE,
		BOSS_SLAIN_1_HUNTRESS,
		BOSS_SLAIN_1(StringsManager.getVar(R.string.Badges_BossSlain1), 12),
		BOSS_SLAIN_2(StringsManager.getVar(R.string.Badges_BossSlain2), 13),
		BOSS_SLAIN_3(StringsManager.getVar(R.string.Badges_BossSlain3), 14),
		BOSS_SLAIN_4(StringsManager.getVar(R.string.Badges_BossSlain4), 15),
		BOSS_SLAIN_1_ALL_CLASSES(StringsManager.getVar(R.string.Badges_BossSlain1All), 32, true),
		BOSS_SLAIN_3_GLADIATOR,
		BOSS_SLAIN_3_BERSERKER,
		BOSS_SLAIN_3_WARLOCK,
		BOSS_SLAIN_3_BATTLEMAGE,
		BOSS_SLAIN_3_FREERUNNER,
		BOSS_SLAIN_3_ASSASSIN,
		BOSS_SLAIN_3_SNIPER,
		BOSS_SLAIN_3_WARDEN,
		BOSS_SLAIN_3_ALL_SUBCLASSES(StringsManager.getVar(R.string.Badges_BossSlain3All), 33, true),
		RING_OF_HAGGLER(StringsManager.getVar(R.string.Badges_RingHaggler), 20),
		RING_OF_THORNS(StringsManager.getVar(R.string.Badges_RingThorns), 21),
		STRENGTH_ATTAINED_1(StringsManager.getVar(R.string.Badges_StrengthAttained1), 40),
		STRENGTH_ATTAINED_2(StringsManager.getVar(R.string.Badges_StrengthAttained2), 41),
		STRENGTH_ATTAINED_3(StringsManager.getVar(R.string.Badges_StrengthAttained3), 42),
		STRENGTH_ATTAINED_4(StringsManager.getVar(R.string.Badges_StrengthAttained4), 43),
		STRENGTH_ATTAINED_5(StringsManager.getVar(R.string.Badges_StrengthAttained5), 76),

		FOOD_EATEN_1(StringsManager.getVar(R.string.Badges_FoodEaten1), 44),
		FOOD_EATEN_2(StringsManager.getVar(R.string.Badges_FoodEaten2), 45),
		FOOD_EATEN_3(StringsManager.getVar(R.string.Badges_FoodEaten3), 46),
		FOOD_EATEN_4(StringsManager.getVar(R.string.Badges_FoodEaten4), 47),
		FOOD_EATEN_5(StringsManager.getVar(R.string.Badges_FoodEaten5), 77),
		MASTERY_WARRIOR,
		MASTERY_MAGE,
		MASTERY_ROGUE,
		MASTERY_HUNTRESS,
		ITEM_LEVEL_1(StringsManager.getVar(R.string.Badges_ItemLvl1), 48),
		ITEM_LEVEL_2(StringsManager.getVar(R.string.Badges_ItemLvl2), 49),
		ITEM_LEVEL_3(StringsManager.getVar(R.string.Badges_ItemLvl3), 50),
		ITEM_LEVEL_4(StringsManager.getVar(R.string.Badges_ItemLvl4), 51),
		ITEM_LEVEL_5(StringsManager.getVar(R.string.Badges_ItemLvl5), 78),
		RARE_ALBINO,
		RARE_BANDIT,
		RARE_SHIELDED,
		RARE_SENIOR,
		RARE_ACIDIC,
		RARE_SPIDER_SOLDIER,
		RARE_SPIDER_MIND,
		RARE_DREAD_KNIGHT,
		RARE_DEEP_SNAIL,
		RARE_SHAMAN_ELDER,
		RARE(StringsManager.getVar(R.string.Badges_RareAll), 37, true),
		VICTORY_WARRIOR,
		VICTORY_MAGE,
		VICTORY_ROGUE,
		VICTORY_HUNTRESS,
		VICTORY(StringsManager.getVar(R.string.Badges_Victory), 22),
		VICTORY_ALL_CLASSES(StringsManager.getVar(R.string.Badges_VictoryAll), 36, true),
		MASTERY_COMBO(StringsManager.getVar(R.string.Badges_MasteryCombo), 56),
		POTIONS_COOKED_1(StringsManager.getVar(R.string.Badges_PotionsCooked1), 52),
		POTIONS_COOKED_2(StringsManager.getVar(R.string.Badges_PotionsCooked2), 53),
		POTIONS_COOKED_3(StringsManager.getVar(R.string.Badges_PotionsCooked3), 54),
		POTIONS_COOKED_4(StringsManager.getVar(R.string.Badges_PotionsCooked4), 55),
		POTIONS_COOKED_5(StringsManager.getVar(R.string.Badges_PotionsCooked5), 79),

		NO_MONSTERS_SLAIN(StringsManager.getVar(R.string.Badges_NoMonsterSlain), 28),
		GRIM_WEAPON(StringsManager.getVar(R.string.Badges_GrimWepon), 29),
		PIRANHAS(StringsManager.getVar(R.string.Badges_Piranhas), 30),
		NIGHT_HUNTER(StringsManager.getVar(R.string.Badges_NightHunter), 58),
		GAMES_PLAYED_1(StringsManager.getVar(R.string.Badges_GamesPlayed1), 60, true),
		GAMES_PLAYED_2(StringsManager.getVar(R.string.Badges_GamesPlayed2), 61, true),
		GAMES_PLAYED_3(StringsManager.getVar(R.string.Badges_GamesPlayed3), 62, true),
		GAMES_PLAYED_4(StringsManager.getVar(R.string.Badges_GamesPlayed4), 63, true),
		GAMES_PLAYED_5(StringsManager.getVar(R.string.Badges_GamesPlayed5), 80, true),
		HAPPY_END(StringsManager.getVar(R.string.Badges_HappyEnd), 38),
		CHAMPION(StringsManager.getVar(R.string.Badges_Champion), 39, true),
		SUPPORTER(StringsManager.getVar(R.string.Badges_Supporter), 31, true),
		IMMURED(StringsManager.getVar(R.string.Badges_Immured), 64),
		LICH_SLAIN(StringsManager.getVar(R.string.Badges_Lich_Slain), 69),
		ICE_GUARDIAN_SLAIN(StringsManager.getVar(R.string.Badges_Ice_Guardian_Slain), 70),
		SPIDER_QUEEN_SLAIN(StringsManager.getVar(R.string.Badges_SpiderQueen_Slain), 66),
		SHADOW_LORD_SLAIN(StringsManager.getVar(R.string.Badges_ShadowLord_Slain), 67),
		YOG_SLAIN(StringsManager.getVar(R.string.Badges_Yog_Slain), 65),
		DEATH_FROM_NECROTISM(StringsManager.getVar(R.string.Badges_DeathNecrotism), 71),
		MASTERY_ELF, VICTORY_ELF, BOSS_SLAIN_1_ELF, BOSS_SLAIN_3_SHAMAN, BOSS_SLAIN_3_SCOUT,
		MASTERY_NECROMANCER, VICTORY_NECROMANCER, BOSS_SLAIN_1_NECROMANCER, BOSS_SLAIN_3_LICH, VICTORY_GNOLL, BOSS_SLAIN_1_GNOLL,
		GNOLL_UNLOCKED(StringsManager.getVar(R.string.Badges_GnollUnlocked),72, true),
		DOCTOR_QUEST_COMPLETED(StringsManager.getVar(R.string.MedicineMask_Obtained),81),
		MASTERY_GNOLL;

		public boolean meta;

		public String description;
		public int    image;

		Badge(String description, int image) {
			this(description, image, false);
		}

		Badge(String description, int image, boolean meta) {
			this.description = description;
			this.image = image;
			this.meta = meta;
		}

		Badge() {
			this(Utils.EMPTY_STRING, -1);
		}
	}


	private static HashSet<Badge> global;
	private static HashSet<Badge> local = new HashSet<>();

	private static boolean saveNeeded = false;

	public static void reset() {
		local.clear();
		loadGlobal();
	}

	public static final   String BADGES_FILE = "badges.dat";
	private static final  String BADGES_BACKUP_FILE = "badges_backup.dat";
	private static final  String BADGES      = "badges";

	private static HashSet<Badge> restore(Bundle bundle) {
		HashSet<Badge> badges = new HashSet<>();

		String[] names = bundle.getStringArray(BADGES);

		for (String name : names) {
			try {
				badges.add(Badge.valueOf(name));
			} catch (IllegalArgumentException ignored) { //Allow badge renaming

			}
        }
		return badges;
	}

	private static void store(Bundle bundle, HashSet<Badge> badges) {
		int count = 0;

		badges.remove(null);

		String[] names = new String[badges.size()];

		for (Badge badge : badges) {
			names[count++] = badge.toString();
		}
		bundle.put(BADGES, names);
	}

	public static void loadLocal(Bundle bundle) {
		local = restore(bundle);
	}

	public static void saveLocal(Bundle bundle) {
		store(bundle, local);
	}

	public static void loadGlobal() {
		loadGlobalFrom(BADGES_FILE);

		if(global.isEmpty()) {
			loadGlobalFrom(BADGES_BACKUP_FILE);
			setSaveNeeded(true);
		}
	}

	private static void loadGlobalFrom(String file){
		try {
			InputStream input = FileSystem.getInputStream(file);

			Bundle bundle = Bundle.read(input);
			input.close();

			global = restore(bundle);

			for (var badge:global) {
				unlockPlayGamesBadge(badge);
			}
		} catch (FileNotFoundException e) {
			global = new HashSet<>();
		} catch (Exception e) {
			global = new HashSet<>();
			EventCollector.logException(e, "Badges.loadGlobal");
		}
	}

	private static void saveGlobal() {
		if (!ModdingMode.inRemixed()) {
			return;
		}

		if (isSaveNeeded()) {
			Bundle bundle = new Bundle();
			store(bundle, global);

			try {
				OutputStream output = FileSystem.getOutputStream(BADGES_FILE);
				Bundle.write(bundle, output);
				output.close();

				var backup = FileSystem.getOutputStream(BADGES_BACKUP_FILE);
				Bundle.write(bundle, backup);
				backup.close();

				setSaveNeeded(false);
			} catch (IOException e) {
				EventCollector.logException(e, "Badges.saveGlobal");
			}
		}
	}

	public static void validateMonstersSlain() {
		Badge []badges = {Badge.MONSTERS_SLAIN_1,Badge.MONSTERS_SLAIN_2,Badge.MONSTERS_SLAIN_3,Badge.MONSTERS_SLAIN_4,Badge.MONSTERS_SLAIN_5};
		int []limits = {10, 50, 150, 250, 1000};
		Badge badge = awardBadge(badges, limits, Statistics.enemiesSlain);

		displayBadge(badge);
	}

	public static void validateGoldCollected() {

		Badge []badges = {Badge.GOLD_COLLECTED_1,Badge.GOLD_COLLECTED_2,Badge.GOLD_COLLECTED_3,Badge.GOLD_COLLECTED_4,Badge.GOLD_COLLECTED_5};
		int []limits = {100,500,2500,7500,35000};
		Badge badge = awardBadge(badges, limits, Statistics.goldCollected);

		displayBadge(badge);
	}

	public static void validateLevelReached() {

		Badge []badges = {Badge.LEVEL_REACHED_1,Badge.LEVEL_REACHED_2,Badge.LEVEL_REACHED_3,Badge.LEVEL_REACHED_4,Badge.LEVEL_REACHED_5};
		int []limits = {6,12,18,24,30};
		Badge badge = awardBadge(badges, limits, Dungeon.hero.lvl());
		
		displayBadge(badge);
	}

	private static Badge awardBadge(Badge [] badges, int [] limits, int score) {
		Badge badge = null;

		for(int i = 0;i<badges.length;++i) {
			if (!local.contains(badges[i]) && score >= limits[i]) {
				badge = badges[i];
				local.add(badge);
			}
		}
		return badge;
	}

	public static void validateStrengthAttained(Hero hero) {
		if(hero.getHeroClass() == HeroClass.GNOLL) {
			return;
		}

		Badge []badges = {Badge.STRENGTH_ATTAINED_1,Badge.STRENGTH_ATTAINED_2,Badge.STRENGTH_ATTAINED_3,Badge.STRENGTH_ATTAINED_4,Badge.STRENGTH_ATTAINED_5};
		int []limits = {13,15,17,19,21};
		Badge badge = awardBadge(badges, limits, Dungeon.hero.STR());

		displayBadge(badge);
	}

	public static void validateFoodEaten() {

		Badge []badges = {Badge.FOOD_EATEN_1,Badge.FOOD_EATEN_2,Badge.FOOD_EATEN_3,Badge.FOOD_EATEN_4,Badge.FOOD_EATEN_5};
		int []limits = {10,20,30,40,60};
		Badge badge = awardBadge(badges, limits, Statistics.foodEaten);

		displayBadge(badge);
	}

	public static void validatePotionsCooked() {

		Badge []badges = {Badge.POTIONS_COOKED_1,Badge.POTIONS_COOKED_2,Badge.POTIONS_COOKED_3,Badge.POTIONS_COOKED_4,Badge.POTIONS_COOKED_5};
		int []limits = {3,6,9,12,25};
		Badge badge = awardBadge(badges, limits, Statistics.potionsCooked);

		displayBadge(badge);
	}

	public static void validatePiranhasKilled() {
		Badge badge = null;

		if (!local.contains(Badge.PIRANHAS) && Statistics.piranhasKilled >= 6) {
			badge = Badge.PIRANHAS;
			local.add(badge);
		}

		displayBadge(badge);
	}

	public static void validateItemLevelAcquired(Item item) {

		// This method should be called:
		// 1) When an item is obtained (Item.collect)
		// 2) When an item is upgraded (ScrollOfUpgrade, ScrollOfWeaponUpgrade, ShortSword, WandOfMagicMissile)
		// 3) When an item is identified
		if (!item.isLevelKnown()) {
			return;
		}

		Badge []badges = {Badge.ITEM_LEVEL_1,Badge.ITEM_LEVEL_2,Badge.ITEM_LEVEL_3,Badge.ITEM_LEVEL_4,Badge.ITEM_LEVEL_5};
		int []limits = {3,6,9,12,15};
		Badge badge = awardBadge(badges, limits, item.level());

		displayBadge(badge);
	}

	public static void validateAllPotionsIdentified() {
		if (Dungeon.hero != null && Dungeon.hero.isAlive() &&
				!local.contains(Badge.ALL_POTIONS_IDENTIFIED) && Potion.allKnown()) {

			Badge badge = Badge.ALL_POTIONS_IDENTIFIED;
			local.add(badge);
			displayBadge(badge);

			validateAllItemsIdentified();
		}
	}

	public static void validateAllScrollsIdentified() {
		if (Dungeon.hero != null && Dungeon.hero.isAlive() &&
				!local.contains(Badge.ALL_SCROLLS_IDENTIFIED) && Scroll.allKnown()) {

			Badge badge = Badge.ALL_SCROLLS_IDENTIFIED;
			local.add(badge);
			displayBadge(badge);

			validateAllItemsIdentified();
		}
	}

	public static void validateAllRingsIdentified() {
		if (Dungeon.hero != null && Dungeon.hero.isAlive() &&
				!local.contains(Badge.ALL_RINGS_IDENTIFIED) && Ring.allKnown()) {

			Badge badge = Badge.ALL_RINGS_IDENTIFIED;
			local.add(badge);
			displayBadge(badge);

			validateAllItemsIdentified();
		}
	}

	public static void validateAllWandsIdentified() {
		if (Dungeon.hero != null && Dungeon.hero.isAlive() &&
				!local.contains(Badge.ALL_WANDS_IDENTIFIED) && Wand.allKnown()) {

			Badge badge = Badge.ALL_WANDS_IDENTIFIED;
			local.add(badge);
			displayBadge(badge);

			validateAllItemsIdentified();
		}
	}

	public static void validateAllBagsBought(Item bag) {

		Badge badge = null;
		if (bag instanceof SeedPouch) {
			badge = Badge.BAG_BOUGHT_SEED_POUCH;
		} else if (bag instanceof ScrollHolder) {
			badge = Badge.BAG_BOUGHT_SCROLL_HOLDER;
		} else if (bag instanceof WandHolster) {
			badge = Badge.BAG_BOUGHT_WAND_HOLSTER;
		} else if (bag instanceof PotionBelt) {
			badge = Badge.BAG_BOUGHT_POTION_BELT;
		} else if (bag instanceof Quiver) {
			badge = Badge.BAG_BOUGHT_QUIVER;
		} else if (bag instanceof Keyring) {
			badge = Badge.BAG_BOUGHT_KEY_RING;
		}

		if (badge != null) {

			local.add(badge);

			if (!local.contains(Badge.ALL_BAGS_BOUGHT) &&
					local.contains(Badge.BAG_BOUGHT_SCROLL_HOLDER) &&
					local.contains(Badge.BAG_BOUGHT_SEED_POUCH) &&
					local.contains(Badge.BAG_BOUGHT_WAND_HOLSTER) &&
					local.contains(Badge.BAG_BOUGHT_POTION_BELT) &&
					local.contains(Badge.BAG_BOUGHT_QUIVER) &&
					local.contains(Badge.BAG_BOUGHT_KEY_RING)
					) {

				badge = Badge.ALL_BAGS_BOUGHT;
				local.add(badge);
				displayBadge(badge);
			}
		}
	}

	private static void validateAllItemsIdentified() {
		if (!global.contains(Badge.ALL_ITEMS_IDENTIFIED) &&
				global.contains(Badge.ALL_POTIONS_IDENTIFIED) &&
				global.contains(Badge.ALL_SCROLLS_IDENTIFIED) &&
				global.contains(Badge.ALL_RINGS_IDENTIFIED) &&
				global.contains(Badge.ALL_WANDS_IDENTIFIED)) {

			Badge badge = Badge.ALL_ITEMS_IDENTIFIED;
			displayBadge(badge);
		}
	}

	public static void validateDeathFromFire() {
		Badge badge = Badge.DEATH_FROM_FIRE;
		local.add(badge);
		displayBadge(badge);

		validateYASD();
	}

	public static void validateDeathFromPoison() {
		Badge badge = Badge.DEATH_FROM_POISON;
		local.add(badge);
		displayBadge(badge);

		validateYASD();
	}

	public static void validateDeathFromGas() {
		Badge badge = Badge.DEATH_FROM_GAS;
		local.add(badge);
		displayBadge(badge);

		validateYASD();
	}

	public static void validateDeathFromHunger() {
		Badge badge = Badge.DEATH_FROM_HUNGER;
		local.add(badge);
		displayBadge(badge);

		validateYASD();
	}

	public static void validateDeathFromGlyph() {
		Badge badge = Badge.DEATH_FROM_GLYPH;
		local.add(badge);
		displayBadge(badge);
	}

	public static void validateDeathFromFalling() {
		Badge badge = Badge.DEATH_FROM_FALLING;
		local.add(badge);
		displayBadge(badge);
	}

	private static void validateYASD() {
		if (global.contains(Badge.DEATH_FROM_FIRE) &&
				global.contains(Badge.DEATH_FROM_POISON) &&
				global.contains(Badge.DEATH_FROM_GAS) &&
				global.contains(Badge.DEATH_FROM_HUNGER)) {

			Badge badge = Badge.YASD;
			local.add(badge);
			displayBadge(badge);
		}
	}

	public static void validateDeathFromNecrotism() {
		Badge badge = Badge.DEATH_FROM_NECROTISM;
		local.add(badge);
		displayBadge(badge);
	}

	public static void validateBossSlain(Badge badge) {

		local.add(badge);
		displayBadge(badge);

		if (badge == Badge.BOSS_SLAIN_1) {
			switch (Dungeon.hero.getHeroClass()) {
				case WARRIOR:
					badge = Badge.BOSS_SLAIN_1_WARRIOR;
					break;
				case MAGE:
					badge = Badge.BOSS_SLAIN_1_MAGE;
					break;
				case ROGUE:
					badge = Badge.BOSS_SLAIN_1_ROGUE;
					break;
				case HUNTRESS:
					badge = Badge.BOSS_SLAIN_1_HUNTRESS;
					break;
				case ELF:
					badge = Badge.BOSS_SLAIN_1_ELF;
					break;
				case NECROMANCER:
					badge = Badge.BOSS_SLAIN_1_NECROMANCER;
					break;
				case GNOLL:
					badge = Badge.BOSS_SLAIN_1_GNOLL;
					break;
			}
			local.add(badge);
			if (!global.contains(badge)) {
				global.add(badge);
				setSaveNeeded(true);
			}

			if (global.contains(Badge.BOSS_SLAIN_1_WARRIOR) &&
					global.contains(Badge.BOSS_SLAIN_1_MAGE) &&
					global.contains(Badge.BOSS_SLAIN_1_ROGUE) &&
					global.contains(Badge.BOSS_SLAIN_1_HUNTRESS) &&
					global.contains(Badge.BOSS_SLAIN_1_ELF) &&
					global.contains(Badge.BOSS_SLAIN_1_NECROMANCER) &&
					global.contains(Badge.BOSS_SLAIN_1_GNOLL)) {

				badge = Badge.BOSS_SLAIN_1_ALL_CLASSES;
				if (!global.contains(badge)) {
					displayBadge(badge);
					global.add(badge);
					setSaveNeeded(true);
				}
			}
		} else if (badge == Badge.BOSS_SLAIN_3) {
			switch (Dungeon.hero.getSubClass()) {
				case GLADIATOR:
					badge = Badge.BOSS_SLAIN_3_GLADIATOR;
					break;
				case BERSERKER:
					badge = Badge.BOSS_SLAIN_3_BERSERKER;
					break;
				case WARLOCK:
					badge = Badge.BOSS_SLAIN_3_WARLOCK;
					break;
				case BATTLEMAGE:
					badge = Badge.BOSS_SLAIN_3_BATTLEMAGE;
					break;
				case FREERUNNER:
					badge = Badge.BOSS_SLAIN_3_FREERUNNER;
					break;
				case ASSASSIN:
					badge = Badge.BOSS_SLAIN_3_ASSASSIN;
					break;
				case SNIPER:
					badge = Badge.BOSS_SLAIN_3_SNIPER;
					break;
				case WARDEN:
					badge = Badge.BOSS_SLAIN_3_WARDEN;
					break;
				case SHAMAN:
					badge = Badge.BOSS_SLAIN_3_SHAMAN;
					break;
				case SCOUT:
					badge = Badge.BOSS_SLAIN_3_SCOUT;
					break;
				case LICH:
					badge = Badge.BOSS_SLAIN_3_LICH;
					break;
				default:
					return;
			}
			local.add(badge);
			if (!global.contains(badge)) {
				global.add(badge);
				setSaveNeeded(true);
			}

			if (global.contains(Badge.BOSS_SLAIN_3_GLADIATOR) &&
					global.contains(Badge.BOSS_SLAIN_3_BERSERKER) &&
					global.contains(Badge.BOSS_SLAIN_3_WARLOCK) &&
					global.contains(Badge.BOSS_SLAIN_3_BATTLEMAGE) &&
					global.contains(Badge.BOSS_SLAIN_3_FREERUNNER) &&
					global.contains(Badge.BOSS_SLAIN_3_ASSASSIN) &&
					global.contains(Badge.BOSS_SLAIN_3_SNIPER) &&
					global.contains(Badge.BOSS_SLAIN_3_WARDEN) &&
					global.contains(Badge.BOSS_SLAIN_3_SHAMAN) &&
					global.contains(Badge.BOSS_SLAIN_3_SCOUT) &&
					global.contains(Badge.BOSS_SLAIN_3_LICH)) {

				badge = Badge.BOSS_SLAIN_3_ALL_SUBCLASSES;
				if (!global.contains(badge)) {
					displayBadge(badge);
					global.add(badge);
					setSaveNeeded(true);
				}
			}
		}
	}

	public static void validateMastery(@NotNull HeroClass heroClass) {

		Badge badge = null;
		switch (heroClass) {
			case WARRIOR:
				badge = Badge.MASTERY_WARRIOR;
				break;
			case MAGE:
				badge = Badge.MASTERY_MAGE;
				break;
			case ROGUE:
				badge = Badge.MASTERY_ROGUE;
				break;
			case HUNTRESS:
				badge = Badge.MASTERY_HUNTRESS;
				break;
			case ELF:
				badge = Badge.MASTERY_ELF;
				break;
			case NECROMANCER:
				badge = Badge.MASTERY_NECROMANCER;
				break;
			case GNOLL:
				badge = Badge.MASTERY_GNOLL;
				break;
		}

		if (!global.contains(badge)) {
			global.add(badge);
			setSaveNeeded(true);
		}
	}

	public static void validateMasteryCombo(int n) {
		if (!local.contains(Badge.MASTERY_COMBO) && n == 7) {
			Badge badge = Badge.MASTERY_COMBO;
			local.add(badge);
			displayBadge(badge);
		}
	}

	public static void validateRingOfHaggler() {
		if (!local.contains(Badge.RING_OF_HAGGLER) && new RingOfHaggler().isKnown()) {
			Badge badge = Badge.RING_OF_HAGGLER;
			local.add(badge);
			displayBadge(badge);
		}
	}

	public static void validateRingOfThorns() {
		if (!local.contains(Badge.RING_OF_THORNS) && new RingOfThorns().isKnown()) {
			Badge badge = Badge.RING_OF_THORNS;
			local.add(badge);
			displayBadge(badge);
		}
	}

	public static void validateRare(Mob mob) {

		Badge badge = null;
		if (mob instanceof Albino) {
			badge = Badge.RARE_ALBINO;
		} else if (mob instanceof Bandit) {
			badge = Badge.RARE_BANDIT;
		} else if (mob instanceof Shielded) {
			badge = Badge.RARE_SHIELDED;
		} else if (mob instanceof Senior) {
			badge = Badge.RARE_SENIOR;
		} else if (mob instanceof Acidic) {
			badge = Badge.RARE_ACIDIC;
		} else if (mob instanceof SpiderGuard) {
			badge = Badge.RARE_SPIDER_SOLDIER;
		} else if (mob instanceof SpiderMindAmber) {
			badge = Badge.RARE_SPIDER_MIND;
		} else if (mob instanceof DreadKnight) {
			badge = Badge.RARE_DREAD_KNIGHT;
		} else if (mob.getEntityKind().equals("DeepSnail")) {
			badge = Badge.RARE_DEEP_SNAIL;
		} else if (mob.getEntityKind().equals("ShamanElder")) {
			badge = Badge.RARE_SHAMAN_ELDER;
		}
		if (!global.contains(badge)) {
			global.add(badge);
			setSaveNeeded(true);
		}

		if (global.contains(Badge.RARE_ALBINO) &&
				global.contains(Badge.RARE_BANDIT) &&
				global.contains(Badge.RARE_SHIELDED) &&
				global.contains(Badge.RARE_SENIOR) &&
				global.contains(Badge.RARE_ACIDIC) &&
				global.contains(Badge.RARE_SPIDER_SOLDIER) &&
				global.contains(Badge.RARE_SPIDER_MIND) &&
				global.contains(Badge.RARE_DREAD_KNIGHT) &&
				global.contains(Badge.RARE_DEEP_SNAIL) &&
				global.contains(Badge.RARE_SHAMAN_ELDER)) {

			badge = Badge.RARE;
			displayBadge(badge);
		}
	}

	public static void validateVictory() {

		Badge badge = Badge.VICTORY;
		displayBadge(badge);

		switch (Dungeon.hero.getHeroClass()) {
			case WARRIOR:
				badge = Badge.VICTORY_WARRIOR;
				break;
			case MAGE:
				badge = Badge.VICTORY_MAGE;
				break;
			case ROGUE:
				badge = Badge.VICTORY_ROGUE;
				break;
			case HUNTRESS:
				badge = Badge.VICTORY_HUNTRESS;
				break;
			case ELF:
				badge = Badge.VICTORY_ELF;
				break;
			case NECROMANCER:
				badge = Badge.VICTORY_NECROMANCER;
				break;
			case GNOLL:
				badge = Badge.VICTORY_GNOLL;
				break;
		}
		local.add(badge);
		if (!global.contains(badge)) {
			global.add(badge);
			setSaveNeeded(true);
		}

		if (global.contains(Badge.VICTORY_WARRIOR) &&
				global.contains(Badge.VICTORY_MAGE) &&
				global.contains(Badge.VICTORY_ROGUE) &&
				global.contains(Badge.VICTORY_HUNTRESS) &&
				global.contains(Badge.VICTORY_ELF) &&
				global.contains(Badge.VICTORY_NECROMANCER) &&
				global.contains(Badge.VICTORY_GNOLL)) {

			badge = Badge.VICTORY_ALL_CLASSES;
			displayBadge(badge);
		}
	}

	public static void validateNoKilling() {
		if (!local.contains(Badge.NO_MONSTERS_SLAIN) && Statistics.completedWithNoKilling) {
			Badge badge = Badge.NO_MONSTERS_SLAIN;
			local.add(badge);
			displayBadge(badge);
		}
	}

	public static void validateGrimWeapon() {
		if (!local.contains(Badge.GRIM_WEAPON)) {
			Badge badge = Badge.GRIM_WEAPON;
			local.add(badge);
			displayBadge(badge);
		}
	}

	public static void validateNightHunter() {
		if (!local.contains(Badge.NIGHT_HUNTER) && Statistics.nightHunt >= 15) {
			Badge badge = Badge.NIGHT_HUNTER;
			local.add(badge);
			displayBadge(badge);
		}
	}

	public static void validateSupporter() {
		loadGlobal();
		global.add(Badge.SUPPORTER);
		setSaveNeeded(true);
		PixelScene.showBadge(Badge.SUPPORTER);
	}

	public static void validateGamesPlayed() {
		Badge badge = null;
		if (Rankings.INSTANCE.totalNumber >= 10) {
			badge = Badge.GAMES_PLAYED_1;
		}
		if (Rankings.INSTANCE.totalNumber >= 100) {
			badge = Badge.GAMES_PLAYED_2;
		}
		if (Rankings.INSTANCE.totalNumber >= 500) {
			badge = Badge.GAMES_PLAYED_3;
		}
		if (Rankings.INSTANCE.totalNumber >= 2000) {
			badge = Badge.GAMES_PLAYED_4;
		}
		if (Rankings.INSTANCE.totalNumber >= 5000) {
			badge = Badge.GAMES_PLAYED_5;
		}


		displayBadge(badge);
	}

	public static void validateHappyEnd() {
		displayBadge(Badge.HAPPY_END);
	}

	public static void validateChampion() {
		displayBadge(Badge.CHAMPION);
	}

	public static void displayBadge(Badge badge) {

		if (badge == null) {
			return;
		}

		unlockPlayGamesBadge(badge);

		if (global.contains(badge)) {
			if (!badge.meta) {
				GLog.h(StringsManager.getVar(R.string.Badges_Info1), badge.description);
			}
		} else {

			global.add(badge);
			setSaveNeeded(true);
			EventCollector.badgeUnlocked(badge.name());

			if (badge.meta) {
				GLog.h(StringsManager.getVar(R.string.Badges_Info2), badge.description);
			} else {
				GLog.h(StringsManager.getVar(R.string.Badges_Info3), badge.description);
			}
			PixelScene.showBadge(badge);
		}
	}

	private static void unlockPlayGamesBadge(Badge badge) {
		if (ModdingMode.inRemixed()) {
			String achievementCode = StringsManager.getVar("achievement_" + badge.name().toLowerCase(Locale.ROOT));

			if(achievementCode.isEmpty()) {
				return;
			}

			Game.instance().playGames.unlockAchievement(achievementCode);
		}
	}

	@LuaInterface
	public static boolean isUnlocked(Badge badge) {
		return global.contains(badge);
	}

	@LuaInterface
	public static boolean isUnlockedInThisGame(Badge badge) {
		return local.contains(badge);
	}

	public static List<Badge> filtered(boolean global) {

		HashSet<Badge> filtered = new HashSet<>(global ? Badges.global : Badges.local);

		if (!global) {
			Iterator<Badge> iterator = filtered.iterator();
			while (iterator.hasNext()) {
				Badge badge = iterator.next();
				if (badge.meta) {
					iterator.remove();
				}
			}
		}

		leaveBest(filtered, Badge.MONSTERS_SLAIN_1, Badge.MONSTERS_SLAIN_2, Badge.MONSTERS_SLAIN_3, Badge.MONSTERS_SLAIN_4, Badge.MONSTERS_SLAIN_5);
		leaveBest(filtered, Badge.GOLD_COLLECTED_1, Badge.GOLD_COLLECTED_2, Badge.GOLD_COLLECTED_3, Badge.GOLD_COLLECTED_4, Badge.GOLD_COLLECTED_5);
		leaveBest(filtered, Badge.BOSS_SLAIN_1, Badge.BOSS_SLAIN_2, Badge.BOSS_SLAIN_3, Badge.BOSS_SLAIN_4, Badge.SHADOW_LORD_SLAIN, Badge.YOG_SLAIN);
		leaveBest(filtered, Badge.LEVEL_REACHED_1, Badge.LEVEL_REACHED_2, Badge.LEVEL_REACHED_3, Badge.LEVEL_REACHED_4, Badge.LEVEL_REACHED_5);
		leaveBest(filtered, Badge.STRENGTH_ATTAINED_1, Badge.STRENGTH_ATTAINED_2, Badge.STRENGTH_ATTAINED_3, Badge.STRENGTH_ATTAINED_4, Badge.STRENGTH_ATTAINED_5);
		leaveBest(filtered, Badge.FOOD_EATEN_1, Badge.FOOD_EATEN_2, Badge.FOOD_EATEN_3, Badge.FOOD_EATEN_4, Badge.FOOD_EATEN_5);
		leaveBest(filtered, Badge.ITEM_LEVEL_1, Badge.ITEM_LEVEL_2, Badge.ITEM_LEVEL_3, Badge.ITEM_LEVEL_4, Badge.ITEM_LEVEL_5);
		leaveBest(filtered, Badge.POTIONS_COOKED_1, Badge.POTIONS_COOKED_2, Badge.POTIONS_COOKED_3, Badge.POTIONS_COOKED_4, Badge.POTIONS_COOKED_5);
		leaveBest(filtered, Badge.BOSS_SLAIN_1_ALL_CLASSES, Badge.BOSS_SLAIN_3_ALL_SUBCLASSES);
		leaveBest(filtered, Badge.DEATH_FROM_FIRE, Badge.YASD);
		leaveBest(filtered, Badge.DEATH_FROM_GAS, Badge.YASD);
		leaveBest(filtered, Badge.DEATH_FROM_HUNGER, Badge.YASD);
		leaveBest(filtered, Badge.DEATH_FROM_POISON, Badge.YASD);
		leaveBest(filtered, Badge.VICTORY, Badge.VICTORY_ALL_CLASSES);
		leaveBest(filtered, Badge.GAMES_PLAYED_1, Badge.GAMES_PLAYED_2, Badge.GAMES_PLAYED_3, Badge.GAMES_PLAYED_4, Badge.GAMES_PLAYED_5);

		ArrayList<Badge> list = new ArrayList<>(filtered);
		Collections.sort(list);

		return list;
	}

	private static void leaveBest(HashSet<Badge> list, Badge... badges) {
		for (int i = badges.length - 1; i > 0; i--) {
			if (list.contains(badges[i])) {
				for (int j = 0; j < i; j++) {
					list.remove(badges[j]);
				}
				break;
			}
		}
	}

	public static void validateDeathInStone() {
		displayBadge(Badge.IMMURED);
	}

	public static void validateGnollUnlocked() {
		displayBadge(Badge.GNOLL_UNLOCKED);
	}
}
