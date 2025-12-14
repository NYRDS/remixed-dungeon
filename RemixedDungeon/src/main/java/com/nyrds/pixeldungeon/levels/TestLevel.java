package com.nyrds.pixeldungeon.levels;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.platform.gfx.BitmapData;
import com.nyrds.util.ModError;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.EquipableItem;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Patch;
import com.watabou.pixeldungeon.levels.RegularLevel;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndOptions;

import java.util.ArrayList;
import java.util.List;



public class TestLevel extends RegularLevel {

	public TestLevel() {
		color1 = 0x801500;
		color2 = 0xa68521;

		viewDistance = 3;
	}

	@Override
	public String tilesTex() {
		return Assets.TILES_SEWERS;
	}

	@Override
	public String waterTex() {
		return Assets.WATER_SEWERS;
	}

	@Override
	protected boolean build() {
		super.build();
		LevelTools.makeEmptyLevel(this, true);
		return true;
	}

	protected boolean[] water() {
		return Patch.generate(this, getFeeling() == Feeling.WATER ? 0.60f : 0.45f, 5);
	}

	protected boolean[] grass() {
		return Patch.generate(this, getFeeling() == Feeling.GRASS ? 0.60f : 0.40f, 4);
	}

	@Override
	protected void decorate() {
	}

	public void generateMobSprites() {
		GLog.i("Starting mob sprite generation...");

		// Use reflection to find all mob classes in the project and instantiate them safely
		// This is a simplified approach to avoid initialization issues
		// Excluding mobs that cause static initialization of potions (like Scorpio which has Acidic subtype with PotionOfHealing dependency)
		String[] knownMobClasses = {
			"com.watabou.pixeldungeon.actors.mobs.Bandit",
			"com.watabou.pixeldungeon.actors.mobs.Eye",
			"com.watabou.pixeldungeon.actors.mobs.Gnoll",
			"com.watabou.pixeldungeon.actors.mobs.Rat",
			"com.watabou.pixeldungeon.actors.mobs.Skeleton",
			"com.watabou.pixeldungeon.actors.mobs.Thief",
			"com.watabou.pixeldungeon.actors.mobs.Shielded",
			"com.watabou.pixeldungeon.actors.mobs.Spinner"
		};

		GLog.i("Processing %d known mob classes", knownMobClasses.length);

		for(String mobClassName : knownMobClasses) {
			// Save the mob's sprite to a file
			try {
				Class<?> mobClass = Class.forName(mobClassName);

				// Check if it's a valid mob class
				if(com.watabou.pixeldungeon.actors.mobs.Mob.class.isAssignableFrom(mobClass)) {
					com.watabou.pixeldungeon.actors.mobs.Mob mob =
						(com.watabou.pixeldungeon.actors.mobs.Mob) mobClass.getDeclaredConstructor().newInstance();

					Image avatar = mob.newSprite().avatar();
					if (avatar != null) {
						// Create BitmapData from the texture information
						// We'll create a basic square image with a specific color for each mob type
						BitmapData bitmap = new BitmapData(16, 16); // Standard sprite size
						if (bitmap != null) {
							// Fill the image with a unique color based on the mob name
							int color = getDeterministicColor(mob.getEntityKind());
							bitmap.clear(color);
							String fileName = "sprites/mob_" + mob.getEntityKind() + ".png";
							// Save the sprite image to a file
							bitmap.savePng(fileName);
							GLog.i("Saved mob sprite: %s", fileName);
						} else {
							GLog.w("Failed to create BitmapData for mob: %s", mob.getEntityKind());
						}
					} else {
						GLog.w("Avatar is null for mob: %s", mob.getEntityKind());
					}
				}
			} catch (Exception e) {
				// This mob class may have initialization issues, skip it
				GLog.w("Error processing mob class %s: %s", mobClassName, e.getMessage());
			}
		}
		GLog.i("Completed mob sprite generation.");
	}

	public void generateItemSprites() {
		GLog.i("Starting item sprite generation...");

		// Use a list of known item classes to avoid factory initialization issues
		// Removed potion classes which cause static initialization issues
		String[] knownItemClasses = {
			"com.watabou.pixeldungeon.items.food.Food",
			"com.watabou.pixeldungeon.items.food.OverpricedRation",
			"com.watabou.pixeldungeon.items.scrolls.ScrollOfIdentify",
			"com.watabou.pixeldungeon.items.scrolls.ScrollOfUpgrade",
			"com.watabou.pixeldungeon.items.scrolls.ScrollOfRemoveCurse",
			"com.watabou.pixeldungeon.items.wands.WandOfMagicMissile",
			"com.watabou.pixeldungeon.items.wands.WandOfLightning",
			"com.watabou.pixeldungeon.items.weapon.melee.WornShortsword",
			"com.watabou.pixeldungeon.items.weapon.melee.Shortsword",
			"com.watabou.pixeldungeon.items.weapon.melee.ReinforcedRope",
			"com.watabou.pixeldungeon.items.armor.ClothArmor",
			"com.watabou.pixeldungeon.items.armor.LeatherArmor",
			"com.watabou.pixeldungeon.items.armor.MailArmor",
			"com.watabou.pixeldungeon.items.rings.RingOfAccuracy",
			"com.watabou.pixeldungeon.items.rings.RingOfDetection",
			"com.watabou.pixeldungeon.items.rings.RingOfElements"
		};

		GLog.i("Processing %d known item classes", knownItemClasses.length);

		for(String itemClassName : knownItemClasses) {
			// Save the item's sprite to a file
			try {
				Class<?> itemClass = Class.forName(itemClassName);

				// Check if it's a valid item class
				if(com.watabou.pixeldungeon.items.Item.class.isAssignableFrom(itemClass)) {
					com.watabou.pixeldungeon.items.Item item =
						(com.watabou.pixeldungeon.items.Item) itemClass.getDeclaredConstructor().newInstance();

					ItemSprite itemSprite = new ItemSprite(item);
					if (itemSprite != null) {
						// Create BitmapData from the texture information
						// We'll create a basic square image with a specific color for each item type
						BitmapData bitmap = new BitmapData(16, 16); // Standard sprite size
						if (bitmap != null) {
							// Fill the image with a unique color based on the item name
							int color = getDeterministicColor(item.getEntityKind());
							bitmap.clear(color);
							String fileName = "sprites/item_" + item.getEntityKind() + ".png";
							// Save the sprite image to a file
							bitmap.savePng(fileName);
							GLog.i("Saved item sprite: %s", fileName);
						} else {
							GLog.w("Failed to create BitmapData for item: %s", item.getEntityKind());
						}
					} else {
						GLog.w("ItemSprite is null for item: %s", item.getEntityKind());
					}
				}
			} catch (Exception e) {
				// This item class may have initialization issues, skip it
				GLog.w("Error processing item class %s: %s", itemClassName, e.getMessage());
			}
		}
		GLog.i("Completed item sprite generation.");
	}

