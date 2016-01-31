package com.watabou.noosa;

import com.nyrds.android.util.ModdingMode;

import java.util.regex.Pattern;

public abstract class Text extends Visual {

	protected static final Pattern PARAGRAPH	= Pattern.compile( "\n" );
	protected static final Pattern WORD			= Pattern.compile( "\\s+" );
	protected              int maxWidth = Integer.MAX_VALUE;
	
	protected boolean dirty = true;
	
	public boolean[] mask;
	
	protected Text(float x, float y, float width, float height) {
		super(x, y, width, height);
	}

	public static Text createBasicText(Font font) {
		if(!ModdingMode.getClassicTextRenderingMode()) {
			return new SystemText(font.baseLine * 2);
		}
		return new BitmapText(font);
	}
	
	public static Text createBasicText(String text,Font font) {
		if(!ModdingMode.getClassicTextRenderingMode()) {
			return new SystemText(text, font.baseLine * 2, false);
		}
		return new BitmapText(text, font);
	}

	public static Text create(Font font) {
		if(!ModdingMode.getClassicTextRenderingMode()) {
			return new SystemText(font.baseLine);
		}
		return new BitmapText(font);
	}
	
	public static Text create(String text, Font font) {
		if(!ModdingMode.getClassicTextRenderingMode()) {
			return new SystemText(text, font.baseLine, false);
		}
		return new BitmapText(text, font);
	}
	
	public static Text createMultiline(String text, Font font) {
		
		if(!ModdingMode.getClassicTextRenderingMode()) {
			return new SystemText(text, font.baseLine,true);
		}
		
		return new BitmapTextMultiline(text, font);
	}
	
	@Override
	public void destroy(){
		super.destroy();
	}
	
	@Override
	public void draw(){
		super.draw();
	}
	
	public int getMaxWidth() {
		return maxWidth;
	}

	public void maxWidth(int maxWidth) {
		this.maxWidth = maxWidth;
		dirty = true;
		measure();
	}
	
	public abstract void measure();
	public abstract float baseLine();
	public abstract String text();
	public abstract void text(String str);

}
