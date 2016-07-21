package com.nyrds.pixeldungeon.mobs.common;

import com.nyrds.pixeldungeon.items.common.GoldenSword;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Journal;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfPsionicBlast;
import com.watabou.pixeldungeon.items.weapon.Weapon;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.pixeldungeon.items.weapon.enchantments.Leech;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class GoldenStatue extends Mob {

	private Weapon weapon;

	public GoldenStatue() {
		EXP = 0;
		state = PASSIVE;

		weapon = new GoldenSword();
		weapon.identify();
		weapon.upgrade(4);
		
		hp(ht(15 + Dungeon.depth * 5));
		defenseSkill = 4 + Dungeon.depth;
		
		RESISTANCES.add( ToxicGas.class );
		RESISTANCES.add( Poison.class );
		RESISTANCES.add( Death.class );
		RESISTANCES.add( ScrollOfPsionicBlast.class );
		IMMUNITIES.add( Leech.class );
	}

	private static final String WEAPON	= "weapon";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( WEAPON, weapon );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		weapon = (Weapon)bundle.get( WEAPON );
	}

	@Override
	protected boolean act() {
		if (Dungeon.visible[getPos()]) {
			Journal.add( Journal.Feature.STATUE );
		}
		return super.act();
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( weapon.MIN, weapon.MAX );
	}

	@Override
	public int attackSkill( Char target ) {
		return (int)((9 + Dungeon.depth) * weapon.ACU);
	}

	@Override
	protected float attackDelay() {
		return weapon.DLY;
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
	public int attackProc( Char enemy, int damage ) {

		if (Random.Int( 10 ) == 1) {
			Buff.affect( enemy, Burning.class ).reignite( enemy );
		}

		return damage;
	}

	@Override
	public void beckon( int cell ) {
	}

	@Override
	public void die( Object cause ) {
		Dungeon.level.drop( weapon, getPos() ).sprite.drop();
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
		return Utils.format(Game.getVar(R.string.GoldenStatue_Desc), weapon.name());
	}
}
