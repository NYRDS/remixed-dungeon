package com.nyrds.pixeldungeon.items.necropolis;

import com.watabou.pixeldungeon.items.rings.Artifact;
import com.watabou.pixeldungeon.sprites.ItemSprite.Glowing;

public class BlackSkull extends Artifact {

	public BlackSkull() {
		imageFile = "items/artifacts.png";
		image = 19;
	}

	@Override
	public Glowing glowing() {
		return new Glowing((int) (Math.random() * 0x000000));
	}


}
