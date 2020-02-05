package com.nyrds.pixeldungeon.items.drinks;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.SpellSprite;
import com.watabou.pixeldungeon.utils.GLog;

public class ManaPotion extends Drink {

	public ManaPotion() {
		imageFile = "items/drinks.png";
		image = 0;
	}
	
	@Override
	public int price() {
		return 100 * quantity();
	}

	@Override
	public void execute(Hero hero, String action ) {
		if (action.equals( AC_DRINK )) {
			detach( hero.getBelongings().backpack );
			GLog.i( Game.getVar(R.string.Drink_Message) );

			hero.setSoulPoints(hero.getSkillPoints() + hero.getSkillPointsMax()/3);
			hero.getSprite().operate( hero.getPos() );
			hero.busy();

			SpellSprite.show( hero, SpellSprite.FOOD );
			Sample.INSTANCE.play( Assets.SND_DRINK );

			hero.spend( TIME_TO_DRINK );
		} else {
			super.execute( hero, action );
		}
	}

}
