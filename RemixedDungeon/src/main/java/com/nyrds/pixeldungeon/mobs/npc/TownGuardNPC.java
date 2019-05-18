package com.nyrds.pixeldungeon.mobs.npc;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.windows.WndQuest;

public class TownGuardNPC extends ImmortalNPC {

	public TownGuardNPC() {
		movable = false;
	}

	@Override
	public boolean interact(final Char hero) {
		getSprite().turnTo( getPos(), hero.getPos() );

		GameScene.show(new WndQuest(this, R.string.TownGuardNPC_Message1,
													R.string.TownGuardNPC_Message2,
													R.string.TownGuardNPC_Message3));
		return true;
	}

}
