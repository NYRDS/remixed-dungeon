package com.nyrds.platform.gl;

import com.watabou.glscripts.Script;
import com.watabou.noosa.Camera;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

// headless: draws are dropped, camera tracking is kept so callers behave identically
public class NoosaScript extends Script {

	public Uniform uCamera;
	public Uniform uModel;
	public Uniform uTex;
	public Uniform uColorM;
	public Uniform uColorA;
	public Attribute aXY;
	public Attribute aUV;

	private Camera lastCamera;

	public NoosaScript() {
		super();
		compile(shader());

		uCamera = uniform("uCamera");
		uModel = uniform("uModel");
		uTex = uniform("uTex");
		uColorM = uniform("uColorM");
		uColorA = uniform("uColorA");
		aXY = attribute("aXYZW");
		aUV = attribute("aUV");
	}

	@Override
	public void use() {
		super.use();
	}

	@Override
	public void unuse() {
		super.unuse();
	}

	public void drawElements(FloatBuffer vertices, ShortBuffer indices, int size) {
	}

	public void drawQuad(FloatBuffer vertices) {
	}

	public void drawQuadSet(FloatBuffer vertices, int size) {
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

	public static NoosaScript get() {
		return Script.use(NoosaScript.class);
	}

	protected String shader() {
		return SHADER;
	}

	private static final String SHADER =
			"uniform mat4 uCamera;" +
					"uniform mat4 uModel;" +
					"attribute vec4 aXYZW;" +
					"attribute vec2 aUV;" +
					"varying vec2 vUV;" +
					"void main() {" +
					"  gl_Position = uCamera * uModel * aXYZW;" +
					"  vUV = aUV;" +
					"}" +
					"//\n" +
					"varying vec2 vUV;" +
					"uniform sampler2D uTex;" +
					"uniform vec4 uColorM;" +
					"uniform vec4 uColorA;" +
					"void main() {" +
					"  gl_FragColor = texture2D( uTex, vUV ) * uColorM + uColorA;" +
					"}";
}
