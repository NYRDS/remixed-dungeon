
package com.watabou.pixeldungeon.windows;


import static com.watabou.pixeldungeon.ui.Window.GAP;
import static com.watabou.pixeldungeon.ui.Window.MARGIN;

import com.nyrds.pixeldungeon.windows.VBox;
import com.nyrds.pixeldungeon.windows.VHBox;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.actors.Char;

import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.ui.Highlighter;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.elements.CharTitle;

import com.watabou.pixeldungeon.windows.elements.TabContent;

public class CharDescTab extends TabContent {

	public CharDescTab(Char mob) {

		VBox mainBox = new VBox();

		var charTitle = new CharTitle( mob );
		charTitle.setPos(MARGIN,MARGIN);

		mainBox.add(charTitle);

		Highlighter.addHilightedText(charTitle.left(), charTitle.bottom() +  GAP, (int) width, mainBox, desc( mob, true ));

		VHBox actions = CharUtils.makeActionsBlock(new Window(), mob, mob);

		mainBox.add(actions);

		mainBox.setPos(MARGIN,MARGIN);
		add(mainBox);
	}

	private static String desc(Char mob, boolean withStatus ) {
		if(withStatus) {
			return mob.getDescription() + "\n\n" + Utils.capitalize(mob.getState().status(mob)) + ".";
		} else {
			return mob.getDescription();
		}
	}

}
