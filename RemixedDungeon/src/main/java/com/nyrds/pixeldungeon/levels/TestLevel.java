package com.nyrds.pixeldungeon.levels;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.util.ModError;
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
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndOptions;

import java.util.List;

import lombok.var;

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

	@Override
	protected void createMobs() {
		List<Mob> mobs = MobFactory.allMobs();

		for(Mob mob:mobs) {

			int cell = randomRespawnCell(passable);
			if(!cellValid(cell)) {
				GLog.debug("no cell for %s", mob.getMobClassName());
				continue;
			}

			mob.setPos(cell);
			try {
				spawnMob(mob);
			} catch (Exception e) {
				ModError.doReport(Utils.format("Failed to spawn %s", mob.getMobClassName()), e);
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
				GLog.debug("no cell for %s", mob.getMobClassName());
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
}
