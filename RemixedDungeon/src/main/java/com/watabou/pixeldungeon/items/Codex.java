
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
