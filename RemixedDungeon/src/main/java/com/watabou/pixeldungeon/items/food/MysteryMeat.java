
package com.watabou.pixeldungeon.items.food;

import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.actors.buffs.Slow;
import com.watabou.pixeldungeon.actors.buffs.Stun;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class MysteryMeat extends Food {

	{
		image   = ItemSpriteSheet.MEAT;
		energy  = Hunger.STARVING - Hunger.HUNGRY;
        message = StringsManager.getVar(R.string.MysteryMeat_Message);
	}
	
	@Override
	public void _execute(@NotNull Char chr, @NotNull String action ) {
		
		super._execute(chr, action );
		
		if (action.equals( CommonActions.AC_EAT )) {
			
			switch (Random.Int( 5 )) {
			case 0:
                GLog.w(StringsManager.getVar(R.string.MysteryMeat_Info1));
				Buff.affect(chr, Burning.class ).reignite(chr);
				break;
			case 1:
                GLog.w(StringsManager.getVar(R.string.MysteryMeat_Info2));
				Buff.prolong(chr, Roots.class, Stun.duration(chr) );
				break;
			case 2:
                GLog.w(StringsManager.getVar(R.string.MysteryMeat_Info3));
				Buff.affect(chr, Poison.class,Poison.durationFactor(chr) * chr.ht() / 5 );
				break;
			case 3:
                GLog.w(StringsManager.getVar(R.string.MysteryMeat_Info4));
				Buff.prolong(chr, Slow.class, Slow.duration(chr) );
				break;
			}
		}
	}
	
	public int price() {
		return 5 * quantity();
	}

	@Override
	public Item burn(int cell){
		return morphTo(ChargrilledMeat.class);
	}
	
	@Override
	public Item freeze(int cell){
		return morphTo(FrozenCarpaccio.class);
	}
	
	@Override
	public Item poison(int cell){
		return morphTo(RottenMeat.class);
	}
}
