package com.nyrds.pixeldungeon.levels.objects;

import com.nyrds.LuaInterface;
import com.nyrds.Packable;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.JsonHelper;
import com.nyrds.util.ModError;
import com.nyrds.util.Util;
import com.watabou.noosa.Animation;
import com.watabou.noosa.Group;
import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.SneakyThrows;

/**
 * Created by mike on 01.07.2016.
 */
public class Deco extends LevelObject {

	private static final String ANIMATIONS = "animations";
	static protected final Map<String, JSONObject> defMap = new HashMap<>();

	private JSONObject animations;

	private String name;
	private String desc;

	private Animation basic;

	private Group effect = new Group();

	@Getter
	@LuaInterface
	@Packable
	protected String objectDesc;

	private int width = 16;
	private int height = 16;

	private boolean canStepOn   = false;
	private boolean nonPassable = true;

	private String effectName = Utils.EMPTY_STRING;

	public Deco(){
		super(Level.INVALID_CELL);
	}

	public Deco(int pos) {
		super(pos);
	}

	public boolean stepOn(Char hero) {
		return canStepOn;
	}

	@Override
	public boolean nonPassable(Char ch) {
		return nonPassable;
	}

	@Override
	void setupFromJson(Level level, JSONObject obj) throws JSONException {
		super.setupFromJson(level,obj);
		objectDesc = obj.optString("object_desc", objectDesc);
		readObjectDesc();
	}

	@SneakyThrows
	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		readObjectDesc();
	}

	@Override
	public String getEntityKind() {
		return objectDesc;
	}

	protected void readObjectDesc() throws JSONException {
		if (!defMap.containsKey(objectDesc)) {
			defMap.put(objectDesc, JsonHelper.readJsonFromAsset("levelObjects/"+ objectDesc +".json"));
		}

		JSONObject objectDesc = defMap.get(this.objectDesc);

		layer = objectDesc.optInt("layer",3);

		JSONObject appearance  = objectDesc.getJSONObject("appearance");

		effectName = appearance.optString("particles", effectName);

		JSONObject desc = appearance.getJSONObject("desc");

		this.name = desc.optString("name","smth");
		this.desc = desc.optString("desc","smth");

		canStepOn = desc.optBoolean("canStepOn",canStepOn);
		nonPassable = desc.optBoolean("nonPassable",nonPassable);

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

	private void updateEffect() {
		effect.killAndErase();
		effect = GameScene.particleEffect(effectName,pos);
	}

	@Override
	public void setPos(int pos) {
		super.setPos(pos);
		updateEffect();
	}

	@Override
	public void resetVisualState() {
		if(basic==null) {
			basic = loadAnimation("basic");
		}
		lo_sprite.ifPresent(
				sprite -> sprite.playAnim(basic, Util.nullCallback));
		updateEffect();
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
