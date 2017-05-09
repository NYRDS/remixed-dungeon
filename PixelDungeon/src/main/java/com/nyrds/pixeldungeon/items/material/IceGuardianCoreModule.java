package com.nyrds.pixeldungeon.items.material;

import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSprite.Glowing;

public class IceGuardianCoreModule extends Item {

	public IceGuardianCoreModule() {
		stackable = true;
		imageFile = "items/materials.png";
		image = 2;
		identify();
	}

	@Override
	public Glowing glowing() {
		return new Glowing((int) (Math.random() * 0x99ccff));
	}


}