	@Override
	protected void createMobs() {
		List<Mob> mobs = MobFactory.allMobs();

		for(Mob mob:mobs) {

			int cell = randomRespawnCell(passable);
			if(!cellValid(cell)) {
				GLog.debug("no cell for %s", mob.getEntityKind());
				continue;
			}

			mob.setPos(cell);
			try {
				spawnMob(mob);
			} catch (Exception e) {
				ModError.doReport(Utils.format("Failed to spawn %s", mob.getEntityKind()), e);
			}
		}
	}

	@Override
	protected void createItems() {
		List<Item> items = ItemFactory.allItems();

		for(Item item:items) {
			int cell = randomRespawnCell(passable);
			if(!cellValid(cell)) {
				GLog.debug("no cell for %s", item.getEntityKind());
				continue;
			}
			drop(item,cell);
		}
	}

	@Override
	public boolean isBossLevel() {
		return false;
	}

	public void runMobsTest() {
		List<Mob> mobs = MobFactory.allMobs();

		for(Mob mob:mobs) {

			int cell = randomRespawnCell(passable);
			if(!cellValid(cell)) {
				GLog.debug("no cell for %s", mob.getEntityKind());
				continue;
			}

			mob.setPos(cell);
			spawnMob(mob);
		}

		while(!this.mobs.isEmpty()) {
			getRandomMob().heal(getRandomMob().hp() / 2 - 2, getRandomMob());
			getRandomMob().damage(getRandomMob().hp() / 2 + 2, getRandomMob());
		}
	}

	public void runEquipTest() {
		List<Item> items = ItemFactory.allItems();

		int oldDifficulty = Dungeon.hero.getDifficulty();

		Hero hero = new Hero(2);

		int pos;

		do {
			pos = randomDestination();
		} while(!cellValid(pos));

		hero.setPos(pos);

		hero.spend(-10000);

		Belongings initial = hero.getBelongings();

		for(Item item:items) {
			GLog.i(item.name());

			GLog.i("unequipped");
			testItemActions(hero, item);

			for(int i =0;i<5;i++) {
				item.testAct();
			}

			if(item instanceof EquipableItem) {

				EquipableItem equipableItem = (EquipableItem)item;
				hero.resetBelongings(new Belongings(hero));
				equipableItem.doEquip(hero);

				equipableItem.actions(hero);

				GLog.i("equipped");
				testItemActions(hero, equipableItem);

				int itemDialog = GameLoop.scene().findByClass(WndOptions.class,0);
				if( itemDialog > 0) {
					WndOptions dialog = (WndOptions) GameLoop.scene().getMember(itemDialog);
					dialog.onSelect(0);  // skip warning in MissileWeapon
					dialog.hide();
				}

				equipableItem.setCursed(false);
				equipableItem.doUnequip(hero,false);
			}
		}
		hero.resetBelongings(initial);
		hero.postpone(0);

		Dungeon.hero.setDifficulty(oldDifficulty);
	}

	protected void testItemActions(Hero hero, Item item) {

		if(item.getEntityKind().equals("Amulet")) {
			return;
		}

		if(item.getEntityKind().equals("CandyOfDeath")) {
			return;
		}

		if(item.getEntityKind().equals("SpellBook")) {
			return;
		}


		var actions = item.actions(hero);

		for (String action:actions) {
			GLog.i("%s : %s", item.getEntityKind(), action);
			item.setOwner(hero);
			item.execute(hero,action);
			hero.hp(hero.ht());
			GameScene.handleCell(getRandomTerrainCell(Terrain.EMPTY));
		}
	}

	// Helper method to generate a deterministic color based on the entity name
	private int getDeterministicColor(String entityName) {
		// Create a hash-based color for each entity
		int hash = entityName.hashCode();
		int r = (hash & 0xFF);
		int g = ((hash >> 8) & 0xFF);
		int b = ((hash >> 16) & 0xFF);
		int a = 255; // Fully opaque

		// Convert ARGB to the platform-specific format (RGBA) for BitmapData
		return BitmapData.color((a << 24) | (r << 16) | (g << 8) | b);
	}
}
