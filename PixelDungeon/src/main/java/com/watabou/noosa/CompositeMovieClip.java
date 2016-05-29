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
		boolean enabled = true;
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

	public void setLayerTexture(String id, Texture img) {
		if (mLayers != null) {
			for (LayerDesc layer : mLayers) {
				if (layer.id.equals(id)) {
					layer.texture = img;
				}
			}
		}
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
