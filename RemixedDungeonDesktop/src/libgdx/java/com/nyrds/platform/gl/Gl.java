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

    private static final float[] zeroMatrix = new float[16];
    private static boolean poisonReported = false;

    public static boolean matrixIsPoisoned(float[] m) {
        for (float v : m) {
            if (!Float.isFinite(v)) {
                if (!poisonReported) {
                    poisonReported = true;
                    System.out.println("[gl] poisoned matrix refused: "
                        + java.util.Arrays.toString(java.util.Arrays.copyOf(m, Math.min(m.length, 16))));
                    new Exception("matrix source").printStackTrace(System.out);
                }
                return true;
            }
        }
        return false;
    }

    public static float[] zeroMatrix() {
        return zeroMatrix;
    }

    private static boolean vertexPoisonReported = false;

    public static boolean verticesArePoisoned(java.nio.FloatBuffer vertices, int quadCount) {
        try {
            // caveman: callers use different layouts (quad sets, Flare's triangle
            // fan) - scan whatever the buffer actually holds, never past its limit
            int from = vertices.position();
            int to = Math.min(vertices.limit(), from + quadCount * 16);
            for (int i = from; i < to; i++) {
                float v = vertices.get(i);
                if (!Float.isFinite(v)) {
                    if (!vertexPoisonReported) {
                        vertexPoisonReported = true;
                        System.out.println("[gl] poisoned quad refused, floats=" + (to - from)
                            + " firstBad=" + i + " val=" + v);
                        new Exception("quad source").printStackTrace(System.out);
                    }
                    return true;
                }
            }
        } catch (Throwable t) {
            // caveman: the guard must never kill the render loop - skip the draw
            return true;
        }
        return false;
    }

    public static void flush() {
        Gdx.gl20.glFlush();
    }
}
