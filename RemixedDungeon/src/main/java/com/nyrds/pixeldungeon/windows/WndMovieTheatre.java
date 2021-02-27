
package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.npc.ServiceManNPC;
import com.nyrds.pixeldungeon.support.Ads;
import com.watabou.noosa.Game;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.RemixedDungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndQuest;

public class WndMovieTheatre extends WndQuest implements InterstitialPoint{

	public WndMovieTheatre(final ServiceManNPC npc, int filmsSeen, int limit, int goldReward) {
		
		super(npc, Utils.format(Game.getVar(R.string.WndMovieTheatre_Instruction), goldReward) + "\n\n" + Utils.format(Game.getVar(R.string.WndMovieTheatre_Instruction_2), filmsSeen, limit));

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
				npc.say( Game.getVar(R.string.WndMovieTheatre_Bye) );
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

		Game.instance().runOnUiThread(() -> {
			Ads.removeEasyModeBanner();
			Ads.showRewardVideo(this);
		});
	}

	@Override
	public void returnToWork(final boolean result) {

		Hero.movieRewardPending = result;
		Dungeon.save(true);

		RemixedDungeon.pushUiTask(() -> {
			Game.softPaused = false;
			Hero.doOnNextAction = new MovieRewardTask(result);

			RemixedDungeon.landscape(RemixedDungeon.storedLandscape());
			RemixedDungeon.setNeedSceneRestart(true);
		});

	}

}
