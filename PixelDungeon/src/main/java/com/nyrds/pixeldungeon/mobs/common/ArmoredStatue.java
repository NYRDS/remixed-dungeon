package com.nyrds.pixeldungeon.mobs.common;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Journal;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfPsionicBlast;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.pixeldungeon.items.weapon.enchantments.Leech;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;

public class ArmoredStatue extends Mob {

	private Armor armor;

	public ArmoredStatue() {
		EXP = 0;
		state = PASSIVE;
		

		hp(ht(15 + Dungeon.depth * 5));
		defenseSkill = 4 + Dungeon.depth;
		
		RESISTANCES.add( ToxicGas.class );
		RESISTANCES.add( Poison.class );
		RESISTANCES.add( Death.class );
		RESISTANCES.add( ScrollOfPsionicBlast.class );
		IMMUNITIES.add( Leech.class );
	}
	
	private static final String ARMOR	= "armor";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( ARMOR, armor );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		armor = (Armor) bundle.get( ARMOR );
	}
	
	@Override
	protected boolean act() {
		if (Dungeon.visible[getPos()]) {
			Journal.add( Journal.Feature.STATUE );
		}
		return super.act();
	}

	@Override
	public int dr() {
		return Dungeon.depth;
	}
	
	@Override
	public void damage( int dmg, Object src ) {

		if (state == PASSIVE) {
			state = HUNTING;
		}
		
		super.damage( dmg, src );
	}
	@Override
	public void beckon( int cell ) {
	}
	
	@Override
	public void die( Object cause ) {
		Dungeon.level.drop( armor, getPos() ).sprite.drop();
		super.die( cause );
	}
	
	@Override
	public void destroy() {
		Journal.remove( Journal.Feature.STATUE );
		super.destroy();
	}
	
	@Override
	public boolean reset() {
		state = PASSIVE;
		return true;
	}

	@Override
	public String description() {
		return Utils.format(Game.getVar(R.string.Statue_Desc), armor.name());
	}	
}
