package com.watabou.pixeldungeon.sprites;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.items.accessories.Accessory;
import com.nyrds.util.ModdingMode;
import com.watabou.noosa.Animation;
import com.watabou.noosa.Camera;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.tweeners.JumpTweener;
import com.watabou.noosa.tweeners.Tweener;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.EquipableItem;
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

    protected Animation fly;
    private Tweener jumpTweener;
    private Callback jumpCallback;


    protected HeroSpriteDef(String defName, int kind) {
        super(defName, kind);
    }

    public static HeroSpriteDef createHeroSpriteDef(@NotNull EquipableItem item) {
        return new ModernHeroSpriteDef(item);
    }

    public static HeroSpriteDef createHeroSpriteDef(String[] lookDesc, String deathEffectDesc) {
        if (ModdingMode.useRetroHeroSprites) {
            return new RetroHeroSpriteDef(lookDesc);
        } else {
            return new ModernHeroSpriteDef(lookDesc, deathEffectDesc);
        }
    }

    public static HeroSpriteDef createHeroSpriteDef(Hero hero) {
        if (ModdingMode.useRetroHeroSprites) {
            return new RetroHeroSpriteDef(hero);
        } else {
            return new ModernHeroSpriteDef(hero);
        }
    }

    public static HeroSpriteDef createHeroSpriteDef(Hero hero, Accessory accessory) {
        return new ModernHeroSpriteDef(hero, accessory);
    }

    public abstract String[] getLayersDesc();

    @Override
    public void onComplete(Tweener tweener) {

            if (tweener == jumpTweener) {
                ch.ifPresent(chr -> {
                    if (getVisible() && chr.level().water[chr.getPos()] && !chr.isFlying()) {
                        GameScene.ripple(chr.getPos());
                    }
                });
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
    public void idle() {
        ch.ifPresent(chr -> {
            if (chr.isFlying()) { //ch can be null when used in indicators
                play(fly);
            } else {
                super.idle();
            }
        });
    }

    @Override
    public void move(int from, int to, boolean playRunAnimation) {
        ch.ifPresent(chr -> {
            super.move(from, to, !chr.isFlying());
            if (chr.isFlying()) {
                play(fly);
            }
            if (chr instanceof Hero) {
                Camera.main.target = this;
            }
        });
    }

    @NotNull
    public abstract String getDeathEffect();

    public abstract void heroUpdated(Hero hero);

    @Override
    public void place(int p) {
        ch.ifPresent(chr -> {
            super.place(p);
            if (chr instanceof Hero) {
                Camera.main.target = this;
            }
        });
    }

    @Override
    protected void loadAdditionalData(JSONObject json, TextureFilm film, int kind) throws JSONException {
        super.loadAdditionalData(json, film, kind);
        fly = readAnimation(json, "fly", film);
        operate = readAnimation(json, "operate", film);
        extras.put("std_fly", fly.clone());
        extras.put("std_operate", operate.clone());
    }


    @LuaInterface
    public void jump(float height) {
        jumpCallback = null;
        ch.ifPresent(chr -> {
            jumpTweener = new JumpTweener(this, worldToCamera(chr.getPos()), height,
                    height / 40);

            GameScene.addToMobLayer(jumpTweener);
        });
    }

    @LuaInterface
    public void dash(int from, int to) {
        jumpCallback = null;

        int distance = Dungeon.level.distance(from, to);
        jumpTweener = new JumpTweener(this, worldToCamera(to), 0,
                distance * 0.1f);
        jumpTweener.listener = this;
        GameScene.addToMobLayer(jumpTweener);

        turnTo(from, to);
        play(fly);
    }


    public void jump(int from, int to, Callback callback) {
        jumpCallback = callback;

        int distance = Dungeon.level.distance(from, to);
        jumpTweener = new JumpTweener(this, worldToCamera(to), distance * 4,
                distance * 0.1f);
        jumpTweener.listener = this;
        GameScene.addToMobLayer(jumpTweener);

        turnTo(from, to);
        play(fly);
    }

    @Override
    public boolean doingSomething() {
        return (curAnim != null && curAnim != idle && curAnim != run && curAnim != fly) || isMoving;
    }
}
