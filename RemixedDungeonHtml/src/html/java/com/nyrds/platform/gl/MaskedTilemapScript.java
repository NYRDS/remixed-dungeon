package com.nyrds.platform.gl;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * HTML version of MaskedTilemapScript
 */
public class MaskedTilemapScript extends ShaderProgram {
    public MaskedTilemapScript(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
    }
    
    public static MaskedTilemapScript get() {
        // Simple implementation for HTML version
        return new MaskedTilemapScript(
            "attribute vec4 a_position;\n" +
            "attribute vec2 a_texCoord0;\n" +
            "uniform mat4 u_projTrans;\n" +
            "varying vec2 v_texCoords;\n" +
            "void main() {\n" +
            "    v_texCoords = a_texCoord0;\n" +
            "    gl_Position = u_projTrans * a_position;\n" +
            "}",
            "#ifdef GL_ES\n" +
            "precision mediump float;\n" +
            "#endif\n" +
            "varying vec2 v_texCoords;\n" +
            "uniform sampler2D u_texture;\n" +
            "uniform sampler2D u_mask;\n" +
            "void main() {\n" +
            "    vec4 color = texture2D(u_texture, v_texCoords);\n" +
            "    vec4 mask = texture2D(u_mask, v_texCoords);\n" +
            "    gl_FragColor = color * mask.a;\n" +
            "}"
        );
    }
}