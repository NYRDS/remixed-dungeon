package com.watabou.pixeldungeon.ui;

import com.watabou.noosa.Image;
import com.watabou.noosa.SystemText;
import com.watabou.noosa.Text;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Button;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.windows.WndModSelect;


public class ModsButton extends Button {
	
	private Image image;
	private Text  text;
	
	public ModsButton() {
		super();
		
		width = image.width;
		height = image.height;
	}
	
	@Override
	protected void createChildren() {
		super.createChildren();
		
		image = Icons.MODDING_MODE.get();
		add( image );
		
		text = new SystemText(8);
		text.text(PixelDungeon.activeMod());
		add(text);
	}
	
	@Override
	protected void layout() {
		super.layout();
		
		image.x = x;
		image.y = y;
		
		text.x = x;
		text.y = image.y + image.height + 2;
	}
	
	@Override
	protected void onTouchDown() {
		image.brightness( 1.5f );
		Sample.INSTANCE.play( Assets.SND_CLICK );
	}
	
	@Override
	protected void onTouchUp() {
		image.resetColor();
	}
	
	@Override
	protected void onClick() {
		getParent().add(new WndModSelect());
	}
}
