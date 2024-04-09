package com.watabou.noosa.tweeners;

import com.watabou.noosa.Visual;
import com.watabou.utils.PointF;

public class FallTweener extends ScaleTweener {
    private final Visual sprite;

    public FallTweener(Visual sprite) {
        super(sprite, new PointF(0, 0), 1f);
        this.sprite = sprite;
    }

    @Override
    protected void onComplete() {
        target.killAndErase();
    }

    @Override
    protected void updateValues( float progress ) {
        super.updateValues( progress );
        sprite.am = 1 - progress;
    }
}
