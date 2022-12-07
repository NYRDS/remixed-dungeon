package com.nyrds.retrodungeon.items.necropolis;

import com.nyrds.retrodungeon.items.common.MasteryItem;
import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.windows.WndChooseWay;

import java.util.ArrayList;

public class BlackSkullOfMastery extends MasteryItem {

	public static final float TIME_TO_READ = 10;

	public static final String AC_NECROMANCY = Game.getVar(R.string.Necromancer_ACSpecial);

	{
		stackable = false;
		imageFile = "items/artifacts.png";
		identify();
		image = 19;
	}
	
	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );		
		actions.add( AC_NECROMANCY );
		
		return actions;
	}
	
	@Override
	public void execute( Hero hero, String action ) {
		if (action.equals( AC_NECROMANCY )) {

			setCurUser(hero);
			
			HeroSubClass way;

			switch (hero.heroClass) {
			default:
				GLog.w("Error: How did you get this item?! You're not supposed to be able to obtain it!!");
				return;
			case NECROMANCER:
				way = HeroSubClass.LICH;
				hero.setMaxSoulPoints(hero.getSoulPointsMax() * 2);
				break;
			}
			GameScene.show( new WndChooseWay( this, way ) );
		} else {
			super.execute( hero, action );
		}
	}
}
