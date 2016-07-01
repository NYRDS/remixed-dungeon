package com.nyrds.pixeldungeon.levels.objects;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.windows.WndMessage;

/**
 * Created by mike on 01.07.2016.
 */
public class Sign extends LevelObject {

	private String signText;

	public Sign(int pos, String text) {
		super(pos);
		signText = text;
	}

	@Override
	public boolean interact(Hero hero) {
		super.interact(hero);

		GameScene.show(new WndMessage(signText));

		return true;
	}

	@Override
	public String desc() {
		return Dungeon.level.tileName(Terrain.SIGN);
	}

	@Override
	public int image() {
		return 0;
	}
}
