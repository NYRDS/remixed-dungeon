package com.nyrds.platform.gl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.nyrds.pixeldungeon.game.GameLoop;

public class Gl {

    public static void clear() {
        Gdx.gl20.glScissor(0, 0, GameLoop.width, GameLoop.height);
        Gdx.gl20.glClearColor(0, 0, 0, 0.0f);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    public static void blendSrcAlphaOne() {
        Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
    }

    public static void blendSrcAlphaOneMinusAlpha() {
        Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    public static void fowBlend() {
        Gdx.gl.glBlendFunc(GL20.GL_ZERO, GL20.GL_SRC_ALPHA);
    }


    public static void glCheck() {
        if( Gdx.gl20.glGetError() != GL20.GL_NO_ERROR ) {
            throw new RuntimeException();
        }
    }

    public static void flush() {
        Gdx.gl20.glFlush();
    }
}
