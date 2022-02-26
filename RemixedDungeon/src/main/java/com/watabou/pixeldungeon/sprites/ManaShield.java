package com.watabou.pixeldungeon.sprites;

import android.opengl.GLES20;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.watabou.pixeldungeon.effects.Halo;
import com.watabou.utils.PointF;

import javax.microedition.khronos.opengles.GL10;

public class ManaShield extends Halo {

    private final CharSprite charSprite;
    private float phase;

    public ManaShield(CharSprite charSprite) {

        super( 14, 0xBBAACC, 1f );
        this.charSprite = charSprite;

        am = -1;
        aa = +1;

        phase = 1;
    }

    @Override
    public void update() {
        super.update();

        if (phase < 1) {
            if ((phase -= GameLoop.elapsed) <= 0) {
                killAndErase();
            } else {
                setScale( (2 - phase) * radius / RADIUS );
                am = phase * (-1);
                aa = phase * (+1);
            }
        }

        if (setVisible(charSprite.getVisible())) {
            PointF p = charSprite.center();
            point(p.x + visualOffsetX(), p.y + charSprite.visualOffsetY());
        }
    }

    @Override
    public void draw() {
        GLES20.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE );
        super.draw();
        GLES20.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA );
    }

    public void putOut() {
        phase = 0.999f;
    }
}
