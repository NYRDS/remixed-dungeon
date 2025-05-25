package com.watabou.glscripts;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.gl.Attribute;
import com.nyrds.platform.gl.Uniform;
import com.watabou.glwrap.Quad;
import com.watabou.noosa.Camera;

import java.nio.FloatBuffer;

public class FogShader extends Script {

    // Uniform declarations
    public Uniform uCamera;
    public Uniform uModel;
    public Uniform uTex;
    public Uniform uColorM;
    public Uniform uColorA;
    public Uniform uTexSize;

    // Attribute declarations
    public Attribute aXY;
    public Attribute aUV;

    private Camera lastCamera;

    public FogShader() {
        super();
        compile(SHADER);

        // Get uniform locations
        uCamera	= uniform( "uCamera" );
        uModel	= uniform( "uModel" );
        uTex	= uniform( "uTex" );
        uTexSize = uniform( "uTexSize");
        uColorM	= uniform( "uColorM" );
        uColorA	= uniform( "uColorA" );
        aXY		= attribute( "aXYZW" );
        aUV		= attribute( "aUV" );
    }

    public void lighting(float rm, float gm, float bm, float am,
                         float ra, float ga, float ba, float aa) {
        uColorM.value4f(rm, gm, bm, am);
        uColorA.value4f(ra, ga, ba, aa);
    }

    public void texSize(float width, float height) {
        uTexSize.value2f(width, height);
    }

    @Override
    public void use() {
        super.use();
        aXY.enable();
        aUV.enable();
    }

    @Override
    public void unuse() {
        super.unuse();
        aXY.disable();
        aUV.disable();
    }

    public void resetCamera() {
        lastCamera = null;
    }

    public void camera( Camera camera ) {
        if (camera == null) {
            camera = Camera.main;
        }
        if (camera != lastCamera) {
            lastCamera = camera;

            uCamera.valueM4( camera.matrix );

            Gdx.gl20.glScissor(
                    camera.x,
                    GameLoop.height- camera.screenHeight - camera.y,
                    camera.screenWidth,
                    camera.screenHeight );
        }
    }

    public void drawQuad( FloatBuffer vertices ) {

        if(vertices.limit()<16){
            throw new AssertionError();
        }

        vertices.position( 0);
        aXY.vertexPointer( 2, 4, vertices );

        vertices.position( 2 );
        aUV.vertexPointer( 2, 4, vertices );

        Gdx.gl20.glDrawElements( GL20.GL_TRIANGLES, Quad.SIZE, GL20.GL_UNSIGNED_SHORT, Quad.getIndices( 1 ) );

    }
    private static final String SHADER =
            // Vertex shader
            "#version 100\n" +
                    "uniform mat4 uCamera;\n" +
                    "uniform mat4 uModel;\n" +
                    "attribute vec4 aXYZW;\n" +
                    "attribute vec2 aUV;\n" +
                    "varying vec2 vUV;\n" +
                    "void main() {\n" +
                    "  gl_Position = uCamera * uModel * aXYZW;\n" +
                    "  vUV = aUV;\n" +
                    "}\n" +

                    "//\n" + // Shader separation marker

                    // Fragment shader
                    "#version 100\n" +
                    "precision mediump float;\n" +
                    "varying vec2 vUV;\n" +
                    "uniform sampler2D uTex;\n" +
                    "uniform vec2 uTexSize;\n" +
                    "uniform vec4 uColorM;\n" +
                    "uniform vec4 uColorA;\n" +

                    // Weight calculation function
                    "float getWeight(int x, int y) {\n" +
                    "  int index = (y+1)*3 + (x+1);\n" +
                    "  if(index == 0) return 1.0/16.0;\n" +  // Top-left
                    "  if(index == 1) return 2.0/16.0;\n" +  // Top-center
                    "  if(index == 2) return 1.0/16.0;\n" +  // Top-right
                    "  if(index == 3) return 2.0/16.0;\n" +  // Middle-left
                    "  if(index == 4) return 4.0/16.0;\n" +  // Center
                    "  if(index == 5) return 2.0/16.0;\n" +  // Middle-right
                    "  if(index == 6) return 1.0/16.0;\n" +  // Bottom-left
                    "  if(index == 7) return 2.0/16.0;\n" +  // Bottom-center
                    "  return 1.0/16.0;\n" +                 // Bottom-right
                    "}\n" +

                    "void main() {\n" +
                    "  vec4 center = texture2D(uTex, vUV);\n" +
                    "  float originalAlpha = center.a;\n" +
                    "  float smoothedAlpha = 0.0;\n" +
                    "  float totalWeight = 0.0;\n" +
                    "  vec2 pixelSize = 1.0/uTexSize;\n" +

                    "  for(int y = -1; y <= 1; y++) {\n" +
                    "    for(int x = -1; x <= 1; x++) {\n" +
                    "      vec2 offset = vec2(x,y) * pixelSize;\n" +
                    "      vec4 sample = texture2D(uTex, vUV + offset);\n" +
                    "      float sampleAlpha = min(sample.a, originalAlpha);\n" +
                    "      float weight = getWeight(x,y);\n" +
                    "      smoothedAlpha += sampleAlpha * weight;\n" +
                    "      totalWeight += weight;\n" +
                    "    }\n" +
                    "  }\n" +

                    "  smoothedAlpha = smoothedAlpha/totalWeight;\n" +
                    "  gl_FragColor = vec4(0,0,0, smoothedAlpha);\n" +
                    "}";

    public static FogShader get() {
        return Script.use(FogShader.class);
    }
}