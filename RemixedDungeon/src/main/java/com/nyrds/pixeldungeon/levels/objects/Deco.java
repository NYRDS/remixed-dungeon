package com.nyrds.pixeldungeon.levels.objects;

import com.nyrds.Packable;
import com.nyrds.android.util.JsonHelper;
import com.nyrds.android.util.ModError;
import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.android.util.Util;
import com.watabou.noosa.Animation;
import com.watabou.noosa.StringsManager;
import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Bundle;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mike on 01.07.2016.
 */
public class Deco extends LevelObject {


	public static final String ANIMATIONS = "animations";
	static private Map<String, JSONObject> defMap = new HashMap<>();

	private JSONObject animations;

	private String name;
	private String desc;

	private Animation basic;

	@Packable
	protected String object_desc;

	private int width = 16;
	private int height = 16;

	public Deco(){
		super(Level.INVALID_CELL);
	}

	public Deco(int pos) {
		super(pos);
	}

	@Override
	void setupFromJson(Level level, JSONObject obj) throws JSONException {
		super.setupFromJson(level,obj);

		object_desc = obj.optString("object_desc",object_desc);

		readObjectDesc();
	}


	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		try {
			readObjectDesc();
		} catch (JSONException e) {
			throw new TrackedRuntimeException(e);
		}
	}

	private void readObjectDesc() throws JSONException {
		if (!defMap.containsKey(object_desc)) {
			defMap.put(object_desc, JsonHelper.readJsonFromAsset("levelObjects/"+object_desc+".json"));
		}

		JSONObject objectDesc = defMap.get(object_desc);
		JSONObject appearance  = objectDesc.getJSONObject("appearance");
		JSONObject desc = appearance.getJSONObject("desc");

		this.name = desc.optString("name","smth");
		this.desc = desc.optString("desc","smth");

		JSONObject sprite = appearance.getJSONObject("sprite");

		textureFile = sprite.optString("textureFile",textureFile);
		imageIndex  = sprite.optInt("imageIndex",imageIndex);

		width = sprite.optInt("width", width);
		height = sprite.optInt("height", height);


		if(sprite.has(ANIMATIONS)) {
			animations = sprite.getJSONObject(ANIMATIONS);
		}
	}

	@Override
	public String desc() {
		return StringsManager.maybeId(desc);
	}

	@Override
	public String name() {
		return StringsManager.maybeId(name);
	}


	@Nullable
	protected Animation loadAnimation(String kind) {
		if(animations!=null) {
			try {
				return JsonHelper.readAnimation(animations,
						kind,
						new TextureFilm(textureFile, width, height),
						0);
			} catch (JSONException e) {
				throw new ModError("Deco:" + name + "|" + kind + ":", e);
			}
		}
		return null;
	}

	@Override
	public void resetVisualState() {
		if(basic==null) {
			basic = loadAnimation("basic");
			sprite.playAnim(basic, Util.nullCallback);
		}
	}

	@Override
	public int getSpriteXS() {
		return width;
	}

	@Override
	public int getSpriteYS() {
		return height;
	}
}
