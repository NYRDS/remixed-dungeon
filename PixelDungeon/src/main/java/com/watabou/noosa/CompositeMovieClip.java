package com.watabou.noosa;

import com.watabou.gltextures.SmartTexture;

import java.util.ArrayList;

/**
 * Created by mike on 29.03.2016.
 */
public class CompositeMovieClip extends MovieClip {

	private ArrayList<LayerDesc> mLayers;

	private class LayerDesc {
		String id;
		boolean enabled = true;
		SmartTexture texture;

		LayerDesc(String _id, SmartTexture _tex) {
			id = _id;
			texture = _tex;
		}
	}

	public CompositeMovieClip() {
		super();
	}

	public void addLayer(String id, SmartTexture img) {
		if (mLayers == null) {
			mLayers = new ArrayList<>();
		}

		LayerDesc layerDesc = new LayerDesc(id, img);
		mLayers.add(layerDesc);
	}

	public void setLayerTexture(String id, SmartTexture img) {
		if (mLayers != null) {
			for (LayerDesc layer : mLayers) {
				if (layer.id.equals(id)) {
					layer.texture = img;
				}
			}
		}
	}

	public void setLayerState(String id, boolean state) {
		if (mLayers != null) {
			for (LayerDesc layer : mLayers) {
				if (layer.id.equals(id)) {
					layer.enabled = state;
				}
			}
		}
	}

	@Override
	public void draw() {

		super.draw();

		if (mLayers != null) {
			NoosaScript script = NoosaScript.get();

			for (LayerDesc layer : mLayers) {
				if (layer.enabled) {
					layer.texture.bind();
					script.drawQuad(verticesBuffer);
				}
			}
		}
	}
}
