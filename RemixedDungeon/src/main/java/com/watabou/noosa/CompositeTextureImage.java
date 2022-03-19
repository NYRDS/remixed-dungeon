package com.watabou.noosa;

import com.nyrds.platform.gl.NoosaScript;
import com.nyrds.platform.gl.Texture;

import java.util.ArrayList;

public class CompositeTextureImage extends Image {

	private ArrayList<Texture> mLayers = new ArrayList<>();

	public CompositeTextureImage() {
		super();
	}

	public CompositeTextureImage(Object tx) {
		this();
		texture(tx);
	}

	public void addLayer(Texture img) {
		mLayers.add(img);
	}

	public void copy(CompositeTextureImage other) {
		super.copy(other);
		mLayers.addAll(other.mLayers);
	}

	@Override
	public void draw() {

		super.draw();

		NoosaScript script = NoosaScript.get();

		for (Texture img : mLayers) {
			img.bind();
			script.drawQuad(getVerticesBuffer());
		}
	}

	public void clearLayers() {
		mLayers.clear();
	}
}
