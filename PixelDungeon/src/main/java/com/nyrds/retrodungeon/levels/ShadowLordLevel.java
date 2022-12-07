package com.nyrds.retrodungeon.levels;

import com.nyrds.retrodungeon.mobs.common.ShadowLord;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.levels.Level;

public class ShadowLordLevel extends Level {

	public ShadowLordLevel() {
		color1 = 0x801500;
		color2 = 0xa68521;

		viewDistance = 3;
	}

	@Override
	public String tilesTex() {
		return Assets.TILES_SHADOW_LORD;
	}

	@Override
	public String waterTex() {
		return Assets.WATER_HALLS;
	}

	@Override
	protected boolean build() {
		Tools.makeShadowLordLevel(this);
		return true;
	}

	@Override
	protected void decorate() {
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
}
