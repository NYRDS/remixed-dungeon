package com.nyrds.pixeldungeon.items.books;

import com.nyrds.pixeldungeon.mechanics.spells.SpellHelper;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;

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
		hero.magicLvlUp();
		getCurUser().spendAndNext( TIME_TO_READ );
	}
}
