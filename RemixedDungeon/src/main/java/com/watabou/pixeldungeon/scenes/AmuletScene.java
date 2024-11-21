
package com.watabou.pixeldungeon.scenes;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Rankings;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.effects.Flare;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.ui.RedButton;
import com.nyrds.util.Random;

public class AmuletScene extends PixelScene {

	private static final int WIDTH			= 120;
	private static final int BTN_HEIGHT		= 18;
	private static final float SMALL_GAP	= 2;
	private static final float LARGE_GAP	= 8;
	
	public static boolean noText = false;
	
	private Image amulet;
	
	@Override
	public void create() {
		super.create();
		
		Text text = null;
		if (!noText) {
            text = createMultiline(R.string.AmuletScene_Txt, GuiProperties.regularFontSize() );
			text.maxWidth(WIDTH);
			add( text );
		}
		
		amulet = new Image( Assets.AMULET );
		add( amulet );

        RedButton btnExit = new RedButton(R.string.AmuletScene_Exit) {
			@Override
			protected void onClick() {
				Dungeon.win( ResultDescriptions.getDescription(ResultDescriptions.Reason.WIN), Rankings.gameOver.WIN_AMULET );
				Dungeon.gameOver();
				GameLoop.switchScene( noText ? TitleScene.class : RankingsScene.class );
			}
		};
		btnExit.setSize( WIDTH, BTN_HEIGHT );
		add( btnExit );

        RedButton btnStay = new RedButton(R.string.AmuletScene_Stay) {
			@Override
			protected void onClick() {
				onBackPressed();
			}
		};
		btnStay.setSize( WIDTH, BTN_HEIGHT );
		add( btnStay );
		
		float height;
		if (noText) {
			height = amulet.height + LARGE_GAP + btnExit.height() + SMALL_GAP + btnStay.height();

            amulet.setX(align( (Camera.main.width - amulet.width) / 2 ));
			amulet.setY(align( (Camera.main.height - height) / 2 ));

			btnExit.setPos( (Camera.main.width - btnExit.width()) / 2, amulet.getY() + amulet.height + LARGE_GAP );
			btnStay.setPos( btnExit.left(), btnExit.bottom() + SMALL_GAP );
			
		} else {
			height = amulet.height + LARGE_GAP + text.height() + LARGE_GAP + btnExit.height() + SMALL_GAP + btnStay.height();

            amulet.setX(align( (Camera.main.width - amulet.width) / 2 ));
			amulet.setY(align( (Camera.main.height - height) / 2 ));
			
			text.setX(align( (Camera.main.width - text.width()) / 2 ));
			text.setY(amulet.getY() + amulet.height + LARGE_GAP);
			
			btnExit.setPos( (Camera.main.width - btnExit.width()) / 2, text.getY() + text.height() + LARGE_GAP );
			btnStay.setPos( btnExit.left(), btnExit.bottom() + SMALL_GAP );
		}

		new Flare( 8, 48 ).color( 0xFFDDBB, true ).show( amulet, 0 ).angularSpeed = +30;
		
		fadeIn();
	}
	
	@Override
	protected void onBackPressed() {
		InterlevelScene.Do(InterlevelScene.Mode.CONTINUE);
	}
	
	private float timer = 0;
	
	@Override
	public void update() {
		super.update();
		
		if ((timer -= GameLoop.elapsed) < 0) {
			timer = Random.Float( 0.5f, 5f );
			
			Speck star = (Speck)recycle( Speck.class );
			star.reset( 0, amulet.getX() + 10.5f, amulet.getY() + 5.5f, Speck.DISCOVER );
			add( star );
		}
	}
}
