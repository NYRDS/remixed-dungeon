package com.watabou.pixeldungeon.items.rings;

import com.nyrds.retrodungeon.ml.R;

import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

public class RingOfStoneWalking extends Artifact{

	public RingOfStoneWalking() {
		image = ItemSpriteSheet.RING_OF_STONE_WALKING;
		identify();
	}
	
	@Override
	protected ArtifactBuff buff( ) {
		return new StoneWalking();
	}
	
	public class StoneWalking extends ArtifactBuff implements Hero.Doom{
		@Override
		public int icon() {
			return BuffIndicator.STONEBLOOD;
		}

		@Override
		public String toString() {
			return Game.getVar(R.string.StoneBlood_Buff);
		}

		@Override
		public void onDeath() {
			Badges.validateDeathInStone();
			
			Dungeon.fail( Utils.format( ResultDescriptions.IMMURED, Dungeon.depth ) );
			GLog.n( Game.getVar(R.string.RingOfStoneWalking_ImmuredInStone));
			
		}
	}
}
