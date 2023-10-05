
package com.watabou.pixeldungeon.ui;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.windows.elements.Tool;

import org.jetbrains.annotations.NotNull;

public class GoldIndicator extends Component {

	private static final float TIME	= 2f;
	
	private int lastValue = 0;

	@NotNull
	private final BitmapText tf;
	
	private float time;

	GoldIndicator() {
		super();
		tf = new BitmapText( PixelScene.font1x );
		tf.hardlight( 0xFFFF00 );
		add( tf );

		setVisible(false);
	}

	@Override
	protected void layout() {
		tf.setX(x + (width - tf.width()) / 2);
		tf.setY(bottom() - tf.height());
	}
	
	@Override
	public void update() {
		super.update();
		
		if (getVisible()) {
			
			time -= GameLoop.elapsed;
			if (time > 0) {
				tf.alpha( time > TIME / 2 ? 1f : time * 2 / TIME );
			} else {
				setVisible(false);
			}
			
		}

		int gold = Dungeon.hero.gold();

		if (gold != lastValue && !GamePreferences.toolStyle().equals(Tool.Size.Tiny.name())) {
			
			lastValue = gold;
			
			tf.text( Integer.toString( lastValue ) );

			setVisible(true);
			time = TIME;
			
			layout();
		}
	}
}
