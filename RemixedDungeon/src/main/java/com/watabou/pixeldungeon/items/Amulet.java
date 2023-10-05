
package com.watabou.pixeldungeon.items;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.scenes.AmuletScene;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Amulet extends Item {
	
	private static final String AC_END = "Amulet_ACEnd";
	
	{
        name = StringsManager.getVar(R.string.Amulet_Name);
		image = ItemSpriteSheet.AMULET;
	}
	
	@Override
	public ArrayList<String> actions(Char hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.remove(AC_THROW);
		actions.remove(AC_DROP);
		actions.add( AC_END );
		return actions;
	}
	
	@Override
	public void _execute(@NotNull Char chr, @NotNull String action ) {
		if (action.equals(AC_END)) {
			showAmuletScene( false );
		} else {
			super._execute(chr, action );
		}
	}
	
	@Override
	public boolean doPickUp(@NotNull Char hero ) {
		if (super.doPickUp( hero )) {
			
			if (!Statistics.amuletObtained) {
				Statistics.amuletObtained = true;
				Badges.validateVictory();

				showAmuletScene( true );
			}
			
			return true;
		}

		return false;
	}
	
	private void showAmuletScene( boolean showText ) {
		Dungeon.save(false);
		AmuletScene.noText = !showText;
		GameLoop.switchScene( AmuletScene.class );
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
}
