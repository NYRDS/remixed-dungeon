
package com.watabou.pixeldungeon.actors.mobs;

import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Frost;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.sprites.PiranhaSprite;

import org.jetbrains.annotations.NotNull;

public class Piranha extends Mob {
	
	public Piranha() {
		spriteClass = PiranhaSprite.class;

		walkingType = WalkingType.WATER;

		hp(ht(10 + Dungeon.depth * 5));
		baseDefenseSkill = 10 + Dungeon.depth * 2;
		baseAttackSkill = 20 + Dungeon.depth * 2;

		dr = Dungeon.depth;

		dmgMin = Dungeon.depth;
		dmgMax = 4 + Dungeon.depth * 2;

		baseSpeed = 2f;
		
		expForKill = 0;

		collect(ItemFactory.itemByName("RawFish"));

		addImmunity( Burning.class );
		addImmunity( Paralysis.class );
		addImmunity( ToxicGas.class );
		addImmunity( Roots.class );
		addImmunity( Frost.class );
	}
	
	@Override
    public boolean act() {
		if (!level().water[getPos()]) {
			die(CharsList.DUMMY);
			return true;
		} else {
			return super.act();
		}
	}

	@Override
	public void die(@NotNull NamedEntityKind cause) {
		super.die( cause );
		
		Statistics.piranhasKilled++;
		Badges.validatePiranhasKilled();
	}
	
	@Override
	public boolean reset() {
		return true;
	}
}
