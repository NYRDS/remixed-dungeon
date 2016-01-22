package com.watabou.noosa;

import java.util.ArrayList;

public class CompositeImage extends Image {

	private ArrayList<Image> mLayers; 
	
	public CompositeImage() {
		super();
	}
	
	public CompositeImage( CompositeImage src ) {
		this();
		copy( src );
	}
	
	public CompositeImage( Object tx ) {
		this();
		texture( tx );
	}
	
	public CompositeImage( Object tx, int left, int top, int width, int height ) {
		this( tx );
		frame( texture.uvRect( left,  top,  left + width, top + height ) );
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

		texture.bind();
		
		script.camera( camera() );
		
		script.uModel.valueM4( matrix );
		script.lighting( 
			rm, gm, bm, am, 
			ra, ga, ba, aa );
		
		updateVerticesBuffer();
		
		script.drawQuad( verticesBuffer );
		
		if(mLayers!=null) {
			for(Image img:mLayers) {
				img.texture.bind();
				img.updateVerticesBuffer();
				script.drawQuad(img.verticesBuffer);
				
			}
		}
	}
}
