package com.nyrds.platform.gl;

import com.watabou.glscripts.Script;
import com.watabou.noosa.Camera;
import java.nio.FloatBuffer;

// headless: draws are dropped
public class MaskedTilemapScript extends Script {

	public Uniform uCamera;
	public Uniform uModel;
	public Uniform uTex;
	public Uniform uTex_mask;
	public Uniform uColorM;
	public Uniform uColorA;
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
	}

	@Override
	public void use() {
		super.use();
	}

	public void drawQuadSet(FloatBuffer vertices, FloatBuffer mask, int size) {
	}

	public void lighting(float rm, float gm, float bm, float am, float ra, float ga, float ba, float aa) {
	}

	public void resetCamera() {
		lastCamera = null;
	}

	public void camera(Camera camera) {
		if (camera == null) {
			camera = Camera.main;
		}
		lastCamera = camera;
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
