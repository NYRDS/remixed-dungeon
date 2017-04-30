package com.nyrds.pixeldungeon.windows;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.items.common.ItemLibrary;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.ScrollPane;
import com.watabou.pixeldungeon.ui.TextButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.windows.WndInfoItem;

import java.util.Map;

public class WndLibraryCatalogue extends Window {

	private static final int BTN_HEIGHT		  = 16;
	private static final int BTN_WIDTH		  = 38;
	private static final int GAP			  = 4;

	private static final int HEIGHT_PORTRAIT  = 180;
	private static final int HEIGHT_LANDSCAPE = (int) PixelScene.MIN_HEIGHT_L;
	private static final int WIDTH			  = 112;

	String[] dummyEntriesList = {
			"Entry 1",
			"Entry two",
			"Three entries so far",
			"Quadruple entry"
	};

	public WndLibraryCatalogue(int catalogueNumber, String catalogueName) {
		super();
		final Window context = this;

		int yPos = 0;

		//List of Accessories
		//Title
		Text listTitle = PixelScene.createMultiline(catalogueName, GuiProperties.mediumTitleFontSize());
		listTitle.hardlight(TITLE_COLOR);
		listTitle.maxWidth(WIDTH - GAP * 2);
		listTitle.measure();
		listTitle.x = (WIDTH - listTitle.width()) / 2;
		listTitle.y = GAP * 2;

		add(listTitle);

		//List<String> entriesList = getEntryList(catalogueNumber);

		Component content = new Component();

		Map<String,Integer> knownMap = ItemLibrary.getKnowledgeMap();

		//List
		for (final String entry : knownMap.keySet()) {


			Text name = PixelScene.createMultiline(entry, GuiProperties.mediumTitleFontSize());

			name.hardlight(0xFFFFFF);

			name.y = yPos + GAP;
			name.maxWidth(BTN_WIDTH * 3);
			name.measure();
			name.x = GAP;

			content.add(name);
			float rbY = name.bottom() + GAP;

			String buttonText = Game.getVar(R.string.WndHats_InfoButton);
			final Window currentWindow = this;

			//Button
			TextButton rb = new RedButton(buttonText) {
				@Override
				protected void onClick() {
					super.onClick();

					GameScene.show(new WndInfoItem(ItemFactory.itemByName(entry)));
					//GameScene.show(new WndLibraryEntry(entry));
				}
			};

			rb.setRect(WIDTH - BTN_WIDTH, yPos, BTN_WIDTH, BTN_HEIGHT);

			content.add(rb);
			yPos = (int) (rb.bottom() + GAP * 2);
		}

		int HEIGHT = PixelDungeon.landscape() ? HEIGHT_LANDSCAPE : HEIGHT_PORTRAIT;
		int h = Math.min(HEIGHT - GAP, yPos);

		resize( WIDTH,  h);

		content.setSize(WIDTH, yPos);
		ScrollPane list = new ScrollPane(content);
		list.dontCatchTouch();

		add(list);

		float topGap = listTitle.height() + GAP;
		float BottomGap =  listTitle.bottom() + GAP * 5;

		list.setRect(0, topGap, WIDTH, HEIGHT - BottomGap);

		//Back Button
		TextButton back = new RedButton(Game.getVar(R.string.Wnd_Button_Back)) {
			@Override
			protected void onClick() {
				super.onClick();
				context.hide();
				GameScene.show(new WndLibrary());
			}
		};

		back.setRect((WIDTH / 2) - (BTN_WIDTH / 2), (int) list.bottom()+ GAP, BTN_WIDTH + GAP, BTN_HEIGHT + GAP);

		add(back);

	}
}