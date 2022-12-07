package com.nyrds.retrodungeon.items.common.armor;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.utils.GLog;

import java.util.ArrayList;

abstract public class UsableArmor extends Armor {

	private static final String TXT_LOW_HEALTH   = Game.getVar(R.string.ClassArmor_LowHealt);
	private static final String TXT_NOT_EQUIPPED = Game.getVar(R.string.ClassArmor_NotEquipped);

	{
		levelKnown = true;
		cursedKnown = true;
		defaultAction = special();
	}

	public UsableArmor(int i) {
		super(i);
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (hero.hp() >= 3 && isEquipped( hero )) {
			actions.add( special() );
		}
		return actions;
	}
	
	@Override
	public void execute( Hero hero, String action ) {
		if (action.equals(special())) {
			
			if (!isEquipped( hero )) {
				GLog.w( TXT_NOT_EQUIPPED );
			} else {
				setCurUser(hero);
				doSpecial();
			}
			
		} else {	
			super.execute( hero, action );		
		}
	}
	
	abstract public String special();
	abstract public void doSpecial();
	
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	@Override
	public int price() {
		return 0;
	}
}
