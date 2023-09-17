
package com.watabou.pixeldungeon.ui;

import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.pixeldungeon.Chrome;
import com.watabou.pixeldungeon.scenes.PixelScene;

public class RedButton extends TextButton {

	public RedButton( int labelStringId ) {
        this(StringsManager.getVar(labelStringId));
	}

	public RedButton( String label ) {
		super(label);
	}
	
	@Override
	protected void createChildren() {
		super.createChildren();
		
		bg = Chrome.get( Chrome.Type.BUTTON );
		add( bg );

		text = PixelScene.createText(GuiProperties.titleFontSize());
		add( text );
	}

	public void regenText() {
		String txt = text.text();
		text.destroy();

		text = PixelScene.createText(GuiProperties.titleFontSize());
		text.text(txt);
		add( text );
		layout();
	}
}
