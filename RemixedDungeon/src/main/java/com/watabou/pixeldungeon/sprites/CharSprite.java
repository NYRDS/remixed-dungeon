
package com.watabou.pixeldungeon.sprites;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.audio.Sample;
import com.nyrds.util.Util;
import com.nyrds.util.WeakOptional;
import com.watabou.noosa.Animation;
import com.watabou.noosa.CompositeMovieClip;
import com.watabou.noosa.Image;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.noosa.tweeners.FallTweener;
import com.watabou.noosa.tweeners.PosTweener;
import com.watabou.noosa.tweeners.Tweener;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.EmoIcon;
import com.watabou.pixeldungeon.effects.IceBlock;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.effects.Splash;
import com.watabou.pixeldungeon.effects.SystemFloatingText;
import com.watabou.pixeldungeon.effects.TorchHalo;
import com.watabou.pixeldungeon.effects.particles.FlameParticle;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import lombok.val;

public class CharSprite extends CompositeMovieClip implements Tweener.Listener, MovieClip.Listener {

    // Color constants for floating text
    public static final int DEFAULT = 0xFFFFFF;
    public static final int POSITIVE = 0x00FF00;
    public static final int NEGATIVE = 0xFF0000;
    public static final int WARNING = 0xFF8800;
    public static final int NEUTRAL = 0xFFFF00;
    public static final int BLUE = 0x0000FF;

    private static final float MOVE_INTERVAL = 0.1f;
    private static final float FLASH_INTERVAL = 0.05f;
    private static final float INVISIBILITY_ALPHA = 0.4f;

    protected float charScale = 1.0f;
    public long layersMask = 0;

    @Nullable
    protected Image avatar;
    protected Image carcass;


    private Glowing glowing = Glowing.NO_GLOWING;

    private float   phase;
    private boolean glowUp;


    public void fall() {

        setOrigin(width * charScale/ 2, height * charScale- DungeonTilemap.SIZE / 2);
        angularSpeed = Random.Int(2) == 0 ? -720 : 720;

        if (hasParent()) {
            getParent().add(new FallTweener(this));
        }
        die();
    }

    public enum State {
        NONE, BURNING, LEVITATING, INVISIBLE, PARALYSED, FROZEN, ILLUMINATED
    }

    protected Animation idle;
    protected Animation run;
    protected Animation attack;
    protected Animation operate;
    protected Animation zap;
    protected Animation die;

    protected final Map<String,Animation> extras = new HashMap<>();

    private Callback animCallback;

    private Tweener motion;

    private Emitter burning;
    private Emitter levitation;

    private IceBlock iceBlock;
    private TorchHalo halo;

    private EmoIcon emo;

    private float flashTime = 0;

    boolean sleeping = false;
    boolean controlled = false;

    // Char owner
    public WeakOptional<Char> ch = WeakOptional.empty();

    // The sprite is currently in motion
    public boolean isMoving = false;
    public int cellIndex;

    public CharSprite() {
        super();
        listener = this;
        setIsometricShift(true);
        GLog.debug("new sprite");
    }

    public void link(Char owner) {
        ch = WeakOptional.of(owner);

        ch.ifPresent(chr -> {
                    layer = owner.getSpriteLayer();

                    place(chr.getPos());
                    turnTo(chr.getPos(), Random.Int(chr.level().getLength()));

                    removeAllStates();

                    chr.forEachBuff(b -> this.add(b.charSpriteStatus()));

                    isMoving = false;
                }
        );
    }

    public PointF worldCoords() {
        final int csize = DungeonTilemap.SIZE;
        PointF point = point();
        point.x = (point.x + width * charScale * 0.5f) / csize - 0.5f;
        point.y = (point.y + height * charScale) / csize - 1.0f;
        return point;
    }

    public PointF worldToCamera(int cell) {
        final int csize = DungeonTilemap.SIZE;
        final Level level = Dungeon.level;

        return new PointF(
                (level.cellX(cell) + 0.5f) * csize - width * charScale * 0.5f,
                (level.cellY(cell) + 1.0f) * csize - height * charScale
        );
    }

    public void place(int cell) {
        cellIndex = cell;
        point(worldToCamera(cell));
    }

