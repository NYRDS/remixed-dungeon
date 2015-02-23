package com.watabou.pixeldungeon.items.food;

import com.watabou.noosa.Game;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.actors.buffs.Slow;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Random;


public class RottenMeat extends Food {

	{
		image   = ItemSpriteSheet.ROTTEN_MEAT;
		energy  = (Hunger.STARVING - Hunger.HUNGRY)/2;
		message = Game.getVar(R.string.MysteryMeat_Message);
	}
	
	@Override
	public void execute( Hero hero, String action ) {
		
		super.execute( hero, action );

		
		if (action.equals( AC_EAT )) {
			GLog.w(Game.getVar(R.string.MysteryMeat_Info2));
			Buff.prolong( hero, Roots.class, Paralysis.duration( hero ) );
		}
	}
	
	public int price() {
		return 2 * quantity;
	};
}
