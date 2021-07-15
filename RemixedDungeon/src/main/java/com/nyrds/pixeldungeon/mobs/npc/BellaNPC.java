package com.nyrds.pixeldungeon.mobs.npc;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.windows.WndQuest;

public class BellaNPC extends ImmortalNPC {

	public BellaNPC() {
	}

	@Override
	public boolean interact(final Char hero) {
		getSprite().turnTo( getPos(), hero.getPos() );

        GameScene.show(new WndQuest(this, StringsManager.getVar(R.string.BellaNPC_Message)));
		return true;
	}
}


