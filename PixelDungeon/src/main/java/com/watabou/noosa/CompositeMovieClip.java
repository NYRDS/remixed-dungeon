package com.watabou.noosa;

import com.watabou.gltextures.SmartTexture;
import com.watabou.utils.SparseArray;

/**
 * Created by mike on 29.03.2016.
 */
public class CompositeMovieClip extends MovieClip {

	private SparseArray<LayerDesc> mLayers;

	private class LayerDesc {
		String       id;
		boolean      enabled = false;
		SmartTexture texture;

		LayerDesc(String _id, SmartTexture _tex) {
			id =_id;
			texture = _tex;
		}
	}

	public CompositeMovieClip() {
		super();
	}

	public void addLayer(String id, int z, SmartTexture img) {
		if (mLayers == null) {
			mLayers = new SparseArray<>();
		}

		LayerDesc layerDesc = new LayerDesc(id, img);

		mLayers.put(z, layerDesc);
	}

	public void setLayerState(String id, boolean state) {
		if(mLayers!=null) {
			for(int i=0;i<mLayers.size();++i){
				LayerDesc layer = mLayers.valueAt(i);
				if(layer.id.equals(id)) {
					layer.enabled = state;
				}
			}
		}
	}

	@Override
	public void draw() {

		super.draw();

		NoosaScript script = NoosaScript.get();

		texture.bind();

		script.camera( camera() );

		script.uModel.valueM4( matrix );
		script.lighting(
				rm, gm, bm, am,
				ra, ga, ba, aa );

		updateVerticesBuffer();

		script.drawQuad(verticesBuffer);

		if(mLayers!=null) {
			for(int i=0;i<mLayers.size();++i){
				LayerDesc layer = mLayers.valueAt(i);
				if(layer.enabled) {
					layer.texture.bind();
					script.drawQuad(verticesBuffer);
				}
			}
		}
	}
}
