
package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.npc.ServiceManNPC;
import com.nyrds.pixeldungeon.support.Ads;
import com.nyrds.pixeldungeon.support.AdsUtils;
import com.watabou.noosa.Game;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.RemixedDungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndMessage;
import com.watabou.pixeldungeon.windows.WndQuest;

public class WndMovieTheatre extends WndQuest implements InterstitialPoint{

	private ServiceManNPC serviceMan;

	public WndMovieTheatre(final ServiceManNPC npc, int filmsSeen, int limit, int goldReward) {
		
		super(npc, Utils.format(Game.getVar(R.string.WndMovieTheatre_Instruction), goldReward) + "\n\n" + Utils.format(Game.getVar(R.string.WndMovieTheatre_Instruction_2), filmsSeen, limit));

		serviceMan = npc;

		float y = height + 2*GAP;

		RedButton btnYes = new RedButton( Game.getVar(R.string.WndMovieTheatre_Watch) ) {
			@Override
			protected void onClick() {
				showAd( );
			}
		};
		btnYes.setRect( 0, y + GAP, STD_WIDTH, BUTTON_HEIGHT);
		add( btnYes );

		RedButton btnNo = new RedButton( Game.getVar(R.string.WndMovieTheatre_No) ) {
			@Override
			protected void onClick() {
				serviceMan.say( Game.getVar(R.string.WndMovieTheatre_Bye) );
				hide();
			}
		};
		btnNo.setRect( 0, btnYes.bottom(), STD_WIDTH, BUTTON_HEIGHT );
		add( btnNo );
		
		resize( STD_WIDTH, (int)btnNo.bottom() );
	}

	private void showAd() {
		hide();

		Game.softPaused = true;
		Dungeon.save();

		Game.instance().runOnUiThread(() -> {
			AdsUtils.removeTopBanner();
			Ads.showRewardVideo(this);
		});
	}

	@Override
	public void returnToWork(final boolean result) {
		RemixedDungeon.pushUiTask(() -> {
			Game.softPaused = false;
			Hero.doOnNextAction = new RewardTask(result);

			RemixedDungeon.landscape(RemixedDungeon.storedLandscape());
			RemixedDungeon.setNeedSceneRestart(true);
		});

	}

	private class RewardTask implements Runnable {
		private boolean needReward;

		RewardTask(boolean result) {
			needReward = result;
		}

		@Override
		public void run() {
			GameScene.show(new WndMessage(Game.getVar(R.string.WndMovieTheatre_Thank_You) ) {

				@Override
				public void destroy() {
					super.destroy();
					if(needReward) {
						serviceMan.say(Game.getVar(R.string.WndMovieTheatre_Ok));
						ServiceManNPC.reward();
						EventCollector.logCountedEvent("ad_reward5",  6);
						EventCollector.logCountedEvent("ad_reward10", 11);
						needReward = false;
					} else {
						serviceMan.say(Game.getVar(R.string.WndMovieTheatre_Sorry));
					}

					if (RemixedDungeon.donated() == 0) {
						if (RemixedDungeon.getDifficulty() == 0) {
							Ads.displayEasyModeBanner();
						}
					}

				}
			});
		}
	}
}
