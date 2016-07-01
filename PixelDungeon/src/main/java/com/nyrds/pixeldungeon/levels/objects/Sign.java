package com.nyrds.pixeldungeon.levels.objects;

import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.windows.WndMessage;

/**
 * Created by mike on 01.07.2016.
 */
public class Sign extends LevelObject {

	@Override
	public boolean interact(Hero hero) {
		super.interact(hero);

		GameScene.show(new WndMessage("Yay!"));

		return true;
	}

	@Override
	public int image() {
		return 0;
	}
}
