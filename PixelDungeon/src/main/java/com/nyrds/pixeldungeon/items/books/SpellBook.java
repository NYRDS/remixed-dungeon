package com.nyrds.pixeldungeon.items.books;

import com.nyrds.pixeldungeon.mechanics.spells.SpellHelper;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.effects.SpellSprite;
import com.watabou.pixeldungeon.sprites.CharSprite;

public class SpellBook extends Book {

	{
		image = getImageIndex();
	}

	private int getImageIndex() {
		String affinity = Dungeon.hero.heroClass.getMagicAffinity();
		if (affinity.equals(SpellHelper.AFFINITY_ELEMENTAL)) {
			return 1;
		}
		if (affinity.equals(SpellHelper.AFFINITY_NECROMANCY)) {
			return 2;
		}
		return 0;
	}

	@Override
	protected void doRead(Hero hero) {
		SpellSprite.show( getCurUser(), SpellSprite.MASTERY );
		getCurUser().getSprite().emitter().burst( Speck.factory( Speck.MAGIC ), 8 );
		hero.getSprite().showStatus( CharSprite.BLUE, "+ 1");
		Sample.INSTANCE.play( Assets.SND_READ );

		getCurUser().spendAndNext( TIME_TO_READ );
		getCurUser().busy();
		hero.magicLvlUp();
	}

	@Override
	public int price() {
		return 100;
	}
}
