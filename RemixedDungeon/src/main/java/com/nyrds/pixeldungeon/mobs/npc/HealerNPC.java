package com.nyrds.pixeldungeon.mobs.npc;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.windows.WndPriest;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Random;

public class HealerNPC extends ImmortalNPC {

	public HealerNPC() {
		movable = false;
	}
	
	@Override
	public boolean interact(final Char hero) {
		getSprite().turnTo( getPos(), hero.getPos() );

		GameScene.show( new WndPriest( this, hero ) );
		return true;
	}

	@Override
    public boolean act() {
		Hero hero = Dungeon.hero;
		if (Dungeon.level.distanceL2(hero.getPos(), getPos()) < 4) {
			if(Random.Int(20) == 0) {
				sayRandomPhrase(R.string.HealerNPC_Message1,
						        R.string.HealerNPC_Message2,
			        			R.string.HealerNPC_Message3);
			}
		}

		return super.act();
	}
}