    public void showStatus(int color, String text) {
        if (getVisible()) {
            ch.ifPresent(
                    chr -> {
                            SystemFloatingText.show(getX() + width * charScale * 0.5f, getY(), chr.getPos(), text, color);
                    }
            );
        }
    }

    public void showStatus(int color, String text, Object... args) {
        showStatus(color, Utils.format(text, args));
    }

    public void playExtra(String key) {
        if (extras.containsKey(key)) {
            play(extras.get(key));
        }
    }

    public void idle() {
        if(curAnim==null || curAnim==run) {
            play(idle);
        }
    }

    @LuaInterface
    public void move(int from, int to) {
        move(from, to, true);
    }

    public void move(int from, int to, boolean playRunAnimation) {
        ch.ifPresent(chr -> {
            if (playRunAnimation) {
                play(run);
            }

            motion = new PosTweener(this, worldToCamera(to), MOVE_INTERVAL);
            motion.listener = this;
            GameScene.addToMobLayer(motion);

            isMoving = true;

            turnTo(from, to);

            if (getVisible() && chr.level().water[from] && !chr.isFlying()) {
                GameScene.ripple(from);
            }
        });
    }

    public void interruptMotion() {
        if (motion != null) {
            onComplete(motion);
        }
    }

    public void attack(int cell) {
        ch.ifPresent(chr -> {
            turnTo(chr.getPos(), cell);
            play(attack);
        });
    }

    public void attack(int cell, Callback callback) {
        ch.ifPresent(chr -> {
            if (callback != null) {
                animCallback = callback;
            }
            turnTo(chr.getPos(), cell);
            play(attack);
        });
    }

    @Deprecated
    @LuaInterface
    public void dummyAttack(int cell) {
        ch.ifPresent(chr -> {
            if (Dungeon.isCellVisible(chr.getPos())) {
                attack(cell, () -> ch.ifPresent(Actor::next));
            }
        });
    }

    @LuaInterface
    public void operate() { //used by Epic
        ch.ifPresent(chr -> {
            turnTo(chr.getPos(), chr.getPos());
            play(operate);
        });
    }


    public void operate(int cell) {
        ch.ifPresent(chr -> {
            turnTo(chr.getPos(), cell);
            play(operate);
        });
    }

    public void operate(int cell, @NotNull Callback callback) {
        ch.ifPresent(chr -> {
            animCallback = callback;
            turnTo(chr.getPos(), cell);
            play(operate);
        });
    }

    public void zap(int cell, Callback callback) {
        ch.ifPresent(chr -> {
            if (callback != null) {
                animCallback = callback;
            }
            turnTo(chr.getPos(), cell);
            play(zap);
        });
    }

    public void zap(int cell) {
        ch.ifPresent(chr -> {
            turnTo(chr.getPos(), cell);
            play(zap);
        });
    }

    public void turnTo(int from, int to) {
        final Level level = Dungeon.level;

        int fx = from % level.getWidth();
        int tx = to % level.getWidth();
        if (tx > fx) {
            flipHorizontal = false;
        } else if (tx < fx) {
            flipHorizontal = true;
        }
    }

    public void die() {
        glowing = Glowing.NO_GLOWING;
        sleeping = false;
        play(die);

        removeEmo();
    }

    public Emitter emitter() {
        Emitter emitter = GameScene.emitter();
        emitter.pos(this);
        return emitter;
    }

    public Emitter centerEmitter() {
        Emitter emitter = GameScene.emitter();
        emitter.pos(center());
        return emitter;
    }

    public Emitter bottomEmitter() {
        Emitter emitter = GameScene.emitter();
        emitter.pos(getX(), getY() + height * charScale, width * charScale, 0);
        return emitter;
    }

    public void burst(final int color, int n) {
        if (getVisible()) {
            Splash.at(center(), color, n);
        }
    }

    public void bloodBurstA(PointF from, int damage) {
        ch.ifPresent(chr -> {
            if (getVisible()) {
                PointF c = center();
                int n = (int) Math.min(9 * Math.sqrt((double) damage / chr.ht()), 9);
                Splash.at(c, PointF.angle(from, c), 3.1415926f / 2, blood(), n);
            }
        });
    }

