package com.nyrds.pixeldungeon.levels;

import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.utils.GLog;

import java.util.List;

public class TestLevel extends Level {

	public TestLevel() {
		color1 = 0x801500;
		color2 = 0xa68521;

		viewDistance = 3;
	}

	@Override
	public String tilesTex() {
		return Assets.WATER_HALLS;
	}

	@Override
	public String waterTex() {
		return Assets.WATER_HALLS;
	}

	@Override
	protected boolean build() {
		Tools.makeEmptyLevel(this);
		return true;
	}

	@Override
	protected void decorate() {
	}

	@Override
	protected void createMobs() {
		List<Mob> mobs = MobFactory.allMobs();

		for(Mob mob:mobs) {

			int cell = mob.respawnCell(this);
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
	}

	@Override
	public boolean isBossLevel() {
		return true;
	}
}
