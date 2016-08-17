package com.nyrds.pixeldungeon.items.common.armor;

import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.armor.Armor;


public class NecromancerRobe extends Armor {
	
	public NecromancerRobe() {
		super( 1 );
		image = 22;
	}
	
	@Override
	public Item burn(int cell){
		return null;
	}
}
