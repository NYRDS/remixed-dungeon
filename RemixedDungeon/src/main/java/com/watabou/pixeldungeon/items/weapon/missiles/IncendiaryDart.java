
package com.watabou.pixeldungeon.items.weapon.missiles;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.Fire;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class IncendiaryDart extends Dart {

	public IncendiaryDart() {
		this( 1 );
	}
	
	public IncendiaryDart( int number ) {
		super();
		
		image = 3;
		
		setSTR(12);
		
		MIN = 1;
		MAX = 2;
		
		quantity(number);
	}
	
	@Override
	protected void onThrow(int cell, @NotNull Char thrower, Char enemy) {
		if (enemy == null || enemy == thrower) {
			if (thrower.level().flammable[cell]) {
				GameScene.add( Blob.seed( cell, 4, Fire.class ) );
			} else {
				super.onThrow( cell, thrower, enemy);
			}
		} else {
			if (!thrower.shoot( enemy, this )) {
				thrower.level().animatedDrop( this, cell );
			}
		}
	}
	
	@Override
	public void attackProc(Char attacker, Char defender, int damage ) {
		Buff.affect( defender, Burning.class ).reignite( defender );
		super.attackProc( attacker, defender, damage );
	}

	@Override
	public Item random() {
		quantity(Random.Int( 3, 6 ));
		return this;
	}
	
	@Override
	public int price() {
		return 10 * quantity();
	}
}
