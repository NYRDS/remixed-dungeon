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

import android.opengl.GLES20;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.gl.Attribute;
import com.nyrds.platform.gl.Uniform;
import com.watabou.glscripts.Script;
import com.watabou.glwrap.Quad;

import java.nio.FloatBuffer;

public class MaskedTilemapScript extends Script {

    public Uniform   uCamera;
    public Uniform   uModel;
    public Uniform   uTex;
    public Uniform   uTex_mask;
    public Uniform   uColorM;
    public Uniform   uColorA;
    public Attribute aXY;
    public Attribute aUV;
    public Attribute aUV_mask;

    private Camera lastCamera;

    public MaskedTilemapScript() {

        super();
        compile(shader());

        uCamera = uniform("uCamera");
        uModel = uniform("uModel");
        uTex = uniform("uTex");
        uTex_mask = uniform("uTex_mask");
        uColorM = uniform("uColorM");
        uColorA = uniform("uColorA");
        aXY = attribute("aXYZW");
        aUV = attribute("aUV");
        aUV_mask = attribute("aUV_mask");

    }


    @Override
    public void unuse() {
        super.unuse();

        aXY.disable();
        aUV.disable();
        aUV_mask.disable();
    }

    @Override
    public void use() {

        super.use();

        aXY.enable();
        aUV.enable();
        aUV_mask.enable();

        int texSampler = GLES20.glGetUniformLocation(this.handle(),"uTex");
        int maskSampler = GLES20.glGetUniformLocation(this.handle(),"uTex_mask");

        GLES20.glUniform1i(texSampler,0);
        GLES20.glUniform1i(maskSampler,1);
    }


    public void drawQuadSet(FloatBuffer vertices, FloatBuffer mask, int size) {

        if (size == 0) {
            return;
        }

        if(vertices.limit() < 16 * size){
            throw new AssertionError();
        }

        if(mask.limit() < 8 * size){
            throw new AssertionError();
        }


        vertices.position(0);
        aXY.vertexPointer(2, 4, vertices);

        vertices.position(2);
        aUV.vertexPointer(2, 4, vertices);

        mask.position(0);
        //vertices.position(2);
        //aUV_mask.vertexPointer(2, 4, vertices);
        aUV_mask.vertexPointer(2, 2, mask);

        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES,
                Quad.SIZE * size,
                GLES20.GL_UNSIGNED_SHORT,
                Quad.getIndices(size));
    }

    public void lighting(float rm, float gm, float bm, float am, float ra, float ga, float ba, float aa) {
        uColorM.value4f(rm, gm, bm, am);
        uColorA.value4f(ra, ga, ba, aa);
    }

    public void resetCamera() {
        lastCamera = null;
    }

    public void camera(Camera camera) {
        if (camera == null) {
            camera = Camera.main;
        }
        if (camera != lastCamera) {
            lastCamera = camera;
            uCamera.valueM4(camera.matrix);

            GLES20.glScissor(
                    camera.x,
                    GameLoop.height() - camera.screenHeight - camera.y,
                    camera.screenWidth,
                    camera.screenHeight);
        }
    }

    public static MaskedTilemapScript get() {
        return Script.use(MaskedTilemapScript.class);
    }


    protected String shader() {
        return SHADER;
    }

    private static final String SHADER =

            "uniform mat4 uCamera;" +
                    "uniform mat4 uModel;" +
                    "attribute vec4 aXYZW;" +
                    "attribute vec2 aUV;" +
                    "attribute vec2 aUV_mask;" +
                    "varying vec2 vUV;" +
                    "varying vec2 vUV_mask;" +
                    "void main() {" +
                    "  gl_Position = uCamera * uModel * aXYZW;" +
                    "  vUV = aUV;" +
                    "  vUV_mask = aUV_mask;" +
                    "}" +

                    "//\n" +

                    "precision mediump float;" +
                    "varying vec2 vUV;" +
                    "varying vec2 vUV_mask;" +
                    "uniform sampler2D uTex;" +
                    "uniform sampler2D uTex_mask;" +
                    "uniform vec4 uColorM;" +
                    "uniform vec4 uColorA;" +
                    "void main() {" +
                    "  gl_FragColor =texture2D( uTex, vUV ) * uColorM  * texture2D( uTex_mask, vUV_mask ).a;" +
                    "}";
}
