package com.nyrds.pixeldungeon.windows;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.storage.Preferences;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;

public class WndTilesKind extends Window {

	private static final int WIDTH = 100;

	public WndTilesKind() {

		super();

		VBox vbox = new VBox();
		vbox.setAlign(VBox.Align.Center);
		vbox.setGap(4);

		Text title = PixelScene.createMultiline(R.string.WndTilesKind_Title, GuiProperties.titleFontSize());
		title.maxWidth(WIDTH);

		vbox.addRow(WIDTH, HBox.Align.Center, title);


		Image image = new Image("ui/xyz_tiles.png");
		vbox.addRow(WIDTH, HBox.Align.Center, image);

		Text info = PixelScene.createMultiline(R.string.WndTilesKind_text, GuiProperties.regularFontSize());
		info.maxWidth(WIDTH);

		vbox.addRow(WIDTH, HBox.Align.Center, info);

		RedButton newTiles = new RedButton(R.string.WndTilesKind_NewLook) {
			@Override
			public void onClick() {
				setTilesMode(true);
			}
		};
		newTiles.autoSize();

		RedButton classicTiles = new RedButton(R.string.WndTilesKind_ClassicLook) {
			@Override
			public void onClick() {
				setTilesMode(false);
			}
		};
		classicTiles.autoSize();


		vbox.addRow(WIDTH, HBox.Align.Width, newTiles, classicTiles);

		add(vbox);

		vbox.layout();
		vbox.setSize(WIDTH,vbox.height() + 8);
		vbox.layout();
		resize(WIDTH, (int) vbox.height());
	}

	@LuaInterface
	public boolean shownBefore() {
		return Preferences.INSTANCE.getBoolean(Preferences.KEY_TILES_QUESTION_ASKED, false);
	}

	private void setTilesMode(boolean newTiles) {
		Preferences.INSTANCE.put(Preferences.KEY_TILES_QUESTION_ASKED, true);
		Preferences.INSTANCE.put(Preferences.KEY_USE_ISOMETRIC_TILES, newTiles);
		Dungeon.setIsometricMode(newTiles);
		hide();
	}
}
