
package com.watabou.pixeldungeon.items.weapon.missiles;
import com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class Tamahawk extends MissileWeapon {

	public Tamahawk() {
		this( 1 );
	}
	
	public Tamahawk( int number ) {
		super();
		image = ItemSpriteSheet.TOMAHAWK;
		
		setSTR(17);
		
		MIN = 4;
		MAX = 20;
		
		quantity(number);
	}
	
	@Override
	public void attackProc(Char attacker, Char defender, int damage ) {
		super.attackProc( attacker, defender, damage );
		Buff.affect( defender, BuffFactory.BLEEDING ).level( damage );
	}	

	@Override
	public Item random() {
		quantity(Random.Int( 5, 12 ));
		return this;
	}
	
	@Override
	public int price() {
		return 20 * quantity();
	}

	@Override
	public String getAttackAnimationClass() {
		return SWORD_ATTACK;
	}

	@Override
	public String getVisualName() {
		return "Tomahawk";
	}

}
