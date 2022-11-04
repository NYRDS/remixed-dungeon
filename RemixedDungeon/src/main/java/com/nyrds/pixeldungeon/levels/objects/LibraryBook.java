package com.nyrds.pixeldungeon.levels.objects;

import androidx.annotation.Keep;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.windows.WndLibrary;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.scenes.GameScene;

import org.json.JSONObject;

/**
 * Created by mike on 01.07.2016.
 */
public class LibraryBook extends LevelObject {

	@Keep
	public LibraryBook(){
		super(-1);
	}

	@Keep
	public LibraryBook(int pos) {
		super(pos);
	}

	@Override
	void setupFromJson(Level level, JSONObject obj) {
	}

	@Override
	public boolean interact(Char hero) {
		GameScene.show(new WndLibrary());
		return false;
	}

	@Override
	public boolean interactive() {
		return true;
	}


	@Override
	public void burn() {
	}

	@Override
	public boolean stepOn(Char hero) {
		return false;
	}

	@Override
	public String desc() {
        return StringsManager.getVar(R.string.LibraryBook_Description);
    }

	@Override
	public String name() {
        return StringsManager.getVar(R.string.LibraryBook_Name);
    }

	@Override
	public int image() {
		return 1;
	}
}
