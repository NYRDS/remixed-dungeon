package com.watabou.pixeldungeon.items.rings;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.actors.hero.Doom;
import com.watabou.pixeldungeon.items.bags.Bag;
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
	
	public static class StoneWalking extends ArtifactBuff implements Doom {
		@Override
		public int icon() {
			return BuffIndicator.STONEBLOOD;
		}

		@Override
		public String name() {
			return Game.getVar(R.string.StoneBlood_Buff);
		}

		@Override
		public void onDeath() {
			Badges.validateDeathInStone();
			
			Dungeon.fail( Utils.format( ResultDescriptions.getDescription(ResultDescriptions.Reason.IMMURED), Dungeon.depth ) );
			GLog.n( Game.getVar(R.string.RingOfStoneWalking_ImmuredInStone));
			
		}
	}

	@Override
	public String bag() {
		return Bag.KEYRING;
	}
}
