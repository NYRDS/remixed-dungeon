package com.nyrds.pixeldungeon.levels;

import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.levels.BossLevel;

public class ShadowLordLevel extends BossLevel {

	public ShadowLordLevel() {
		color1 = 0x801500;
		color2 = 0xa68521;

		viewDistance = 3;
	}

	@Override
	protected String tilesTexXyz() {
		return Assets.TILES_HALLS_XYZ;
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
		LevelTools.makeShadowLordLevel(this);
		return true;
	}

	@Override
	protected void decorate() {
	}

	@Override
	protected void pressHero(int cell, Hero ch) {

		super.pressHero( cell, ch );

		if (!enteredArena) {
			enteredArena = true;
			spawnBoss(cell(width/2,height / 2));
		}
	}

	@Override
	protected void createMobs() {
	}

	@Override
	protected void createItems() {
	}
}
