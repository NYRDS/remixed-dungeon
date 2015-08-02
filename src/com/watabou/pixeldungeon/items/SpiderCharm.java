package com.watabou.pixeldungeon.items;

import com.watabou.noosa.Game;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.pixeldungeon.items.rings.Artifact;
import com.watabou.pixeldungeon.sprites.ItemSprite.Glowing;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class SpiderCharm extends Artifact {
	
	public static final float TIME_TO_USE = 1;
	
	public static final String AC_FLIP	= Game.getVar(R.string.LloidsBeacon_ACSet);
	

	public SpiderCharm() {
		image = ItemSpriteSheet.SPIDER_CHARM;
		unique = true;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}
	
	private static final Glowing WHITE = new Glowing( 0xFFFFFF );
	
	@Override
	public Glowing glowing() {
		return WHITE;
	}
}
