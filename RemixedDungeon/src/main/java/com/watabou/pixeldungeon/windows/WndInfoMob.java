
package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.windows.VHBox;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.elements.CharTitle;

import org.jetbrains.annotations.NotNull;

public class WndInfoMob extends WndTitledMessage {

	public WndInfoMob(Char mob, @NotNull Char selector ) {
		super( new CharTitle( mob ), desc( mob, true ) );

		VHBox actions = CharUtils.makeActionsBlock(this, mob, selector);

		add(actions);
		actions.setPos(GAP, height+2*GAP);

		resize( width, (int) (actions.bottom() + GAP));
	}

	public WndInfoMob( Mob mob, int knowledge) {
		super( new CharTitle( mob ), desc( mob, false) );
	}

	private static String desc(Char mob, boolean withStatus ) {
		if(withStatus) {
			return mob.getDescription() + "\n\n" + Utils.capitalize(mob.getState().status(mob)) + ".";
		} else {
			return mob.getDescription();
		}
	}

}
