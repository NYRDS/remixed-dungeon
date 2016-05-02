package com.watabou.noosa;

import com.watabou.glwrap.Texture;

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

	@Override
	public void draw() {

		super.draw();

		NoosaScript script = NoosaScript.get();

		for (Texture img : mLayers) {
			img.bind();
			script.drawQuad(verticesBuffer);
		}
	}

	public void clearLayers() {
		mLayers.clear();
	}
}
