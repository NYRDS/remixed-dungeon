
package com.nyrds.retrodungeon.windows;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.retrodungeon.ml.R;
import com.nyrds.retrodungeon.mobs.npc.ServiceManNPC;
import com.nyrds.retrodungeon.support.Ads;
import com.nyrds.retrodungeon.support.RewardVideo;
import com.watabou.noosa.Game;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.PixelDungeon;
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

	private static final String BTN_WATCH       = Game.getVar(R.string.WndMovieTheatre_Watch);
	private static final String BTN_NO          = Game.getVar(R.string.WndMovieTheatre_No);
	private static final String TXT_BYE         = Game.getVar(R.string.WndMovieTheatre_Bye);
	private static final String TXT_INSTRUCTION = Game.getVar(R.string.WndMovieTheatre_Instruction);
	private static final String TXT_TITLE       = Game.getVar(R.string.WndMovieTheatre_Title);
	private static final String TXT_THANK_YOU   = Game.getVar(R.string.WndMovieTheatre_Thank_You);
	private static final String TXT_SORRY       = Game.getVar(R.string.WndMovieTheatre_Sorry);
	private static final String TXT_OK       = Game.getVar(R.string.WndMovieTheatre_Ok);

	private static final int BTN_HEIGHT	= 18;
	private static final int WIDTH		= 120;

	private ServiceManNPC serviceMan;

	public WndMovieTheatre(final ServiceManNPC npc, int filmsSeen, int limit, int goldReward) {
		
		super();

		serviceMan = npc;
		
		IconTitle titlebar = new IconTitle();
		titlebar.icon( new ItemSprite(new Gold()) );
		titlebar.label( Utils.capitalize( TXT_TITLE ) );
		titlebar.setRect( 0, 0, WIDTH, 0 );
		add( titlebar );
		
		String instruction = Utils.format(TXT_INSTRUCTION, goldReward) + "\n\n" + Utils.format(Game.getVar(R.string.WndMovieTheatre_Instruction_2), filmsSeen, limit);

		Text message = PixelScene.createMultiline( instruction, GuiProperties.regularFontSize() );

		message.maxWidth(WIDTH);
		message.measure();
		message.y = titlebar.bottom() + GAP;
		add( message );
		
		RedButton btnYes = new RedButton( BTN_WATCH ) {
			@Override
			protected void onClick() {
				showAd( );
			}
		};
		btnYes.setRect( 0, message.y + message.height() + GAP, WIDTH, BTN_HEIGHT );
		add( btnYes );

		RedButton btnNo = new RedButton( BTN_NO ) {
			@Override
			protected void onClick() {
				serviceMan.say( TXT_BYE );
				hide();
			}
		};
		btnNo.setRect( 0, btnYes.bottom() + GAP, WIDTH, BTN_HEIGHT );
		add( btnNo );
		
		resize( WIDTH, (int)btnNo.bottom() );
	}

	private void showAd() {
		hide();

		Game.paused = true;
		Ads.removeEasyModeBanner();
		RewardVideo.showCinemaRewardVideo(this);
	}

	@Override
	public void returnToWork(final boolean result) {
		PixelDungeon.executeInGlThread(new Runnable() {
			@Override
			public void run() {

				Dungeon.hero.doOnNextAction = new Runnable() {
					@Override
					public void run() {
						GameScene.show(new WndMessage(TXT_THANK_YOU) {
							@Override
							public void hide() {
								super.hide();
								if(result) {
									serviceMan.say(TXT_OK);
									serviceMan.reward();
								} else {
									serviceMan.say(TXT_SORRY);
								}

								if (PixelDungeon.donated() == 0) {
									if (PixelDungeon.getDifficulty() == 0) {
										Ads.displayEasyModeBanner();
									}
								}
							}
						});
					}
				};

				PixelDungeon.landscape(PixelDungeon.landscape());
				PixelDungeon.setNeedSceneRestart(true);
			}
		});

	}
}
