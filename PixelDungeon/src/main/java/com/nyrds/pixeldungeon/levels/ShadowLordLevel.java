package com.nyrds.pixeldungeon.levels;

import com.nyrds.pixeldungeon.levels.com.nyrds.pixeldungeon.levels.Tools;
import com.nyrds.pixeldungeon.mobs.common.ShadowLord;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.particles.FlameParticle;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.utils.Random;

public class ShadowLordLevel extends Level {

	public ShadowLordLevel() {
		color1 = 0x801500;
		color2 = 0xa68521;

		viewDistance = 3;
	}

	@Override
	public String tilesTex() {
		return Assets.TILES_PRISON;
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
		for (int i = 0; i < getLength(); i++) {
			if (map[i] == Terrain.EMPTY && Random.Int(2) == 0) {
				map[i] = Terrain.EMPTY_DECO;
			} else if (map[i] == Terrain.WALL && Random.Int(8) == 0) {
				map[i] = Terrain.WALL_DECO;
			}
		}
	}

	@Override
	protected void createMobs() {
		ShadowLord lord = new ShadowLord();
		lord.setPos(cell(width/2,height / 2));
		mobs.add(lord);
	}

	@Override
	protected void createItems() {
	}

	@Override
	public boolean isBossLevel() {
		return true;
	}

	private void doMagic(int cell) {
		set(cell, Terrain.EMPTY_SP);
		CellEmitter.get(cell).start(FlameParticle.FACTORY, 0.1f, 3);
	}
}
