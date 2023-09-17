
package com.watabou.pixeldungeon.ui;

import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.Dungeon;

public class BusyIndicator extends Image {
	
	public BusyIndicator() {
		super();
		copy( Icons.BUSY.get() );

		setOrigin( width / 2, height / 2 );
		angularSpeed = 720;
	}
	
	@Override
	public void update() {
		super.update();
		setVisible(Dungeon.hero.isAlive() && !Dungeon.hero.isReady());
	}
}
