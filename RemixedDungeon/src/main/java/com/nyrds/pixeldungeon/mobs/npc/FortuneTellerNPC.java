package com.nyrds.pixeldungeon.mobs.npc;

import com.nyrds.pixeldungeon.windows.WndFortuneTeller;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.scenes.GameScene;

public class FortuneTellerNPC extends ImmortalNPC {

	public FortuneTellerNPC() {
	}

	@Override
	public boolean interact(final Char hero) {
		getSprite().turnTo( getPos(), hero.getPos() );
		GameScene.show(new WndFortuneTeller(this, hero));
		return true;
	}

}
