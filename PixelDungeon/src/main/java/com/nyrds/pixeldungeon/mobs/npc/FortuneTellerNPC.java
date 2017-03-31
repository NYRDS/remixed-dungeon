package com.nyrds.pixeldungeon.mobs.npc;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.windows.WndFortuneTeller;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.Identification;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndBag;
import com.watabou.pixeldungeon.windows.WndQuest;
import com.watabou.pixeldungeon.windows.WndTradeItem;

public class FortuneTellerNPC extends ImmortalNPC {

	private static final String TXT_MESSAGE = Game.getVar(R.string.BellaNPC_Message);
	private static final String TXT_AMULET_M = Game.getVar(R.string.BellaNPC_Amulet_M);
	private static final String TXT_AMULET_F = Game.getVar(R.string.BellaNPC_Amulet_F);
	private static final String TXT_Angry = Game.getVar(R.string.BellaNPC_Angry);

	public FortuneTellerNPC() {
	}




	@Override
	public boolean interact(final Hero hero) {
		getSprite().turnTo( getPos(), hero.getPos() );
		GameScene.show(new WndFortuneTeller(this));
		return true;
	}

}


