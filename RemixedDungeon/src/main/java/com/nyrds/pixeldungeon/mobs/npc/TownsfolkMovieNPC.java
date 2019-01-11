package com.nyrds.pixeldungeon.mobs.npc;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.support.PollfishSurveys;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.windows.WndQuest;

public class TownsfolkMovieNPC extends ImmortalNPC {

	public TownsfolkMovieNPC() {
	}

	@Override
	public boolean interact(final Hero hero) {
		getSprite().turnTo( getPos(), hero.getPos() );
		PollfishSurveys.init();
		PollfishSurveys.showSurvey();
		GameScene.show(new WndQuest(this, Game.getVar(R.string.TownsfolkMovieNPC_Message)));
		return true;
	}
}


