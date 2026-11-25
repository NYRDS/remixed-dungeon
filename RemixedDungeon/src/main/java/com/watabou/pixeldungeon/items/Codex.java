
package com.watabou.pixeldungeon.items;

import java.util.ArrayList;
import com.watabou.pixeldungeon.utils.GLog;
import com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory;
import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.Packable;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.windows.WndStory;
import com.watabou.utils.Random;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

public class Codex extends Item {

	// Book base inlined (the book family shrank to Codex alone)
	public static final float TIME_TO_READ = 2f;

	{
		imageFile = "items/books.png";
	}

	@Override
	public ArrayList<String> actions(Char hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( CommonActions.AC_READ );
		return actions;
	}

	@Override
	public void _execute(@NotNull Char chr, @NotNull String action ) {
		if (action.equals( CommonActions.AC_READ )) {
			if (chr.hasBuff( BuffFactory.BLINDNESS )) {
				GLog.w(StringsManager.getVar(R.string.Codex_Blinded));
			} else {
				doRead(chr);
			}
		} else {
			super._execute(chr, action );
		}
	}



	@Packable(defaultValue = "-1")
	public int codexId=-1;

	@Packable
	public String text;

	public Codex(){
		stackable = false;
		image     = 4;
	}

	protected void doRead(Char hero) {
		if(text != null && !text.isEmpty() && !text.equals("Unknown")) {
			WndStory.showCustomStory(text);
		} else {
            WndStory.showCustomStory(StringsManager.getVars(R.array.Codex_Story)[getCodexId()]);
		}
	}

	@Override
	public boolean isIdentified() {
        return super.isIdentified();
    }
	
	@Override
	public boolean isUpgradable() {
        return super.isUpgradable();
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
