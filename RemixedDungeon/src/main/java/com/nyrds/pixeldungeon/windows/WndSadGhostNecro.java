package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.items.necropolis.BlackSkull;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.IconTitle;

public class WndSadGhostNecro extends Window {

	private static final int WIDTH		= 120;
	private static final int BTN_HEIGHT	= 18;

	private boolean persuade;

	public WndSadGhostNecro() {
		
		super();
		
		IconTitle titlebar = new IconTitle();
		titlebar.icon( new ItemSprite( new BlackSkull()) );
        titlebar.label( Utils.capitalize(StringsManager.getVar(R.string.Necromancy_Title)) );
		titlebar.setRect( 0, 0, WIDTH, 0 );
		add( titlebar );

        Text message = PixelScene.createMultiline(R.string.WndSadGhostNecro_Text, GuiProperties.regularFontSize() );
		message.maxWidth(WIDTH);
		message.setY(titlebar.bottom() + GAP);
		add( message );

        RedButton btnWeapon = new RedButton(R.string.WndSadGhostNecro_Yes) {
			@Override
			protected void onClick() {
                GLog.w(StringsManager.getVar(R.string.WndSadGhostNecro_Persuaded));
				persuade = true;
				hide();
			}
		};
		btnWeapon.setRect( 0, message.getY() + message.height() + GAP, WIDTH, BTN_HEIGHT );
		add( btnWeapon );

        RedButton btnArmor = new RedButton(R.string.WndSadGhostNecro_No) {
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
