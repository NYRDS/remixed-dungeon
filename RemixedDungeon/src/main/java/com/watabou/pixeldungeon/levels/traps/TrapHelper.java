package com.watabou.pixeldungeon.levels.traps;

import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.TerrainFlags;
import com.watabou.pixeldungeon.scenes.GameScene;

public class TrapHelper {

	public static boolean stepConfirmed = false;

	public static boolean isVisibleTrap(Level level, int cell){

		int cellType = level.map[cell];

		if(TerrainFlags.is(cellType, TerrainFlags.TRAP) && !TerrainFlags.is(cellType, TerrainFlags.SECRET)) {
			return true;
		}

		LevelObject lo = level.getTopLevelObject(cell);
		if(lo != null) {
			return lo.avoid();
		}
		return false;
	}

	public static void heroTriggerTrap( final Hero hero ) {
		GameScene.show(new WndStepOnTrap(hero));
	}

	public static void heroPressed(){
		stepConfirmed = false;
	}

}
