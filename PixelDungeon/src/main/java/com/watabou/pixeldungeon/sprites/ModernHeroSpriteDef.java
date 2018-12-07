package com.watabou.pixeldungeon.sprites;

import com.nyrds.pixeldungeon.effects.CustomClipEffect;
import com.nyrds.pixeldungeon.items.accessories.Accessory;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Animation;
import com.watabou.noosa.Camera;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.tweeners.Tweener;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.items.KindOfWeapon;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.items.weapon.Weapon;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Callback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import androidx.annotation.NonNull;

/**
 * Created by mike on 16.04.2016.
 */
public class ModernHeroSpriteDef extends HeroSpriteDef {

	private static final int RUN_FRAMERATE = 20;
	private static final String HERO_EMPTY_PNG = "hero_modern/empty.png";
	private static final String WEAPON_ANIM = "weapon_anim";

	// body goes as main texture
	private static final String LAYER_ARMOR       = "armor";
	private static final String LAYER_HEAD        = "head";
	private static final String LAYER_HAIR        = "hair";
	private static final String LAYER_FACIAL_HAIR = "facial_hair";
	private static final String LAYER_HELMET      = "helmet";
	private static final String LAYER_BODY        = "body";
	private static final String LAYER_COLLAR      = "collar";
	private static final String LAYER_ACCESSORY   = "accessory";

	private static final String LAYER_LEFT_HAND   = "left_hand";
	private static final String LAYER_RIGHT_HAND  = "right_hand";

	private static final String LAYER_LEFT_ARMOR  = "left_hand_armor";
	private static final String LAYER_RIGHT_ARMOR = "right_hand_armor";

	//private static final String LAYER_LEFT_ITEM   = "left_hand_item";
	private static final String LAYER_RIGHT_ITEM  = "right_hand_item";
	private static final String HERO_MODERN_SPRITES_DESC_HERO_JSON = "hero_modern/spritesDesc/Hero.json";
	private static final String HERO_MODERN_SPRITES_DESC_STATUE_JSON = "hero_modern/spritesDesc/Statue.json";

	private CustomClipEffect deathEffect;

	private Animation fly;
	private Map<String, Animation> weapon_anims;

	private static final String[] layersOrder = {
		LAYER_BODY,
		LAYER_COLLAR,
		LAYER_HEAD,
		LAYER_HAIR,
		LAYER_ARMOR,
		LAYER_FACIAL_HAIR,
		LAYER_HELMET,
		LAYER_LEFT_ARMOR,
		LAYER_RIGHT_ARMOR,
		LAYER_LEFT_HAND,
		LAYER_RIGHT_HAND,
		LAYER_ACCESSORY,
	//	LAYER_LEFT_ITEM,
		LAYER_RIGHT_ITEM,
	};

	private Map<String,String> layersDesc = new HashMap<>();

	private Tweener  jumpTweener;
	private Callback jumpCallback;
	private String deathEffectDesc;

	public ModernHeroSpriteDef(String[] lookDesc, String deathEffectDesc){
		super(HERO_MODERN_SPRITES_DESC_HERO_JSON,0);
		this.deathEffectDesc = deathEffectDesc;
		applyLayersDesc(lookDesc);
	}

	public ModernHeroSpriteDef(Armor armor){
		super(HERO_MODERN_SPRITES_DESC_STATUE_JSON,0);
		createStatueSprite(armor);
		applyLayersDesc(getLayersDesc());
	}

	public ModernHeroSpriteDef(Hero hero) {
		super(HERO_MODERN_SPRITES_DESC_HERO_JSON,0);
		createLayersDesc(hero);
		applyLayersDesc(getLayersDesc());
	}

	public ModernHeroSpriteDef(Hero hero, Accessory accessory) {
		super(HERO_MODERN_SPRITES_DESC_HERO_JSON,0);
		createLayersDesc(hero, accessory);
		applyLayersDesc(getLayersDesc());
	}

