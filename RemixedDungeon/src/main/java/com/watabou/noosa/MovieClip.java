/*
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

package com.watabou.noosa;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.watabou.pixeldungeon.sprites.ModernHeroSpriteDef;
import com.watabou.pixeldungeon.utils.GLog;

public class MovieClip extends Image {

    protected Animation curAnim;
    protected int curFrame;

    private float frameTimer;
    private boolean finished;

    public boolean paused = false;

    public Listener listener;

    public MovieClip() {
        super();
    }

    public MovieClip(Object tx) {
        super(tx);
    }

    @Override
    public void update() {
        super.update();
        if (!paused) {
            updateAnimation();
        }
    }

    private void updateAnimation() {
        Animation anim = curAnim;
        
        if (anim != null && anim.delay > 0 && (anim.looped || !finished)) {

            int prevFrame = curFrame;

            frameTimer += GameLoop.elapsed;
            while (frameTimer > anim.delay) {
                frameTimer -= anim.delay;

                if (curFrame == anim.frames.length - 1) {
                    if (anim.looped) {
                        curFrame = 0;
                    }
                    finishAnimation();
                    if (!anim.looped) {
                        return;
                    }
                } else {
                    curFrame++;
                }
            }

            if (curFrame != prevFrame) {
                //if(this instanceof ModernHeroSpriteDef && !anim.looped) {
                //    GLog.debug("%s frame %d", this, curFrame);
                //}
                frame(anim.frames[curFrame]);
            }

        }
    }

    private void finishAnimation() {
        finished = true;
        if (listener != null) {
            listener.onComplete(curAnim);
        }
    }

    public void interruptAnimation() {
        finished = true;
    }

    public void play(Animation anim) {
        play(anim, false);
    }

    protected void play(final Animation anim, final boolean force) {
        if (!force
                && (curAnim != null)
                && (curAnim == anim)
                && (curAnim.looped || !finished)) {
            return;
        }

        curAnim = anim;
        curFrame = 0;
        finished = false;

        frameTimer = 0;


        if (!getVisible()) {
            finishAnimation();
            return;
        }

        if (anim != null) {
            frame(anim.frames[curFrame]);
        }
    }


    public interface Listener {
        void onComplete(Animation anim);
    }
}
