
package com.watabou.pixeldungeon.ui;

import com.nyrds.platform.gfx.SystemText;
import com.nyrds.util.GuiProperties;
import com.watabou.pixeldungeon.Chrome;

public class SystemRedButton extends TextButton{
	
	public SystemRedButton( String label ) {
		super(label);
	}
	
	@Override
	protected void createChildren() {
		super.createChildren();
		
		bg = Chrome.get( Chrome.Type.BUTTON );
		add( bg );
		
		text = new SystemText(GuiProperties.titleFontSize());
		
		add( text );
	}
}
