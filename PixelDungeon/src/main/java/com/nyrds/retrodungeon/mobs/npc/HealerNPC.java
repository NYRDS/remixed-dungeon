package com.nyrds.retrodungeon.mobs.npc;

import com.nyrds.retrodungeon.ml.R;
import com.nyrds.retrodungeon.windows.WndPriest;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.GameScene;

public class HealerNPC extends ImmortalNPC {

	private static final String TXT_MESSAGE1 = Game.getVar(R.string.HealerNPC_Message1);
	private static final String TXT_MESSAGE2 = Game.getVar(R.string.HealerNPC_Message2);
	private static final String TXT_MESSAGE3 = Game.getVar(R.string.HealerNPC_Message3);

	private static String[] TXT_PHRASES = {TXT_MESSAGE1, TXT_MESSAGE2, TXT_MESSAGE3};

	public HealerNPC() {
	}
	
	@Override
	public boolean interact(final Hero hero) {
		getSprite().turnTo( getPos(), hero.getPos() );

		GameScene.show( new WndPriest( this, hero ) );
		return true;
	}
}


