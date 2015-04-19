package com.watabou.pixeldungeon.items.weapon.melee;

import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class RubyBow extends Bow {

	public RubyBow() {
		super( 5, 0.8f, 1.5f );
		image = ItemSpriteSheet.BOW_RUBY;
	}
	
	@Override
	public double acuFactor() {
		return 1;
	}
	
	@Override
	public double dmgFactor() {
		return 1 + level;
	}
	
	public double dlyFactor() {
		return 1 + level * 0.2;
	}
}
