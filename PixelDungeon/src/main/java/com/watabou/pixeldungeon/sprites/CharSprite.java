/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon.sprites;

import com.nyrds.android.util.ModdingMode;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.watabou.noosa.Animation;
import com.watabou.noosa.CompositeMovieClip;
import com.watabou.noosa.CompositeTextureImage;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.tweeners.PosTweener;
import com.watabou.noosa.tweeners.Tweener;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.EmoIcon;
import com.watabou.pixeldungeon.effects.FloatingText;
import com.watabou.pixeldungeon.effects.IceBlock;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.effects.Splash;
import com.watabou.pixeldungeon.effects.SystemFloatingText;
import com.watabou.pixeldungeon.effects.TorchHalo;
import com.watabou.pixeldungeon.effects.particles.FlameParticle;
import com.watabou.pixeldungeon.items.potions.PotionOfInvisibility;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import java.util.Locale;

import androidx.annotation.Nullable;

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

    public enum State {
        BURNING, LEVITATING, INVISIBLE, PARALYSED, FROZEN, ILLUMINATED
    }

    protected Animation idle;
    protected Animation run;
    protected Animation attack;
    protected Animation operate;
    protected Animation zap;
    protected Animation die;

    private Callback animCallback;

    private Tweener motion;

    protected Emitter burning;
    protected Emitter levitation;

    private IceBlock iceBlock;
    private TorchHalo halo;

    private EmoIcon emo;

    private float flashTime = 0;

    boolean sleeping = false;
    boolean controlled = false;

    // Char owner
    @Nullable
    public Char ch;

    // The sprite is currently in motion
    public boolean isMoving = false;

    public CharSprite() {
        super();
        listener = this;
    }

    public void link(Char ch) {
        this.ch = ch;

        place(ch.getPos());
        turnTo(ch.getPos(), Random.Int(Dungeon.level.getLength()));

        ch.updateSpriteState();

        isMoving = false;
    }


    public PointF worldCoords() {
        final int csize = DungeonTilemap.SIZE;
        PointF point = point();
        point.x = (point.x + width * 0.5f) / csize - 0.5f;
        point.y = (point.y + height - visualOffsetY()) / csize - 1.0f;
        return point;
    }

    public PointF worldToCamera(int cell) {
        final int csize = DungeonTilemap.SIZE;

        return new PointF(
                (Dungeon.level.cellX(cell) + 0.5f) * csize - width * 0.5f,
                (Dungeon.level.cellY(cell) + 1.0f) * csize - height + visualOffsetY()
        );
    }

    public void place(int cell) {
        point(worldToCamera(cell));
    }

    public void showStatus(int color, String text) {
        if (getVisible()) {

            if (ch != null) {
                if (ModdingMode.getClassicTextRenderingMode()) {
                    FloatingText.show(x + width * 0.5f, y, ch.getPos(), text, color);
                } else {
                    SystemFloatingText.show(x + width * 0.5f, y, ch.getPos(), text, color);
                }
            }
        }
    }


    public void showStatus(int color, String text, Object... args) {
        showStatus(color, Utils.format(text, args));
    }

    public void idle() {
        play(idle);
    }

    public void move(int from, int to) {
        play(run);

        if (getParent() != null) {
            motion = new PosTweener(this, worldToCamera(to), MOVE_INTERVAL);
            motion.listener = this;
            getParent().add(motion);

            isMoving = true;

            turnTo(from, to);

            if (getVisible() && Dungeon.level.water[from] && !ch.isFlying()) {
                GameScene.ripple(from);
            }
        }
        ch.onMotionComplete();
    }

    public void interruptMotion() {
        if (motion != null) {
            onComplete(motion);
        }
    }

    public void attack(int cell) {
        turnTo(ch.getPos(), cell);
        play(attack);
    }

    public void attack(int cell, Callback callback) {
        animCallback = callback;
        turnTo(ch.getPos(), cell);
        play(attack);
    }

    public void operate(int cell) {
        turnTo(ch.getPos(), cell);
        play(operate);
    }

    public void zap(int cell) {
        turnTo(ch.getPos(), cell);
        play(zap);
    }

    public void zap(int cell, Callback callback) {
        animCallback = callback;
        turnTo(ch.getPos(), cell);
        play(zap);
    }

    public void turnTo(int from, int to) {
        int fx = from % Dungeon.level.getWidth();
        int tx = to % Dungeon.level.getWidth();
        if (tx > fx) {
            flipHorizontal = false;
        } else if (tx < fx) {
            flipHorizontal = true;
        }
    }

    public void die() {
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
        emitter.pos(x, y + height, width, 0);
        return emitter;
    }

    public void burst(final int color, int n) {
        if (getVisible()) {
            Splash.at(center(), color, n);
        }
    }

    public void bloodBurstA(PointF from, int damage) {
        if (getVisible()) {
            PointF c = center();
            int n = (int) Math.min(9 * Math.sqrt((double) damage / ch.ht()), 9);
            Splash.at(c, PointF.angle(from, c), 3.1415926f / 2, blood(), n);
        }
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
                PotionOfInvisibility.melt(ch);
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
            case INVISIBLE:
                alpha(1f);
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

    @Override
    public void update() {

        super.update();

        if (paused && listener != null) {
            listener.onComplete(curAnim);
        }

        if (flashTime > 0 && (flashTime -= Game.elapsed) <= 0) {
            resetColor();
        }

        boolean visible = getVisible() && (ch == null || ch.invisible <= 0);

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
            showMindControl();
        }

        if (emo != null) {
            emo.setVisible(visible);
        }
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

    private void showMindControl() {
        if (!(emo instanceof EmoIcon.Controlled)) {
            removeEmo();

            if (ch != null && ch.isAlive()) {
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
        super.kill();
        removeEmo();
    }

    @Override
    public void onComplete(Tweener tweener) {
        if (tweener == motion) {

            isMoving = false;

            motion.killAndErase();
            motion = null;
        }
    }

    @Override
    public void onComplete(Animation anim) {

        if (animCallback != null) {
            animCallback.call();
            animCallback = null;
        } else {
            if (anim == attack) {
                ch.onAttackComplete();
                idle();
            } else if (anim == zap) {
                ch.onZapComplete();
                idle();
            } else if (anim == operate) {
                ch.onOperateComplete();
                idle();
            }
        }
    }

    @Override
    public void play(Animation anim) {
        if (anim == null) {
            if (ch == null) {
                EventCollector.logException("null anim on null char, WTF?");
                return;
            } else {
                EventCollector.logException(String.format(Locale.ROOT, "null anim for %s", ch.getClass()));
                ch.next();
                return;
            }
        }

        if (ch != null && !Dungeon.visible[ch.getPos()]) {
            onComplete(anim);
            return;
        }

        if (curAnim == die) {
            return;
        }
        super.play(anim);
    }

    public void selectKind(int i) {
    }

    public Image avatar() {
        CompositeTextureImage avatar = new CompositeTextureImage(texture);
        avatar.frame(idle.frames[0]);
        avatar.addLayer(texture);
        return avatar;
    }

    @Override
    public void draw() {
        super.draw();
    }

    public void reset() {
        curAnim = null;
    }
}
