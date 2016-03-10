package com.watabou.pixeldungeon.windows;

import java.util.ArrayList;

import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.TextButton;
import com.watabou.pixeldungeon.ui.Window;

public class WndOptionsColumns extends Window {

	private static final int WIDTH			= 120;
	private static final int MARGIN 		= 2;
	private static final int BUTTON_HEIGHT	= 20;
	private static final int BUTTON_WIDTH	= 58;
	
	private ArrayList<TextButton> buttons = new ArrayList<>();
		
	public WndOptionsColumns( String title, String message, String... options ) {
		super();
		
		Text tfTitle = PixelScene.createMultiline( title, 9 );
		tfTitle.hardlight( TITLE_COLOR );
		tfTitle.x = tfTitle.y = MARGIN;
		tfTitle.maxWidth(WIDTH - MARGIN * 2);
		tfTitle.measure();
		add( tfTitle );
		
		Text tfMesage = PixelScene.createMultiline( message, 8 );
		tfMesage.maxWidth(WIDTH - MARGIN * 2);
		tfMesage.measure();
		tfMesage.x = MARGIN;
		tfMesage.y = tfTitle.y + tfTitle.height() + MARGIN;
		add( tfMesage );
		
		float pos = tfMesage.y + tfMesage.height() + MARGIN;
		
		for (int i = 0; i < options.length / 2 + 1; i++) {
			for(int j =0;j<2;j++) {
				final int index = i*2+j;
				if(!(index<options.length)) {
					break;
				}
				RedButton btn = new RedButton( options[index] ) {
					@Override
					protected void onClick() {
						hide();
						onSelect( index );
					}
				};
				buttons.add(btn);
				
				btn.setRect( MARGIN + j*(BUTTON_WIDTH+MARGIN), pos, BUTTON_WIDTH, BUTTON_HEIGHT );
				add( btn );
			}
			pos += BUTTON_HEIGHT + MARGIN;
		}
		
		resize( WIDTH, (int)pos );
	}
	
	public void setEnabled(int i, boolean enabled) {
		buttons.get(i).enable(enabled);
	}
	
	protected void onSelect( int index ) {}
}
