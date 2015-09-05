package com.watabou.noosa;

import com.watabou.glwrap.Matrix;
import com.watabou.utils.PointF;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

public class SystemText extends Text {

	protected String text;
	
	
	
	protected TextPaint textPaint = new TextPaint();
	
	private StaticLayout sl;
	private Image image;
	private int size = 8;
	private final int oversample = 4;
	
	
	
	private String[] lines;
	
	private boolean dirty = true;
	Rect bounds = new Rect();
	
	public SystemText(){
		this( "", null );
	}
	
	public SystemText( Font font )  {
		this( "", font );
	}
	
	public SystemText( String text, Font font )  {
		super( 0, 0, 0, 0 );
		size = (int) font.baseLine;
		
		if(size == 0) {
			try {
				throw new Exception("zero sized font!!!");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Typeface tf = Typeface.create((String)null, Typeface.BOLD);

		textPaint.setTextSize(size*oversample);
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
			Bitmap bitmap = Bitmap.createBitmap((int)(width*oversample), (int)(height*oversample), Bitmap.Config.ARGB_4444);
			Canvas canvas = new Canvas(bitmap);
			
			sl.draw(canvas);
			//canvas.drawText(text, 0, textPaint.descent()*oversample, textPaint);
			
			image = new Image(bitmap, true);
			if(parent != null) {
				parent.add(image);
			}
		}
	}
	
	@Override
	protected void updateMatrix() {
		// "origin" field is ignored
		Matrix.setIdentity( matrix );
		Matrix.translate( matrix, x, y );
		Matrix.scale( matrix, scale.x, scale.y );
		Matrix.rotate( matrix, angle );
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
		}
	}
	
	public void measure() {
		if(dirty){
			dirty = false;
			if(text.equals("")){
				return;
			}
			sl = new StaticLayout(text, textPaint, maxWidth, Layout.Alignment.ALIGN_NORMAL, 1, 1, false);
			
			width = sl.getLineRight(0)/oversample;
			height = sl.getHeight()/oversample;
			/*
			width = textPaint.measureText(text)/oversample;
			height = ( textPaint.descent() - textPaint.ascent())/oversample;
			
			*/
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
