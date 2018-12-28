package com.watabou.pixeldungeon.sprites;

import com.nyrds.android.util.ModdingMode;
import com.nyrds.pixeldungeon.items.accessories.Accessory;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.items.weapon.Weapon;
import com.watabou.utils.Callback;

import androidx.annotation.NonNull;

/**
 * Created by mike on 16.04.2016.
 */
public abstract class HeroSpriteDef extends MobSpriteDef {


	protected Image avatar;

	protected HeroSpriteDef(String defName, int kind) {
		super(defName, kind);
	}

	public static HeroSpriteDef createHeroSpriteDef(Armor armor) {
		return new ModernHeroSpriteDef(armor);
	}

	public static HeroSpriteDef createHeroSpriteDef(String[] lookDesc, String deathEffectDesc) {
		if(ModdingMode.useRetroHeroSprites) {
			return new RetroHeroSpriteDef(lookDesc);
		} else {
			return new ModernHeroSpriteDef(lookDesc, deathEffectDesc);
		}
	}
	public static HeroSpriteDef createHeroSpriteDef(Hero hero) {
		if(ModdingMode.useRetroHeroSprites) {
			return new RetroHeroSpriteDef(hero);
		} else {
			return new ModernHeroSpriteDef(hero);
		}
	}

	public static HeroSpriteDef createHeroSpriteDef(Weapon weapon) {
		return new ModernHeroSpriteDef(weapon);
	}

	public static HeroSpriteDef createHeroSpriteDef(Hero hero, Accessory accessory) {
		return new ModernHeroSpriteDef(hero, accessory);
	}

	public abstract String[] getLayersDesc();

	public abstract boolean sprint(boolean b);

	@Override
	public Image avatar() {

		if(avatar==null) {
			avatar = snapshot(idle.frames[0]);
		}

		return avatar;
	}

	@NonNull
	public abstract String getDeathEffect();

	public abstract void heroUpdated(Hero hero);

	public abstract void jump(int pos, int cell, Callback callback);
}