    // Blood color
    public int blood() {
        return 0xFFBB0000;
    }

    public void flash() {
        ra = ba = ga = 1f;
        flashTime = FLASH_INTERVAL;
    }

    public void add(State state) {
        ch.ifPresent(chr -> {
            switch (state) {
                case BURNING:
                    burning = emitter();
                    burning.pour(FlameParticle.FACTORY, 0.06f);
                    if (getVisible()) {
                        Sample.INSTANCE.play(Assets.SND_BURNING);
                    }
                    break;
                case LEVITATING:
                    levitation = emitter();
                    levitation.pour(Speck.factory(Speck.JET), 0.02f);
                    break;
                case INVISIBLE:
                    float alpha = chr instanceof Hero ? INVISIBILITY_ALPHA : 0.0f;

                    if (hasParent()) {
                        GameScene.addToMobLayer( new AlphaTweener(this, alpha, 0.4f));
                    } else {
                        alpha(alpha);
                    }
                    break;
                case PARALYSED:
                    paused = true;
                    break;
                case FROZEN:
                    iceBlock = IceBlock.freeze(this);
                    paused = true;
                    break;
                case ILLUMINATED:
                    GameScene.effect(halo = new TorchHalo(this));
                    break;
            }
        });
    }

    private void removeEmo() {
        if (emo != null) {
            emo.killAndErase();
            emo = null;
        }
    }

    public void removeAllStates() {
        remove(State.BURNING);
        remove(State.LEVITATING);
        remove(State.INVISIBLE);
        remove(State.FROZEN);
        remove(State.ILLUMINATED);
        removeEmo();
    }

    public void remove(State state) {
        switch (state) {
            case BURNING:
                if (burning != null) {
                    burning.setVisible(false);
                    burning.on = false;
                    burning.kill();
                    burning = null;
                }
                break;
            case LEVITATING:
                if (levitation != null) {
                    levitation.setVisible(false);
                    levitation.on = false;
                    levitation.kill();
                    levitation = null;
                }
                break;
            case INVISIBLE: {
                float alpha = 1.0f;

                if (hasParent()) {
                    GameScene.addToMobLayer(new AlphaTweener(this, alpha, 0.4f));
                } else {
                    alpha(alpha);
                }
            }
                break;
            case PARALYSED:
                paused = false;
                break;
            case FROZEN:
                if (iceBlock != null) {
                    iceBlock.melt();
                    iceBlock = null;
                }
                paused = false;
                break;
            case ILLUMINATED:
                if (halo != null) {
                    halo.putOut();
                }
                break;
        }
    }

    public boolean doingSomething() {
        return ((curAnim != null) && (curAnim == attack || curAnim == operate || curAnim == zap)) || isMoving;
    }

    @Override
    public void update() {

        super.update();

        if (paused && listener != null) {
            listener.onComplete(curAnim);
        }

        if (flashTime > 0 && (flashTime -= GameLoop.elapsed) <= 0) {
            resetColor();
        }

        ch.ifPresent(chr -> {
            boolean visible = getVisible() && chr.invisible <= 0;

            if (burning != null) {
                burning.setVisible(visible);
            }

            if (levitation != null) {
                levitation.setVisible(visible);
            }

            if (iceBlock != null) {
                iceBlock.setVisible(visible);
            }

            if (sleeping && visible) {
                showSleep();
            } else {
                hideSleep();
            }

            if (controlled && visible) {
                showMindControl(chr);
            } else {
                if (emo instanceof EmoIcon.Controlled) {
                    removeEmo();
                }
            }

            if (emo != null) {
                emo.setVisible(visible);
            }

            if (visible && curAnim == null) {
                idle();
            }

            if (visible) {
                if (glowing != Glowing.NO_GLOWING) {
                    final float elapsed = GameLoop.elapsed + chr.getId()/1000f;

                    if (glowUp && (phase += elapsed) > glowing.period) {
                        glowUp = false;
                        phase = glowing.period;
                    } else if (!glowUp && (phase -= elapsed) < 0) {
                        glowUp = true;
                        phase = 0;
                    }

                    float value = phase / glowing.period * 0.6f;

                    rm = gm = bm = 1 - value;
                    ra = glowing.red * value;
                    ga = glowing.green * value;
                    ba = glowing.blue * value;
                }
            }

            int chrPos = chr.getPos();
            if(!isMoving && cellIndex!= chrPos) {
                EventCollector.logException(Utils.format("CharSprite desync %s (%d not %d)", chr.getEntityKind(), cellIndex, chrPos));
                place(chrPos);
            }
        });
    }

