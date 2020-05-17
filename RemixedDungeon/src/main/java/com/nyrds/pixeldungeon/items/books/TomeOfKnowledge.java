package com.nyrds.pixeldungeon.items.books;

import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.effects.SpellSprite;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.CharSprite;

public class TomeOfKnowledge extends Book {

	{
		image = 1;
	}

	@Override
	protected void doRead(Char hero) {
		hero.getBelongings().setSelectedItem(detach( hero.getBelongings().backpack ));

		SpellSprite.show( hero, SpellSprite.MASTERY );
		hero.getSprite().emitter().burst( Speck.factory( Speck.MAGIC ), 8 );
		hero.getSprite().showStatus( CharSprite.BLUE, "+ 1");
		Sample.INSTANCE.play( Assets.SND_READ );

		hero.spendAndNext( TIME_TO_READ );
		hero.busy();
		hero.skillLevelUp();
	}

	@Override
	public int price() {
		return 100 * quantity();
	}

	@Override
	public Item burn(int cell){
		return null;
	}
}
