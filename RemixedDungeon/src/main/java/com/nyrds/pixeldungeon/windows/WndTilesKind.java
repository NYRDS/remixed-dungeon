package com.nyrds.pixeldungeon.windows;

import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.Window;

public class WndTilesKind extends Window {

	private static final int WIDTH = 120;

	public WndTilesKind() {

		super();

		Image icon = new Image("ui/xyz_tiles.png");
		icon.setScale(2);
		add(icon);

		Text info = PixelScene.createMultiline("You're about to enter Dungeon possessed by twisted magic, yet you now would chose how do you see it: Do you like be in modern", GuiProperties.regularFontSize());
		info.maxWidth(WIDTH - MARGIN * 2);

		float w = Math.max(icon.width(), info.width()) + MARGIN * 2;

		icon.setX((w - icon.width()) / 2);
		icon.setY(MARGIN);

		float pos = icon.getY() + icon.height() + MARGIN;

		add(info);

		resize((int) w, (int) (pos + info.height() + MARGIN));
	}
}
