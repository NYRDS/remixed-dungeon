package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.items.common.Library;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.ScrollPane;
import com.watabou.pixeldungeon.ui.TextButton;
import com.watabou.pixeldungeon.ui.Window;

import java.text.Collator;
import java.util.Arrays;
import java.util.Map;



public class WndLibraryCatalogue extends Window {

	private static final int BTN_HEIGHT = 16;
	private static final int BTN_WIDTH  = 38;
	private static final int GAP        = 4;

	private static final int WIDTH = WndHelper.getLimitedWidth(112);

	public WndLibraryCatalogue(String category, String catalogueName) {
		super();

		int yPos = 0;

		//Title
		Text listTitle = PixelScene.createMultiline(catalogueName, GuiProperties.mediumTitleFontSize());
		listTitle.hardlight(TITLE_COLOR);
		listTitle.maxWidth(WIDTH - GAP * 2);
		listTitle.setX((WIDTH - listTitle.width()) / 2);
		listTitle.setY(0);

		add(listTitle);

		Component content = new Component();

		Map<String, Integer> knownMap = Library.getKnowledgeMap(category);

		var sortedKeys = knownMap.keySet().toArray(new String[0]);

		Arrays.sort(sortedKeys,
				(o1, o2) -> {
					var e1 = Library.infoHeader(category, o1);
					var e2 = Library.infoHeader(category, o2);
					return Collator.getInstance().compare(e1.header, e2.header);
				});

		//List
		for (final String entry : sortedKeys) {

			//Button
			Library.EntryHeader entryHeader = Library.infoHeader(category, entry);

			if(!entryHeader.header.isEmpty()) {
				LibraryListItem rb = new LibraryListItem(category, entry, entryHeader);

				rb.setRect(0, yPos, WIDTH, BTN_HEIGHT);
				content.add(rb);

				yPos = (int) rb.bottom() + 1;
			}
		}

		int HEIGHT = WndHelper.getFullscreenHeight() - BTN_HEIGHT * 2;
		int h = Math.min(HEIGHT - GAP, yPos);

		resize(WIDTH, h + BTN_WIDTH);

		content.setSize(WIDTH, yPos);

		ScrollPane list = new ScrollableList(content);

		add(list);

		float topGap = listTitle.height() + GAP;
		float BottomGap = listTitle.bottom() - BTN_HEIGHT / 2;

		list.setRect(0, topGap, WIDTH, HEIGHT - BottomGap);

		//Back Button
        TextButton back = new RedButton(R.string.Wnd_Button_Back) {
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