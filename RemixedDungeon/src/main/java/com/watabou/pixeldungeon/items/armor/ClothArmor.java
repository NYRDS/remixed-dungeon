
package com.watabou.pixeldungeon.items.armor;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.items.Item;


public class ClothArmor extends Armor {
	
	public ClothArmor() {
		super( 1 );
		image = 0;
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.ClothArmor_Desc);
    }
	
	@Override
	public Item burn(int cell){
		return null;
	}
}