	public ModernHeroSpriteDef(Weapon weapon) {
		super(HERO_MODERN_SPRITES_DESC_STATUE_JSON,0);
		createStatueSprite(weapon);
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
			facialHairDescriptor = "hero_modern/head/facial_hair/" + classDescriptor + "_FACIAL_HAIR.png";
		}

		if (accessory  == null){
			if(hero.belongings.armor  != null && hero.belongings.armor.hasHelmet()){
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

		if (drawHair){ hairDescriptor = "hero_modern/head/hair/" + classDescriptor + "_HAIR.png"; }

		String bodyType = bodyDescriptor(hero);

		layersDesc.put(LAYER_BODY, "hero_modern/body/" +bodyType+".png" );
		layersDesc.put(LAYER_COLLAR, collarDescriptor(hero.belongings.armor, hero));
		layersDesc.put(LAYER_HEAD, "hero_modern/head/" + classDescriptor + ".png");
		layersDesc.put(LAYER_HAIR, hairDescriptor);
		layersDesc.put(LAYER_ARMOR, armorDescriptor(hero.belongings.armor));
		layersDesc.put(LAYER_FACIAL_HAIR, facialHairDescriptor);
		layersDesc.put(LAYER_HELMET, helmetDescriptor);


		String weaponAnimationClassLeft  = KindOfWeapon.BASIC_ATTACK;
		String weaponAnimationClassRight = KindOfWeapon.BASIC_ATTACK;

		if(hero.belongings.weapon!=null) {
			weaponAnimationClassLeft = hero.belongings.weapon.getAnimationClass();
			weaponAnimationClassRight = hero.belongings.weapon.getAnimationClass();
		}

		layersDesc.put(LAYER_LEFT_HAND, "hero_modern/body/hands/" + bodyType + "_" + weaponAnimationClassLeft + "_left.png");
		layersDesc.put(LAYER_RIGHT_HAND, "hero_modern/body/hands/" + bodyType + "_" + weaponAnimationClassRight + "_right.png");

		layersDesc.put(LAYER_ACCESSORY, accessoryDescriptor);

		if(accessory==null || !accessory.isCoveringItems()) {
		//	layersDesc.put(LAYER_LEFT_ITEM, itemHandDescriptor(hero.belongings.weapon, "left"));
			layersDesc.put(LAYER_RIGHT_ITEM, itemHandDescriptor(hero.belongings.weapon, "right"));
		}

		deathEffectDesc = "hero_modern/death/" +deathDescriptor+".png";
	}

	private void createStatueSprite(Weapon weapon) {
		layersDesc.put(LAYER_BODY, "hero_modern/body/statue.png");
		layersDesc.put(LAYER_HEAD, "hero_modern/head/statue.png");


		String weaponAnimationClassLeft  = KindOfWeapon.BASIC_ATTACK;
		String weaponAnimationClassRight = KindOfWeapon.BASIC_ATTACK;

		if(weapon!=null) {
			weaponAnimationClassLeft = weapon.getAnimationClass();
			weaponAnimationClassRight = weapon.getAnimationClass();
		}

		layersDesc.put(LAYER_LEFT_HAND,  "hero_modern/body/hands/statue_" +weaponAnimationClassLeft+"_left.png");
		layersDesc.put(LAYER_RIGHT_HAND, "hero_modern/body/hands/statue_" +weaponAnimationClassRight+"_right.png");

		//layersDesc.put(LAYER_LEFT_ITEM, itemHandDescriptor(weapon, "left"));
		layersDesc.put(LAYER_RIGHT_ITEM, itemHandDescriptor(weapon, "right"));


		deathEffectDesc = "hero_modern/death/statue.png";
	}

	private void createStatueSprite(Armor armor) {
		layersDesc.put(LAYER_BODY, "hero_modern/body/statue.png");
		layersDesc.put(LAYER_HEAD, "hero_modern/head/statue.png");
		layersDesc.put(LAYER_ARMOR,       armorDescriptor(armor));


		layersDesc.put(LAYER_LEFT_HAND, "hero_modern/body/hands/statue_none_left.png");
		layersDesc.put(LAYER_RIGHT_HAND, "hero_modern/body/hands/statue_none_right.png");

		deathEffectDesc = "hero_modern/death/statue.png";
	}

	public void heroUpdated(Hero hero) {
		reset();
		createLayersDesc(hero);
		applyLayersDesc(getLayersDesc());

		if(weapon_anims!=null) { //old mods compatibility
			KindOfWeapon weapon = hero.belongings.weapon;

			if (weapon != null) {
				attack = weapon_anims.get(weapon.getAnimationClass());

				String zapAnim = weapon.getAnimationClass()+"_zap";

				if(weapon_anims.containsKey(zapAnim)) {
                    zap = weapon_anims.get(zapAnim);
                }

			} else {
				attack = weapon_anims.get(KindOfWeapon.BASIC_ATTACK);
				zap    = weapon_anims.get(KindOfWeapon.BASIC_ATTACK);
			}
		}

		Accessory accessory = Accessory.equipped();

		if(accessory != null && accessory.isCoveringItems()) { // no fancy attacks in costumes
			attack = weapon_anims.get(KindOfWeapon.BASIC_ATTACK);
			zap    = weapon_anims.get(KindOfWeapon.BASIC_ATTACK);
		}

		avatar = null;
	}

	public String[] getLayersDesc() {
		ArrayList<String> ret= new ArrayList<>();
		for (String aLayersOrder : layersOrder) {
			if (layersDesc.containsKey(aLayersOrder)) {
				ret.add(layersDesc.get(aLayersOrder));
			}
		}

		return ret.toArray(new String[ret.size()]);
	}

	private void applyLayersDesc(String[] lookDesc) {
		clearLayers();
		for(int i = 0;i<layersOrder.length && i<lookDesc.length;++i){
			addLayer(layersOrder[i],TextureCache.get(lookDesc[i]));
		}
		deathEffect = new CustomClipEffect(deathEffectDesc, (int)width, (int)height);
	}

	private String armorDescriptor(Armor armor) {
		if(armor==null) {
			return HERO_EMPTY_PNG;
		}
		return "hero_modern/armor/" +armor.getVisualName()+".png";
	}

	private String itemHandDescriptor(KindOfWeapon item, String hand) {
		if(item==null) {
			return HERO_EMPTY_PNG;
		}
		return "hero_modern/items/" +item.getVisualName()+"_"+hand+".png";
	}

	private String helmetDescriptor(Armor armor, Hero hero) {
		if(armor!=null) {
			if(hero.belongings.armor.hasHelmet()){
				return "hero_modern/armor/helmet/" +armor.getClass().getSimpleName()+".png";
			}
		}
			return HERO_EMPTY_PNG;
	}

	private String collarDescriptor(Armor armor, Hero hero) {
		if(armor!=null) {
			if(hero.belongings.armor.hasCollar()){
				return "hero_modern/armor/collar/" +armor.getClass().getSimpleName()+".png";
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

		if(hero.heroClass == HeroClass.GNOLL) {
			descriptor = "gnoll";
		}

		return descriptor;
	}

	@Override
	protected void loadAdditionalData(JSONObject json, TextureFilm film, int kind) throws JSONException {
		fly     = readAnimation(json, "fly", film);
		operate = readAnimation(json, "operate", film);

		if(json.has(WEAPON_ANIM)){
			weapon_anims = new HashMap<>();

			JSONObject anims = json.getJSONObject(WEAPON_ANIM);
			Iterator<String> keys = anims.keys();

			while( keys.hasNext() ) {
				String key = keys.next();
				weapon_anims.put(key,readAnimation(anims,key,film));
			}
		}
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
	public void die() {
		deathEffect.place(ch.getPos());
		getParent().add(deathEffect);
		deathEffect.setVisible(true);
		deathEffect.playAnim(die, new Callback() {
			@Override
			public void call() {
				deathEffect.killAndErase();
			}
		});
		killAndErase();
	}

	@NonNull
	public String getDeathEffect() {
		return deathEffectDesc;
	}
}
