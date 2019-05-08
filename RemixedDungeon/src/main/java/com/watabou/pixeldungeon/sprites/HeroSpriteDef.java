package com.watabou.pixeldungeon.sprites;

import com.nyrds.LuaInterface;
import com.nyrds.android.util.ModdingMode;
import com.nyrds.pixeldungeon.items.accessories.Accessory;
import com.watabou.noosa.Animation;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.tweeners.JumpTweener;
import com.watabou.noosa.tweeners.Tweener;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.items.weapon.Weapon;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Callback;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mike on 16.04.2016.
 */
public abstract class HeroSpriteDef extends MobSpriteDef {


    private static final int RUN_FRAMERATE = 20;
    protected Image avatar;
	protected Animation fly;
    private Tweener  jumpTweener;
    private Callback jumpCallback;


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
	public Image avatar() {

		if(avatar==null) {
			avatar = snapshot(idle.frames[0]);
		}

		return avatar;
	}

	@Override
	public void idle() {
		if(ch!=null && ch.isFlying()) { //ch can be null when used in indicators
			play(fly);
		} else {
			super.idle();
		}
	}

	@Override
	public void move(int from, int to, boolean playRunAnimation) {
		super.move(from, to, !ch.isFlying());
		if (ch.isFlying()) {
			play(fly);
		}
		if(ch instanceof Hero) {
			Camera.main.target = this;
		}
	}

	@NotNull
	public abstract String getDeathEffect();

	public abstract void heroUpdated(Hero hero);

    @Override
    public void place(int p) {
        super.place(p);
        if(ch instanceof Hero) {
            Camera.main.target = this;
        }
    }

    @Override
    protected void loadAdditionalData(JSONObject json, TextureFilm film, int kind) throws JSONException {
        super.loadAdditionalData(json, film, kind);
        fly     = readAnimation(json, "fly", film);
        operate = readAnimation(json, "operate", film);
    }

    @LuaInterface
	public void dash(int from, int to) {
		jumpCallback = null;

		int distance = Dungeon.level.distance(from, to);
		jumpTweener = new JumpTweener(this, worldToCamera(to), 0,
				distance * 0.1f);
		jumpTweener.listener = this;
		getParent().add(jumpTweener);

		turnTo(from, to);
		play(fly);
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
}
