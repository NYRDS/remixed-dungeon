package com.watabou.pixeldungeon.sprites;

import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Animation;
import com.watabou.noosa.Camera;
import com.watabou.noosa.CompositeTextureImage;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.tweeners.Tweener;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Callback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mike on 16.04.2016.
 */
public class HeroSpriteDef extends MobSpriteDef {

	private static final int RUN_FRAMERATE = 20;
	private CompositeTextureImage avatar;

	// body goes as main texture
	private static final String LAYER_ARMOR  = "armor";
	private static final String LAYER_HEAD   = "head";
	private static final String LAYER_DEATH  = "death";
	private static final String LAYER_BODY   = "body";
	private static final String LAYER_SHIELD = "shield";
	private static final String LAYER_WEAPON = "weapon";

	private Animation fly;

	private static final String[] layersOrder = {
		LAYER_BODY,
		LAYER_HEAD,
		LAYER_ARMOR,
		LAYER_DEATH
	};

	Map<String,String> layersDesc = new HashMap<>();

	private Tweener  jumpTweener;
	private Callback jumpCallback;

	public HeroSpriteDef(String[] lookDesc){
		super("spritesDesc/Hero.json",0);
		applyLayersDesc(lookDesc);
	}

	public HeroSpriteDef(Hero hero, boolean link) {
		super("spritesDesc/Hero.json",0);

		createLayersDesc(hero);
		applyLayersDesc(getLayersDesc());
		if(link) {
			link(hero);
		}
	}

	public void createLayersDesc(Hero hero) {
		layersDesc.clear();
		layersDesc.put(LAYER_BODY,bodyDescriptor(hero));

		String classDescriptor = hero.heroClass.toString()+"_"+hero.subClass.toString();
		layersDesc.put(LAYER_HEAD,"hero/head/"+classDescriptor+".png");
		layersDesc.put(LAYER_ARMOR,armorDescriptor(hero.belongings.armor));
		String deathDescriptor = classDescriptor.equals("MAGE_WARLOCK") ? "warlock" : "common";
		layersDesc.put(LAYER_DEATH,"hero/death/"+deathDescriptor+".png");
	}

	public String[] getLayersDesc() {
		java.util.Collection<String> var = layersDesc.values();
		return var.toArray(new String[var.size()]);
	}

	public void applyLayersDesc(String[] lookDesc) {
		for(int i = 0;i<layersOrder.length && i<lookDesc.length;++i){
			addLayer(layersOrder[i],TextureCache.get(lookDesc[i]));
		}
	}

	private String armorDescriptor(Armor armor) {
		String descriptor = armor == null ? "no_armor" :  armor.getClass().getSimpleName();
		return "hero/armor/"+descriptor+".png";
	}

	private String bodyDescriptor(Hero hero) {
		String descriptor = "man";

		if(hero.getGender()== Utils.FEMININE) {
			descriptor = "woman";
		}

		if(hero.subClass.equals(HeroSubClass.WARLOCK)) {
			descriptor = "warlock";
		}

		return "hero/body/"+descriptor+".png";
	}

	@Override
	protected void loadAdditionalData(JSONObject json, TextureFilm film, int kind) throws JSONException {
		fly     = readAnimation(json, "fly", film);
		operate = readAnimation(json, "operate", film);
	}

	@Override
	public void place(int p) {
		super.place(p);
		if(ch instanceof Hero) {
			Camera.main.target = this;
		}
	}

	@Override
	public void move(int from, int to) {
		super.move(from, to);
		if (ch.flying) {
			play(fly);
		}
		if(ch instanceof Hero) {
			Camera.main.target = this;
		}
	}

	public void updateArmor(Armor armor) {
		layersDesc.put(LAYER_ARMOR,armorDescriptor(armor));
		applyLayersDesc(getLayersDesc());
		avatar();
	}

	public void jump(int from, int to, Callback callback) {
		jumpCallback = callback;

		int distance = Dungeon.level.distance(from, to);
		jumpTweener = new JumpTweener(this, worldToCamera(to), distance * 4,
				distance * 0.1f);
		jumpTweener.listener = this;
		getParent().add(jumpTweener);

		turnTo(from, to);
		play(fly);
	}

	@Override
	public void onComplete(Tweener tweener) {
		if (tweener == jumpTweener) {

			if (getVisible() && Dungeon.level.water[ch.getPos()] && !ch.flying) {
				GameScene.ripple(ch.getPos());
			}
			if (jumpCallback != null) {
				jumpCallback.call();
			}
		} else {
			super.onComplete(tweener);
		}
	}

	public boolean sprint(boolean on) {
		run.delay = on ? 0.625f / RUN_FRAMERATE : 1f / RUN_FRAMERATE;
		return on;
	}

	public CompositeTextureImage avatar() {
		if(avatar==null) {
			avatar = new CompositeTextureImage(texture);
			avatar.frame(idle.frames[0]);
		}

		avatar.clearLayers();

		avatar.addLayer(getLayerTexture(LAYER_BODY));
		avatar.addLayer(getLayerTexture(LAYER_HEAD));
		avatar.addLayer(getLayerTexture(LAYER_ARMOR));
		avatar.addLayer(getLayerTexture(LAYER_DEATH));

		return avatar;
	}
}
