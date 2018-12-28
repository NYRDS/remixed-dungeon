package com.nyrds.pixeldungeon.levels.objects;

import com.nyrds.Packable;
import com.watabou.noosa.StringsManager;
import com.watabou.pixeldungeon.levels.Level;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mike on 01.07.2016.
 */
public class Deco extends LevelObject {

	@Packable
	private String name;

	@Packable
	private String desc;


	public Deco(){
		super(Level.INVALID_CELL);
	}

	public Deco(int pos) {
		super(pos);
	}

	@Override
	void setupFromJson(Level level, JSONObject obj) throws JSONException {
		super.setupFromJson(level,obj);

		name = obj.optString("name","smth");
		desc = obj.optString("desc","smth");
	}


	@Override
	public String desc() {
		return StringsManager.maybeId(desc);
	}

	@Override
	public String name() {
		return StringsManager.maybeId(name);
	}

	@Override
	public int image() {
		return 0;
	}
}
