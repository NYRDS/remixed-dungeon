package com.nyrds.pixeldungeon.mobs.npc;

import com.nyrds.Packable;
import com.nyrds.android.util.Util;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.support.EuConsent;
import com.nyrds.pixeldungeon.support.RewardVideo;
import com.nyrds.pixeldungeon.windows.WndEuConsent;
import com.nyrds.pixeldungeon.windows.WndMovieTheatre;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndQuest;

public class ServiceManNPC extends ImmortalNPC {

	@Packable
	private static int filmsSeen = 0;

	private static final int BASIC_GOLD_REWARD = 150;

	public ServiceManNPC() {
		if(EuConsent.getConsentLevel()>EuConsent.UNKNOWN) {
			RewardVideo.init();
		}
	}

	private static int getReward(){
		return BASIC_GOLD_REWARD + (filmsSeen / 5) * 50;
	}

	public static void reward() {
		filmsSeen++;
		Dungeon.hero.collect(new Gold(getReward()));
	}

	@Override
	public boolean interact(final Hero hero) {

		if(EuConsent.getConsentLevel()<EuConsent.NON_PERSONALIZED) {
			Game.scene().add(new WndEuConsent() {
				@Override
				public void done() {
					RewardVideo.init();
				}
			});
			return true;
		}

		getSprite().turnTo( getPos(), hero.getPos() );

		if(!Util.isConnectedToInternet()) {
			GameScene.show(new WndQuest(this, Game.getVar(R.string.ServiceManNPC_NoConnection)));
			return true;
		}

		if(filmsSeen >= getLimit()){
			GameScene.show(new WndQuest(this, Utils.format(Game.getVar(R.string.ServiceManNPC_Limit_Reached), getLimit())));
			return true;
		}

		if(RewardVideo.isReady()) {
			GameScene.show(new WndMovieTheatre(this, filmsSeen, getLimit(), getReward()));
		} else {
			say(Game.getVar(R.string.ServiceManNPC_NotReady));
		}

		return true;
	}

	private static int getLimit(){
		return 4 + Dungeon.hero.lvl();
	}

	public static void resetLimit() {
		filmsSeen = 0;
	}

}
