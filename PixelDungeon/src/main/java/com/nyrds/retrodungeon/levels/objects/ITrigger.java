package com.nyrds.retrodungeon.levels.objects;

import com.watabou.pixeldungeon.actors.Char;

/**
 * Created by mike on 27.07.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public interface ITrigger {
	void doTrigger(int cell, Char ch);
}
