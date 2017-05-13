package com.nyrds.pixeldungeon.items.common;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.actors.mobs.Boss;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.npcs.NPC;
import com.watabou.pixeldungeon.items.weapon.Weapon;
import com.watabou.pixeldungeon.items.weapon.melee.MeleeWeapon;
import com.watabou.pixeldungeon.items.weapon.melee.SpecialWeapon;
import com.watabou.pixeldungeon.sprites.ItemSprite.Glowing;

public class ElvenDagger extends MeleeWeapon {
	{
		imageFile = "items/swords.png";
		image = 8;
	}

	public ElvenDagger() {
		super( 1, 1.5f, 0.7f );
	}

	@Override
	public int typicalSTR() {
		return 9;
	}

}