    private void showSleep() {
        if (!(emo instanceof EmoIcon.Sleep)) {
            removeEmo();
            emo = new EmoIcon.Sleep(this);
        }
    }

    private void hideSleep() {
        if (emo instanceof EmoIcon.Sleep) {
            removeEmo();
        }
    }

    private void showMindControl(@NotNull Char chr) {
        if (!(emo instanceof EmoIcon.Controlled)) {
            removeEmo();

            if (chr.isAlive()) {
                emo = new EmoIcon.Controlled(this);
            }
        }
    }

    public void showAlert() {
        if (!(emo instanceof EmoIcon.Alert)) {
            removeEmo();
            emo = new EmoIcon.Alert(this);
        }
    }

    public void hideAlert() {
        if (emo instanceof EmoIcon.Alert) {
            removeEmo();
        }
    }

    @Override
    public void kill() {
        removeAllStates();
        super.kill();
    }

    @Override
    public void onComplete(Tweener tweener) {
        if (tweener == motion) {
            motion.killAndErase();
            motion = null;

            isMoving = false;
            ch.ifPresent(chr -> {
                if (Actor.all().contains(chr)) {
                    chr.onMotionComplete();
                    place(chr.getPos());
                }
            });
        }
    }

    @Override
    public void onComplete(Animation anim) {
        if (anim == null) {
            return;
        }

        ch.ifPresent(chr -> {
            if (!Actor.all().contains(chr)) {
                return;
            }

            final boolean realtime = Dungeon.realtime();

            if (animCallback != null) {
                val callback = animCallback;
                animCallback = null;
                callback.call();
            } else {
                if (anim!=idle) {
                    if(Dungeon.isCellVisible(chr.getPos())) {
                        idle();
                    }

                    if (anim == attack) {
                        if (!realtime) {
                            chr.onAttackComplete();
                        }
                        resetIfNotDying();
                        return;
                    } else if (anim == zap) {
                        if (!realtime) {
                            chr.onZapComplete();
                        }
                        resetIfNotDying();
                        return;
                    } else if (anim == operate) {
                        if (!realtime) {
                            chr.onOperateComplete();
                        }
                        resetIfNotDying();
                        return;
                    }
                }
            }

            if(curAnim!=run) {
                curAnim = null;
            }
        });
    }

    @Override
    public void play(@NotNull Animation anim) {

        if(Util.isDebug()) {
            assert (anim != null);
        }

        if (curAnim == die) {
            return;
        }

        boolean[] skipAnim = {false};

        ch.ifPresent(chr -> {
            if (anim == null) {
                EventCollector.logException(String.format(Locale.ROOT, "null anim for %s", chr.getClass()));
                chr.next();
                skipAnim[0] = true;
                return;
            }

            if (!Dungeon.isCellVisible(chr.getPos())) {
                onComplete(anim);
                skipAnim[0] = true;
            }
        });

        if(skipAnim[0]) {
            return;
        }

        super.play(anim);
    }

    public void selectKind(int i) {
    }

    public Image avatar() {

        if (avatar == null || !avatar.alive) {
            avatar = snapshot(idle.frames[0]);
            avatar.setScale(charScale);
        }

        return avatar;
    }

    public Image carcass() {
        if (carcass == null) {
            carcass = snapshot(die.frames[die.frames.length - 1]);
            carcass.setScale(charScale);
        }
        return carcass;
    }

    private void resetIfNotDying(){
        if(curAnim!=die) {
            curAnim = null;
        }
    }

    public void reset() {
        curAnim = null;
    }

    public void completeForce() {
        interruptMotion();
        interruptAnimation();
        animCallback = null;

        reset();
    }

    public void setGlowing(Glowing glowing) {
        this.glowing = glowing;
    }
}
