package com.nyrds.pixeldungeon.windows;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.pixeldungeon.levels.objects.PortalGate;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.Position;
import com.watabou.noosa.Game;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.InterlevelScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.TextButton;
import com.watabou.pixeldungeon.ui.Window;

public class WndPortal extends Window {

	protected static final int BTN_HEIGHT	= 18;
	protected static final int WIDTH		= 100;
	protected static final int GAP	    	= 2;

	protected String getDesc(){
		return Game.getVar(R.string.WndPortal_Info);
	}

	public WndPortal(final PortalGate portal, final Hero hero, final Position returnTo ) {
		super();

		//Title text
		Text tfTitle = PixelScene.createMultiline(Game.getVar(R.string.WndPortal_Title), GuiProperties.mediumTitleFontSize());
		tfTitle.hardlight(TITLE_COLOR);
		tfTitle.maxWidth(WIDTH - GAP);
		tfTitle.x = (WIDTH - tfTitle.width())/2;
		tfTitle.y = GAP;
		add(tfTitle);

		//Instruction text
		Text message = PixelScene.createMultiline(getDesc(), GuiProperties.regularFontSize() );
		message.maxWidth(WIDTH);
		message.y = tfTitle.bottom()+ GAP;
		add( message );

		//Yes Button
		TextButton btnYes = new RedButton(Game.getVar(R.string.Wnd_Button_Yes)) {
			@Override
			protected void onClick() {
				super.onClick();
				hide();
				portal.useUp();

				hero.setPortalLevelCoordinates(portal.getPosition());

				InterlevelScene.returnTo = new Position(returnTo);
				InterlevelScene.Do(InterlevelScene.Mode.RETURN);
			}
		};

		btnYes.setRect(0, message.bottom() + GAP, WIDTH, BTN_HEIGHT);
		add(btnYes);

		//No Button
		TextButton btnNo = new RedButton(Game.getVar(R.string.Wnd_Button_No)) {
			@Override
			protected void onClick() {
				super.onClick();
				hide();
			}
		};

		btnNo.setRect(0, btnYes.bottom() + GAP, WIDTH, BTN_HEIGHT);
		add(btnNo);

		resize( WIDTH, (int) btnNo.bottom() + BTN_HEIGHT / 2);
	}
}
