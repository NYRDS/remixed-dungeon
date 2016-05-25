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
import com.watabou.pixeldungeon.items.armor.ClassArmor;
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
	private static final String HERO_EMPTY_PNG = "hero/empty.png";
	private CompositeTextureImage avatar;

	// body goes as main texture
	private static final String LAYER_ARMOR     = "armor";
	private static final String LAYER_HEAD      = "head";
	private static final String LAYER_HAIR      = "hair";
	private static final String LAYER_HELMET      = "helmet";
	private static final String LAYER_DEATH     = "death";
	private static final String LAYER_BODY      = "body";
	private static final String LAYER_SHIELD    = "shield";
	private static final String LAYER_WEAPON    = "weapon";
	private static final String LAYER_ACCESSORY = "accessory";

	private Animation fly;

	private static final String[] layersOrder = {
		LAYER_BODY,
		LAYER_HEAD,
		LAYER_ARMOR,
		LAYER_HAIR,
		LAYER_HELMET,
		LAYER_DEATH,
		LAYER_ACCESSORY
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
		layersDesc.put(LAYER_HEAD, "hero/head/" + classDescriptor + ".png");
		layersDesc.put(LAYER_ARMOR, armorDescriptor(hero.belongings.armor));
		layersDesc.put(LAYER_HAIR, "hero/head/hair/" + classDescriptor + "_HAIR.png");
		if (hero.belongings.armor  != null && hero.belongings.armor.hasHelmet) {
			layersDesc.put(LAYER_HELMET, classHelmetDescriptor(hero.belongings.armor));
		} else {
			layersDesc.put(LAYER_HELMET, HERO_EMPTY_PNG);
		}
		String deathDescriptor = classDescriptor.equals("MAGE_WARLOCK") ? "warlock" : "common";
		layersDesc.put(LAYER_DEATH,"hero/death/"+deathDescriptor+".png");

		layersDesc.put(LAYER_ACCESSORY,"hero/accessories/shades.png");
	}

	public void heroUpdated(Hero hero) {
		createLayersDesc(hero);
		applyLayersDesc(getLayersDesc());
		avatar();
	}

	public String[] getLayersDesc() {
		String [] ret = new String[layersOrder.length];
		for(int i = 0;i<layersOrder.length;++i){
			ret[i] = layersDesc.get(layersOrder[i]);
		}

		return ret;
	}

	public void applyLayersDesc(String[] lookDesc) {
		clearLayers();
		for(int i = 0;i<layersOrder.length && i<lookDesc.length;++i){
			addLayer(layersOrder[i],TextureCache.get(lookDesc[i]));
		}
	}

	private String armorDescriptor(Armor armor) {
		if(armor==null) {
			return HERO_EMPTY_PNG;
		}
		return "hero/armor/"+armor.getClass().getSimpleName()+".png";
	}
	private String classHelmetDescriptor(Armor armor) {
		if(armor==null) {
			return HERO_EMPTY_PNG;
		}
		return "hero/armor/helmet/"+armor.getClass().getSimpleName()+".png";
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
		avatar.addLayer(getLayerTexture(LAYER_HAIR));
		avatar.addLayer(getLayerTexture(LAYER_HELMET));
		avatar.addLayer(getLayerTexture(LAYER_DEATH));
		avatar.addLayer(getLayerTexture(LAYER_ACCESSORY));

		return avatar;
	}
}
