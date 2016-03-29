package com.watabou.pixeldungeon.sprites;

import com.nyrds.android.util.JsonHelper;
import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.watabou.noosa.Animation;
import com.watabou.noosa.Game;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.Lightning;
import com.watabou.pixeldungeon.effects.MagicMissile;
import com.watabou.utils.Callback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MobSpriteDef extends MobSprite {

	private int     bloodColor;
	private boolean levitating;
	private int     framesInRow;
	private int     kind;
	private String  zapEffect;
	Callback        zapCallback;

	static private Map<String, JSONObject> defMap = new HashMap<>();

	private String name;

	public MobSpriteDef(String defName, int kind) {
		super();

		name = defName;

		if (!defMap.containsKey(name)) {
			defMap.put(name, JsonHelper.readFile(name));
		}

		selectKind(kind);
	}

	@Override
	public void selectKind(int kind) {
		this.kind = kind;
		JSONObject json = defMap.get(name);

		try {
			texture(json.getString("texture"));

			int width = json.getInt("width");

			TextureFilm film = new TextureFilm(texture, width, json.getInt("height"));

			bloodColor = json.optInt("bloodColor", 0xFFBB0000);
			levitating = json.optBoolean("levitating", false);
			framesInRow = texture.width / width;

			idle = readAnimation(json, "idle", film);
			run = readAnimation(json, "run", film);
			attack = readAnimation(json, "attack", film);
			die = readAnimation(json, "die", film);

			if (json.has("zap")) {
				zap = readAnimation(json, "zap", film);
			} else {
				zap = attack.clone();
			}

			if (json.has("zapEffect")) {
				zapEffect = json.getString("zapEffect");

				zapCallback = new Callback() {
					@Override
					public void call() {
						ch.onZapComplete();
					}
				};
			}

		} catch (Exception e) {
			Game.toast(String.format("Something bad happens when loading %s", name), e);
			throw new TrackedRuntimeException(String.format("Something bad happens when loading %s", name), e);
		}

		play(idle);
	}

	private Animation readAnimation(JSONObject root, String animKind, TextureFilm film) throws JSONException {
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
		super.die();
		if (levitating) {
			remove(State.LEVITATING);
		}
	}

	@Override
	public void zap(int cell) {

		turnTo(ch.getPos(), cell);
		play(zap);

		if (zapEffect != null) {
			int[] points = new int[2];
			points[0] = ch.getPos();
			points[1] = cell;

			if(ItemFactory.isValidItemClass(zapEffect)) {
				((MissileSprite)getParent().recycle( MissileSprite.class )).
						reset(ch.getPos(), cell, ItemFactory.itemByName(zapEffect), null, zapCallback );
				return;
			}

			if(zapEffect.equals("Lightning")) {
				getParent().add(new Lightning(points, 2, zapCallback));
				return;
			}

			if(zapEffect.equals("Shadow")) {
				MagicMissile.shadow(getParent(), ch.getPos(), cell, zapCallback);
				Sample.INSTANCE.play(Assets.SND_ZAP);
				return;
			}

			if(zapEffect.equals("Fire")) {
				MagicMissile.fire(getParent(), ch.getPos(), cell, zapCallback);
				return;
			}
		}
	}

	@Override
	public int blood() {
		return bloodColor;
	}

}
