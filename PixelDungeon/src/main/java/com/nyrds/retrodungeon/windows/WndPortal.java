package com.nyrds.retrodungeon.windows;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.retrodungeon.levels.objects.PortalGate;
import com.nyrds.retrodungeon.ml.R;
import com.nyrds.retrodungeon.utils.Position;
import com.watabou.noosa.Game;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.InterlevelScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.TextButton;
import com.watabou.pixeldungeon.ui.Window;

public class WndPortal extends Window {

	protected static final int BTN_HEIGHT	= 18;
	protected static final int BTN_WIDTH	= 38;
	protected static final int WIDTH		= 100;
	protected static final int GAP		= 2;

	protected static final String TXT_TITLE = Game.getVar(R.string.WndPortal_Title);
	protected final String TXT_INFO = getDesc();
	protected static final String BTN_YES = Game.getVar(R.string.Wnd_Button_Yes);
	protected static final String BTN_NO = Game.getVar(R.string.Wnd_Button_No);

	protected String getDesc(){
		return Game.getVar(R.string.WndPortal_Info);
	}

	public WndPortal(final PortalGate portal, final Hero hero, final Position returnTo ) {
		super();

		//Title text
		Text tfTitle = PixelScene.createMultiline(TXT_TITLE, GuiProperties.mediumTitleFontSize());
		tfTitle.hardlight(TITLE_COLOR);
		tfTitle.maxWidth(WIDTH - GAP);
		tfTitle.measure();
		tfTitle.x = (WIDTH - tfTitle.width())/2;
		tfTitle.y = GAP;
		add(tfTitle);

		//Instruction text
		Text message = PixelScene.createMultiline(TXT_INFO, GuiProperties.regularFontSize() );
		message.maxWidth(WIDTH);
		message.measure();
		message.y = tfTitle.bottom()+ GAP;
		add( message );

		int buttonY = (int) message.bottom()+ GAP;


		//Yes Button
		TextButton btnYes = new RedButton(BTN_YES) {
			@Override
			protected void onClick() {
				super.onClick();
				hide();
				portal.useUp();

				hero.setPortalLevelCoordinates(Dungeon.currentPosition());
				InterlevelScene.mode = InterlevelScene.Mode.RETURN;
				InterlevelScene.returnTo = new Position(returnTo);
				Game.switchScene( InterlevelScene.class );
			}
		};

		btnYes.setRect(0, message.bottom() + GAP, WIDTH, BTN_HEIGHT);
		add(btnYes);

		buttonY = (int) btnYes.bottom()+ GAP;

		//No Button
		TextButton btnNo = new RedButton(BTN_NO) {
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
