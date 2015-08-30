package com.watabou.noosa;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

public class SystemText extends Text {

	protected String text;
	
	protected Paint textPaint = new Paint();
	private Image image;
	private int size = 8;
	private final int oversample = 4;
	
	private boolean dirty = true;
	
	public SystemText() {
		this( "", null );
	}
	
	public SystemText( Font font ) {
		this( "", font );
	}
	
	public SystemText( String text, Font font ) {
		super( 0, 0, 0, 0 );
		size = (int) font.lineHeight;
		
		Typeface tf = Typeface.create((String)null, Typeface.BOLD);

		textPaint.setTextSize(size*2);
		textPaint.setAntiAlias(true);
		textPaint.setColor(0xffffffff);
		textPaint.setTypeface(tf);
		
		this.text(text);
	}
	
	@Override
	public void destroy() {
		text = null;
		super.destroy();
	}
	
	private void createText() {
		measure();
		if (width > 0 && height > 0){
			Bitmap bitmap = Bitmap.createBitmap((int)width*oversample, (int)height*oversample, Bitmap.Config.ARGB_4444);
			Canvas canvas = new Canvas(bitmap);
			
			canvas.drawText(text, 0, (int)height*oversample, textPaint);
			
			image = new Image(bitmap, true);
		}
	}
	
	
	@Override
	public void draw() {
		super.draw();
		
		measure();
		if(image != null){
			image.x = x;
			image.y = y;
			
			image.scale.x = scale.x/oversample;
			image.scale.y = scale.y/oversample;
			
			image.draw();
		}
	}

	public void measure() {
		if(dirty){
			dirty = false;
			Rect bounds = new Rect();
			
			textPaint.getTextBounds(text, 0, text.length(), bounds);
			width  = (bounds.right - bounds.left)/oversample;
			height = (bounds.bottom - bounds.top)/oversample;
		}
	}
	
	public String text() {
		return text;
	}
	
	public void text( String str ) {
		dirty = true;
		if(str == null){
			text = "";
		}
		else{
			text = str;
		}
		createText();
	}

	@Override
	public float baseLine() {
		return height * scale.y;
	}
}
