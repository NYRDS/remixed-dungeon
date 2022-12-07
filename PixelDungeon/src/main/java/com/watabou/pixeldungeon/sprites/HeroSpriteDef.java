package com.watabou.pixeldungeon.sprites;

import com.nyrds.retrodungeon.items.accessories.Accessory;
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
	private static final String HERO_EMPTY_PNG = "hero/empty.png";
	private CompositeTextureImage avatar;

	// body goes as main texture
	private static final String LAYER_ARMOR       = "armor";
	private static final String LAYER_HEAD        = "head";
	private static final String LAYER_HAIR        = "hair";
	private static final String LAYER_FACIAL_HAIR = "facial_hair";
	private static final String LAYER_HELMET      = "helmet";
	private static final String LAYER_DEATH       = "death";
	private static final String LAYER_BODY        = "body";
	private static final String LAYER_COLLAR      = "collar";
	private static final String LAYER_SHIELD      = "shield";
	private static final String LAYER_WEAPON      = "weapon";
	private static final String LAYER_ACCESSORY   = "accessory";

	private Animation fly;

	private static final String[] layersOrder = {
		LAYER_BODY,
		LAYER_COLLAR,
		LAYER_HEAD,
		LAYER_HAIR,
		LAYER_ARMOR,
		LAYER_FACIAL_HAIR,
		LAYER_HELMET,
		LAYER_DEATH,
		LAYER_ACCESSORY
	};

	private Map<String,String> layersDesc = new HashMap<>();

	private Tweener  jumpTweener;
	private Callback jumpCallback;

	public HeroSpriteDef(String[] lookDesc){
		super("spritesDesc/Hero.json",0);
		applyLayersDesc(lookDesc);
	}

	public HeroSpriteDef(Armor armor){
		super("spritesDesc/ArmoredStatue.json",0);
		createStatueSprite(armor);
		applyLayersDesc(getLayersDesc());
	}

	public HeroSpriteDef(Hero hero) {
		super("spritesDesc/Hero.json",0);
		createLayersDesc(hero);
		applyLayersDesc(getLayersDesc());
	}

	public HeroSpriteDef(Hero hero, Accessory accessory) {
		super("spritesDesc/Hero.json",0);
		createLayersDesc(hero, accessory);
		applyLayersDesc(getLayersDesc());
	}

	private void createLayersDesc(Hero hero) {
		Accessory accessory = Accessory.equipped();
		createLayersDesc(hero, accessory);
	}

	private void createLayersDesc(Hero hero, Accessory accessory) {
		layersDesc.clear();
		boolean drawHair = true;

		String accessoryDescriptor = HERO_EMPTY_PNG;
		String classDescriptor = hero.heroClass.toString()+"_"+hero.subClass.toString();
		String deathDescriptor = classDescriptor.equals("MAGE_WARLOCK") ? "warlock" : "common";
		String facialHairDescriptor = HERO_EMPTY_PNG;
		String hairDescriptor = HERO_EMPTY_PNG;
		String helmetDescriptor = HERO_EMPTY_PNG;


		if(classDescriptor.equals("MAGE_WARLOCK")
				|| classDescriptor.equals("MAGE_BATTLEMAGE")
				|| classDescriptor.equals("WARRIOR_BERSERKER")
				|| classDescriptor.equals("NECROMANCER_NONE")){
			facialHairDescriptor = "hero/head/facial_hair/" + classDescriptor + "_FACIAL_HAIR.png";
		}

		if (accessory  == null){
			if(hero.belongings.armor  != null && hero.belongings.armor.isHasHelmet()){
				helmetDescriptor = helmetDescriptor(hero.belongings.armor, hero);
				if(hero.belongings.armor.isCoveringHair()){
					drawHair = false;
				}
			}
		}
		else{
			accessoryDescriptor = accessory.getLayerFile();
			if(accessory.isCoveringHair()){
				drawHair = false;
			}
		}

		if (drawHair){ hairDescriptor = "hero/head/hair/" + classDescriptor + "_HAIR.png"; }

		layersDesc.put(LAYER_BODY,bodyDescriptor(hero));
		layersDesc.put(LAYER_COLLAR, collarDescriptor(hero.belongings.armor, hero));
		layersDesc.put(LAYER_HEAD, "hero/head/" + classDescriptor + ".png");
		layersDesc.put(LAYER_HAIR, hairDescriptor);
		layersDesc.put(LAYER_ARMOR, armorDescriptor(hero.belongings.armor));
		layersDesc.put(LAYER_FACIAL_HAIR, facialHairDescriptor);
		layersDesc.put(LAYER_HELMET, helmetDescriptor);
		layersDesc.put(LAYER_DEATH,"hero/death/"+deathDescriptor+".png");
		layersDesc.put(LAYER_ACCESSORY, accessoryDescriptor);
	}

	private void createStatueSprite(Armor armor) {
		layersDesc.put(LAYER_BODY,        "hero/body/statue.png");
		layersDesc.put(LAYER_COLLAR,      HERO_EMPTY_PNG);
		layersDesc.put(LAYER_HEAD,        "hero/head/statue.png");
		layersDesc.put(LAYER_HAIR,        HERO_EMPTY_PNG);
		layersDesc.put(LAYER_ARMOR,       armorDescriptor(armor));
		layersDesc.put(LAYER_FACIAL_HAIR, HERO_EMPTY_PNG);
		layersDesc.put(LAYER_HELMET,      HERO_EMPTY_PNG);
		layersDesc.put(LAYER_DEATH,       "hero/death/statue.png");
		layersDesc.put(LAYER_ACCESSORY,   HERO_EMPTY_PNG);
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

	private void applyLayersDesc(String[] lookDesc) {
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

	private String helmetDescriptor(Armor armor, Hero hero) {
		if(armor!=null) {
			if(hero.belongings.armor.isHasHelmet()){
				return "hero/armor/helmet/"+armor.getClass().getSimpleName()+".png";
			}
		}
			return HERO_EMPTY_PNG;
	}

	private String collarDescriptor(Armor armor, Hero hero) {
		if(armor!=null) {
			if(hero.belongings.armor.isHasCollar()){
				return "hero/armor/collar/"+armor.getClass().getSimpleName()+".png";
			}
		}
		return HERO_EMPTY_PNG;
	}

	private String bodyDescriptor(Hero hero) {
		String descriptor = "man";

		if(hero.getGender()== Utils.FEMININE) {
			descriptor = "woman";
		}

		if(hero.subClass.equals(HeroSubClass.WARLOCK)) {
			descriptor = "warlock";
		}

		if(hero.subClass.equals(HeroSubClass.LICH)) {
			descriptor = "lich";
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
		if (ch.isFlying()) {
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

			if (getVisible() && Dungeon.level.water[ch.getPos()] && !ch.isFlying()) {
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

	@Override
	public CompositeTextureImage avatar() {
		if(avatar==null) {
			avatar = new CompositeTextureImage(texture);
			avatar.frame(idle.frames[0]);
		}

		avatar.clearLayers();


		avatar.addLayer(getLayerTexture(LAYER_BODY));
		avatar.addLayer(getLayerTexture(LAYER_HEAD));
		avatar.addLayer(getLayerTexture(LAYER_HAIR));
		avatar.addLayer(getLayerTexture(LAYER_ARMOR));
		avatar.addLayer(getLayerTexture(LAYER_FACIAL_HAIR));
		avatar.addLayer(getLayerTexture(LAYER_HELMET));
		avatar.addLayer(getLayerTexture(LAYER_DEATH));
		avatar.addLayer(getLayerTexture(LAYER_ACCESSORY));

		return avatar;
	}
}
