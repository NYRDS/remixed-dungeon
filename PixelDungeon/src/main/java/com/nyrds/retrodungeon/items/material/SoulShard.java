package com.nyrds.retrodungeon.items.material;

import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSprite.Glowing;

public class SoulShard extends Item {
	public SoulShard() {
		stackable = true;
		imageFile = "items/materials.png";
		image = 0;
		identify();
	}

	@Override
	public Glowing glowing() {
		return new Glowing((int) (Math.random() * 0xaaaaff));
	}
}
