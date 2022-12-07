package com.nyrds.retrodungeon.mobs.npc;

import com.nyrds.retrodungeon.windows.WndFortuneTeller;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.GameScene;

public class FortuneTellerNPC extends ImmortalNPC {

	public FortuneTellerNPC() {
	}

	@Override
	public boolean interact(final Hero hero) {
		getSprite().turnTo( getPos(), hero.getPos() );
		GameScene.show(new WndFortuneTeller(this));
		return true;
	}

}
