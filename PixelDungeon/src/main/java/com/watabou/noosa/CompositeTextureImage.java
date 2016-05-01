package com.watabou.noosa;

import com.watabou.gltextures.SmartTexture;

import java.util.ArrayList;

public class CompositeTextureImage extends Image {

	private ArrayList<SmartTexture> mLayers = new ArrayList<>();

	public CompositeTextureImage() {
		super();
	}

	public CompositeTextureImage(Object tx) {
		this();
		texture(tx);
	}

	public void addLayer(SmartTexture img) {
		mLayers.add(img);
	}

	@Override
	public void draw() {

		super.draw();

		NoosaScript script = NoosaScript.get();

		for (SmartTexture img : mLayers) {
			img.bind();
			script.drawQuad(verticesBuffer);
		}
	}

	public void clearLayers() {
		mLayers.clear();
	}
}
