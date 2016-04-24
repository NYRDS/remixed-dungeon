package com.watabou.noosa;

import java.util.ArrayList;

public class CompositeImage extends Image {

	private ArrayList<Image> mLayers; 
	
	public CompositeImage() {
		super();
	}

	public CompositeImage( Object tx ) {
		this();
		texture( tx );
	}

	public void addLayer(Image img) {
		if(mLayers == null) {
			mLayers = new ArrayList<>();
		}
		mLayers.add(img);
	}
	
	@Override
	public void draw() {
		
		super.draw();

		NoosaScript script = NoosaScript.get();

		if(mLayers!=null) {
			for(Image img:mLayers) {
				img.texture.bind();
				img.updateVerticesBuffer();
				script.drawQuad(img.verticesBuffer);
				
			}
		}
	}
}
