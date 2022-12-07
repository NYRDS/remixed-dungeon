package com.watabou.pixeldungeon.levels.traps;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.levels.TerrainFlags;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.windows.WndOptions;

public class TrapHelper {

	private static final String TXT_CHASM = Game.getVar(R.string.TrapWnd_Title);
	private static final String TXT_YES   = Game.getVar(R.string.Chasm_Yes);
	private static final String TXT_NO    = Game.getVar(R.string.Chasm_No);
	private static final String TXT_STEP  = Game.getVar(R.string.TrapWnd_Step);

	public static boolean stepConfirmed = false;

	public static boolean isVisibleTrap(int cellType){
		return TerrainFlags.is(cellType, TerrainFlags.TRAP) && !TerrainFlags.is(cellType, TerrainFlags.SECRET);
	}

	public static void heroTriggerTrap( final Hero hero ) {
		GameScene.show(
				new WndOptions( TXT_CHASM, TXT_STEP, TXT_YES, TXT_NO ) {
					@Override
					protected void onSelect( int index ) {
						if (index == 0) {
							stepConfirmed = true;
							hero.resume();
						}
					}
				}
		);
	}

	public static void heroPressed(){
		stepConfirmed = false;
	}
}
