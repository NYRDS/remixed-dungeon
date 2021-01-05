package com.watabou.pixeldungeon.levels.traps;

import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.levels.TerrainFlags;
import com.watabou.pixeldungeon.scenes.GameScene;

public class TrapHelper {

	public static boolean stepConfirmed = false;

	public static boolean isVisibleTrap(int cellType){
		return TerrainFlags.is(cellType, TerrainFlags.TRAP) && !TerrainFlags.is(cellType, TerrainFlags.SECRET);
	}

	public static void heroTriggerTrap( final Hero hero ) {
		GameScene.show(new WndStepOnTrap(hero));
	}

	public static void heroPressed(){
		stepConfirmed = false;
	}

}
