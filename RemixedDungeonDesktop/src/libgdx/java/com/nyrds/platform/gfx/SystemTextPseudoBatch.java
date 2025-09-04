package com.nyrds.platform.gfx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.nyrds.platform.gl.NoosaScript;
import com.watabou.glwrap.Quad;
import com.watabou.noosa.Visual;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.HashMap;


public class SystemTextPseudoBatch extends PseudoBatch {
    public static SystemText textBeingRendered = null;
    private static final float[] vertices = new float[16];
    private static final HashMap<Integer, FloatBuffer> buffers = new HashMap<>();

    private static final Color tempColor = new Color();

    @Override
    public void draw(Texture texture, float[] spriteVertices, int offset, int count) {
        Visual v = textBeingRendered;

        int quadCount = count / 20;
        FloatBuffer verticesBuffer;

        if (buffers.containsKey(quadCount)){
            verticesBuffer = buffers.get(quadCount);
            ((Buffer)verticesBuffer).position(0);
        } else {
            verticesBuffer = Quad.createSet(quadCount);
            buffers.put(quadCount, verticesBuffer);
        }

        // This loop can remain as is, since we are sending the color via a uniform
        for (int i = 0; i < count; i += 20){
            vertices[0]     = spriteVertices[i+0];
            vertices[1]     = spriteVertices[i+1];

            vertices[2]     = spriteVertices[i+3];
            vertices[3]     = spriteVertices[i+4];

            vertices[4]     = spriteVertices[i+5];
            vertices[5]     = spriteVertices[i+6];

            vertices[6]     = spriteVertices[i+8];
            vertices[7]     = spriteVertices[i+9];

            vertices[8]     = spriteVertices[i+10];
            vertices[9]     = spriteVertices[i+11];

            vertices[10]    = spriteVertices[i+13];
            vertices[11]    = spriteVertices[i+14];

            vertices[12]    = spriteVertices[i+15];
            vertices[13]    = spriteVertices[i+16];

            vertices[14]    = spriteVertices[i+18];
            vertices[15]    = spriteVertices[i+19];
            verticesBuffer.put(vertices);
        }

        ((Buffer)verticesBuffer).position(0);

        NoosaScript script = NoosaScript.get();

        com.nyrds.platform.gl.Texture.activate(0);
        com.nyrds.platform.gl.Texture.unbind();
        texture.bind(); //bind libgdx texture

        script.camera( v.camera() );
        script.uModel.valueM4( v.matrix);

        float packedColor = spriteVertices[offset + 2];

        Color.abgr8888ToColor(tempColor, packedColor);

        float r = tempColor.r * v.rm;
        float g = tempColor.g * v.gm;
        float b = tempColor.b * v.bm;
        float a = tempColor.a * v.am;

        script.lighting(
                r, g, b, a,
                v.ra, v.ga, v.ba, v.aa );

        script.drawQuadSet( verticesBuffer, quadCount);
    }
}