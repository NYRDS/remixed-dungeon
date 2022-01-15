package com.nyrds.platform.gl;

import com.badlogic.gdx.Gdx;
import com.nyrds.pixeldungeon.game.GameLoop;

public class Gl {

    public static void clear() {
        Gdx.gl20.glScissor(0, 0, GameLoop.width(), GameLoop.height());
        Gdx.gl20.glClearColor(0, 0, 0, 0.0f);
        Gdx.gl20.glClear(Gdx.gl20.GL_COLOR_BUFFER_BIT);
    }
}
