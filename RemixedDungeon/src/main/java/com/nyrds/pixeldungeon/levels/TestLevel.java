package com.nyrds.pixeldungeon.levels;

import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Patch;
import com.watabou.pixeldungeon.levels.RegularLevel;
import com.watabou.pixeldungeon.utils.GLog;

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
		Tools.makeEmptyLevel(this);
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
			spawnMob(mob);
		}
	}

	@Override
	protected void createItems() {
		List<Item> items = ItemFactory.allItems();

		for(Item item:items) {
			int cell = randomRespawnCell(passable);
			if(!cellValid(cell)) {
				GLog.debug("no cell for %s", item.getClassName());
				continue;
			}
			drop(item,cell);
		}
	}

	@Override
	public boolean isBossLevel() {
		return false;
	}
}
