package com.nyrds.pixeldungeon.items.common;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.weapon.melee.MeleeWeapon;
import com.watabou.pixeldungeon.sprites.Glowing;
import com.watabou.utils.Random;

public class GoldenSword extends MeleeWeapon {
	{
		imageFile = "items/swords.png";
		image = 5;
		enchatable = false;
		animation_class = SWORD_ATTACK;
	}

	public GoldenSword() {
		super( 3, 1.1f, 0.8f );
	}
	
	@Override
	public Glowing glowing() {
		float period = 1;
		return new Glowing(0xFFFF66, period);
	}

	@Override
	public void attackProc(Char attacker, Char defender, int damage ) {
		//Gold proc
		if (Random.Int(10) == 1){
			int price = this.price() / 10;
			if ( price > 500) { price = 500;}
			new Gold(price).doDrop(defender);
		}
		usedForHit();
	}
}
