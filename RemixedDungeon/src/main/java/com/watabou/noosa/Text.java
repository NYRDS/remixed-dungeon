package com.watabou.noosa;

import com.nyrds.pixeldungeon.windows.IPlaceable;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.gfx.SystemText;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public abstract class Text extends Visual implements IPlaceable {

	@NotNull
	protected String text= Utils.EMPTY_STRING;


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
		return new SystemText(text, font.baseLine * 2, false);
	}

	public static Text create(Font font) {
		return new SystemText(font.baseLine * 2);
	}
	
	public static Text create(String text, Font font) {
		return new SystemText(text, font.baseLine*2, false);
	}
	
	public static Text createMultiline(String text, Font font) {
		return new SystemText(text, font.baseLine * 2,true);
	}

	@Override
	public void draw(){
		clean();
		super.draw();
	}
	
	public int getMaxWidth() {
		return maxWidth;
	}

	protected void clean() {
		if(dirty) {
			measure();
			dirty = false;
		}
	}

	public void maxWidth(int maxWidth) {
		this.maxWidth = maxWidth;
		dirty = true;
	}

	@Override
	public float height() {
		clean();
		return super.height();
	}

	@Override
	public float width() {
		clean();
		return super.width();
	}

	protected abstract void measure();
	public abstract float baseLine();

	@NotNull
	public String text() {
		return text;
	}


	public void text(int id) {
        text(StringsManager.getVar(id));
	}

	public void text(@NotNull String str) {
		if(text.equals(str)) {
			return;
		}

		dirty = true;

		if(str == null) {
		    text = Utils.EMPTY_STRING;
            EventCollector.logException("Trying to create null string!!!");
		    return;
        }

		text = str;
	}

	public abstract int lines();
}
