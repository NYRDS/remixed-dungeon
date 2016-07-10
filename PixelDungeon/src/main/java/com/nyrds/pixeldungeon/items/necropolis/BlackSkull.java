package com.nyrds.pixeldungeon.items.necropolis;

import com.watabou.pixeldungeon.items.rings.Artifact;
import com.watabou.pixeldungeon.sprites.ItemSprite.Glowing;

public class BlackSkull extends Artifact {

	//Supposed to accumulate charge when player kills mobs, and once charge is full goes "Awakened" state
	//Once Awakened Skull gets 25 charges and 10% chance to revive next slain enemy as an ally
	//Each try to revive consumes 1 charge of Awakend Skull. Once charges goes down to 0, it becomes regular "non-Awakened" Black Skull
	//If Black Skull was discovered in bones of previous hero, it will instantly get full charge and thus become an Awakened Skull

	public BlackSkull() {
		imageFile = "items/artifacts.png";
		image = 19;
	}

	@Override
	public Glowing glowing() {
		return new Glowing((int) (Math.random() * 0x000000));
	}


}
