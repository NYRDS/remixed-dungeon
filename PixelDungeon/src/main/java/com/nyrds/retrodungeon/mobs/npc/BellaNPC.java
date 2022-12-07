package com.nyrds.retrodungeon.mobs.npc;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.windows.WndQuest;

public class BellaNPC extends ImmortalNPC {

	private static final String TXT_MESSAGE = Game.getVar(R.string.BellaNPC_Message);
	private static final String TXT_AMULET_M = Game.getVar(R.string.BellaNPC_Amulet_M);
	private static final String TXT_AMULET_F = Game.getVar(R.string.BellaNPC_Amulet_F);
	private static final String TXT_Angry = Game.getVar(R.string.BellaNPC_Angry);

	public BellaNPC() {
	}

	@Override
	public boolean interact(final Hero hero) {
		getSprite().turnTo( getPos(), hero.getPos() );

		GameScene.show(new WndQuest(this, TXT_MESSAGE));
		return true;
	}
}


