package com.nyrds.pixeldungeon.support;

import com.watabou.noosa.Game;

import java.io.FileNotFoundException;
import java.io.OutputStream;

/**
 * Created by mike on 09.05.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class Storage {
	public OutputStream getOutputStream(String id) throws FileNotFoundException {
		return Game.instance().openFileOutput(id,Game.MODE_PRIVATE);
	}
}
