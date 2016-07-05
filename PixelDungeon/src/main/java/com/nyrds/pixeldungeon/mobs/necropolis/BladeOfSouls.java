package com.nyrds.pixeldungeon.mobs.necropolis;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.weapon.melee.SpecialWeapon;
import com.watabou.pixeldungeon.sprites.ItemSprite.Glowing;
import com.watabou.utils.Random;

public class BladeOfSouls extends SpecialWeapon {
	{
		imageFile = "items/swords.png";
		image = 7;
		enchatable = false;
	}

	public BladeOfSouls() {
		super( 3, 1f, 1f );
	}
	
	@Override
	public Glowing glowing() {
		float period = 1;
		return new Glowing(0x72BCD4, period);
	}

	@Override
	public void proc( Char attacker, Char defender, int damage ) {
		//Gold proc
		if (Random.Int(10) == 1){
			int price = this.price() / 10;
			if ( price > 500) { price = 500;}
			Dungeon.level.drop(new Gold(price), defender.getPos());
		}
		usedForHit();
	}

}
