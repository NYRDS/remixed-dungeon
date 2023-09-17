
package com.watabou.pixeldungeon.scenes;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Music;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Camera;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Chrome;
import com.watabou.pixeldungeon.ui.Archs;
import com.watabou.pixeldungeon.ui.BadgesList;
import com.watabou.pixeldungeon.ui.ExitButton;
import com.watabou.pixeldungeon.ui.ScrollPane;
import com.watabou.pixeldungeon.ui.Window;

public class BadgesScene extends PixelScene {

	@Override
	public void create() {
		super.create();
		
		Music.INSTANCE.play( Assets.THEME, true );
		Music.INSTANCE.volume( 1f );
		
		uiCamera.setVisible(false);
		
		int w = Camera.main.width;
		int h = Camera.main.height;
		
		Archs archs = new Archs();
		archs.setSize( w, h );
		add( archs );
		
		int pw = Math.min( 160, w - 6 );
		int ph = h - 30;
		
		NinePatch panel = Chrome.get( Chrome.Type.WINDOW );
		panel.size( pw, ph );
		panel.setX((w - pw) / 2);
		panel.setY((h - ph) / 2);
		add( panel );

        Text title = PixelScene.createText(StringsManager.getVar(R.string.BadgesScene_Title), GuiProperties.titleFontSize());
		title.hardlight( Window.TITLE_COLOR );
		title.setX(align( (w - title.width()) / 2 ));
		title.setY(align( (panel.getY() - title.baseLine()) / 2 ));
		add( title );
		
		Badges.loadGlobal();
		
		ScrollPane list = new BadgesList( true );
		add( list );
		
		list.setRect( 
			panel.getX() + panel.marginLeft(),
			panel.getY() + panel.marginTop(),
			panel.innerWidth(), 
			panel.innerHeight() );
		
		ExitButton btnExit = new ExitButton();
		btnExit.setPos( Camera.main.width - btnExit.width(), 0 );
		add( btnExit );
		
		fadeIn();
	}
	
	@Override
	protected void onBackPressed() {
		RemixedDungeon.switchNoFade( TitleScene.class );
	}
}
