package com.watabou.pixeldungeon.sprites;

import com.watabou.gltextures.TextureCache;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.utils.Utils;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

/**
 * Created by mike on 16.04.2016.
 */
public class RetroHeroSpriteDef extends HeroSpriteDef {

	private static final String HERO_EMPTY_PNG = "hero/empty.png";

	// body goes as main texture
	private static final String LAYER_ARMOR       = "armor";
	private static final String LAYER_HEAD        = "head";
	private static final String LAYER_HAIR        = "hair";
	private static final String LAYER_FACIAL_HAIR = "facial_hair";
	private static final String LAYER_HELMET      = "helmet";
	private static final String LAYER_DEATH       = "death";
	private static final String LAYER_BODY        = "body";
	private static final String LAYER_COLLAR      = "collar";
	private static final String HERO_SPRITES_DESC_HERO_JSON = "hero/spritesDesc/Hero.json";


	private static final String[] layersOrder = {
			LAYER_BODY,
			LAYER_COLLAR,
			LAYER_HEAD,
			LAYER_HAIR,
			LAYER_ARMOR,
			LAYER_FACIAL_HAIR,
			LAYER_HELMET,
			LAYER_DEATH
	};

	private Map<String,String> layersDesc = new HashMap<>();

	public RetroHeroSpriteDef(String[] lookDesc){
		super(HERO_SPRITES_DESC_HERO_JSON,0);
		applyLayersDesc(lookDesc);
	}

	public RetroHeroSpriteDef(Hero hero) {
		super(HERO_SPRITES_DESC_HERO_JSON,0);
		createLayersDesc(hero);
		applyLayersDesc(getLayersDesc());
	}


	private void createLayersDesc(Hero hero) {
		layersDesc.clear();
		boolean drawHair = true;

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

		if(hero.belongings.armor  != null && hero.belongings.armor.hasHelmet()){
			helmetDescriptor = helmetDescriptor(hero.belongings.armor, hero);
			if(hero.belongings.armor.isCoveringHair()){
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
		layersDesc.put(LAYER_DEATH, "hero/death/" +deathDescriptor+".png");
	}

	public void heroUpdated(Hero hero) {
		reset();
		createLayersDesc(hero);
		applyLayersDesc(getLayersDesc());
		avatar = null;
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
		return "hero/armor/" +armor.getClass().getSimpleName()+".png";
	}

	private String helmetDescriptor(Armor armor, Hero hero) {
		if(armor!=null) {
			if(hero.belongings.armor.hasHelmet()){
				return "hero/armor/helmet/" +armor.getClass().getSimpleName()+".png";
			}
		}
		return HERO_EMPTY_PNG;
	}

	private String collarDescriptor(Armor armor, Hero hero) {
		if(armor!=null) {
			if(hero.belongings.armor.hasCollar()){
				return "hero/armor/collar/" +armor.getClass().getSimpleName()+".png";
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

		return "hero/body/" +descriptor+".png";
	}

	@NonNull
	@Override
	public String getDeathEffect() {
		return HERO_EMPTY_PNG;
	}
}
