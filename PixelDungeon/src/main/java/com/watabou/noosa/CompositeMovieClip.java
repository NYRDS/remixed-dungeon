package com.watabou.noosa;

import com.watabou.gltextures.SmartTexture;

import java.util.ArrayList;

/**
 * Created by mike on 29.03.2016.
 */
public class CompositeMovieClip extends MovieClip {

	private ArrayList<SmartTexture> mLayers;

	public CompositeMovieClip() {
		super();
	}

	public void addLayer(SmartTexture img) {
		if (mLayers == null) {
			mLayers = new ArrayList<>();
		}
		mLayers.add(img);
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

		script.drawQuad( verticesBuffer );

		if(mLayers!=null) {
			for(SmartTexture img:mLayers) {
				img.bind();
				script.drawQuad( verticesBuffer );
			}
		}
	}

}
