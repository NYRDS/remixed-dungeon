
package com.nyrds.pixeldungeon.windows;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.npc.ServiceManNPC;
import com.nyrds.pixeldungeon.support.Ads;
import com.nyrds.pixeldungeon.support.RewardVideoAds;
import com.watabou.noosa.Game;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.IconTitle;

public class WndMovieTheatre extends Window implements InterstitialPoint{

	private static final String BTN_WATCH       = Game.getVar(R.string.WndMovieTheatre_Watch);
	private static final String BTN_NO          = Game.getVar(R.string.WndMovieTheatre_No);
	private static final String TXT_BYE         = Game.getVar(R.string.WndMovieTheatre_Bye);
	private static final String TXT_INSTRUCTION = Game.getVar(R.string.WndMovieTheatre_Instruction);
	private static final String TXT_TITLE       = Game.getVar(R.string.WndMovieTheatre_Title);
	private static final String TXT_THANK_YOU   = Game.getVar(R.string.WndMovieTheatre_Thank_You);

	private static final int BTN_HEIGHT	= 18;
	private static final int WIDTH		= 120;
	private static final int GOLD_REWARD = 150;

	public WndMovieTheatre(final ServiceManNPC npc) {
		
		super();
		
		IconTitle titlebar = new IconTitle();
		titlebar.icon( new ItemSprite(new Gold()) );
		titlebar.label( Utils.capitalize( TXT_TITLE ) );
		titlebar.setRect( 0, 0, WIDTH, 0 );
		add( titlebar );
		
		Text message = PixelScene.createMultiline( Utils.format(TXT_INSTRUCTION, GOLD_REWARD), GuiProperties.regularFontSize() );
		message.maxWidth(WIDTH);
		message.measure();
		message.y = titlebar.bottom() + GAP;
		add( message );
		
		RedButton btnYes = new RedButton( BTN_WATCH ) {
			@Override
			protected void onClick() {
				showAd( npc );
			}
		};
		btnYes.setRect( 0, message.y + message.height() + GAP, WIDTH, BTN_HEIGHT );
		add( btnYes );
		
		RedButton btnNo = new RedButton( BTN_NO ) {
			@Override
			protected void onClick() {
				npc.say( TXT_BYE );
				hide();
			}
		};
		btnNo.setRect( 0, btnYes.bottom() + GAP, WIDTH, BTN_HEIGHT );
		add( btnNo );
		
		resize( WIDTH, (int)btnNo.bottom() );
	}

	private void showAd(final ServiceManNPC npc) {
		hide();
		npc.say( TXT_THANK_YOU );
		Game.paused = true;
		Ads.removeEasyModeBanner();
		RewardVideoAds.showCinemaRewardVideo(this);
	}

	@Override
	public void returnToWork(final boolean result) {
		PixelDungeon.executeInGlThread(new Runnable() {
			@Override
			public void run() {

				if (PixelDungeon.donated() == 0) {
					if (PixelDungeon.getDifficulty() == 0) {
						Ads.displayEasyModeBanner();
					}
				}

				PixelDungeon.landscape(PixelDungeon.landscape());

				Game.paused = false;

				if(result) {
					Dungeon.gold(Dungeon.gold() + 150);

				} else {
					GLog.i("Индейский национальный фигвам");
				}

			}
		});

	}
}
