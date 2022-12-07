package com.nyrds.retrodungeon.windows;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.retrodungeon.items.necropolis.BlackSkull;
import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.IconTitle;

public class WndSadGhostNecro extends Window {

	private static final String TXT_TEXT = Game.getVar(R.string.WndSadGhostNecro_Text);
	private static final String TXT_YES = Game.getVar(R.string.WndSadGhostNecro_Yes);
	private static final String TXT_NO = Game.getVar(R.string.WndSadGhostNecro_No);
	private static final String TXT_PERSUADED = Game.getVar(R.string.WndSadGhostNecro_Persuaded);
	
	private static final int WIDTH		= 120;
	private static final int BTN_HEIGHT	= 18;

	private boolean persuade;

	public WndSadGhostNecro() {
		
		super();
		
		IconTitle titlebar = new IconTitle();
		titlebar.icon( new ItemSprite( new BlackSkull()) );
		titlebar.label( Utils.capitalize( Game.getVar(R.string.Necromancy_Title) ) );
		titlebar.setRect( 0, 0, WIDTH, 0 );
		add( titlebar );
		
		Text message = PixelScene.createMultiline( TXT_TEXT, GuiProperties.regularFontSize() );
		message.maxWidth(WIDTH);
		message.measure();
		message.y = titlebar.bottom() + GAP;
		add( message );
		
		RedButton btnWeapon = new RedButton( TXT_YES ) {
			@Override
			protected void onClick() {
				GLog.w( TXT_PERSUADED );
				persuade = true;
				hide();
			}
		};
		btnWeapon.setRect( 0, message.y + message.height() + GAP, WIDTH, BTN_HEIGHT );
		add( btnWeapon );
		
		RedButton btnArmor = new RedButton( TXT_NO ) {
			@Override
			protected void onClick() {
				persuade = false;
				hide();
			}
		};
		btnArmor.setRect( 0, btnWeapon.bottom() + GAP, WIDTH, BTN_HEIGHT );
		add( btnArmor );
		
		resize( WIDTH, (int)btnArmor.bottom() );
	}
	
	public boolean getPersuade() {
		return persuade;
	}
}
