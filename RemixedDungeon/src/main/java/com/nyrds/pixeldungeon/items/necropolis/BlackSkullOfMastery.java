package com.nyrds.pixeldungeon.items.necropolis;

import com.nyrds.pixeldungeon.items.common.MasteryItem;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.windows.WndChooseWay;

import java.util.ArrayList;

public class BlackSkullOfMastery extends MasteryItem {

	public static final float TIME_TO_READ = 10;

	private static final String AC_NECROMANCY = "Necromancer_ACSpecial";

	{
		stackable = false;
		imageFile = "items/artifacts.png";
		identify();
		image = 19;
	}

	@Override
	protected boolean givesMasteryTo(Char hero) {
		return hero.getHeroClass() == HeroClass.NECROMANCER;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );		
		actions.add( AC_NECROMANCY );
		
		return actions;
	}
	
	@Override
	public void execute(Char chr, String action ) {
		if (action.equals( AC_NECROMANCY )) {

			HeroSubClass way;

			if (chr.getHeroClass() == HeroClass.NECROMANCER) {
				way = HeroSubClass.LICH;
			} else {
				GLog.w("Error: How did you get this item?! You're not supposed to be able to obtain it!!");
				return;
			}
			GameScene.show( new WndChooseWay( this, way ) );
		} else {
			super.execute(chr, action );
		}
	}
}
