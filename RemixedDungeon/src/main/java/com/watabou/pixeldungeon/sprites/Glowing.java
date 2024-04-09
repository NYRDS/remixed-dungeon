package com.watabou.pixeldungeon.sprites;

import com.nyrds.util.Util;

public class Glowing {

    public static final Glowing WHITE      = new Glowing(0xFFFFFF, 0.6f);
    public static final Glowing NO_GLOWING = new Glowing(0, Util.BIG_FLOAT);

    public final float red;
    public final float green;
    public final float blue;
    public final float period;

    public Glowing(int color) {
        this(color, 1f);
    }

    public Glowing(int color, float period) {
        red = (color >> 16) / 255f;
        green = ((color >> 8) & 0xFF) / 255f;
        blue = (color & 0xFF) / 255f;

        this.period = period;
    }
}