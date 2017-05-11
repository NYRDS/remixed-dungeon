package com.nyrds.pixeldungeon.mobs.npc;

import com.watabou.pixeldungeon.actors.hero.Hero;

public class ArtificerNPC extends ImmortalNPC {

	public ArtificerNPC() {
	}

	@Override
	public boolean interact(final Hero hero) {
		getSprite().turnTo( getPos(), hero.getPos() );
		return true;
	}
}


