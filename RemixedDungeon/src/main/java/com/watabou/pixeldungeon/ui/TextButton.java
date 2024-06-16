package com.watabou.pixeldungeon.ui;

import com.nyrds.platform.audio.Sample;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Button;
import com.watabou.pixeldungeon.Assets;

public class TextButton extends Button {
	public NinePatch bg;
	protected Text text;
	protected Image icon;
	
	public TextButton( String label ) {
		super();
		text.text( label );
	}

	@Override
	protected void layout() {
		super.layout();
		
		bg.setX(x);
		bg.setY(y);
		bg.size( width, height );
		
		text.setX(x + (int)(width - text.width()) / 2);
		text.setY(y + (int)(height - Math.max(text.height(),text.baseLine())) / 2);

		if (icon != null) {
			icon.setX(x + text.getX() - icon.width() - 2);
			icon.setY(y + (height - icon.height()) / 2);
		}
	}

	@Override
	protected void onTouchDown() {
		bg.brightness( 1.2f );
		Sample.INSTANCE.play( Assets.SND_CLICK );
	}

	@Override
	protected void onTouchUp() {
		bg.resetColor();
	}

	public void enable( boolean value ) {
		setActive(value);
		text.alpha( value ? 1.0f : 0.3f );
	}
	
	public void text( String value ) {
		text.text( value );
		layout();
	}
	
	public void icon( Image icon ) {
		if (this.icon != null) {
			remove( this.icon );
		}
		this.icon = icon;
		if (this.icon != null) {
			add( this.icon );
			layout();
		}
	}

	public void autoSize() {
		setSize(reqWidth() + 2, reqHeight() + 2 );
	}

	public float reqWidth() {
		return text.width() + 4;
	}
	
	public float reqHeight() {
		return text.height() + 4;
	}
}
