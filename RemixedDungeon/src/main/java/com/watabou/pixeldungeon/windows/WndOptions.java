
package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.windows.VBox;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.IconButton;
import com.watabou.pixeldungeon.ui.Window;

public abstract class WndOptions extends Window {

	protected final VBox buttonsVbox;

	public WndOptions(String title, String message, String... options ) {
		super();

        VBox vbox = new VBox();
        vbox.setGap(GAP);

		Text tfTitle = PixelScene.createMultiline(StringsManager.maybeId(title), GuiProperties.titleFontSize() );
		tfTitle.hardlight( TITLE_COLOR );
		tfTitle.setX(GAP);
		tfTitle.maxWidth(STD_WIDTH - GAP * 2);
		vbox.add( tfTitle );
		
		Text tfMessage = PixelScene.createMultiline(StringsManager.maybeId(message), GuiProperties.regularFontSize() );
		tfMessage.maxWidth(STD_WIDTH - GAP * 2);
		tfMessage.setX(GAP);
		vbox.add( tfMessage );

		buttonsVbox = new VBox();
		for (int i=0; i < options.length; i++) {
			final int index = i;
			var btn = new IconButton( StringsManager.maybeId(options[i]) ) {
				@Override
				protected void onClick() {
					hide();
					onSelect( index );
				}
			};

			btn.setSize(STD_WIDTH - GAP * 2, BUTTON_HEIGHT);
			buttonsVbox.add( btn );
		}

		buttonsVbox.setRect(GAP,0, STD_WIDTH, buttonsVbox.childsHeight());
		vbox.add(buttonsVbox);

		vbox.setRect(GAP,0, STD_WIDTH,vbox.childsHeight());
		add(vbox);
		resize(STD_WIDTH, (int) vbox.height());
	}

	abstract public void onSelect( int index );
}
