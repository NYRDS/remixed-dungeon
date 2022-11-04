/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon.items;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.items.books.Book;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.windows.WndStory;
import com.watabou.utils.Random;

import org.json.JSONException;
import org.json.JSONObject;

public class Codex extends Book {

	@Packable(defaultValue = "-1")
	private int codexId=-1;

	@Packable
	private String text;

	public Codex(){
		stackable = false;
		image     = 4;
	}

	@Override
	protected void doRead(Char hero) {
		if(text != null && !text.isEmpty() && !text.equals("Unknown")) {
			WndStory.showCustomStory(text);
		} else {
            WndStory.showCustomStory(StringsManager.getVars(R.array.Codex_Story)[getCodexId()]);
		}
	}

	@Override
	public boolean isIdentified() {
		return true;
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}

	public void fromJson(JSONObject itemDesc) throws JSONException {
		super.fromJson(itemDesc);

		text = StringsManager.maybeId(itemDesc.optString("text"));
	}

	@Override
	public int price(){
		return 5 * quantity();
	}

	private int getCodexId()
	{
        int maxId = StringsManager.getVars(R.array.Codex_Story).length;
		if(codexId < 0) {
			codexId = Random.Int(maxId);
		}

		if(codexId > maxId-1) {
			codexId = 0;
		}

		return codexId;
	}
}
