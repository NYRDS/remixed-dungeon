package com.nyrds.pixeldungeon.mobs.npc;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.windows.WndQuest;

public class TownsfolkNPC extends ImmortalNPC {

	public TownsfolkNPC() {
	}

	@Override
	public boolean interact(final Char hero) {
		getSprite().turnTo( getPos(), hero.getPos() );

		GameScene.show(new WndQuest(this, R.string.TownsfolkNPC_Message1,
													R.string.TownsfolkNPC_Message2,
													R.string.TownsfolkNPC_Message3,
													R.string.TownsfolkNPC_Message4));
		return true;
	}
}


