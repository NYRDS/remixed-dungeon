package com.watabou.noosa;

import com.watabou.gltextures.SmartTexture;

import java.util.ArrayList;

public class CompositeTextureImage extends Image {

	private ArrayList<SmartTexture> mLayers;

	public CompositeTextureImage() {
		super();
	}

	public CompositeTextureImage(CompositeTextureImage src ) {
		this();
		copy( src );
	}

	public CompositeTextureImage(Object tx ) {
		this();
		texture( tx );
	}


	public void addLayer(SmartTexture img) {
		if(mLayers == null) {
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
