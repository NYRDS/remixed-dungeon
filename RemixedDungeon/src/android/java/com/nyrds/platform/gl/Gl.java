package com.nyrds.platform.gl;

import android.opengl.GLES20;

import com.nyrds.pixeldungeon.game.GameLoop;


public class Gl {

    public static void clear() {
        GLES20.glScissor(0, 0, GameLoop.width(), GameLoop.height());
        GLES20.glClearColor(0, 0, 0, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }

    public static void blendSrcAlphaOne() {
        GLES20.glBlendFunc( GLES20.GL_SRC_ALPHA, GLES20.GL_ONE );
    }

    public static void blendSrcAlphaOneMinusAlpha() {
        GLES20.glBlendFunc( GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA );
    }
}
