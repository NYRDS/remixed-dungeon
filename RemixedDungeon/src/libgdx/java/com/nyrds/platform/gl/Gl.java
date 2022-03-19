package com.nyrds.platform.gl;

import com.badlogic.gdx.Gdx;
import com.nyrds.pixeldungeon.game.GameLoop;

public class Gl {

    public static void clear() {
        Gdx.gl20.glScissor(0, 0, GameLoop.width(), GameLoop.height());
        Gdx.gl20.glClearColor(0, 0, 0, 0.0f);
        Gdx.gl20.glClear(Gdx.gl20.GL_COLOR_BUFFER_BIT);
    }

    public static void blendSrcAlphaOne() {
        Gdx.gl20.glBlendFunc(Gdx.gl20.GL_SRC_ALPHA, Gdx.gl20.GL_ONE);
    }

    public static void blendSrcAlphaOneMinusAlpha() {
        Gdx.gl20.glBlendFunc(Gdx.gl20.GL_SRC_ALPHA, Gdx.gl20.GL_ONE_MINUS_SRC_ALPHA);
    }
}
