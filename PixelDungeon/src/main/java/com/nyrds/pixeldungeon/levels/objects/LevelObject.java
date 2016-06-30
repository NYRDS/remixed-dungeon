package com.nyrds.pixeldungeon.levels.objects;

import com.nyrds.pixeldungeon.levels.objects.sprites.LevelObjectSprite;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

public class LevelObject implements Bundlable, Placeable {

	private static final String POS		= "pos";
	public int pos                      = 0;
	
	public LevelObjectSprite sprite;

	public String imageFile() {
		return Assets.ITEMS;
	}
	
	public int image() {
		return ItemSpriteSheet.CODEX;
	}

	public void interact( Hero hero ) {}
	

	public void burn() {}
	public void freeze() {}
	public void poison(){}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		pos   = bundle.getInt( POS );
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		bundle.put( POS, pos );
	}
	
	public boolean dontPack() {
		return false;
	}
}
