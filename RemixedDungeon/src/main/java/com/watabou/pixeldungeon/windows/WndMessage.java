
package com.watabou.pixeldungeon.windows;

import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.Window;

public class WndMessage extends Window {

	private static final int WIDTH = 120;
	protected final Text info;

	public WndMessage(String text) {

		super();

		info = PixelScene.createMultiline(text, GuiProperties.regularFontSize());
		info.maxWidth(WIDTH - MARGIN * 2);
		info.setX(MARGIN);
		info.setY(MARGIN);
		add(info);

		resize(
				(int) info.width() + MARGIN * 2,
				(int) info.height() + MARGIN * 2);
	}

	public void setText(String text) {
		info.text(text);
		resize(
				(int) info.width() + MARGIN * 2,
				(int) info.height() + MARGIN * 2);
	}
}
