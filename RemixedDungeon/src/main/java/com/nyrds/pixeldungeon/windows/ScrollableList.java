package com.nyrds.pixeldungeon.windows;

import com.watabou.noosa.Gizmo;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.ui.IClickable;
import com.watabou.pixeldungeon.ui.ScrollPane;
import com.watabou.pixeldungeon.utils.GLog;

/**
 * Created by mike on 26.05.2017.
 * This file is part of Remixed Pixel Dungeon.
 */
public class ScrollableList extends ScrollPane {
	public ScrollableList(Component content) {
		super(content);
	}

	@Override
	public void onClick(float x, float y) {
		//GLog.debug("click %3.0f %3.0f", x,y);
		int size = content.getLength();
		for (int i = 0; i < size; i++) {
			Gizmo item = content.getMember(i);
			if (item instanceof IClickable) {
				if (((IClickable) item).onClick(x, y)) {
					//GLog.debug("click propagated");
					break;
				}
			}
		}
	}
}
