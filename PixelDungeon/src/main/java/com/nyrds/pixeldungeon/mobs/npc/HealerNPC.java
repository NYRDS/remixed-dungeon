package com.nyrds.pixeldungeon.mobs.npc;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.windows.WndPriest;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Random;

public class HealerNPC extends ImmortalNPC {

	private static final String TXT_MESSAGE1 = Game.getVar(R.string.HealerNPC_Message1);
	private static final String TXT_MESSAGE2 = Game.getVar(R.string.HealerNPC_Message2);
	private static final String TXT_MESSAGE3 = Game.getVar(R.string.HealerNPC_Message3);

	private static String[] TXT_PHRASES = {TXT_MESSAGE1, TXT_MESSAGE2, TXT_MESSAGE3};

	public HealerNPC() {
		movable = false;
	}
	
	@Override
	public boolean interact(final Hero hero) {
		getSprite().turnTo( getPos(), hero.getPos() );

		GameScene.show( new WndPriest( this, hero ) );
		return true;
	}

	@Override
	protected boolean act() {
		Hero hero = Dungeon.hero;
		if (Dungeon.level.distanceL2(hero.getPos(), getPos()) < 4) {
			if(Random.Int(20) == 0) {
				say(Random.element(TXT_PHRASES));
			}
		}
		return super.act();
	}
}


