package com.nyrds.retrodungeon.items.material;

import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSprite;

public class IceGuardianCoreModule extends Item {
	public IceGuardianCoreModule() {
		stackable = true;
		imageFile = "items/materials.png";
		image = 2;
		identify();
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing( 0x00FFFF );
	}
}
