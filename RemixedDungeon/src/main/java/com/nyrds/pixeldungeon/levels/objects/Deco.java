package com.nyrds.pixeldungeon.levels.objects;

import com.nyrds.Packable;
import com.nyrds.android.util.JsonHelper;
import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.android.util.Util;
import com.watabou.noosa.Animation;
import com.watabou.noosa.StringsManager;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Bundle;

import org.json.JSONArray;
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

	private String name;
	private String desc;

	private Animation.AnimationSeq basic;

	@Packable
	private String object_desc;

	public Deco(){
		super(Level.INVALID_CELL);
	}

	public Deco(int pos) {
		super(pos);
	}

	@Override
	void setupFromJson(Level level, JSONObject obj) throws JSONException {
		super.setupFromJson(level,obj);

		object_desc = obj.getString("object_desc");

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

		if(sprite.has(ANIMATIONS)) {
			JSONObject basicAnim = sprite.getJSONObject(ANIMATIONS).getJSONObject("basic");

			basic = new Animation.AnimationSeq();
			basic.fps = basicAnim.getInt("fps");
			basic.looped = basicAnim.getBoolean("looped");

			JSONArray basicFrames = basicAnim.getJSONArray("frames");

			int []frames = new int[basicFrames.length()];

			for(int i = 0; i<frames.length; ++i) {
				frames[i] = basicFrames.getInt(i);
			}
			basic.frames = frames;
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

	@Override
	public void resetVisualState() {
		if(basic!=null) {
			sprite.playAnim(basic, Util.nullCallback);
		}
	}
}
