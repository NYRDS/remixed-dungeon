package com.nyrds.retrodungeon.items.food;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.SpellSprite;
import com.watabou.pixeldungeon.items.food.Food;
import com.watabou.pixeldungeon.utils.GLog;

public class Candy extends Food {

	private String fMessage = Game.getVar(R.string.Candy_Warning_1);

	public Candy() {
		imageFile = "items/artifacts.png";
		image = 21;
	}

	@Override
	public void execute(Hero hero, String action ) {
		if (action.equals( AC_EAT )) {

			detach( hero.belongings.backpack );

			GLog.w( fMessage );

			hero.getSprite().operate( hero.getPos() );
			hero.busy();
			SpellSprite.show( hero, SpellSprite.FOOD );
			Sample.INSTANCE.play( Assets.SND_EAT );

			hero.spend( TIME_TO_EAT );

		} else {

			super.execute( hero, action );

		}
	}

	@Override
	public int price() {
		return 20 * quantity();
	}

}
