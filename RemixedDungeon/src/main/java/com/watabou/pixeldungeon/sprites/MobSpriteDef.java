package com.watabou.pixeldungeon.sprites;

import com.nyrds.pixeldungeon.effects.ZapEffect;
import com.nyrds.platform.util.TrackedRuntimeException;
import com.nyrds.util.JsonHelper;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Animation;
import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MobSpriteDef extends MobSprite {

	private static final String DEATH_EFFECT = "deathEffect";
	private static final String ZAP_EFFECT = "zapEffect";
	private static final String ZAP = "zap";
	private static final String ATTACK = "attack";
	private static final String LAYERS = "layers";
	private int      bloodColor;

	private int      framesInRow;
	private int      kind;
	private String   zapEffect;

	private final Set<State> initialState = new HashSet<>();

	private float visualWidth;
	private float visualHeight;

	private float visualOffsetX;
	private float visualOffsetY;


	static private final Map<String, JSONObject> defMap = new HashMap<>();

	private final String name;
	private String deathEffect;

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

			if(json.has(LAYERS)) {
				JSONArray layers = json.getJSONArray(LAYERS);

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

			charScale = (float) json.optDouble("scale",1.0);
			setScale(charScale);

			TextureFilm film = new TextureFilm(texture, width, height);

			bloodColor = 0xFFBB0000;
			Object _bloodColor = json.opt("bloodColor");
			if(_bloodColor instanceof Number) {
				bloodColor = (int) _bloodColor;
			}

			if(_bloodColor instanceof String) {
				bloodColor = Long.decode((String) _bloodColor).intValue();
			}

			Set<String> states = new HashSet<>();
			JsonHelper.readStringSet(json, "states", states);

			for(String state : states) {
				initialState.add(CharSprite.State.valueOf(state.toUpperCase(Locale.ROOT)));
			}

			framesInRow = texture.width / width;

			idle = readAnimation(json, "idle", film);
			run = readAnimation(json, "run", film);
			die = readAnimation(json, "die", film);

			if(json.has(ATTACK)) { //attack was not defined for some peaceful NPC's
				attack = readAnimation(json, ATTACK, film);
			} else {
				attack = run.clone();
			}

			if (json.has(ZAP)) {
				zap = readAnimation(json, ZAP, film);
			} else {
				zap = attack.clone();
			}

			if (json.has(ZAP_EFFECT)) {
				zapEffect = json.getString(ZAP_EFFECT);
			}

			if(json.has(DEATH_EFFECT)) {
				deathEffect = json.getString(DEATH_EFFECT);
			}

			loadAdditionalData(json,film, kind);

		} catch (Exception e) {
			throw new TrackedRuntimeException(Utils.format("Something bad happens when loading %s", name), e);
		}

		play(idle);
	}

	protected void loadAdditionalData(JSONObject json, TextureFilm film, int kind) throws JSONException {
	}

	protected Animation readAnimation(JSONObject root, String animKind, TextureFilm film) throws JSONException{
		return JsonHelper.readAnimation(root, animKind, film, kind * framesInRow);
	}

	@Override
	public void link(Char ch) {
		super.link(ch);
		for(CharSprite.State state:initialState) {
			add(state);
		}
	}

	@Override
	public void die() {
		ch.ifPresent( chr -> {
		if(deathEffect!=null) {
			ZapEffect.play(null,chr.getPos(),deathEffect);
		}});

		removeAllStates();
		super.die();
	}

	@Override
	public void zap(int cell) {
		ch.ifPresent( chr -> {
			super.zap(cell);

			ZapEffect.zap(getParent(), chr.getPos(), cell, zapEffect);
		});
	}

	@Override
	public int blood() {
		return bloodColor;
	}


	@Override
	public float visualHeight() {
		return visualHeight * charScale;
	}

	@Override
	public float visualWidth() {
		return visualWidth * charScale;
	}

	@Override
	public float visualOffsetX() {
		return visualOffsetX + super.visualOffsetX();
	}

	@Override
	public float visualOffsetY() {
		return visualOffsetY + super.visualOffsetY();
	}
}
