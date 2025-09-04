
package com.watabou.pixeldungeon.windows;

import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.Window;
import com.nyrds.util.GuiProperties;

public class WndTitledMessage extends Window {

	protected Text text;
	protected Component title;

	public WndTitledMessage(Image icon, String title, String message ) {
		
		this( new IconTitle( icon, title ), message );
	}
	
	public WndTitledMessage( Component titlebar, String message ) {
		
		super();

		resizeLimited(STD_WIDTH);

		titlebar.setRect( 0, 0, width, 0 );
		title = titlebar;
		add( titlebar );

		text = PixelScene.createMultilineHighlighted(message, GuiProperties.regularFontSize());
		text.maxWidth(width);
		text.setX(titlebar.left());
		text.setY(titlebar.bottom() + 2 * GAP);
		add(text);

		resize( width, (int)(text.getY() + text.height()+GAP) );
	}

	public void setText(String msg)  {
		text.text(msg);
		resize( width, (int)(text.getY() + text.height()+GAP) );
	}

}
