package com.nyrds.platform.gfx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Matrix4;

public class PseudoBatch implements Batch {
    public void begin() {
    }

    public void end() {
    }

    public void setColor(Color tint) {
    }

    public void setColor(float r, float g, float b, float a) {
    }

    public Color getColor() {
        return null;
    }

    public void setPackedColor(float packedColor) {
    }

    public float getPackedColor() {
        return 0;
    }

    public void draw(Texture texture, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX, boolean flipY) {
    }

    public void draw(Texture texture, float x, float y, float width, float height, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX, boolean flipY) {
    }

    public void draw(Texture texture, float x, float y, int srcX, int srcY, int srcWidth, int srcHeight) {
    }

    public void draw(Texture texture, float x, float y, float width, float height, float u, float v, float u2, float v2) {
    }

    public void draw(Texture texture, float x, float y) {
    }

    public void draw(Texture texture, float x, float y, float width, float height) {
    }

    public void draw(Texture texture, float[] spriteVertices, int offset, int count) {
    }

    public void draw(TextureRegion region, float x, float y) {
    }

    public void draw(TextureRegion region, float x, float y, float width, float height) {
    }

    public void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation) {
    }

    public void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation, boolean clockwise) {
    }

    public void draw(TextureRegion region, float width, float height, Affine2 transform) {
    }

    public void flush() {
    }

    public void disableBlending() {
    }

    public void enableBlending() {
    }

    public void setBlendFunction(int srcFunc, int dstFunc) {
    }

    public void setBlendFunctionSeparate(int srcFuncColor, int dstFuncColor, int srcFuncAlpha, int dstFuncAlpha) {
    }

    public int getBlendSrcFunc() {
        return 0;
    }

    public int getBlendDstFunc() {
        return 0;
    }

    public int getBlendSrcFuncAlpha() {
        return 0;
    }

    public int getBlendDstFuncAlpha() {
        return 0;
    }

    public Matrix4 getProjectionMatrix() {
        return null;
    }

    public Matrix4 getTransformMatrix() {
        return null;
    }

    public void setProjectionMatrix(Matrix4 projection) {
    }

    public void setTransformMatrix(Matrix4 transform) {
    }

    public void setShader(ShaderProgram shader) {
    }

    public ShaderProgram getShader() {
        return null;
    }

    public boolean isBlendingEnabled() {
        return false;
    }

    public boolean isDrawing() {
        return false;
    }

    public void dispose() {
    }
}
