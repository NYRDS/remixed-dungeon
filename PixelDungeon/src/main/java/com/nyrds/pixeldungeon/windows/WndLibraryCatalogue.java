package com.nyrds.pixeldungeon.windows;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.pixeldungeon.items.common.Library;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.ScrollPane;
import com.watabou.pixeldungeon.ui.TextButton;
import com.watabou.pixeldungeon.ui.Window;

import java.util.Map;

public class WndLibraryCatalogue extends Window {

	private static final int BTN_HEIGHT = 16;
	private static final int BTN_WIDTH  = 38;
	private static final int GAP        = 4;

	private static final int WIDTH = WndHelper.getLimitedWidth(112);

	public WndLibraryCatalogue(String category, String catalogueName) {
		super();

		int yPos = 0;

		//List of Accessories
		//Title
		Text listTitle = PixelScene.createMultiline(catalogueName, GuiProperties.mediumTitleFontSize());
		listTitle.hardlight(TITLE_COLOR);
		listTitle.maxWidth(WIDTH - GAP * 2);
		listTitle.measure();
		listTitle.x = (WIDTH - listTitle.width()) / 2;
		listTitle.y = 0;

		add(listTitle);

		Component content = new Component();

		Map<String, Integer> knownMap = Library.getKnowledgeMap(category);

		//List
		for (final String entry : knownMap.keySet()) {

			//Button
			final String finalCategory = category;
			Library.EntryHeader entryHeader = Library.infoHeader(category, entry);
			TextButton rb = new RedButton(entryHeader.header) {
				@Override
				protected void onClick() {
					super.onClick();
					GameScene.show(Library.infoWindow(finalCategory, entry));
				}

				@Override
				protected void layout() {
					super.layout();

					float margin = (height - text.baseLine()) / 2;

					text.x = PixelScene.align(PixelScene.uiCamera, x + margin);
					text.y = PixelScene.align(PixelScene.uiCamera, y + margin);

					icon.x = PixelScene.align(PixelScene.uiCamera, x + width - margin - icon.width);
					icon.y = PixelScene.align(PixelScene.uiCamera, y + (height - icon.height()) / 2);
				}
			};
			rb.icon(entryHeader.icon);

			rb.setRect(0, yPos, WIDTH, BTN_HEIGHT);

			content.add(rb);
			yPos = (int) rb.bottom() + 1;
		}

		int HEIGHT = WndHelper.getFullscreenHeight() - BTN_HEIGHT * 2;
		int h = Math.min(HEIGHT - GAP, yPos);

		resize(WIDTH, h + BTN_WIDTH);

		content.setSize(WIDTH, yPos);
		ScrollPane list = new ScrollPane(content);
		list.dontCatchTouch();

		add(list);

		float topGap = listTitle.height() + GAP;
		float BottomGap = listTitle.bottom() - BTN_HEIGHT / 2;

		list.setRect(0, topGap, WIDTH, HEIGHT - BottomGap);

		//Back Button
		TextButton back = new RedButton(Game.getVar(R.string.Wnd_Button_Back)) {
			@Override
			protected void onClick() {
				super.onClick();
				hide();
				GameScene.show(new WndLibrary());
			}
		};

		back.setRect((WIDTH / 2) - (BTN_WIDTH / 2), (int) list.bottom() + GAP, BTN_WIDTH + GAP, BTN_HEIGHT);

		add(back);
	}
}