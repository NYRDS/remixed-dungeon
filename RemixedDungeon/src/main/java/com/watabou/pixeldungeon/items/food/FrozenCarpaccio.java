
package com.watabou.pixeldungeon.items.food;
import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Random;
import org.jetbrains.annotations.NotNull;

public class FrozenCarpaccio extends Food {

	{
		image  = ItemSpriteSheet.CARPACCIO;
		energy = Hunger.STARVING - Hunger.HUNGRY;
	}
	
	@Override
	public void _execute(@NotNull Char chr, @NotNull String action ) {
		
		super._execute(chr, action );
		
		if (action.equals( CommonActions.AC_EAT )) {
			
			switch (Random.Int( 5 )) {
			case 0:
                GLog.i(StringsManager.getVar(R.string.FrozenCarpaccio_Info1));
				Buff.affect(chr, Invisibility.class, Invisibility.DURATION );
				break;
			case 1:
                GLog.i(StringsManager.getVar(R.string.FrozenCarpaccio_Info2));
				Buff barkskin = Buff.affect(chr, BuffFactory.BARKSKIN);
				if (barkskin.level() < chr.ht() / 4) barkskin.level(chr.ht() / 4);
				break;
			case 2:
                GLog.i(StringsManager.getVar(R.string.FrozenCarpaccio_Info3));
				Buff.detach(chr, BuffFactory.POISON );
				Buff.detach(chr, BuffFactory.CRIPPLE );
				Buff.detach(chr, BuffFactory.WEAKNESS );
				Buff.detach(chr, BuffFactory.BLEEDING );
				break;
			case 3:
                GLog.i(StringsManager.getVar(R.string.FrozenCarpaccio_Info4));
				chr.heal(chr.hp() + chr.ht() / 4, this);
				break;
			}
		}
	}
	
	public int price() {
		return 10 * quantity();
	}
	
	@Override
	public Item burn(int cell){
		return morphTo(MysteryMeat.class);
	}
}
