package com.nyrds.retrodungeon.windows;

import com.watabou.noosa.Gizmo;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.ui.IClickable;
import com.watabou.pixeldungeon.ui.ScrollPane;

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
		int size = content.getLength();
		for (int i = 0; i < size; i++) {
			Gizmo item = content.getMember(i);
			if (item instanceof IClickable) {
				if (((IClickable) item).onClick(x, y)) {
					break;
				}
			}
		}
	}
}
