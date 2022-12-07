package com.nyrds.retrodungeon.mobs.npc;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.windows.WndQuest;

public class TownsfolkMovieNPC extends ImmortalNPC {

	private static final String TXT_MESSAGE = Game.getVar(R.string.TownsfolkMovieNPC_Message);

	public TownsfolkMovieNPC() {
	}

	@Override
	public boolean interact(final Hero hero) {
		getSprite().turnTo( getPos(), hero.getPos() );
		GameScene.show(new WndQuest(this, TXT_MESSAGE));
		return true;
	}
}


