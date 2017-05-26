package com.nyrds.pixeldungeon.mobs.npc;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.windows.WndQuest;

public class LibrarianNPC extends ImmortalNPC {

	private static final String TXT_MESSAGE = Game.getVar(R.string.LibrarianNPC_Message_Instruction);

	public LibrarianNPC() {
	}

	@Override
	public boolean interact(final Hero hero) {
		getSprite().turnTo( getPos(), hero.getPos() );

		GameScene.show(new WndQuest(this, TXT_MESSAGE));
		return true;
	}

}
