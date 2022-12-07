package com.nyrds.retrodungeon.windows;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.retrodungeon.items.common.Library;
import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.CompositeTextureImage;
import com.watabou.noosa.Game;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.ListItem;
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
			Library.EntryHeader entryHeader = Library.infoHeader(category, entry);

			LibraryListItem rb = new LibraryListItem(category, entry, entryHeader);

			rb.setRect(0, yPos, WIDTH, BTN_HEIGHT);
			content.add(rb);

			yPos = (int) rb.bottom() + 1;
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

	private static class LibraryListItem extends ListItem {
		private final String finalCategory;
		private final String entryId;

		public LibraryListItem(String category, String entry, Library.EntryHeader desc) {
			finalCategory = category;
			entryId = entry;
			clickable = true;

			if(desc.icon instanceof CompositeTextureImage) {
				sprite.copy((CompositeTextureImage) desc.icon);
			} else {
				sprite.copy(desc.icon);
			}
			label.text(desc.header);
		}

		@Override
		protected void onClick() {
			GameScene.show(Library.infoWindow(finalCategory, entryId));
		}
	}

}