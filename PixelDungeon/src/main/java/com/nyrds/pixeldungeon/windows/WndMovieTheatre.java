
package com.nyrds.pixeldungeon.windows;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.npc.ServiceManNPC;
import com.nyrds.pixeldungeon.support.Ads;
import com.nyrds.pixeldungeon.support.AdsUtils;
import com.nyrds.pixeldungeon.support.RewardVideo;
import com.watabou.noosa.Game;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.IconTitle;
import com.watabou.pixeldungeon.windows.WndMessage;

public class WndMovieTheatre extends Window implements InterstitialPoint{

	private static final int BTN_HEIGHT	= 18;
	private static final int WIDTH		= 120;

	private ServiceManNPC serviceMan;

	public WndMovieTheatre(final ServiceManNPC npc, int filmsSeen, int limit, int goldReward) {
		
		super();

		serviceMan = npc;
		
		IconTitle titlebar = new IconTitle();
		titlebar.icon( new ItemSprite(new Gold()) );
		titlebar.label( Utils.capitalize( Game.getVar(R.string.WndMovieTheatre_Title) ) );
		titlebar.setRect( 0, 0, WIDTH, 0 );
		add( titlebar );
		
		String instruction = Utils.format(Game.getVar(R.string.WndMovieTheatre_Instruction), goldReward) + "\n\n" + Utils.format(Game.getVar(R.string.WndMovieTheatre_Instruction_2), filmsSeen, limit);

		Text message = PixelScene.createMultiline( instruction, GuiProperties.regularFontSize() );

		message.maxWidth(WIDTH);
		message.y = titlebar.bottom() + GAP;
		add( message );
		
		RedButton btnYes = new RedButton( Game.getVar(R.string.WndMovieTheatre_Watch) ) {
			@Override
			protected void onClick() {
				showAd( );
			}
		};
		btnYes.setRect( 0, message.y + message.height() + GAP, WIDTH, BTN_HEIGHT );
		add( btnYes );

		RedButton btnNo = new RedButton( Game.getVar(R.string.WndMovieTheatre_No) ) {
			@Override
			protected void onClick() {
				serviceMan.say( Game.getVar(R.string.WndMovieTheatre_Bye) );
				hide();
			}
		};
		btnNo.setRect( 0, btnYes.bottom() + GAP, WIDTH, BTN_HEIGHT );
		add( btnNo );
		
		resize( WIDTH, (int)btnNo.bottom() );
	}

	private void showAd() {
		hide();

		Game.softPaused = true;
		Dungeon.save();
		Game.instance().runOnUiThread(() -> {
			AdsUtils.removeTopBanner();
			RewardVideo.showCinemaRewardVideo(this);
		});
	}

	@Override
	public void returnToWork(final boolean result) {
		PixelDungeon.pushUiTask(new Runnable() {
			@Override
			public void run() {
				Game.softPaused = false;
				Hero.doOnNextAction = new RewardTask(result);

				PixelDungeon.landscape(PixelDungeon.storedLandscape());
				PixelDungeon.setNeedSceneRestart(true);
			}
		});

	}

	private class RewardTask implements Runnable {
		private boolean needReward;

		public RewardTask(boolean result) {
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
						needReward = false;
					} else {
						serviceMan.say(Game.getVar(R.string.WndMovieTheatre_Sorry));
					}

					if (PixelDungeon.donated() == 0) {
						if (PixelDungeon.getDifficulty() == 0) {
							Ads.displayEasyModeBanner();
						}
					}

				}
			});
		}
	}
}
