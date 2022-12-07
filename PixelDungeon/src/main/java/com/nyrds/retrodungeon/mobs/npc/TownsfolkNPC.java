package com.nyrds.retrodungeon.mobs.npc;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.windows.WndQuest;
import com.watabou.utils.Random;

public class TownsfolkNPC extends ImmortalNPC {

	private static final String TXT_MESSAGE1 = Game.getVar(R.string.TownsfolkNPC_Message1);
	private static final String TXT_MESSAGE2 = Game.getVar(R.string.TownsfolkNPC_Message2);
	private static final String TXT_MESSAGE3 = Game.getVar(R.string.TownsfolkNPC_Message3);
	private static final String TXT_MESSAGE4 = Game.getVar(R.string.TownsfolkNPC_Message4);

	private static String[] TXT_PHRASES = {TXT_MESSAGE1, TXT_MESSAGE2, TXT_MESSAGE3, TXT_MESSAGE4};

	public TownsfolkNPC() {
	}

	@Override
	public boolean interact(final Hero hero) {
		getSprite().turnTo( getPos(), hero.getPos() );

		int index = Random.Int(0, TXT_PHRASES.length);
		GameScene.show(new WndQuest(this, TXT_PHRASES[index]));
		return true;
	}
}


