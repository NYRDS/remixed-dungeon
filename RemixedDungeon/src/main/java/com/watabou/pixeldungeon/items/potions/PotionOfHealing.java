
package com.watabou.pixeldungeon.items.potions;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Bleeding;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Cripple;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.actors.buffs.Weakness;
import com.watabou.pixeldungeon.items.weapon.missiles.Arrow;
import com.watabou.pixeldungeon.items.weapon.missiles.HealthArrow;
import com.watabou.pixeldungeon.utils.GLog;

public class PotionOfHealing extends Potion {

	private static PotionOfHealing pseudoPotion = new PotionOfHealing();

	{
		labelIndex = 5;
	}

	@Override
	protected void apply(Char hero ) {
		setKnown();
		heal( hero, 1f );
        GLog.p(StringsManager.getVar(R.string.PotionOfHealing_Apply));
	}
	
	public static void heal( Char ch, float portion ) {
		ch.heal((int) (ch.ht()*portion), pseudoPotion);
		Buff.detach( ch, Poison.class );
		Buff.detach( ch, Cripple.class );
		Buff.detach( ch, Weakness.class );
		Buff.detach( ch, Bleeding.class );
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.PotionOfHealing_Info);
    }
	
	@Override
	public int price() {
		return isKnown() ? 30 * quantity() : super.price();
	}
	
	@Override
	public void shatter( int cell ) {
		
		setKnown();
		
		splash( cell );
		Sample.INSTANCE.play( Assets.SND_SHATTER );
		
		Char ch = Actor.findChar(cell);
		
		if(ch != null) {
			heal(ch, 0.5f);
		}
	}

	@Override
	protected void moistenArrow(Arrow arrow, Char owner) {
		int quantity = reallyMoistArrows(arrow,owner);

		HealthArrow moistenArrows = new HealthArrow(quantity);
		owner.collect(moistenArrows);
	}
}
