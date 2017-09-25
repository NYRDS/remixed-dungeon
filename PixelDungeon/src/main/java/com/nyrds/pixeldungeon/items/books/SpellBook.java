package com.nyrds.pixeldungeon.items.books;

import com.watabou.pixeldungeon.actors.hero.Hero;

public class SpellBook extends Book {

	@Override
	protected void doRead(Hero hero) {
		hero.magicLvlUp();
		getCurUser().spendAndNext( TIME_TO_READ );
	}
}
