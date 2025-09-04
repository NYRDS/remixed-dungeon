
package com.watabou.pixeldungeon.windows;


import static com.watabou.pixeldungeon.ui.Window.MARGIN;

import com.nyrds.pixeldungeon.windows.VBox;
import com.nyrds.pixeldungeon.windows.VHBox;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.elements.CharTitle;
import com.watabou.pixeldungeon.windows.elements.TabContent;

public class CharDescTab extends TabContent {

	public CharDescTab(Char mob, Char selector, int maxWidth) {
		setMaxWidth(maxWidth);
		VBox mainBox = new VBox();
		mainBox.setGap(8);

		CharTitle charTitle = new CharTitle(mob);
		charTitle.setPos(MARGIN,MARGIN);
		charTitle.setSize(maxWidth - MARGIN, 0);
		mainBox.add(charTitle);

		Text text = PixelScene.createMultilineHighlighted(desc(mob, true), GuiProperties.regularFontSize());
		text.maxWidth(maxWidth);
		text.setX(0);
		text.setY(0);
		mainBox.add(text);

		if(mob != selector) {
			VHBox actions = CharUtils.makeActionsBlock(maxWidth, mob, selector);
			mainBox.add(actions);
		}
		mainBox.setPos(0,MARGIN);

		add(mainBox);
		setSize(mainBox.right()+MARGIN, mainBox.bottom()+MARGIN);
	}

	private static String desc(Char mob, boolean withStatus ) {
		if(withStatus) {
			return mob.getDescription() + "\n\n" + Utils.capitalize(mob.getState().status(mob)) + ".";
		} else {
			return mob.getDescription();
		}
	}

}
