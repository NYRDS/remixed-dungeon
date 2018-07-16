package com.watabou.noosa;

import com.watabou.glwrap.Texture;

import java.util.ArrayList;

/**
 * Created by mike on 29.03.2016.
 */
public class CompositeMovieClip extends MovieClip {

	private ArrayList<LayerDesc> mLayers;

	private class LayerDesc {
		String id;
		Texture texture;

		LayerDesc(String _id, Texture _tex) {
			id = _id;
			texture = _tex;
		}
	}

	protected CompositeMovieClip() {
		super();
	}

	protected void clearLayers() {
		if (mLayers == null) {
			mLayers = new ArrayList<>();
		}

		mLayers.clear();
	}

	protected void addLayer(String id, Texture img) {
		if (mLayers == null) {
			mLayers = new ArrayList<>();
		}

		LayerDesc layerDesc = new LayerDesc(id, img);
		mLayers.add(layerDesc);
	}


	protected Texture getLayerTexture(String id) {
		if(mLayers!=null) {
			for (LayerDesc layer : mLayers) {
				if (layer.id.equals(id)) {
					return layer.texture;
				}
			}
		}
		return null;
	}

	public Image snapshot(int frame) {
		CompositeTextureImage img = new CompositeTextureImage(texture);
		img.clearLayers();

		for (LayerDesc layer : mLayers) {
			img.addLayer(layer.texture);
		}

		return img;
	}

	@Override
	public void draw() {
		super.draw();

		if (mLayers != null) {
			NoosaScript script = NoosaScript.get();

			for (LayerDesc layer : mLayers) {
					layer.texture.bind();
					script.drawQuad(verticesBuffer);
			}
		}
	}
}
