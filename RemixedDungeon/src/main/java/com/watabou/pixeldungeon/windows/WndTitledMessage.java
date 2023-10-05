
package com.watabou.pixeldungeon.windows;

import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.ui.Highlighter;
import com.watabou.pixeldungeon.ui.Window;

public class WndTitledMessage extends Window {

	public WndTitledMessage( Image icon, String title, String message ) {
		
		this( new IconTitle( icon, title ), message );
	}
	
	public WndTitledMessage( Component titlebar, String message ) {
		
		super();

		resizeLimited(STD_WIDTH);

		titlebar.setRect( 0, 0, width, 0 );
		add( titlebar );

		Text normal = Highlighter.addHilightedText(titlebar.left(), titlebar.bottom() + 2 * GAP, width,this,  message);

		resize( width, (int)(normal.getY() + normal.height()+GAP) );
	}

}
