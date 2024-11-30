
package com.watabou.pixeldungeon.windows;

import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.ui.Highlighter;
import com.watabou.pixeldungeon.ui.Window;

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

		text = Highlighter.addHilightedText(titlebar.left(), titlebar.bottom() + 2 * GAP, width,this,  message);

		resize( width, (int)(text.getY() + text.height()+GAP) );
	}

	public void setText(String msg)  {
		text.text(msg);
		resize( width, (int)(text.getY() + text.height()+GAP) );
	}

}
