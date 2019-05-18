package com.nyrds.pixeldungeon.mobs.npc;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.windows.WndQuest;

public class TownsfolkSilentNPC extends ImmortalNPC {

	public TownsfolkSilentNPC() {
	}

	@Override
	public boolean interact(final Char hero) {
		getSprite().turnTo( getPos(), hero.getPos() );

		GameScene.show(new WndQuest(this, R.string.TownsfolkSilentNPC_Message1,
													R.string.TownsfolkSilentNPC_Message2,
													R.string.TownsfolkSilentNPC_Message3));
		return true;
	}
}


