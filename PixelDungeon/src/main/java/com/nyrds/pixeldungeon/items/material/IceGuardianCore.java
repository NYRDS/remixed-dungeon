package com.nyrds.pixeldungeon.items.material;

import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSprite.Glowing;

public class IceGuardianCore extends Item {

	//This item supposed to drop from Jar of Souls.
	//It is used in a quest of artificer, which he exchanges for Blade of Souls

	public IceGuardianCore() {
		stackable = true;
		imageFile = "items/materials.png";
		image = 2;
	}

	@Override
	public Glowing glowing() {
		return new Glowing((int) (Math.random() * 0xaaaaff));
	}


}
