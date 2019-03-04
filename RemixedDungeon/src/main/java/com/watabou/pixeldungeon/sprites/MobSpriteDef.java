package com.watabou.pixeldungeon.sprites;

import com.nyrds.android.util.JsonHelper;
import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.effects.ZapEffect;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Animation;
import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MobSpriteDef extends MobSprite {

	private int      bloodColor;
	private boolean  levitating;
	private int      framesInRow;
	private int      kind;
	private String   zapEffect;

	private float visualWidth;
	private float visualHeight;

	private float visualOffsetX;
	private float visualOffsetY;


	static private Map<String, JSONObject> defMap = new HashMap<>();

	private String name;

	public MobSpriteDef(String defName, int kind) {
		super();

		name = defName;

		if (!defMap.containsKey(name)) {
			defMap.put(name, JsonHelper.readJsonFromAsset(name));
		}

		selectKind(kind);
	}

	@Override
	public void selectKind(int kind) {

		this.kind = kind;
		JSONObject json = defMap.get(name);

		try {
			texture(json.getString("texture"));

			if(json.has("layers")) {
				JSONArray layers = json.getJSONArray("layers");

				for(int i=0;i<layers.length();++i) {
					JSONObject layer = layers.getJSONObject(i);
					addLayer(layer.getString("id"),TextureCache.get(layer.get("texture")));
				}
			}

			int width = json.getInt("width");
			visualWidth = (float) json.optDouble("visualWidth",width);

			int height = json.getInt("height");
			visualHeight = (float) json.optDouble("visualHeight",height);

			visualOffsetX = (float) json.optDouble("visualOffsetX",0);
			visualOffsetY = (float) json.optDouble("visualOffsetY",0);

			TextureFilm film = new TextureFilm(texture, width, height);

			bloodColor = 0xFFBB0000;
			Object _bloodColor = json.opt("bloodColor");
			if(_bloodColor instanceof Number) {
				bloodColor = (int) _bloodColor;
			}

			if(_bloodColor instanceof String) {
				bloodColor = Long.decode((String) _bloodColor).intValue();
			}

			levitating = json.optBoolean("levitating", false);
			framesInRow = texture.width / width;

			idle = readAnimation(json, "idle", film);
			run = readAnimation(json, "run", film);
			die = readAnimation(json, "die", film);

			if(json.has("attack")) { //attack was not defined for some peaceful NPC's
				attack = readAnimation(json, "attack", film);
			} else {
				attack = run.clone();
			}

			if (json.has("zap")) {
				zap = readAnimation(json, "zap", film);
			} else {
				zap = attack.clone();
			}

			if (json.has("zapEffect")) {
				zapEffect = json.getString("zapEffect");
			}

			loadAdditionalData(json,film, kind);

		} catch (Exception e) {
			throw new TrackedRuntimeException(Utils.format("Something bad happens when loading %s", name), e);
		}

		play(idle);
	}

	protected void loadAdditionalData(JSONObject json, TextureFilm film, int kind) throws JSONException {
	}

	protected Animation readAnimation(JSONObject root, String animKind, TextureFilm film) throws JSONException {
		JSONObject jsonAnim = root.getJSONObject(animKind);

		Animation anim = new Animation(jsonAnim.getInt("fps"), jsonAnim.getBoolean("looped"));

		List<Integer> framesSeq = new ArrayList<>(16);

		JSONArray jsonFrames = jsonAnim.getJSONArray("frames");

		int nextFrame;

		for (int i = 0; (nextFrame = jsonFrames.optInt(i, -1)) != -1; ++i) {
			framesSeq.add(nextFrame);
		}

		anim.frames(film, framesSeq, kind * framesInRow);

		return anim;
	}

	@Override
	public void link(Char ch) {
		super.link(ch);
		if (levitating) {
			add(State.LEVITATING);
		}
	}

	@Override
	public void die() {
		removeAllStates();
		super.die();
	}

	@Override
	public void zap(int cell) {
		super.zap(cell);

		ZapEffect.zap(getParent(),ch.getPos(),cell, zapEffect);
	}

	@Override
	public int blood() {
		return bloodColor;
	}


	@Override
	public float visualHeight() {
		return visualHeight;
	}

	@Override
	public float visualWidth() {
		return visualWidth;
	}

	@Override
	public float visualOffsetX() {
		return visualOffsetX;
	}

	@Override
	public float visualOffsetY() {
		return visualOffsetY;
	}
}
